package com.djawad1.darknull.engine;

import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/** Runtime state for one player's isolated DARKNULL-06 test room. */
public final class TrickSession {
    public final UUID playerId;
    public final int trickId;
    public final ServerWorld roomWorld;
    public final ServerWorld returnWorld;
    public final Vec3d returnPosition;
    public final float returnYaw;
    public final float returnPitch;
    public final BlockPos center;

    public Vec3d spawnPosition;
    public float spawnYaw;
    public float spawnPitch;
    public int age;
    public int cooldown;
    public int cycles;
    public int mazeSeed;
    public int shrinkRadius;
    public float lastYaw;
    public boolean enteredInterior;
    public ArmorStandEntity reflection;
    public final Deque<Vec3d> mirrorPositionTrail = new ArrayDeque<>();
    public final Deque<Float> mirrorYawTrail = new ArrayDeque<>();
    public final List<BlockPos> movingStatues = new ArrayList<>();

    public TrickSession(UUID playerId, int trickId, ServerWorld roomWorld,
                        ServerWorld returnWorld, Vec3d returnPosition,
                        float returnYaw, float returnPitch, BlockPos center) {
        this.playerId = playerId;
        this.trickId = trickId;
        this.roomWorld = roomWorld;
        this.returnWorld = returnWorld;
        this.returnPosition = returnPosition;
        this.returnYaw = returnYaw;
        this.returnPitch = returnPitch;
        this.center = center;
        this.spawnPosition = new Vec3d(center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5);
        this.lastYaw = returnYaw;
        this.shrinkRadius = 8;
    }
}
