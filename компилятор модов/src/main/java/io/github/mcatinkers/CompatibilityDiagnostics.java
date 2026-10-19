package io.github.mcatinkers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * Opt-in diagnostic capture. No external connections or automatic uploads.
 * Captures interactions with MCA entities when a Tinkers item is held.
 * Operators can scan nearby MCA equipment and export bounded JSON records.
 */
public final class CompatibilityDiagnostics {
    private static final Logger LOG = LoggerFactory.getLogger("MCA-Tinkers-Diagnostics");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_EVENTS = 1000;
    private static final Deque<JsonObject> EVENTS = new ArrayDeque<>();
    private static boolean recording = false;

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent e) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("mcatinkersdiag")
            .requires(s -> s.hasPermission(2))
            .then(Commands.literal("start").executes(ctx -> {
                recording = true;
                EVENTS.clear();
                reply(ctx.getSource(), "Запись включена. Попробуй дать жителю предмет Tinkers.");
                LOG.info("Diagnostics recording started");
                return 1;
            }))
            .then(Commands.literal("stop").executes(ctx -> {
                recording = false;
                reply(ctx.getSource(), "Запись остановлена. Для сохранения: /mcatinkersdiag export");
                return 1;
            }))
            .then(Commands.literal("status").executes(ctx -> {
                reply(ctx.getSource(), "Запись: " + (recording ? "включена" : "выключена")
                    + "; событий: " + EVENTS.size()
                    + "; MCA: " + ModList.get().isLoaded("mca")
                    + "; TConstruct: " + ModList.get().isLoaded("tconstruct"));
                return 1;
            }))
            .then(Commands.literal("scan").executes(ctx -> scan(ctx.getSource())))
            .then(Commands.literal("export").executes(ctx -> export(ctx.getSource())));
        e.getDispatcher().register(root);
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!recording || !(event.getEntity() instanceof ServerPlayer player)) return;
        Entity target = event.getTarget();
        ItemStack held = event.getItemStack();
        if (!isMca(target) || !isTinkers(held)) return;

        JsonObject record = new JsonObject();
        record.addProperty("event", "player_interacts_with_mca_using_tinkers");
        record.addProperty("time", Instant.now().toString());
        record.addProperty("dimension", player.level().dimension().location().toString());
        record.addProperty("targetType", entityType(target));
        record.addProperty("targetClass", target.getClass().getName());
        record.addProperty("hand", event.getHand().toString());
        record.addProperty("heldItem", itemName(held));
        record.addProperty("itemDamage", held.getDamageValue());
        record.addProperty("itemMaxDamage", held.getMaxDamage());
        record.addProperty("cancelledAtObservation", event.isCanceled());
        remember(record);
    }

    private int scan(CommandSourceStack source) {
        if (source.getEntity() == null) {
            reply(source, "Команда scan требует игрока рядом с жителями.");
            return 0;
        }
        var level = source.getLevel();
        var center = source.getPosition();
        var villagers = level.getEntitiesOfClass(LivingEntity.class,
            new AABB(center, center).inflate(32), CompatibilityDiagnostics::isMca);
        int matches = 0;
        for (LivingEntity resident : villagers) {
            JsonObject record = new JsonObject();
            record.addProperty("event", "mca_equipment_scan");
            record.addProperty("time", Instant.now().toString());
            record.addProperty("dimension", level.dimension().location().toString());
            record.addProperty("entityType", entityType(resident));
            record.addProperty("entityClass", resident.getClass().getName());
            record.addProperty("mainHand", itemName(resident.getMainHandItem()));
            record.addProperty("offHand", itemName(resident.getOffhandItem()));
            JsonArray armor = new JsonArray();
            for (ItemStack item : resident.getArmorSlots()) armor.add(itemName(item));
            record.add("armor", armor);
            remember(record);
            matches++;
        }
        reply(source, "Проверено жителей MCA в радиусе 32 блоков: " + matches
            + ". Записей в буфере: " + EVENTS.size());
        return matches;
    }

    private int export(CommandSourceStack source) {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("formatVersion", 1);
            root.addProperty("generatedAt", Instant.now().toString());
            root.addProperty("minecraft", "1.20.1");
            root.addProperty("mcaLoaded", ModList.get().isLoaded("mca"));
            root.addProperty("tconstructLoaded", ModList.get().isLoaded("tconstruct"));
            root.addProperty("recording", recording);
            JsonArray entries = new JsonArray();
            for (JsonObject record : EVENTS) entries.add(record.deepCopy());
            root.add("events", entries);
            Path directory = FMLPaths.GAMEDIR.get().resolve("logs").resolve("mcatinkers");
            Files.createDirectories(directory);
            String stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                .withZone(java.time.ZoneOffset.UTC).format(Instant.now());
            Path destination = directory.resolve("diagnostics-" + stamp + ".json");
            Files.writeString(destination, GSON.toJson(root), StandardCharsets.UTF_8);
            reply(source, "Отчёт создан: logs/mcatinkers/" + destination.getFileName()
                + " (" + EVENTS.size() + " записей)");
            LOG.info("Diagnostics exported to {}", destination);
            return 1;
        } catch (Exception ex) {
            LOG.error("Could not export diagnostics", ex);
            reply(source, "Ошибка записи отчёта. Подробности в latest.log.");
            return 0;
        }
    }

    private static boolean isMca(Entity entity) {
        String namespace = entityType(entity);
        return namespace.startsWith("mca:") || namespace.startsWith("minecraft_comes_alive:")
            || entity.getClass().getName().toLowerCase(Locale.ROOT).contains(".mca.");
    }

    private static boolean isTinkers(ItemStack stack) {
        String name = itemName(stack);
        return name.startsWith("tconstruct:") || name.startsWith("tinkers_thinking:");
    }

    private static String itemName(ItemStack stack) {
        if (stack.isEmpty()) return "minecraft:air";
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key == null ? "unknown" : key.toString();
    }

    private static String entityType(Entity entity) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key == null ? "unknown" : key.toString();
    }

    private static void remember(JsonObject object) {
        if (EVENTS.size() == MAX_EVENTS) EVENTS.removeFirst();
        EVENTS.addLast(object);
        LOG.info("MCA_TINKERS_DIAG {}", GSON.toJson(object));
    }

    private static void reply(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal("[MCA Tinkers] " + message), false);
    }
}
