package perimeter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="perimeter_siege",value=Dist.CLIENT)
public class OperatorInput {
    @SubscribeEvent(priority=EventPriority.HIGHEST) public static void click(InputEvent.InteractionKeyMappingTriggered e){
        var mc=Minecraft.getInstance();
        if(!e.isUseItem()||e.getHand()!=InteractionHand.MAIN_HAND||mc.player==null||mc.getConnection()==null||!(mc.hitResult instanceof EntityHitResult hit)||!(hit.getEntity() instanceof Villager v)||v.getCustomName()==null||!v.getCustomName().getString().contains("◆ Ядро"))return;
        mc.getConnection().send(ServerboundInteractPacket.createInteractionPacket(v,mc.player.isShiftKeyDown(),InteractionHand.MAIN_HAND));
        e.setSwingHand(false);e.setCanceled(true);
    }
}