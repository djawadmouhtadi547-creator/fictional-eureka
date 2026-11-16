package com.djawad1.darknull;

import com.djawad1.darknull.command.DarkNullCommands;
import com.djawad1.darknull.engine.TrickEngine;
import com.djawad1.darknull.tutorial.TutorialManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DarkNullMod implements ModInitializer {
    public static final String MOD_ID = "darknull06";
    public static final String MOD_NAME = "DARKNULL-06";
    public static final String AUTHOR = "djawad1";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        DarkNullCommands.register();
        ServerTickEvents.END_SERVER_TICK.register(TrickEngine::tickServer);
        ServerTickEvents.END_SERVER_TICK.register(TutorialManager::tickServer);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            TrickEngine.recoverStranded(handler.getPlayer());
            TutorialManager.onPlayerJoin(handler.getPlayer());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                TrickEngine.onDisconnect(handler.getPlayer()));
        ServerLifecycleEvents.SERVER_STOPPING.register(TrickEngine::stopAll);
        LOGGER.info("DARKNULL-06 initialized. Reality layer online. Created by djawad1.");
    }
}
