package dev.energogrid;

import com.rhille.money.api.MoneyAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

final class Supply {
    static final BlockPos DEPOT = new BlockPos(-2, 81, 2);
    static int score(ServerPlayer p, String id) {
        var objective = p.getScoreboard().getObjective(id);
        if (objective == null) return 0;
        var value = p.getScoreboard().getPlayerScoreInfo(p, objective);
        return value == null ? 0 : value.value();
    }
    static void add(ServerPlayer p, String id, int value) {
        var objective = p.getScoreboard().getObjective(id);
        if (objective != null) p.getScoreboard().getOrCreatePlayerScore(p, objective).add(value);
    }
    static int say(ServerPlayer p, String message) {
        p.sendSystemMessage(Component.literal(message));
        return 0;
    }
    static int purchase(ServerPlayer p, String id, int count, int price) {
        if (price < 1 || count < 1 || count > 64) return 0;
        var item = BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse(id)).orElse(Items.AIR);
        if (item == Items.AIR) return 0;
        var stack = new ItemStack(item, count);
        int room = 0;
        for (var slot : p.getInventory().items) {
            if (slot.isEmpty()) room += stack.getMaxStackSize();
            else if (ItemStack.isSameItemSameComponents(slot, stack)) room += slot.getMaxStackSize() - slot.getCount();
        }
        if (room < count) return say(p, "Освободите место в инвентаре.");
        if (!MoneyAPI.charge(p, MoneyAPI.dollars(price), "Энергосеть: " + id).success())
            return say(p, "Недостаточно денег. Цена партии $" + price);
        p.getInventory().add(stack);
        if (item == Items.COAL) add(p, "eg_coalbuy", 1);
        add(p, "eg_spend", price);
        return say(p, "Доставлено " + count + " × " + item.getDescription().getString() + ". Расход $" + price);
    }
    static int sellSurplus(ServerPlayer p) {
        var level = p.serverLevel();
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD) || !level.hasChunkAt(DEPOT)
                || !(level.getBlockEntity(DEPOT) instanceof ChestBlockEntity chest)) return 0;
        if (chest.countItem(Items.WHEAT) < 40) return 0;
        if (!MoneyAPI.pay(p, MoneyAPI.dollars(2), "Энергосеть: продажа 8 пшеницы").success()) return 0;
        int remaining = 8;
        for (int slot=0; slot<chest.getContainerSize() && remaining>0; slot++) {
            if (chest.getItem(slot).is(Items.WHEAT)) remaining -= chest.removeItem(slot,remaining).getCount();
        }
        chest.setChanged();
        add(p,"eg_sold",2);
        return 1;
    }
    static int order(ServerPlayer p, boolean preview) {
        int fuel = deliver(p,preview,Items.COAL,"Уголь",3,score(p,"eg_bulk")==1?64:8);
        if (score(p,"eg_ice")==1) deliver(p,preview,Items.ICE,"Лёд",5,8);
        return fuel;
    }
    private static int deliver(ServerPlayer p, boolean preview, net.minecraft.world.item.Item item, String label, int price, int target) {
        var world = p.serverLevel();
        if (!world.dimension().equals(net.minecraft.world.level.Level.OVERWORLD) || !world.hasChunkAt(DEPOT)
                || !(world.getBlockEntity(DEPOT) instanceof ChestBlockEntity chest))
            return preview ? say(p,"Нужен обычный сундук в точке −2, 81, 2.") : 0;
        int stock = 0, room = 0;
        for (int i=0; i<chest.getContainerSize(); i++) {
            var s = chest.getItem(i);
            if (s.isEmpty()) room += 64;
            else if (s.is(item)) { stock += s.getCount(); room += 64 - s.getCount(); }
        }
        int reserve = score(p, "eg_bulk") == 1 ? 100 : 6;
        long available = Math.max(0, MoneyAPI.balance(p) / MoneyAPI.dollars(1) - reserve);
        int qty = stock >= target/2 ? 0 : (int)Math.min(Math.min(target-stock,room), available/price);
        if (score(p,"eg_cap")>0 && price>score(p,"eg_cap")) return preview ? say(p,label+": цена выше потолка.") : 0;
        if (preview) return say(p,label+" на складе: " + stock + ". Порог: " + target/2 + ", цель: " + target + ", резерв: $" + reserve + ". Можно доставить: " + qty + " шт. за $" + qty*price);
        if (qty == 0 || !MoneyAPI.charge(p,MoneyAPI.dollars(qty*price),"Энергосеть: склад — "+label).success()) return 0;
        int remaining = qty;
        for (int i=0;i<chest.getContainerSize() && remaining>0;i++) {
            var slot = chest.getItem(i);
            if (!slot.isEmpty() && !slot.is(item)) continue;
            int n = Math.min(remaining,64-slot.getCount());
            if (n>0) chest.setItem(i,new ItemStack(item,slot.getCount()+n));
            remaining -= n;
        }
        chest.setChanged();
        add(p,"eg_spend",qty*price);
        if (item==Items.COAL) add(p,"eg_orders",1);
        return say(p,"Склад получил "+label+": " + qty + " шт. Списано $" + qty*price);
    }
}
