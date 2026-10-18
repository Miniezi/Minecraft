package com.mcavillage.workchests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.UUID;

@Mod(WorkerChests.MODID)
public final class WorkerChests {
    public static final String MODID = "mcaworkchests";
    private static final String SELECTED = "mcaworkchestsSelected";
    private static final String CHEST = "mcaworkchestsChest";
    private static final String DIMENSION = "mcaworkchestsDimension";
    private static final String OWNER = "mcaworkchestsOwner";
    private static final String HELD = "mcaworkchestsHeld";
    public WorkerChests() { MinecraftForge.EVENT_BUS.register(this); }

    private static boolean villager(Entity e) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
        if (id == null) return false;
        return e instanceof Mob && (id.getNamespace().equals("mca")
                || id.toString().equals("minecraft:villager"));
    }

    private static void message(ServerPlayer p, String value) {
        p.displayClientMessage(Component.literal("[Рабочий сундук] " + value).withStyle(ChatFormatting.GOLD), true);
    }

    // Sneak + right-click a villager with a STICK to select that worker.
    @SubscribeEvent
    public void select(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.isShiftKeyDown() || !event.getItemStack().is(Items.STICK)) return;
        if (!villager(event.getTarget())) return;
        player.getPersistentData().putUUID(SELECTED, event.getTarget().getUUID());
        message(player, "Работник выбран. Теперь Shift + ПКМ палкой по сундуку.");
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    // Sneak + right-click with STICK on a chest to assign it to the selected villager.
    @SubscribeEvent
    public void assign(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.isShiftKeyDown() || !event.getItemStack().is(Items.STICK)) return;
        BlockPos pos = event.getPos();
        BlockEntity be = player.level().getBlockEntity(pos);
        if (!(be instanceof Container)) return;
        CompoundTag pd = player.getPersistentData();
        if (!pd.hasUUID(SELECTED)) { message(player, "Сначала выбери жителя Shift + ПКМ палкой."); return; }
        UUID selected = pd.getUUID(SELECTED);
        ServerLevel level = player.serverLevel();
        Entity worker = level.getEntity(selected);
        if (worker == null || !villager(worker) || worker.distanceToSqr(player) > 1024) {
            message(player, "Подойди к выбранному жителю (не дальше 32 блоков).");
            return;
        }
        if (worker.blockPosition().distSqr(pos) > 256) {
            message(player, "Рабочий сундук должен быть не дальше 16 блоков от жителя.");
            return;
        }
        CompoundTag workerData = worker.getPersistentData();
        workerData.putLong(CHEST, pos.asLong());
        workerData.putString(DIMENSION, level.dimension().location().toString());
        workerData.remove(HELD);
        be.getPersistentData().putUUID(OWNER, selected);
        be.setChanged();
        message(player, "Сундук закреплён за жителем! Он может собирать урожай с земли.");
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static boolean crop(Item item) {
        return item == Items.WHEAT || item == Items.WHEAT_SEEDS
            || item == Items.CARROT || item == Items.POTATO || item == Items.BEETROOT
            || item == Items.BEETROOT_SEEDS || item == Items.BREAD
            || item == Items.APPLE || item == Items.EGG || item == Items.SUGAR_CANE
            || item == Items.PUMPKIN || item == Items.MELON_SLICE;
    }

    private static ItemStack store(Container storage, ItemStack incoming) {
        ItemStack rest = incoming.copy();
        for (int i = 0; i < storage.getContainerSize() && !rest.isEmpty(); i++) {
            ItemStack slot = storage.getItem(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameTags(rest,slot)) {
                int transfer=Math.min(storage.getMaxStackSize()-slot.getCount(),rest.getCount());
                if (transfer>0) { slot.grow(transfer);rest.shrink(transfer);storage.setItem(i,slot); }
            }
        }
        for (int i = 0; i < storage.getContainerSize() && !rest.isEmpty(); i++) {
            if (storage.getItem(i).isEmpty()) {
                int count=Math.min(rest.getMaxStackSize(),rest.getCount());
                ItemStack piece=rest.copy();piece.setCount(count);
                storage.setItem(i,piece);rest.shrink(count);
            }
        }
        storage.setChanged();
        return rest;
    }

    // Workers gather only dropped agricultural produce and physically return it to the assigned chest.
    // We intentionally don't reach into MCA private inventories or steal player-placed chest items.
    @SubscribeEvent
    public void tick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity=event.getEntity();
        if (!(entity instanceof Mob worker) || !villager(worker)) return;
        if (!(worker.level() instanceof ServerLevel level)) return;
        if (worker.tickCount % 30 != 0) return;
        CompoundTag data=worker.getPersistentData();
        if (!data.contains(CHEST) || !level.dimension().location().toString().equals(data.getString(DIMENSION))) return;
        BlockPos chestPos=BlockPos.of(data.getLong(CHEST));
        if (worker.blockPosition().distSqr(chestPos)>1024 || !level.isLoaded(chestPos)) return;
        BlockEntity be=level.getBlockEntity(chestPos);
        if (!(be instanceof Container storage) || !be.getPersistentData().hasUUID(OWNER)
                || !be.getPersistentData().getUUID(OWNER).equals(worker.getUUID())) return;
        if (data.contains(HELD)) {
            ItemStack held=ItemStack.of(data.getCompound(HELD));
            if (held.isEmpty()) { data.remove(HELD); return; }
            if (worker.blockPosition().distSqr(chestPos)>6) {
                worker.getNavigation().moveTo(chestPos.getX()+0.5,chestPos.getY(),chestPos.getZ()+0.5,1.0);
                return;
            }
            ItemStack rest=store(storage,held);
            if (rest.isEmpty()) data.remove(HELD);
            else data.put(HELD,rest.save(new CompoundTag()));
            return;
        }
        List<ItemEntity> nearby=level.getEntitiesOfClass(ItemEntity.class,new AABB(worker.blockPosition()).inflate(6),
              item -> item.isAlive() && crop(item.getItem().getItem()) && !item.getItem().isEmpty());
        if (nearby.isEmpty()) return;
        ItemEntity item=nearby.get(0);
        if (worker.distanceToSqr(item)>4) {
            worker.getNavigation().moveTo(item,1.0);
            return;
        }
        ItemStack acquired=item.getItem().copy();
        if (acquired.isEmpty()) return;
        data.put(HELD,acquired.save(new CompoundTag()));
        item.discard();
    }
}
