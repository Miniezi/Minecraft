package dev.energogrid.tests;
import com.mojang.authlib.GameProfile;
import com.rhille.money.api.MoneyAPI;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.server.MinecraftServer;

@Mod("grid_smoke_test")
public class GridSmoke {
    private FakePlayer tester;
    private int ticks = 0;
    private long purchasedAt = -1L;

    public GridSmoke() {
        NeoForge.EVENT_BUS.addListener(this::test);
    }

    private void test(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel world = server.overworld();
        var source = server.createCommandSourceStack();
        try {
            if (++ticks == 40) {
                tester = new FakePlayer(world, new GameProfile(
                        UUID.fromString("afb8fe0f-88af-4702-8e57-86e630739906"), "GridProbe"));
                tester.setPos(0.5, 81, 0.5);
                world.addNewPlayer(tester);
                if (java.nio.file.Files.exists(java.nio.file.Path.of("first-pass"))) {
                    double bal = MoneyAPI.balance(tester);
                    var box=(net.minecraft.world.level.block.entity.ChestBlockEntity)world.getBlockEntity(new BlockPos(-2,81,2));
                    if (bal!=MoneyAPI.dollars(7) || box.countItem(Items.ICE)!=3 || box.countItem(Items.COAL)!=8)
                        throw new IllegalStateException("restart: "+bal+" ice "+box.countItem(Items.ICE)+" coal "+box.countItem(Items.COAL));
                    System.out.println("RESTART PASS: balance $7, coal 8, ice 3");
                    tester.discard(); server.halt(false);return;
                }

                server.getCommands().performPrefixedCommand(source, "function energogrid:island");
                server.getCommands().performPrefixedCommand(tester.createCommandSourceStack().withPermission(2),
                        "function energogrid:player");
                System.out.println("GRID SMOKE: spawn " + tester.position());
                server.getCommands().performPrefixedCommand(source, "setblock 2 81 0 powah:furnator_starter");
                for (int x = 3; x <= 5; ++x) {
                    server.getCommands().performPrefixedCommand(source,
                            "setblock " + x + " 81 0 powah:energy_cable_starter");
                }
                if (MoneyAPI.balance(tester) != 0) throw new IllegalStateException("Test starts with funds");
                fuel(world); server.getCommands().performPrefixedCommand(source,"tick sprint 6000");
                System.out.println("GRID SMOKE: island, Furnator and three real Powah cables ready");
            }
            if (tester == null) return;
            long balance = MoneyAPI.balance(tester);
            if (purchasedAt < 0 && balance >= MoneyAPI.dollars(3)) {
                System.out.println("GRID SMOKE: FE payout funded purchase at tick " + ticks);
                purchasedAt = balance;
                server.getCommands().performPrefixedCommand(tester.createCommandSourceStack().withPermission(2),
                        "function energogrid:buy");
                if (MoneyAPI.balance(tester) != balance - MoneyAPI.dollars(3)
                        || !tester.getInventory().contains(new ItemStack(Items.COAL))) {
                    throw new IllegalStateException("Purchase failed");
                }
            }
            if (ticks == 2520) {
                if (balance != MoneyAPI.dollars(9)) throw new IllegalStateException("Cycle one balance "+balance);
                for (var stack : tester.getInventory().items) if (stack.is(Items.COAL)) { stack.shrink(1); break; }
                fuel(world);
            }
            if (ticks == 5040) {
                if (balance != MoneyAPI.dollars(21)) throw new IllegalStateException("Cycle two balance "+balance);
                var src = tester.createCommandSourceStack().withPermission(2);
                var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) world.getBlockEntity(new BlockPos(-2,81,2));
                server.getCommands().performPrefixedCommand(src,"energogrid_internal preview");
                if (MoneyAPI.balance(tester) != balance) throw new IllegalStateException("Preview charged");
                int qty=chest.countItem(Items.COAL);
                server.getCommands().performPrefixedCommand(src,"energogrid_internal deliver");
                if (MoneyAPI.balance(tester)!=MoneyAPI.dollars(6) || chest.countItem(Items.COAL)!=qty+5) throw new IllegalStateException("Partial delivery");
                server.getCommands().performPrefixedCommand(src,"energogrid_internal deliver");
                if (MoneyAPI.balance(tester)!=MoneyAPI.dollars(6)) throw new IllegalStateException("Duplicate delivery");
                chest.setItem(15,new ItemStack(Items.WHEAT,40));
                server.getCommands().performPrefixedCommand(src,"energogrid_internal sell_surplus");
                if (chest.countItem(Items.WHEAT)!=32 || MoneyAPI.balance(tester)!=MoneyAPI.dollars(8)) throw new IllegalStateException("Surplus mismatch");
                server.getCommands().performPrefixedCommand(src,"energogrid_internal sell_surplus");
                if (MoneyAPI.balance(tester)!=MoneyAPI.dollars(8)) throw new IllegalStateException("Protected stock");
                System.out.println("CAMPAIGN PASS: two FE cycles 12+12, manual fuel 3, auto delivery 5 for 15, reserve 6, surplus 8 for 2, ending balance 8");

                // Isolate warehouse edge cases from generation.
                server.getCommands().performPrefixedCommand(source,"setblock 2 81 0 air");
                MoneyAPI.pay(tester,MoneyAPI.dollars(32),"fixture");
                server.getCommands().performPrefixedCommand(src,"scoreboard players set @s eg_cap 2");
                server.getCommands().performPrefixedCommand(src,"energogrid_internal deliver");
                if (MoneyAPI.balance(tester)!=MoneyAPI.dollars(40)) throw new IllegalStateException("Price cap");
                for(int i=0;i<chest.getContainerSize();i++) chest.setItem(i,new ItemStack(Items.COBBLESTONE,64));
                server.getCommands().performPrefixedCommand(src,"scoreboard players set @s eg_cap 5");
                server.getCommands().performPrefixedCommand(src,"energogrid_internal deliver");
                if (MoneyAPI.balance(tester)!=MoneyAPI.dollars(40)) throw new IllegalStateException("Full stock");
                chest.setItem(0,new ItemStack(Items.COAL,2));
                chest.setItem(1,ItemStack.EMPTY);
                server.getCommands().performPrefixedCommand(src,"scoreboard players set @s eg_ice 1");
                server.getCommands().performPrefixedCommand(src,"energogrid_internal deliver");
                // 6 coal for $18, then 3 ice for $15, leaving $7.
                if (chest.countItem(Items.COAL)!=8 || chest.countItem(Items.ICE)!=3 || MoneyAPI.balance(tester)!=MoneyAPI.dollars(7)) throw new IllegalStateException("Two rules");
                System.out.println("WAREHOUSE PASS: price cap, full chest, priority and reserve: 6 coal then 3 ice, $7 remaining");
                try {java.nio.file.Files.createFile(java.nio.file.Path.of("first-pass"));} catch(java.io.IOException ex){throw new RuntimeException(ex);}
                tester.discard();server.halt(false);
            }
        } catch (RuntimeException ex) {
            System.err.println("GRID SMOKE FAIL: " + ex);
            tester.discard();
            server.halt(false);
        }
    }

    private static void fuel(ServerLevel level) {
        for (Direction direction : Direction.values()) {
            var inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, new BlockPos(2, 81, 0), direction);
            if (inventory == null) continue;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                if (inventory.insertItem(slot, new ItemStack(Items.COAL), true).isEmpty()) {
                    inventory.insertItem(slot, new ItemStack(Items.COAL), false);
                    return;
                }
            }
        }
        throw new IllegalStateException("Failed inserting coal into Furnator");
    }
}
