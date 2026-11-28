package dev.energogrid;

import mekanism.common.lib.multiblock.IMultiblock;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

final class Commissioning {
    static void inspect(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof IMultiblock<?> machine)) return;
        var data = machine.getMultiblock();
        if (!data.isFormed()) return;
        if (data instanceof FissionReactorMultiblockData reactor && reactor.isBurning()) mark(player,"eg_fission");
        if (data instanceof TurbineMultiblockData turbine && turbine.getProductionRate() > 0) mark(player,"eg_turbine");
        if (data instanceof FusionReactorMultiblockData fusion && fusion.isBurning()) mark(player,"eg_fusion");
    }

    private static void mark(ServerPlayer player, String id) {
        if (Supply.score(player,id) == 0) Supply.add(player,id,1);
    }
}
