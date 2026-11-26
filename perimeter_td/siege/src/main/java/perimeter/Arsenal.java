package perimeter;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import static net.minecraft.commands.Commands.literal;

@Mod.EventBusSubscriber(modid="perimeter_siege")
public class Arsenal {
    static final String KEY="PerimeterAffix", SUPPLY="PerimeterSupply", QUEUE="PerimeterRoll";
    static final String[] TIERS={"Обычная","Необычная","Редкая","Эпическая","Легендарная","Мифическая"};
    static final ChatFormatting[] COLORS={ChatFormatting.GRAY,ChatFormatting.GREEN,ChatFormatting.AQUA,ChatFormatting.LIGHT_PURPLE,ChatFormatting.GOLD,ChatFormatting.RED};
    static final String[] WEAPON={"Натиск","Вампиризм","Подвижность"}, ARMOR={"Бастион","Регенерация","Марш"};
    static boolean gun(ItemStack s){return s.hasTag() && s.getTag().contains("GunId");}
    static boolean armor(ItemStack s){return s.getItem() instanceof ArmorItem;}
    static boolean eligible(ItemStack s){return armor(s)||gun(s)||s.getItem() instanceof SwordItem||s.getItem() instanceof AxeItem||s.getItem() instanceof ProjectileWeaponItem||s.getItem() instanceof TridentItem;}
    static int tier(RandomSource rng){int n=rng.nextInt(1000);return n<500?0:n<770?1:n<910?2:n<970?3:n<995?4:5;}
    static CompoundTag affix(ItemStack s){return s.getTagElement(KEY);}
    static int rank(CompoundTag t){return Math.min(5,Math.max(0,t.getInt("tier")));}
    static int prefix(CompoundTag t){return Math.min(2,Math.max(0,t.getInt("prefix")));}
    static void roll(ItemStack s,RandomSource rng){
        CompoundTag old=affix(s);
        int newTier=old==null?tier(rng):rank(old);
        int next=old==null?rng.nextInt(3):(prefix(old)+1+rng.nextInt(2))%3;
        CompoundTag t=s.getOrCreateTagElement(KEY);
        t.putInt("tier",newTier);t.putInt("prefix",next);
    }
    static String info(ItemStack s){
        CompoundTag t=affix(s);
        return t==null?"":TIERS[rank(t)]+" • "+(armor(s)?ARMOR:WEAPON)[prefix(t)];
    }
    @SubscribeEvent public static void tooltip(ItemTooltipEvent e){
        ItemStack s=e.getItemStack(); CompoundTag a=affix(s);
        if(a==null)return;
        int level=rank(a)+1, kind=prefix(a);
        String meaning=armor(s)?switch(kind){
            case 0->"Получаемый урон −"+(2*level)+"%";case 1->"Регенерация при экипировке";default->"Скорость при экипировке";
        }:switch(kind){
            case 0->"Урон +"+(5*level)+"%";case 1->"Лечение при попадании: "+(level*5)+"% урона";default->"Скорость в основной руке";
        };
        e.getToolTip().add(Component.literal(info(s)).withStyle(COLORS[rank(a)]));
        e.getToolTip().add(Component.literal(meaning).withStyle(ChatFormatting.DARK_GRAY));
    }
    static ItemStack supply(boolean box){
        ItemStack s=new ItemStack(box?Items.NAUTILUS_SHELL:Items.RABBIT_FOOT);
        s.getOrCreateTag().putString(SUPPLY,box?"box":"med");
        s.setHoverName(Component.literal(box?"Запечатанный ящик арсенала":"Полевой автоинъектор").withStyle(box?ChatFormatting.GOLD:ChatFormatting.GREEN));
        return s;
    }
    static ItemStack prize(ServerPlayer p){
        RandomSource r=p.getRandom();int choice=r.nextInt(10);ItemStack s;
        if(choice<4){
            Item i=ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz","modern_kinetic_gun"));
            if(i!=null && i!=Items.AIR){
                s=new ItemStack(i);String id=List.of("glock_17","ak47","m4a1","glock_17").get(choice);
                s.getOrCreateTag().putString("GunId","tacz:"+id);
                s.getOrCreateTag().putInt("GunCurrentAmmoCount",choice==0||choice==3?17:30);
            }else s=new ItemStack(Items.CROSSBOW);
        }else if(choice==9){s=supply(false);s.setCount(3);return s;}
        else {
            s=new ItemStack(List.of(Items.DIAMOND_HELMET,Items.DIAMOND_CHESTPLATE,Items.DIAMOND_LEGGINGS,Items.DIAMOND_BOOTS,Items.DIAMOND_SWORD).get(choice-4));
        }
        roll(s,r);
        if(armor(s))s.enchant(Enchantments.ALL_DAMAGE_PROTECTION,1+rank(affix(s))/2);
        return s;
    }
    static void grant(ServerPlayer p,ItemStack s){if(!p.getInventory().add(s))p.drop(s,false);}
    static CompoundTag record(ServerPlayer p){return p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);}
    static void save(ServerPlayer p,CompoundTag t){p.getPersistentData().put(Player.PERSISTED_NBT_TAG,t);}
    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickItem e){
        ItemStack s=e.getItemStack();
        if(!s.hasTag()||!s.getTag().contains(SUPPLY))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        if(s.getTag().getString(SUPPLY).equals("box")){
            CompoundTag data=record(p);
            if(data.contains(QUEUE))return;
            data.put(QUEUE,prize(p).save(new CompoundTag()));data.putInt("RollTicks",0);save(p,data);
        }else{
            if(p.getCooldowns().isOnCooldown(s.getItem()))return;
            p.addEffect(new MobEffectInstance(MobEffects.HEAL,1,1));
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,80,0));
            p.getCooldowns().addCooldown(s.getItem(),160);
            p.displayClientMessage(Component.literal("Автоинъектор применён"),true);
        }
        s.shrink(1);
    }
    static void preview(ServerPlayer p,ItemStack s,boolean last){
        String detail=info(s);
        Component title=Component.literal((last?"Выпало: ":"◀  ")+s.getHoverName().getString()+(last?"":"  ▶")).withStyle(affix(s)==null?ChatFormatting.GREEN:COLORS[rank(affix(s))]);
        p.connection.send(new ClientboundSetTitlesAnimationPacket(0,last?45:10,4));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(detail)));
        p.connection.send(new ClientboundSetTitleTextPacket(title));
        p.playNotifySound(last?SoundEvents.PLAYER_LEVELUP:SoundEvents.UI_BUTTON_CLICK.value(),SoundSource.PLAYERS,0.6f,last?1.25f:1f);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p))return;
        CompoundTag data=record(p);
        if(data.contains(QUEUE)&&p.isAlive()){
            int time=data.getInt("RollTicks")+1;data.putInt("RollTicks",time);
            if(time>=56){
                ItemStack win=ItemStack.of(data.getCompound(QUEUE));data.remove(QUEUE);grant(p,win);preview(p,win,true);
            }else if(time<=24&&time%3==0||time==29||time==35||time==43||time==51)preview(p,prize(p),false);
            save(p,data);
        }
        if(p.tickCount%20!=0)return;
        for(int i=0;i<p.getInventory().getContainerSize();i++){
            ItemStack s=p.getInventory().getItem(i);
            if(eligible(s)&&affix(s)==null)roll(s,p.getRandom());
        }
        for(ItemStack s:p.getArmorSlots())buff(p,s,true);
        buff(p,p.getMainHandItem(),false);
    }
    static void buff(ServerPlayer p,ItemStack s,boolean worn){
        CompoundTag t=affix(s);if(t==null||armor(s)!=worn)return;
        int kind=prefix(t);
        if(kind==2||worn&&kind==1)p.addEffect(new MobEffectInstance(kind==2?MobEffects.MOVEMENT_SPEED:MobEffects.REGENERATION,65,rank(t)/3,true,false));
    }
    @SubscribeEvent public static void hurt(LivingHurtEvent e){
        if(e.getSource().getEntity() instanceof ServerPlayer p){
            ItemStack weapon=p.getMainHandItem();CompoundTag t=affix(weapon);
            if(t!=null && !armor(weapon) && (e.getSource().getDirectEntity()==p || e.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE))){
                float bonus=.05f*(rank(t)+1);
                if(prefix(t)==0)e.setAmount(e.getAmount()*(1+bonus));
                if(prefix(t)==1)p.heal(Math.min(4,e.getAmount()*bonus));
            }
        }
        if(e.getEntity() instanceof ServerPlayer victim){
            int sum=0;
            for(ItemStack s:victim.getArmorSlots()){CompoundTag t=affix(s);if(t!=null&&prefix(t)==0)sum+=rank(t)+1;}
            e.setAmount(e.getAmount()*(1-.02f*sum));
        }
    }
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){
        for(String name:List.of("crate","injector","reforge")){
            e.getDispatcher().register(literal("perimeter_"+name).requires(s->s.hasPermission(2)).executes(c->{
                ServerPlayer p=c.getSource().getPlayerOrException();
                if(name.equals("reforge")){
                    ItemStack s=p.getMainHandItem();
                    if(!eligible(s)){p.displayClientMessage(Component.literal("Возьми оружие или броню в основную руку."),false);return 0;}
                    roll(s,p.getRandom());p.displayClientMessage(Component.literal(info(s)),false);
                }else grant(p,supply(name.equals("crate")));
                return 1;
            }));
        }
    }
}
