package perimeter;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.*;
import net.minecraft.sounds.*;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="perimeter_siege")
public class SiegeBoss {
    static final String RELIC="PerimeterRelic";
    static ItemStack box(String kind){
        ItemStack s=Arsenal.supply(true);
        s.getOrCreateTag().putString("SiegeBossBox",kind);
        s.setHoverName(Component.literal("Трофей: "+(kind.equals("guard")?"Панцирник":"Дозорный")).withStyle(ChatFormatting.GOLD));
        return s;
    }
    static ItemStack weapon(String kind,net.minecraft.util.RandomSource random){
        boolean guard=kind.equals("guard");
        Item item=ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz","modern_kinetic_gun"));
        ItemStack s;
        if(item==null||item==Items.AIR)s=new ItemStack(Items.CROSSBOW);
        else{
            s=new ItemStack(item);
            s.getOrCreateTag().putString("GunId",guard?"tacz:ak47":"tacz:m4a1");
            s.getOrCreateTag().putInt("GunCurrentAmmoCount",30);
        }
        s.getOrCreateTag().putString(RELIC,kind);
        s.setHoverName(Component.literal(guard?"Пролом":"Дальний дозор").withStyle(ChatFormatting.GOLD));
        Arsenal.roll(s,random);Arsenal.affix(s).putInt("tier",4);
        return s;
    }
    static double scale(String kind,double range){
        if(kind.equals("guard"))return range<=10?1.5:0.75;
        if(kind.equals("scout"))return range>=18?1.6:0.65;
        return 1;
    }
    @SubscribeEvent public static void tooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent e){
        ItemStack s=e.getItemStack();
        if(s.hasTag()&&s.getTag().contains(RELIC))e.getToolTip().add(Component.literal(s.getTag().getString(RELIC).equals("guard")?"До 10 блоков: +50% к выстрелу; дальше: −25%":"От 18 блоков: +60% к выстрелу; ближе: −35%").withStyle(ChatFormatting.GOLD));
    }
    @SubscribeEvent public static void fired(EntityJoinLevelEvent e){
        if(!e.getLevel().isClientSide && e.getEntity() instanceof Projectile shot&&shot.getOwner() instanceof ServerPlayer p){
            ItemStack s=p.getMainHandItem();shot.getPersistentData().putBoolean("perimeterShot",true);
            if(s.hasTag()&&s.getTag().contains(RELIC))shot.getPersistentData().putString(RELIC,s.getTag().getString(RELIC));
            shot.getPersistentData().putDouble("px",p.getX());shot.getPersistentData().putDouble("py",p.getY());shot.getPersistentData().putDouble("pz",p.getZ());
        }
    }
    @SubscribeEvent public static void hit(LivingHurtEvent e){
        if(!(e.getSource().getEntity() instanceof ServerPlayer p))return;
        Entity shot=e.getSource().getDirectEntity();
        if(shot!=null&&shot.getPersistentData().getBoolean("perimeterShot")){
            var tag=shot.getPersistentData();
            e.setAmount((float)(e.getAmount()*scale(tag.getString(RELIC),Math.sqrt(e.getEntity().distanceToSqr(tag.getDouble("px"),tag.getDouble("py"),tag.getDouble("pz"))))));
        }else if(e.getSource().typeHolder().unwrapKey().map(k->k.location().getNamespace().equals("tacz")).orElse(false)){
            var tag=p.getMainHandItem().getTag();if(tag!=null)e.setAmount((float)(e.getAmount()*scale(tag.getString(RELIC),p.distanceTo(e.getEntity()))));
        }
    }
    @SubscribeEvent public static void bossTick(LivingEvent.LivingTickEvent event){
        if(!(event.getEntity() instanceof Mob mob)||!(mob.level() instanceof ServerLevel l)||!mob.getTags().contains("rd.boss"))return;
        boolean guard=mob.getTags().contains("rd.guardboss");String kind=guard?"guard":"scout";
        if(!mob.getPersistentData().getBoolean("armed")){
            mob.setItemSlot(EquipmentSlot.MAINHAND,weapon(kind,mob.getRandom()));mob.setDropChance(EquipmentSlot.MAINHAND,0);
            mob.getPersistentData().putBoolean("armed",true);
        }
        if(mob.tickCount%60!=0)return;
        var target=l.getNearestPlayer(mob,guard?10:28);
        if(target==null||target.isCreative()||target.isSpectator()||!mob.hasLineOfSight(target))return;
        target.hurt(l.damageSources().mobAttack(mob),guard?5:target.distanceTo(mob)>=18?6:2);
        mob.playSound(SoundEvents.FIREWORK_ROCKET_BLAST,1,guard?.7f:1.2f);
    }
    @SubscribeEvent public static void drops(LivingDropsEvent e){
        LivingEntity mob=e.getEntity();
        if(!mob.getTags().contains("rd.boss"))return;
        e.getDrops().removeIf(i->i.getItem().hasTag()&&i.getItem().getTag().contains(RELIC));
        if(mob.getTags().contains("rd.cleanup")||e.getSource().is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL))return;
        String kind=mob.getTags().contains("rd.guardboss")?"guard":"scout";
        e.getDrops().add(new ItemEntity(mob.level(),mob.getX(),mob.getY(),mob.getZ(),box(kind)));
    }
}