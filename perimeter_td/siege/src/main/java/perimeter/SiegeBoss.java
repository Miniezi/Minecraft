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
    record Gun(String id,String name,int mag,int min,int max,double strong,double weak){}
    static final java.util.Map<String,Gun> GUNS=java.util.Map.of(
        "guard",new Gun("ak47","Пролом",30,0,10,1.5,.75),
        "scout",new Gun("m4a1","Дальний дозор",30,18,256,1.6,.65),
        "fang",new Gun("deagle","Клык осады",7,5,16,1.45,.85),
        "ram",new Gun("m870","Таран",5,0,8,1.5,.70),
        "spotter",new Gun("m700","Корректировщик",5,22,256,1.6,.65),
        "mortar",new Gun("m320","Навес",1,12,30,1.35,.75));
    static String[] pool(String kind){return switch(kind){
        case "sieger"->new String[]{"ram","guard"};
        case "howitzer"->new String[]{"mortar","spotter"};
        case "hindenburg"->new String[]{"fang","scout"};
        default->new String[]{kind};};}
    static ItemStack box(String kind){
        ItemStack s=Arsenal.supply(true);
        s.getOrCreateTag().putString("SiegeBossBox",kind);
        s.setHoverName(Component.literal("Трофей: "+(switch(kind){case "sieger"->"Sieger";case "howitzer"->"Howitzer";case "hindenburg"->"Hindenburg";case "guard"->"Панцирник";default->"Дозорный";})).withStyle(ChatFormatting.GOLD));
        return s;
    }
    static ItemStack weapon(String kind,net.minecraft.util.RandomSource random){
        String[] options=pool(kind);kind=options[random.nextInt(options.length)];Gun gun=GUNS.get(kind);
        Item item=ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz","modern_kinetic_gun"));
        ItemStack s;
        if(item==null||item==Items.AIR)s=new ItemStack(Items.CROSSBOW);
        else{
            s=new ItemStack(item);
            s.getOrCreateTag().putString("GunId","tacz:"+gun.id());
            s.getOrCreateTag().putInt("GunCurrentAmmoCount",gun.mag());
        }
        s.getOrCreateTag().putString(RELIC,kind);
        s.setHoverName(Component.literal(gun.name()).withStyle(ChatFormatting.GOLD));
        Arsenal.roll(s,random);Arsenal.affix(s).putInt("tier",4);
        return s;
    }
    static double scale(String kind,double range){
        Gun g=GUNS.get(kind);return g==null?1:range>=g.min()&&range<=g.max()?g.strong():g.weak();
    }
    @SubscribeEvent public static void tooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent e){
        ItemStack s=e.getItemStack();
        if(s.hasTag()&&s.getTag().contains(RELIC)){Gun g=GUNS.get(s.getTag().getString(RELIC));if(g!=null)e.getToolTip().add(Component.literal("Дистанция "+g.min()+"–"+g.max()+" бл.: ×"+g.strong()+"; вне диапазона: ×"+g.weak()).withStyle(ChatFormatting.GOLD));}
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
}
