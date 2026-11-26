package perimeter;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="perimeter_siege",value=Dist.CLIENT)
public class ClientCheck {
    static int stage=0, ticks=0;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        if(mc.level==null||mc.player==null||mc.getSingleplayerServer()==null||++ticks%10!=0)return;
        if(ticks%200==0)System.out.println("Terminal probe: "+stage+" at "+mc.player.blockPosition()+" screen="+mc.screen);
        if(stage==0||stage==1&&ticks%80==0){
            var srv=mc.getSingleplayerServer();srv.execute(()->{
                var l=srv.overworld();var a=BaseOps.anchor(l);if(a==null)return;
                var p=srv.getPlayerList().getPlayers().get(0);
                p.teleportTo(l,a.getX(),a.getY(),a.getZ()+2,180,0);
            });
            stage=1;return;
        }
        if(stage==1){
            var vs=mc.level.getEntitiesOfClass(Villager.class,mc.player.getBoundingBox().inflate(6),v->v.hasCustomName()&&v.getCustomName().getString().contains("Ядро"));
            if(mc.screen instanceof TerminalScreen){stage=2;return;}
            if(!vs.isEmpty())mc.gameMode.interact(mc.player,vs.get(0),InteractionHand.MAIN_HAND);
            if(mc.screen instanceof TerminalScreen)stage=2;
            return;
        }
        if(!(mc.screen instanceof TerminalScreen screen))return;
        if(stage==2){mc.gameMode.handleInventoryButtonClick(screen.getMenu().containerId,2);stage=3;return;}
        if(stage==3&&screen.getMenu().tab()==2&&screen.getMenu().rows.getItem(0).isEmpty()==false){
            Screenshot.grab(mc.gameDirectory,mc.getMainRenderTarget(),msg->{});
            System.out.println("PERIMETER_CORE_CLIENT_PASS");stage=4;mc.stop();
        }
    }
}