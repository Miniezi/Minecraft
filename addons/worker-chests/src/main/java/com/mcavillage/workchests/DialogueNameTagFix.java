package com.mcavillage.workchests;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Client-side workaround: in MCA dialogue GUIs, floating entity nametags can
 * render as huge text above the dialog. Keep the GUI's own villager label.
 * Never applies when no screen is open.
 */
@Mod.EventBusSubscriber(modid=WorkerChests.MODID, value=Dist.CLIENT, bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class DialogueNameTagFix {
    private DialogueNameTagFix() {}

    @SubscribeEvent
    public static void hideOverlappingVillagerTag(RenderNameTagEvent event) {
        Minecraft game=Minecraft.getInstance();
        if (game.screen==null || event.getEntity()==null) return;
        ResourceLocation type=ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
        if(type!=null && (type.getNamespace().equals("mca")
                || type.toString().equals("minecraft:villager"))) {
            event.setResult(Event.Result.DENY);
        }
    }
}
