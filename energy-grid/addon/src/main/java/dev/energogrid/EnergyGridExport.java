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
        MoneyAPI.addIncomeListener(Accounts::record);
        MoneyAPI.addExpenseListener((server,account,amount,reason) -> Accounts.record(server,account,-amount,reason));
        MoneyAPI.addIncomeListener((server, account, amount, reason) -> {
            if (!account.isPlayer() || amount <= 0) return;
            var player = server.getPlayerList().getPlayer(account.id());
            if (player == null || Supply.score(player, "eg_job") == 0) return;
            boolean hosted = StackWalker.getInstance().walk(frames -> frames.anyMatch(frame ->
                frame.getClassName().equals("com.example.datacenter.hosting.HostingRackBlockEntity")));
            if (hosted) Supply.add(player, "eg_host", (int)Math.min(amount, 1000000L));
        });
        modBus.addListener(this::registerMeter);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(Commissioning::inspect);
        NeoForge.EVENT_BUS.addListener(ServiceOrders::bind);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("energogrid_internal")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("sell_surplus").executes(c -> Supply.sellSurplus(c.getSource().getPlayerOrException())))
            .then(Commands.literal("preview").executes(c -> Supply.order(c.getSource().getPlayerOrException(), true)))
            .then(Commands.literal("deliver").executes(c -> Supply.order(c.getSource().getPlayerOrException(), false)))
            .then(Commands.literal("contract").executes(c -> {
                var player = c.getSource().getPlayerOrException();
                int job = Supply.score(player, "eg_job");
                if (job==2 || job==3) {
                    String status=ServiceOrders.sample(player,job,true);
                    if (!status.equals(player.getPersistentData().getString("eg_reason")) && !status.startsWith("Работа:"))
                        Supply.say(player,status);
                    player.getPersistentData().putString("eg_reason",status);
                    return 1;
                }
                if (job < 1 || Supply.score(player,"eg_host") < job * 100) return 0;
                if (!MoneyAPI.pay(player,MoneyAPI.dollars(job*2),"Энергосеть: премия за хостинг").success()) return 0;
                Supply.add(player,"eg_job",-job);
                Supply.add(player,"eg_deals",1);
                Supply.add(player,"eg_hostpaid",1);
                return Supply.say(player,"Контракт закрыт. Премия $" + job*2);
            }))
            .then(Commands.literal("goods").then(Commands.argument("id", com.mojang.brigadier.arguments.StringArgumentType.string())
                .then(Commands.argument("count", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,64))
                .then(Commands.argument("price", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,100000))
                .executes(c -> Supply.purchase(c.getSource().getPlayerOrException(),c.getArgument("id",String.class),
                            c.getArgument("count",Integer.class),c.getArgument("price",Integer.class)))))))
            .then(Commands.literal("buy_coal").executes(context ->
                buyCoal(context.getSource().getPlayerOrException())))
            .then(Commands.literal("diagnose").executes(c -> {var p=c.getSource().getPlayerOrException(); return Supply.say(p,p.getPersistentData().getString("eg_reason"));}))
            .then(Commands.literal("balance").executes(context ->
                showBalance(context.getSource().getPlayerOrException())))
            .then(Commands.literal("can_afford_coal").executes(context ->
                MoneyAPI.has(context.getSource().getPlayerOrException(), COAL_PRICE) ? 1 : 0)));
    }

    private static int showBalance(ServerPlayer player) {
        player.sendSystemMessage(Component.literal(
            "Общий счёт Money: " + MoneyAPI.format(MoneyAPI.balance(player))));
        return Accounts.dashboard(player);
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
        Supply.add(player,"eg_coalbuy",1);
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

            if (Supply.score(owner,"eg_guard")==1 && Supply.score(owner,"eg_job")>0) {
                var battery=level.getCapability(Capabilities.EnergyStorage.BLOCK, METER.above(), net.minecraft.core.Direction.DOWN);
                if (battery==null || (long)battery.getEnergyStored()*4<=battery.getMaxEnergyStored()) return 0;
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
            int limit = Supply.score(owner,"eg_turbine") > 0 ? 100000 : Supply.score(owner,"eg_orders") > 0 ? 2000 : EXPORT_LIMIT;
            int accepted = Math.min(offered, Math.max(0, limit - used));
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
            long hour = level.getDayTime() % 24000;
            int multiplier = Supply.score(owner,"eg_spot") == 1 && hour >= 12000 && hour < 18000 ? 2 : 1;
            int received = pending.get() + accepted * multiplier;
            int coins = received / FE_PER_COIN;
            pending.set(received % FE_PER_COIN);
            if (coins > 0) {
                if (!MoneyAPI.pay(owner, MoneyAPI.dollars(coins), "Энергосеть: экспорт FE").success()) {
                    pending.set(received - accepted * multiplier);
                    budget.accepted = used;
                    return 0;
                }
                if (scores.getPlayerScoreInfo(owner, sales) == null
                        || scores.getPlayerScoreInfo(owner, sales).value() == 0) {
                    owner.sendSystemMessage(Component.literal(
                        "Первая продажа FE оплачена. Уголь можно купить в терминале."));
                }
                if (multiplier == 2) Supply.add(owner,"eg_peak",1);
                scores.getOrCreatePlayerScore(owner, revenue).add(coins);
                scores.getOrCreatePlayerScore(owner, sales).increment();
                scores.getOrCreatePlayerScore(owner, exported).add(coins);
            }
            return accepted;
        }

        @Override public int extractEnergy(int amount, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return 0; }
        @Override public int getMaxEnergyStored() { return 100000; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    }
}
