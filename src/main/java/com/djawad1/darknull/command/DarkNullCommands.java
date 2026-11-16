package com.djawad1.darknull.command;

import com.djawad1.darknull.engine.TrickCatalog;
import com.djawad1.darknull.engine.TrickDefinition;
import com.djawad1.darknull.engine.TrickEngine;
import com.djawad1.darknull.tutorial.TutorialManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class DarkNullCommands {
    private DarkNullCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("darknull")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(argument("id", IntegerArgumentType.integer(1, 10))
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    return TrickEngine.start(player, IntegerArgumentType.getInteger(context, "id")) ? 1 : 0;
                                }))
                        .then(literal("help").executes(context -> {
                            sendHelp(context.getSource());
                            return 1;
                        }))
                        .then(literal("stop").executes(context -> {
                            TrickEngine.stop(context.getSource().getPlayer());
                            return 1;
                        }))
                        .then(literal("reset").executes(context -> {
                            TrickEngine.stop(context.getSource().getPlayer());
                            return 1;
                        }))
                        .then(literal("tutorial").executes(context -> {
                            TutorialManager.show(context.getSource().getPlayer());
                            return 1;
                        }))
                )
        );
    }

    private static void sendHelp(ServerCommandSource source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        source.sendFeedback(() -> Text.literal("DARKNULL-06 // REALITY TRICKS // CREATED BY DJAWAD1")
                .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        for (TrickDefinition trick : TrickCatalog.all()) {
            source.sendFeedback(() -> Text.literal("/darknull " + trick.id() + " - " + trick.name())
                    .formatted(Formatting.GRAY), false);
        }
        source.sendFeedback(() -> Text.literal("/darknull stop | reset | tutorial")
                .formatted(Formatting.AQUA), false);
    }
}
