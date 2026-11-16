package com.djawad1.darknull.engine;

import com.djawad1.darknull.DarkNullMod;
import com.djawad1.darknull.world.TrickWorldBuilder;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Server-authoritative trick runtime. Every trick is built in the mod's private void
 * dimension so the player's actual survival/adventure builds are never overwritten.
 */
public final class TrickEngine {
    public static final RegistryKey<World> DARKNULL_WORLD = RegistryKey.of(
            RegistryKeys.WORLD, new Identifier(DarkNullMod.MOD_ID, "darknull_void"));
    private static final Map<UUID, TrickSession> ACTIVE = new HashMap<>();

    private TrickEngine() {
    }

    public static boolean start(ServerPlayerEntity player, int trickId) {
        if (TrickCatalog.byId(trickId).isEmpty()) return false;
        stop(player, false);
        recoverStranded(player);

        MinecraftServer server = player.getServer();
        if (server == null) return false;
        ServerWorld pocket = server.getWorld(DARKNULL_WORLD);
        if (pocket == null) {
            player.sendMessage(Text.literal("[DARKNULL-06] The private dimension is missing. Restart the world after installing the mod.")
                    .formatted(Formatting.RED), false);
            return false;
        }

        BlockPos center = findPlot(pocket, player.getUuid());
        if (center == null) {
            player.sendMessage(Text.literal("[DARKNULL-06] No free room plot could be allocated. Try again later.")
                    .formatted(Formatting.RED), false);
            return false;
        }

        TrickSession session = new TrickSession(
                player.getUuid(), trickId, pocket, player.getServerWorld(), player.getPos(),
                player.getYaw(), player.getPitch(), center);
        try {
            TrickWorldBuilder.build(session, player);
            session.lastYaw = session.spawnYaw;
            rememberPlot(player, center);
            ACTIVE.put(player.getUuid(), session);
            player.stopRiding();
            player.teleport(pocket, session.spawnPosition.x, session.spawnPosition.y,
                    session.spawnPosition.z, Set.of(), session.spawnYaw, session.spawnPitch);
            player.setVelocity(Vec3d.ZERO);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 12, 0,
                    false, false, false));
            pocket.playSound(null, center, SoundEvents.BLOCK_PORTAL_TRAVEL,
                    SoundCategory.AMBIENT, 0.65F, 0.75F);

            TrickDefinition definition = TrickCatalog.byId(trickId).orElseThrow();
            player.sendMessage(Text.literal("[DARKNULL-06] " + definition.name() + " // SESSION STARTED")
                    .formatted(Formatting.DARK_RED, Formatting.BOLD), false);
            player.sendMessage(Text.literal(definition.description()).formatted(Formatting.GRAY), false);
            player.sendMessage(Text.literal("Use /darknull stop to leave this reality layer.")
                    .formatted(Formatting.DARK_GRAY), false);
            DarkNullMod.LOGGER.info("Started trick {} for {}", trickId, player.getGameProfile().getName());
            return true;
        } catch (RuntimeException exception) {
            ACTIVE.remove(player.getUuid());
            forgetPlot(player, center);
            if (session.reflection != null && !session.reflection.isRemoved()) session.reflection.discard();
            if (player.getServerWorld() == pocket) {
                player.teleport(session.returnWorld, session.returnPosition.x, session.returnPosition.y,
                        session.returnPosition.z, Set.of(), session.returnYaw, session.returnPitch);
            }
            TrickWorldBuilder.clearPlot(pocket, center);
            DarkNullMod.LOGGER.error("Could not build DARKNULL-06 trick {}", trickId, exception);
            player.sendMessage(Text.literal("[DARKNULL-06] Room construction failed. Check the server log.")
                    .formatted(Formatting.RED), false);
            return false;
        }
    }

    private static BlockPos findPlot(ServerWorld world, UUID uuid) {
        long a = uuid.getMostSignificantBits();
        long b = uuid.getLeastSignificantBits();
        int baseX = Math.floorMod((int) (a ^ (a >>> 32)), 100_000) * 64 + 32;
        int baseZ = Math.floorMod((int) (b ^ (b >>> 32)), 100_000) * 64 + 32;
        for (int attempt = 0; attempt < 32; attempt++) {
            BlockPos center = new BlockPos(baseX + attempt * 64, TrickWorldBuilder.FLOOR_Y, baseZ);
            if (TrickWorldBuilder.isPlotClear(world, center)) return center;
        }
        return null;
    }

    public static void stop(ServerPlayerEntity player) {
        stop(player, true);
    }

    private static void stop(ServerPlayerEntity player, boolean message) {
        TrickSession session = ACTIVE.remove(player.getUuid());
        if (session == null) {
            recoverStranded(player);
            if (message) {
                player.sendMessage(Text.literal("[DARKNULL-06] No active trick. Your session is clear.")
                        .formatted(Formatting.GRAY), false);
            }
            return;
        }
        cleanup(session);
        forgetPlot(player, session.center);
        if (player.getServerWorld() == session.roomWorld) {
            player.teleport(session.returnWorld, session.returnPosition.x, session.returnPosition.y,
                    session.returnPosition.z, Set.of(), session.returnYaw, session.returnPitch);
            player.setVelocity(Vec3d.ZERO);
        }
        if (message) {
            player.sendMessage(Text.literal("[DARKNULL-06] Session terminated. Original world restored.")
                    .formatted(Formatting.AQUA), false);
        }
        DarkNullMod.LOGGER.info("Stopped trick {} for {}", session.trickId, player.getGameProfile().getName());
    }

    public static void onDisconnect(ServerPlayerEntity player) {
        TrickSession session = ACTIVE.remove(player.getUuid());
        if (session != null) {
            cleanup(session);
            forgetPlot(player, session.center);
        }
    }

    public static void recoverStranded(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerWorld pocket = server.getWorld(DARKNULL_WORLD);
        if (pocket != null) {
            // A saved player tag makes an interrupted session recoverable after a server crash.
            for (String tag : player.getCommandTags().toArray(new String[0])) {
                if (!tag.startsWith("darknull06_plot_")) continue;
                String[] coordinates = tag.substring("darknull06_plot_".length()).split("_");
                if (coordinates.length == 2) {
                    try {
                        BlockPos oldCenter = new BlockPos(Integer.parseInt(coordinates[0]),
                                TrickWorldBuilder.FLOOR_Y, Integer.parseInt(coordinates[1]));
                        TrickWorldBuilder.clearPlot(pocket, oldCenter);
                    } catch (NumberFormatException ignored) {
                        DarkNullMod.LOGGER.warn("Ignoring malformed DARKNULL plot recovery tag: {}", tag);
                    }
                }
                player.removeScoreboardTag(tag);
            }
        }
        if (player.getServerWorld().getRegistryKey().equals(DARKNULL_WORLD)) {
            ServerWorld overworld = server.getOverworld();
            BlockPos spawn = overworld.getSpawnPos();
            player.teleport(overworld, spawn.getX() + 0.5, spawn.getY() + 1.0,
                    spawn.getZ() + 0.5, Set.of(), player.getYaw(), player.getPitch());
            player.sendMessage(Text.literal("[DARKNULL-06] Recovered an interrupted reality session.")
                    .formatted(Formatting.YELLOW), false);
        }
    }

    private static void rememberPlot(ServerPlayerEntity player, BlockPos center) {
        player.addCommandTag("darknull06_plot_" + center.getX() + "_" + center.getZ());
    }

    private static void forgetPlot(ServerPlayerEntity player, BlockPos center) {
        player.removeScoreboardTag("darknull06_plot_" + center.getX() + "_" + center.getZ());
    }

    public static void stopAll(MinecraftServer server) {
        for (TrickSession session : ACTIVE.values().toArray(new TrickSession[0])) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(session.playerId);
            cleanup(session);
            if (player != null) forgetPlot(player, session.center);
            if (player != null && player.getServerWorld() == session.roomWorld) {
                player.teleport(session.returnWorld, session.returnPosition.x, session.returnPosition.y,
                        session.returnPosition.z, Set.of(), session.returnYaw, session.returnPitch);
            }
        }
        ACTIVE.clear();
    }

    private static void cleanup(TrickSession session) {
        if (session.reflection != null && !session.reflection.isRemoved()) {
            session.reflection.discard();
        }
        TrickWorldBuilder.clearPlot(session.roomWorld, session.center);
    }

    public static void tickServer(MinecraftServer server) {
        for (TrickSession session : ACTIVE.values().toArray(new TrickSession[0])) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(session.playerId);
            if (player == null) {
                ACTIVE.remove(session.playerId);
                cleanup(session);
                continue;
            }
            if (player.getServerWorld() != session.roomWorld) {
                stop(player, true);
                continue;
            }
            tickPlayer(player, session);
        }
    }

    private static void tickPlayer(ServerPlayerEntity player, TrickSession s) {
        s.age++;
        if (s.cooldown > 0) s.cooldown--;
        double dx = player.getX() - s.center.getX() - 0.5;
        double dz = player.getZ() - s.center.getZ() - 0.5;
        float yawDelta = Math.abs(MathHelper.wrapDegrees(player.getYaw() - s.lastYaw));
        s.lastYaw = player.getYaw();

        switch (s.trickId) {
            case 1 -> {
                if (player.getY() >= s.center.getY() + 8.0 && s.cooldown == 0) {
                    loopToSpawn(player, s, "You reached the top. Why are you back at the bottom?");
                }
            }
            case 2 -> {
                if (player.getZ() >= s.center.getZ() + 10.5 && s.cooldown == 0) {
                    loopToSpawn(player, s, "END OF CORRIDOR // START OF CORRIDOR");
                    // A small light pattern change makes successive passes visibly different.
                    BlockPos lamp = s.center.add(2, 2, -2 + (s.cycles % 4) * 3);
                    TrickWorldBuilder.set(s.roomWorld, lamp, (s.cycles % 2 == 0)
                            ? net.minecraft.block.Blocks.REDSTONE_LAMP : net.minecraft.block.Blocks.SOUL_LANTERN);
                }
            }
            case 3 -> {
                if (player.getZ() >= s.center.getZ() + 5.2 && s.cooldown == 0) {
                    s.cycles++;
                    BlockPos decor = s.center.add((s.cycles % 2 == 0) ? -3 : 3, 1, -1);
                    TrickWorldBuilder.set(s.roomWorld, decor,
                            (s.cycles % 3 == 0) ? net.minecraft.block.Blocks.CRYING_OBSIDIAN
                                    : (s.cycles % 2 == 0 ? net.minecraft.block.Blocks.BOOKSHELF : net.minecraft.block.Blocks.AMETHYST_BLOCK));
                    loopToSpawn(player, s, "The same room. One detail is different.");
                }
            }
            case 4 -> tickBiggerInside(player, s);
            case 5 -> {
                if ((Math.abs(dx) > 5.6 || Math.abs(dz) > 5.6) && s.cooldown == 0) {
                    int variant = (s.cycles + 1) % 4;
                    TrickWorldBuilder.set(s.roomWorld, s.center.add(-5 + variant * 3, 1, 0),
                            variant == 0 ? net.minecraft.block.Blocks.RED_CARPET
                                    : variant == 1 ? net.minecraft.block.Blocks.BLACK_CARPET
                                    : variant == 2 ? net.minecraft.block.Blocks.BLUE_CARPET
                                    : net.minecraft.block.Blocks.PURPLE_CARPET);
                    loopToSpawn(player, s, "Door 1. Door 2. Door 3. Door 4. Again.");
                }
            }
            case 6 -> tickMirror(player, s);
            case 7 -> tickWeepingBlocks(player, s);
            case 8 -> tickShrinkingRoom(player, s);
            case 9 -> {
                if (s.age == 30) cue(player, s, "UP IS DOWN. KEEP WALKING.");
                if (s.age == 140) cue(player, s, "THE FLOOR IS ABOVE YOU.");
                // The room is a ceiling-side traversal illusion; it does not claim to rewrite vanilla gravity physics.
            }
            case 10 -> {
                if (yawDelta >= 55.0F && s.age > 25 && s.cooldown == 0) {
                    TrickWorldBuilder.regenerateMaze(s);
                    s.cycles++;
                    s.cooldown = 45;
                    cue(player, s, "THE CORNER CHANGED THE MAZE.");
                    s.lastYaw = player.getYaw();
                }
                if (player.getZ() >= s.center.getZ() + 10.0 && player.getX() >= s.center.getX() + 6.5) {
                    cue(player, s, "EXIT FOUND. OR IS IT?");
                    player.requestTeleport(s.spawnPosition.x, s.spawnPosition.y, s.spawnPosition.z);
                    s.cooldown = 50;
                }
            }
            default -> DarkNullMod.LOGGER.warn("Unknown trick id {}", s.trickId);
        }
    }

    private static void tickBiggerInside(ServerPlayerEntity player, TrickSession s) {
        if (!s.enteredInterior && player.getX() >= s.center.getX() + 4.2 && s.cooldown == 0) {
            s.enteredInterior = true;
            s.cooldown = 35;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 6, 0, false, false, false));
            player.requestTeleport(s.center.getX() + 11.5, s.center.getY() + 1.0,
                    s.center.getZ() + 0.5);
            player.setVelocity(Vec3d.ZERO);
            cue(player, s, "INTERIOR DIMENSIONS DO NOT MATCH EXTERIOR.");
        } else if (s.enteredInterior && player.getX() >= s.center.getX() + 26.0 && s.cooldown == 0) {
            s.enteredInterior = false;
            s.cooldown = 35;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 6, 0, false, false, false));
            player.requestTeleport(s.center.getX() - 3.0, s.center.getY() + 1.0,
                    s.center.getZ() + 0.5);
            player.setVelocity(Vec3d.ZERO);
            cue(player, s, "YOU ARE OUTSIDE. THE BUILDING IS STILL TOO SMALL.");
        } else if (s.enteredInterior && player.getX() <= s.center.getX() + 10.5 && s.cooldown == 0) {
            s.enteredInterior = false;
            s.cooldown = 35;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 6, 0, false, false, false));
            player.requestTeleport(s.center.getX() + 3.0, s.center.getY() + 1.0,
                    s.center.getZ() + 0.5);
            player.setYaw(90.0F);
            player.setHeadYaw(90.0F);
            player.setVelocity(Vec3d.ZERO);
            cue(player, s, "THE SMALL ROOM IS BEHIND YOU.");
        }
    }

    private static void tickMirror(ServerPlayerEntity player, TrickSession s) {
        if (s.reflection != null && !s.reflection.isRemoved()) {
            if (s.age < 170) {
                s.mirrorPositionTrail.addLast(player.getPos());
                s.mirrorYawTrail.addLast(player.getYaw());
                while (s.mirrorPositionTrail.size() > 8) s.mirrorPositionTrail.removeFirst();
                while (s.mirrorYawTrail.size() > 8) s.mirrorYawTrail.removeFirst();
                if (s.mirrorPositionTrail.size() == 8 && s.mirrorYawTrail.size() == 8) {
                    Vec3d delayedPosition = s.mirrorPositionTrail.peekFirst();
                    float delayedYaw = s.mirrorYawTrail.peekFirst();
                    double mirrorX = (2.0 * s.center.getX() + 1.0) - delayedPosition.x;
                    float mirrorYaw = MathHelper.wrapDegrees(180.0F - delayedYaw);
                    s.reflection.refreshPositionAndAngles(mirrorX, delayedPosition.y, delayedPosition.z,
                            mirrorYaw, player.getPitch());
                    s.reflection.setHeadYaw(mirrorYaw);
                }
            } else if (s.age == 170) {
                float towardPlayer = (float) (MathHelper.atan2(player.getZ() - s.reflection.getZ(),
                        player.getX() - s.reflection.getX()) * (180.0 / Math.PI)) - 90.0F;
                s.reflection.setYaw(towardPlayer);
                s.reflection.setHeadYaw(towardPlayer);
                cue(player, s, "YOUR REFLECTION STOPPED COPYING YOU.");
            }
        }
        if (s.age == 100) cue(player, s, "Your reflection is a little late.");
    }

    private static void tickWeepingBlocks(ServerPlayerEntity player, TrickSession s) {
        if (s.age % 28 != 0 || s.cooldown > 0) return;
        Vec3d eye = player.getPos().add(0.0, 1.62, 0.0);
        Vec3d look = player.getRotationVec(1.0F);
        for (int i = 0; i < s.movingStatues.size(); i++) {
            BlockPos statue = s.movingStatues.get(i);
            Vec3d target = Vec3d.ofCenter(statue).subtract(eye);
            double distance = target.length();
            if (distance < 2.2 || distance > 16.0) continue;
            double facing = look.dotProduct(target.normalize());
            if (facing < 0.28) {
                int sx = 0;
                int sz = 0;
                if (Math.abs(player.getX() - statue.getX()) > Math.abs(player.getZ() - statue.getZ())) {
                    sx = player.getX() > statue.getX() ? 1 : -1;
                } else {
                    sz = player.getZ() > statue.getZ() ? 1 : -1;
                }
                BlockPos next = statue.add(sx, 0, sz);
                if (Math.abs(next.getX() - player.getX()) > 1.8 || Math.abs(next.getZ() - player.getZ()) > 1.8) {
                    if (s.roomWorld.getBlockState(next).isAir()
                            && s.roomWorld.getBlockState(next.up()).isAir()
                            && s.roomWorld.getBlockState(next.up(2)).isAir()) {
                        TrickWorldBuilder.moveStatue(s.roomWorld, s, i, next);
                        cue(player, s, "DON'T TURN AROUND.");
                        s.cooldown = 18;
                        return;
                    }
                }
            }
        }
    }

    private static void tickShrinkingRoom(ServerPlayerEntity player, TrickSession s) {
        if (s.age % 65 != 0 || s.shrinkRadius <= 3) return;
        s.shrinkRadius--;
        TrickWorldBuilder.addShrinkingWalls(s, s.shrinkRadius);
        double dx = Math.abs(player.getX() - s.center.getX() - 0.5);
        double dz = Math.abs(player.getZ() - s.center.getZ() - 0.5);
        if (dx >= s.shrinkRadius - 1.2 || dz >= s.shrinkRadius - 1.2) {
            player.requestTeleport(s.center.getX() + 0.5, s.center.getY() + 1.0, s.center.getZ() + 0.5);
            player.setVelocity(Vec3d.ZERO);
        }
        cue(player, s, "THE ROOM IS GETTING SMALLER.");
        if (s.shrinkRadius == 3) cue(player, s, "NO MORE SPACE. SESSION WILL NOT CRUSH YOU.");
    }

    private static void loopToSpawn(ServerPlayerEntity player, TrickSession s, String message) {
        s.cycles++;
        s.cooldown = 45;
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 6, 0, false, false, false));
        s.roomWorld.spawnParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0,
                player.getZ(), 28, 0.35, 0.7, 0.35, 0.04);
        s.roomWorld.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRAVEL,
                SoundCategory.AMBIENT, 0.65F, 0.75F + player.getRandom().nextFloat() * 0.2F);
        player.requestTeleport(s.spawnPosition.x, s.spawnPosition.y, s.spawnPosition.z);
        player.setVelocity(Vec3d.ZERO);
        cue(player, s, message);
    }

    private static void cue(ServerPlayerEntity player, TrickSession s, String message) {
        s.roomWorld.spawnParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0,
                player.getZ(), 10, 0.3, 0.4, 0.3, 0.02);
        player.sendMessage(Text.literal("[DARKNULL-06] " + message)
                .formatted(Formatting.DARK_RED, Formatting.ITALIC), false);
    }
}
