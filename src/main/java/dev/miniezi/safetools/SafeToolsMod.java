package dev.miniezi.safetools;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class SafeToolsMod implements ModInitializer {
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("modstatus").executes(context -> {
                context.getSource().sendSuccess(
                    () -> Component.literal("Minecraft Safe Tools 1.0.0 (Fabric / Minecraft 26.3)"),
                    false
                );
                return Command.SINGLE_SUCCESS;
            }))
        );
    }
}
