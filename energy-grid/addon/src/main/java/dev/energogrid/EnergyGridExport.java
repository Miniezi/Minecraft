package dev.energogrid;

import com.mojang.brigadier.CommandDispatcher;
import com.rhille.money.api.MoneyAPI;
import java.util.WeakHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod("energogrid_export")
public final class EnergyGridExport {
    private static final BlockPos METER = new BlockPos(6, 81, 0);
    private static final int EXPORT_LIMIT = 200;
    private static final int FE_PER_COIN = 4_000;
    private static final long COAL_PRICE = MoneyAPI.dollars(3);
    private final WeakHashMap<ServerLevel, TickBudget> budgets = new WeakHashMap<>();

    public EnergyGridExport(IEventBus modBus) {
        modBus.addListener(this::registerMeter);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("energogrid_internal")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("buy_coal").executes(context ->
                buyCoal(context.getSource().getPlayerOrException())))
            .then(Commands.literal("balance").executes(context ->
                showBalance(context.getSource().getPlayerOrException())))
            .then(Commands.literal("can_afford_coal").executes(context ->
                MoneyAPI.has(context.getSource().getPlayerOrException(), COAL_PRICE) ? 1 : 0)));
    }

    private static int showBalance(ServerPlayer player) {
        player.sendSystemMessage(Component.literal(
            "Общий счёт Money: " + MoneyAPI.format(MoneyAPI.balance(player))));
        return 1;
    }

    private static int buyCoal(ServerPlayer player) {
        if (player.getInventory().getFreeSlot() < 0
                && player.getInventory().getSlotWithRemainingSpace(new ItemStack(Items.COAL)) < 0) {
            player.sendSystemMessage(Component.literal("Освободите место для угля."));
            return 0;
        }
        if (!MoneyAPI.charge(player, COAL_PRICE, "Энергосеть: закупка угля").success()) {
            player.sendSystemMessage(Component.literal("Недостаточно средств для покупки угля."));
            return 0;
        }
        player.getInventory().add(new ItemStack(Items.COAL));
        Objective spending = player.getScoreboard().getObjective("eg_spend");
        if (spending != null) {
            player.getScoreboard().getOrCreatePlayerScore(player, spending).add(3);
        }
        player.sendSystemMessage(Component.literal("Уголь доставлен. Списано " + MoneyAPI.format(COAL_PRICE)));
        return 1;
    }

    private void registerMeter(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (level instanceof ServerLevel server && server.dimension().equals(Level.OVERWORLD)
                    && pos.equals(METER)) {
                return new Meter(server);
            }
            return null;
        }, Blocks.LODESTONE);
    }

    private static final class TickBudget {
        private long tick = Long.MIN_VALUE;
        private int accepted;
    }

    private final class Meter implements IEnergyStorage {
        private final ServerLevel level;

        private Meter(ServerLevel level) {
            this.level = level;
        }

        @Override
        public int receiveEnergy(int offered, boolean simulate) {
            if (offered <= 0 || !level.getServer().isSameThread() || level.players().size() != 1) {
                return 0;
            }
            ServerPlayer owner = level.players().getFirst();
            if (owner.distanceToSqr(0.5, 81, 0.5) > 24 * 24) {
                return 0;
            }

            Scoreboard scores = level.getScoreboard();
            Objective remainder = scores.getObjective("eg_fe");
            Objective revenue = scores.getObjective("eg_last");
            Objective sales = scores.getObjective("eg_sales");
            Objective exported = scores.getObjective("eg_fe_paid");
            Objective ready = scores.getObjective("eg_kit");
            if (remainder == null || revenue == null || sales == null
                    || exported == null || ready == null) {
                return 0;
            }
            var readyScore = scores.getPlayerScoreInfo(owner, ready);
            if (readyScore == null || readyScore.value() != 1) {
                return 0;
            }

            long now = level.getGameTime();
            TickBudget budget = budgets.get(level);
            int used = budget != null && budget.tick == now ? budget.accepted : 0;
            int accepted = Math.min(offered, EXPORT_LIMIT - used);
            if (simulate || accepted == 0) {
                return accepted;
            }

            if (budget == null) {
                budget = new TickBudget();
                budgets.put(level, budget);
            }
            budget.tick = now;
            budget.accepted = used + accepted;

            var pending = scores.getOrCreatePlayerScore(owner, remainder);
            int received = pending.get() + accepted;
            int coins = received / FE_PER_COIN;
            pending.set(received % FE_PER_COIN);
            if (coins > 0) {
                if (!MoneyAPI.pay(owner, MoneyAPI.dollars(coins), "Энергосеть: экспорт FE").success()) {
                    pending.set(received - accepted);
                    budget.accepted = used;
                    return 0;
                }
                if (scores.getPlayerScoreInfo(owner, sales) == null
                        || scores.getPlayerScoreInfo(owner, sales).value() == 0) {
                    owner.sendSystemMessage(Component.literal(
                        "Первая продажа FE оплачена. Уголь можно купить в терминале."));
                }
                scores.getOrCreatePlayerScore(owner, revenue).add(coins);
                scores.getOrCreatePlayerScore(owner, sales).increment();
                scores.getOrCreatePlayerScore(owner, exported).add(coins);
            }
            return accepted;
        }

        @Override public int extractEnergy(int amount, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return 0; }
        @Override public int getMaxEnergyStored() { return EXPORT_LIMIT; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }
}
