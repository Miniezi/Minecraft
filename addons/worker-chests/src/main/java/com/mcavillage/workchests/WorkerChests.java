package com.mcavillage.workchests;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
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
    private static final String PAUSED = "mcaworkchestsPaused";
    private static final String DELIVERED = "mcaworkchestsDelivered";
    private static final String COLLECTIONS = "mcaworkchestsCollections";
    private static final String NEED_FOOD = "mcaworkchestsFoodNeed";
    private static final String NEED_COMFORT = "mcaworkchestsComfortNeed";
    private static final String FOOD_RESERVE = "mcaworkchestsFoodReserve";
    private static final String LAST_NEEDS = "mcaworkchestsLastNeeds";
    private static final int RESERVE_LIMIT = 12;
    private static final int DEPOSIT_FOOD_MIN = 16;
    private static boolean edible(Item item) {
        return item == Items.BREAD || item == Items.APPLE || item == Items.CARROT
            || item == Items.POTATO || item == Items.BEETROOT || item == Items.MELON_SLICE;
    }
    // Standalone needs: deliberately NOT claimed to be Townstead hunger or MCA mood.
    private static void updateNeeds(Mob worker, ServerLevel level, CompoundTag data) {
        long now=level.getGameTime(), previous=data.contains(LAST_NEEDS) ? data.getLong(LAST_NEEDS) : now;
        long elapsed=Math.max(0,Math.min(now-previous,24000L));
        if (elapsed<200) return;
        data.putLong(LAST_NEEDS,now);
        int hunger=Math.min(100,data.getInt(NEED_FOOD)+(int)(elapsed/200));
        int comfort=Math.min(100,data.getInt(NEED_COMFORT)+(int)(elapsed/400));
        if (level.isRainingAt(worker.blockPosition())) comfort=Math.min(100,comfort+2);
        if (worker.getHealth()<worker.getMaxHealth()*0.5F) comfort=Math.min(100,comfort+2);
        // Resting in a bedroom restores comfort. Scan at most 75 blocks once per 200 ticks.
        if (data.getBoolean(PAUSED) && comfort>0) {
            BlockPos center=worker.blockPosition();
            boolean bedNearby=false;
            for (BlockPos candidate:BlockPos.betweenClosed(center.offset(-2,-1,-2),center.offset(2,1,2))) {
                if (level.getBlockState(candidate).is(BlockTags.BEDS)) { bedNearby=true;break; }
            }
            comfort=Math.max(0,comfort-(bedNearby?12:3));
        }
        int reserve=data.getInt(FOOD_RESERVE);
        if (hunger>=65 && reserve>0) {
            reserve--;
            hunger=Math.max(0,hunger-45);
            comfort=Math.max(0,comfort-10);
            data.putInt(FOOD_RESERVE,reserve);
        }
        data.putInt(NEED_FOOD,hunger);
        data.putInt(NEED_COMFORT,comfort);
    }
    private static int foodInChest(Container storage) {
        int sum=0;
        for(int i=0;i<storage.getContainerSize();i++){
            ItemStack item=storage.getItem(i);
            if (edible(item.getItem())) sum+=item.getCount();
        }
        return sum;
    }
    // Workers may use one item from communal food surplus, but never empty the pantry.
    private static void replenishFood(Container storage,CompoundTag data) {
        if(data.getInt(FOOD_RESERVE)>0 || data.getInt(NEED_FOOD)<70) return;
        if(foodInChest(storage)<=DEPOSIT_FOOD_MIN) return;
        for(int i=0;i<storage.getContainerSize();i++) {
            ItemStack stack=storage.getItem(i);
            if(!stack.isEmpty() && edible(stack.getItem()) && storage.canTakeItem(storage,i,stack)) {
                stack.shrink(1);
                storage.setChanged();
                data.putInt(FOOD_RESERVE,1);
                return;
            }
        }
    }
    private static int reserveFromCollected(Mob storageOwner,CompoundTag data,ItemStack acquired,Container storage){
        if (!edible(acquired.getItem())) return 0;
        TownsteadNeedsBridge.State real=TownsteadNeedsBridge.read((Mob)storageOwner);
        int hunger=data.getInt(NEED_FOOD);
        int comfort=data.getInt(NEED_COMFORT);
        int desired=real.available()
          ? (real.hunger()<=35?12:(real.thirst()<=30||real.fatigue()>=65?8:4))
          : (hunger>=45?12:(comfort>=70?8:4));
        int owned=data.getInt(FOOD_RESERVE);
        // Do not hoard while the village store is short of food, except in emergencies.
        if(foodInChest(storage)<DEPOSIT_FOOD_MIN && (real.available()?real.hunger()>20:hunger<80)) desired=Math.min(desired,2);
        int reserved=Math.min(acquired.getCount(),Math.max(0,Math.min(RESERVE_LIMIT,desired)-owned));
        if(reserved>0)data.putInt(FOOD_RESERVE,owned+reserved);
        return reserved;
    }
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
        // Never erase a carried harvest: reassignment while transporting is rejected.
        if(workerData.contains(HELD) && !ItemStack.of(workerData.getCompound(HELD)).isEmpty()) {
            message(player,"Работник несёт урожай. Дождись доставки перед сменой склада.");
            return;
        }
        if(be.getPersistentData().hasUUID(OWNER) && !selected.equals(be.getPersistentData().getUUID(OWNER))) {
            message(player,"Этот склад уже принадлежит другому работнику.");
            return;
        }
        // Release the previously owned container, if available in this dimension.
        if(workerData.contains(CHEST) && level.dimension().location().toString().equals(workerData.getString(DIMENSION))) {
            BlockPos oldPos=BlockPos.of(workerData.getLong(CHEST));
            if(!oldPos.equals(pos) && level.isLoaded(oldPos)) {
                BlockEntity old=level.getBlockEntity(oldPos);
                if(old!=null && old.getPersistentData().hasUUID(OWNER)
                     && selected.equals(old.getPersistentData().getUUID(OWNER))) {
                    old.getPersistentData().remove(OWNER);
                    old.setChanged();
                }
            }
        }
        be.getPersistentData().putUUID(OWNER, selected);
        be.setChanged();
        message(player, "Сундук закреплён за жителем! Он может собирать урожай с земли.");
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }


    // Shift + book on a villager shows a compact Russian worker status card.
    @SubscribeEvent
    public void inspectWorker(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.isShiftKeyDown() || !event.getItemStack().is(Items.BOOK) || !villager(event.getTarget())) return;
        CompoundTag data=event.getTarget().getPersistentData();
        boolean assigned=data.contains(CHEST);
        player.sendSystemMessage(Component.literal("§6◆ Работник §7— "+event.getTarget().getName().getString()));
        player.sendSystemMessage(Component.literal("§eСундук: §f"+(assigned?BlockPos.of(data.getLong(CHEST)).toShortString():"не назначен")
              +" §8| §eРежим: §f"+(data.getBoolean(PAUSED)?"отдых":"сбор")));
        player.sendSystemMessage(Component.literal("§eГолод: §f"+data.getInt(NEED_FOOD)+"/100 §8| §eУсталость: §f"
              +data.getInt(NEED_COMFORT)+"/100 §8| §eЗапас еды: §f"+data.getInt(FOOD_RESERVE)));
        player.sendSystemMessage(Component.literal("§eДоставлено: §f"+data.getLong(DELIVERED)
              +" §8| §eСборов: §f"+data.getLong(COLLECTIONS)));
        player.sendSystemMessage(Component.literal("§7Shift+ПКМ книгой по сундуку — управление; палкой — назначение."));
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    // Book = inspect, redstone torch = switch worker between work and rest.
    // Both interactions require explicit sneaking so normal chest use is unchanged.
    @SubscribeEvent
    public void manageChest(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isShiftKeyDown()) return;
        boolean inspect=event.getItemStack().is(Items.BOOK);
        boolean toggle=event.getItemStack().is(Items.REDSTONE_TORCH);
        if (!inspect && !toggle) return;
        BlockEntity be=player.level().getBlockEntity(event.getPos());
        if (!(be instanceof Container) || !be.getPersistentData().hasUUID(OWNER)) return;
        Entity worker=player.serverLevel().getEntity(be.getPersistentData().getUUID(OWNER));
        if (worker==null || !villager(worker)) {
            message(player,"Работник не загружен. Подойди ближе к жителю.");
            return;
        }
        CompoundTag data=worker.getPersistentData();
        if (!data.contains(CHEST) || !event.getPos().equals(BlockPos.of(data.getLong(CHEST)))
                || !player.serverLevel().dimension().location().toString().equals(data.getString(DIMENSION))) {
            message(player,"Привязка сундука устарела. Назначь работника заново.");
            return;
        }
        if (toggle) {
            boolean paused=!data.getBoolean(PAUSED);
            data.putBoolean(PAUSED,paused);
            message(player,paused?"Работник отдыхает: сбор приостановлен.":"Работник вернулся к сбору.");
        } else {
            message(player,"Работник: "+worker.getName().getString()+" | "
                    +(data.getBoolean(PAUSED)?"отдых":"сбор")+" | доставлено "+data.getLong(DELIVERED)
                    +" | еды "+data.getInt(FOOD_RESERVE));
        }
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
                if (!storage.canPlaceItem(i,rest)) continue;
                int cap=Math.min(storage.getMaxStackSize(),slot.getMaxStackSize());
                int transfer=Math.min(cap-slot.getCount(),rest.getCount());
                if (transfer>0) { slot.grow(transfer);rest.shrink(transfer);storage.setItem(i,slot); }
            }
        }
        for (int i = 0; i < storage.getContainerSize() && !rest.isEmpty(); i++) {
            if (storage.getItem(i).isEmpty() && storage.canPlaceItem(i,rest)) {
                int count=Math.min(Math.min(rest.getMaxStackSize(),storage.getMaxStackSize()),rest.getCount());
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
        updateNeeds(worker,level,data);
        if (!data.contains(CHEST) || !level.dimension().location().toString().equals(data.getString(DIMENSION))) return;
        BlockPos chestPos=BlockPos.of(data.getLong(CHEST));
        if (worker.blockPosition().distSqr(chestPos)>1024 || !level.isLoaded(chestPos)) return;
        BlockEntity be=level.getBlockEntity(chestPos);
        if (!(be instanceof Container storage) || !be.getPersistentData().hasUUID(OWNER)
                || !be.getPersistentData().getUUID(OWNER).equals(worker.getUUID())) return;
        if (worker.blockPosition().distSqr(chestPos)<=36) replenishFood(storage,data);
        if (data.getBoolean(PAUSED)) return;
        if (data.contains(HELD)) {
            ItemStack held=ItemStack.of(data.getCompound(HELD));
            if (held.isEmpty()) { data.remove(HELD); return; }
            if (worker.blockPosition().distSqr(chestPos)>6) {
                worker.getNavigation().moveTo(chestPos.getX()+0.5,chestPos.getY(),chestPos.getZ()+0.5,1.0);
                return;
            }
            ItemStack rest=store(storage,held);
            data.putLong(DELIVERED,data.getLong(DELIVERED)+held.getCount()-rest.getCount());
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
        int reserve=reserveFromCollected(worker,data,acquired,storage);
        acquired.shrink(reserve);
        if (!acquired.isEmpty()) data.put(HELD,acquired.save(new CompoundTag()));
        // Items are transferred to persistent personal food reserve or the work chest.
        data.putLong(COLLECTIONS,data.getLong(COLLECTIONS)+1);
        item.discard();
    }
}
