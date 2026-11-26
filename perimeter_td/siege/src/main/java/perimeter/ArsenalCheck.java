package perimeter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="perimeter_siege")
public class ArsenalCheck {
    @SubscribeEvent public static void run(ServerStartedEvent event){
        if(!Boolean.getBoolean("perimeter.testArsenal"))return;
        RandomSource random=RandomSource.create(1234);
        ItemStack gun=new ItemStack(Items.CROSSBOW);
        CompoundTag original=gun.getOrCreateTag();
        original.putString("GunId","tacz:ak47");
        original.putInt("GunCurrentAmmoCount",13);
        CompoundTag attachments=new CompoundTag();
        attachments.putString("scope","test:optic");
        original.put("Attachments",attachments);
        CompoundTag expected=original.copy();
        Arsenal.roll(gun,random);
        int tier=Arsenal.rank(Arsenal.affix(gun));
        for(int i=0;i<100;i++){
            int before=Arsenal.prefix(Arsenal.affix(gun));
            Arsenal.roll(gun,random);
            if(before==Arsenal.prefix(Arsenal.affix(gun))||tier!=Arsenal.rank(Arsenal.affix(gun)))throw new AssertionError("Invalid reforge");
        }
        CompoundTag result=gun.getTag().copy();
        result.remove(Arsenal.KEY);
        if(!result.equals(expected))throw new AssertionError("Gun NBT changed");
        boolean[] found=new boolean[6];
        for(int i=0;i<10000;i++)found[Arsenal.tier(random)]=true;
        for(boolean b:found)if(!b)throw new AssertionError("Missing rarity");
        System.out.println("PERIMETER_ARSENAL_PASS");
        event.getServer().halt(false);
    }
}
