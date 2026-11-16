package com.djawad1.darknull.tutorial;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Staggered first-join onboarding. Completion is stored in the player's persistent command tags. */
public final class TutorialManager {
    private static final String SEEN_TAG = "darknull06_tutorial_seen";
    private static final Map<UUID, Integer> QUEUED = new HashMap<>();

    private TutorialManager() {
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        if (player.getCommandTags().contains(SEEN_TAG)) return;
        player.addCommandTag(SEEN_TAG);
        QUEUED.put(player.getUuid(), 0);
    }

    public static void show(ServerPlayerEntity player) {
        player.sendMessage(Text.literal(" "), false);
        player.sendMessage(Text.literal("to darknull-06").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        player.sendMessage(Text.literal("CONNECTION ESTABLISHED // SESSION 001").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("DARKNULL-06 // CREATED BY DJAWAD1").formatted(Formatting.RED, Formatting.BOLD), false);
        player.sendMessage(Text.literal("The place is unstable. Do not trust doors, corridors, or what stands behind you.")
                .formatted(Formatting.WHITE), false);
        player.sendMessage(Text.literal("/darknull help  - list the ten reality tricks").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("/darknull 1..10 - enter a numbered illusion room").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("/darknull stop  - leave the room and return safely").formatted(Formatting.GRAY), false);
        player.sendMessage(Text.literal("REMEMBER: THE WORLD IS LYING TO YOU.").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
    }

    public static void tickServer(MinecraftServer server) {
        for (Map.Entry<UUID, Integer> entry : new HashMap<>(QUEUED).entrySet()) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player == null) {
                QUEUED.remove(entry.getKey());
                continue;
            }
            int ticks = entry.getValue() + 1;
            QUEUED.put(entry.getKey(), ticks);
            if (ticks == 1) {
                player.sendMessage(Text.literal("to darknull-06").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
            } else if (ticks == 35) {
                player.sendMessage(Text.literal("CONNECTION ESTABLISHED // SESSION 001").formatted(Formatting.GRAY), false);
            } else if (ticks == 70) {
                player.sendMessage(Text.literal("DARKNULL-06").formatted(Formatting.RED, Formatting.BOLD), false);
            } else if (ticks == 105) {
                player.sendMessage(Text.literal("Reality is unstable. Do not trust what you see.").formatted(Formatting.WHITE), false);
            } else if (ticks == 140) {
                player.sendMessage(Text.literal("Use /darknull help to choose an illusion.").formatted(Formatting.AQUA), false);
            } else if (ticks >= 175) {
                player.sendMessage(Text.literal("REMEMBER: THE WORLD IS LYING TO YOU. // CREATED BY DJAWAD1")
                        .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
                QUEUED.remove(entry.getKey());
            }
        }
    }
}
