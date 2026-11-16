package com.djawad1.darknull.world;

import com.djawad1.darknull.engine.TrickSession;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Builds all illusion geometry inside the mod-owned void dimension. */
public final class TrickWorldBuilder {
    public static final int FLOOR_Y = 80;
    private static final int MIN_X = -18;
    private static final int MAX_X = 30;
    private static final int MIN_Z = -16;
    private static final int MAX_Z = 16;
    private static final int MIN_Y = 76;
    private static final int MAX_Y = 96;

    private TrickWorldBuilder() {
    }

    public static boolean isPlotClear(ServerWorld world, BlockPos center) {
        for (int x = MIN_X; x <= MAX_X; x++) {
            for (int y = MIN_Y; y <= MAX_Y; y++) {
                for (int z = MIN_Z; z <= MAX_Z; z++) {
                    if (!world.getBlockState(center.add(x, y - FLOOR_Y, z)).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void clearPlot(ServerWorld world, BlockPos center) {
        // Remove only the mod-owned mirror mannequin; never touch unrelated entities.
        Box plotBox = new Box(center.getX() + MIN_X, MIN_Y,
                center.getZ() + MIN_Z, center.getX() + MAX_X + 1,
                MAX_Y + 1, center.getZ() + MAX_Z + 1);
        for (ArmorStandEntity stand : world.getEntitiesByClass(ArmorStandEntity.class, plotBox,
                entity -> entity.getCommandTags().contains("darknull06_reflection"))) {
            stand.discard();
        }
        for (int x = MIN_X; x <= MAX_X; x++) {
            for (int y = MIN_Y; y <= MAX_Y; y++) {
                for (int z = MIN_Z; z <= MAX_Z; z++) {
                    BlockPos pos = center.add(x, y - FLOOR_Y, z);
                    if (!world.getBlockState(pos).isAir()) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
                    }
                }
            }
        }
    }

    public static void build(TrickSession session, ServerPlayerEntity player) {
        ServerWorld world = session.roomWorld;
        BlockPos c = session.center;
        switch (session.trickId) {
            case 1 -> buildStaircase(session);
            case 2 -> buildCorridor(session);
            case 3 -> buildLyingDoor(session);
            case 4 -> buildBiggerInside(session);
            case 5 -> buildInfiniteRoom(session);
            case 6 -> buildMirrorRoom(session, player);
            case 7 -> buildWeepingRoom(session);
            case 8 -> buildShrinkingRoom(session);
            case 9 -> buildCeilingRoom(session);
            case 10 -> buildMaze(session, 1);
            default -> throw new IllegalArgumentException("Unknown trick id: " + session.trickId);
        }
    }

    private static void buildStaircase(TrickSession s) {
        ServerWorld w = s.roomWorld;
        BlockPos c = s.center;
        floor(w, c, -4, 4, -9, 10, Blocks.POLISHED_DEEPSLATE);
        walls(w, c, -4, 4, -9, 10, 5, Blocks.DEEPSLATE_BRICKS, false);
        for (int i = 0; i < 9; i++) {
            fill(w, c, 0, 0, -6 + i, -6 + i, i, i, Blocks.STONE_BRICKS);
            set(w, c.add(-1, i, -6 + i), Blocks.COBBLESTONE_WALL);
            set(w, c.add(1, i, -6 + i), Blocks.COBBLESTONE_WALL);
            if (i % 2 == 0) {
                set(w, c.add(-3, 2, -5 + i), Blocks.SOUL_LANTERN);
                set(w, c.add(3, 2, -5 + i), Blocks.SOUL_LANTERN);
            }
        }
        s.spawnPosition = pos(c, 0, 1, -7);
        s.spawnYaw = 0.0F;
    }

    private static void buildCorridor(TrickSession s) {
        ServerWorld w = s.roomWorld;
        BlockPos c = s.center;
        room(w, c, 3, 12, 4, Blocks.GRAY_CONCRETE, Blocks.YELLOW_CONCRETE, true);
        for (int z = -6; z <= 10; z += 4) {
            set(w, c.add(-2, 2, z), Blocks.REDSTONE_LAMP);
            set(w, c.add(2, 2, z), Blocks.REDSTONE_LAMP);
        }
        s.spawnPosition = pos(c, 0, 1, -8);
        s.spawnYaw = 0.0F;
    }

    private static void buildLyingDoor(TrickSession s) {
        ServerWorld w = s.roomWorld;
        BlockPos c = s.center;
        room(w, c, 6, 6, 4, Blocks.SMOOTH_STONE, Blocks.DEEPSLATE_BRICKS, true);
        door(w, c.add(0, 1, 6), Direction.SOUTH);
        set(w, c.add(-3, 1, -1), Blocks.BOOKSHELF);
        set(w, c.add(3, 1, -1), Blocks.BOOKSHELF);
        set(w, c.add(0, 1, 2), Blocks.BLACK_CARPET);
        set(w, c.add(-4, 2, 0), Blocks.SOUL_LANTERN);
        set(w, c.add(4, 2, 0), Blocks.SOUL_LANTERN);
        s.spawnPosition = pos(c, 0, 1, -3);
        s.spawnYaw = 0.0F;
    }

    private static void buildBiggerInside(TrickSession s) {
        ServerWorld w = s.roomWorld;
        BlockPos c = s.center;
        room(w, c, 5, 5, 4, Blocks.POLISHED_ANDESITE, Blocks.STONE_BRICKS, true);
        opening(w, c, 5, 1, 0, 3, Direction.EAST);
        door(w, c.add(5, 1, 0), Direction.EAST);
        BlockPos hall = c.add(18, 0, 0);
        room(w, hall, 8, 13, 7, Blocks.DEEPSLATE_TILES, Blocks.POLISHED_DEEPSLATE, true);
        // A long vanishing-point aisle makes the interior visibly larger than the hut outside.
        for (int z = -10; z <= 10; z += 5) {
            set(w, hall.add(0, 1, z), Blocks.RED_CARPET);
            set(w, hall.add(-6, 2, z), Blocks.SOUL_LANTERN);
            set(w, hall.add(6, 2, z), Blocks.SOUL_LANTERN);
        }
        opening(w, hall, -8, 1, 0, 3, Direction.WEST);
        opening(w, hall, 8, 1, 0, 3, Direction.EAST);
        door(w, hall.add(-8, 1, 0), Direction.WEST);
        door(w, hall.add(8, 1, 0), Direction.EAST);
        s.spawnPosition = pos(c, -3, 1, 0);
        s.spawnYaw = -90.0F;
    }

    private static void buildInfiniteRoom(TrickSession s) {
        ServerWorld w = s.roomWorld;
        BlockPos c = s.center;
        room(w, c, 7, 7, 4, Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_BRICKS, true);
        opening(w, c, 0, 1, -7, 3, Direction.NORTH);
        opening(w, c, 0, 1, 7, 3, Direction.SOUTH);
        opening(w, c, -7, 1, 0, 3, Direction.WEST);
        opening(w, c, 7, 1, 0, 3, Direction.EAST);
        door(w, c.add(0, 1, -7), Direction.NORTH);
        door(w, c.add(0, 1, 7), Direction.SOUTH);
        door(w, c.add(-7, 1, 0), Direction.WEST);
        door(w, c.add(7, 1, 0), Direction.EAST);
        for (int[] d : new int[][]{{0,-5},{0,5},{-5,0},{5,0}}) {
            set(w, c.add(d[0], 1, d[1]), Blocks.RED_CARPET);
            set(w, c.add(d[0], 2, d[1]), Blocks.LANTERN);
        }
        s.spawnPosition = pos(c, 0, 1, 0);
        s.spawnYaw = 0.0F;
    }

    private static void buildMirrorRoom(TrickSession s, ServerPlayerEntity player) {
        ServerWorld w = s.roomWorld;
        BlockPos c = s.center;
        room(w, c, 7, 7, 4, Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS, true);
        fill(w, c, 0, 0, -6, 6, 1, 3, Blocks.TINTED_GLASS);
        for (int z = -5; z <= 5; z++) {
            set(w, c.add(0, 1, z), Blocks.AMETHYST_BLOCK);
        }
        s.spawnPosition = pos(c, -3, 1, 0);
        s.spawnYaw = -90.0F;

        ArmorStandEntity stand = EntityType.ARMOR_STAND.create(w);
        if (stand != null) {
            stand.refreshPositionAndAngles(c.getX() + 3.5, c.getY() + 1.0, c.getZ() + 0.5, 90.0F, 0.0F);
            stand.setShowArms(true);
            stand.setNoGravity(true);
            stand.setInvulnerable(true);
            stand.setSilent(true);
            stand.addCommandTag("darknull06_reflection");
            stand.setCustomName(Text.literal(" "));
            stand.setCustomNameVisible(false);
            copyEquipment(player, stand);
            if (stand.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
                ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
                playerHead.getOrCreateNbt().putString("SkullOwner", player.getGameProfile().getName());
                stand.equipStack(EquipmentSlot.HEAD, playerHead);
            }
            if (stand.getEquippedStack(EquipmentSlot.CHEST).isEmpty()) stand.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            if (stand.getEquippedStack(EquipmentSlot.LEGS).isEmpty()) stand.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            if (stand.getEquippedStack(EquipmentSlot.FEET).isEmpty()) stand.equipStack(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
            w.spawnEntity(stand);
            s.reflection = stand;
        }
    }

    private static void copyEquipment(ServerPlayerEntity player, ArmorStandEntity stand) {
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
        }) {
            ItemStack stack = player.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                stand.equipStack(slot, stack.copy());
            }
        }
    }

    private static void buildWeepingRoom(TrickSession s) {
        ServerWorld w = s.roomWorld;
        BlockPos c = s.center;
        room(w, c, 9, 9, 5, Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_BRICKS, true);
        s.movingStatues.clear();
        for (int[] p : new int[][]{{-7,-7},{7,-7},{-7,7},{7,7}}) {
            BlockPos base = c.add(p[0], 1, p[1]);
            buildStatue(w, base);
            s.movingStatues.add(base);
        }
        s.spawnPosition = pos(c, 0, 1, 0);
        s.spawnYaw = 0.0F;
    }

    public static void buildStatue(ServerWorld world, BlockPos base) {
        set(world, base, Blocks.CHISELED_DEEPSLATE);
        set(world, base.up(), Blocks.POLISHED_BLACKSTONE_BRICKS);
        set(world, base.up(2), Blocks.CRYING_OBSIDIAN);
    }

    public static void moveStatue(ServerWorld world, TrickSession session, int index, BlockPos next) {
        if (index < 0 || index >= session.movingStatues.size()) return;
        BlockPos old = session.movingStatues.get(index);
        if (old.equals(next)) return;
        for (int y = 0; y < 3; y++) {
            set(world, old.up(y), Blocks.AIR);
        }
        buildStatue(world, next);
        session.movingStatues.set(index, next);
    }

    private static void buildShrinkingRoom(TrickSession s) {
        ServerWorld w = s.roomWorld;
        BlockPos c = s.center;
        room(w, c, 8, 8, 5, Blocks.POLISHED_DEEPSLATE, Blocks.DEEPSLATE_BRICKS, true);
        s.shrinkRadius = 8;
        s.spawnPosition = pos(c, 0, 1, 0);
        s.spawnYaw = 0.0F;
    }

    public static void addShrinkingWalls(TrickSession s, int radius) {
        ServerWorld w = s.roomWorld;
        BlockPos c = s.center;
        for (int y = 1; y <= 5; y++) {
            for (int i = -radius; i <= radius; i++) {
                set(w, c.add(-radius, y, i), Blocks.DEEPSLATE_BRICKS);
                set(w, c.add(radius, y, i), Blocks.DEEPSLATE_BRICKS);
                set(w, c.add(i, y, -radius), Blocks.DEEPSLATE_BRICKS);
                set(w, c.add(i, y, radius), Blocks.DEEPSLATE_BRICKS);
            }
        }
        for (int[] p : new int[][]{{-radius,-radius},{radius,-radius},{-radius,radius},{radius,radius}}) {
            set(w, c.add(p[0], 2, p[1]), Blocks.REDSTONE_LAMP);
        }
    }

    private static void buildCeilingRoom(TrickSession s) {
        ServerWorld w = s.roomWorld;
        BlockPos c = s.center;
        // The upper deck is deliberately built as a traversable ceiling-side stage.
        room(w, c, 8, 8, 13, Blocks.POLISHED_BLACKSTONE, Blocks.BLACKSTONE, true);
        // A raised ring walkway leaves the centre open, exposing objects suspended below it.
        fill(w, c, -7, 7, -7, -5, 10, 10, Blocks.POLISHED_BLACKSTONE);
        fill(w, c, -7, 7, 5, 7, 10, 10, Blocks.POLISHED_BLACKSTONE);
        fill(w, c, -7, -5, -4, 4, 10, 10, Blocks.POLISHED_BLACKSTONE);
        fill(w, c, 5, 7, -4, 4, 10, 10, Blocks.POLISHED_BLACKSTONE);
        for (int x = -4; x <= 4; x += 4) {
            for (int z = -4; z <= 4; z += 4) {
                set(w, c.add(x, 9, z), Blocks.CHAIN);
                set(w, c.add(x, 8, z), Blocks.SOUL_LANTERN);
                set(w, c.add(x, 7, z), Blocks.CHAIN);
                set(w, c.add(x, 6, z), Blocks.IRON_BLOCK);
            }
        }
        s.spawnPosition = pos(c, 0, 11, -6);
        s.spawnYaw = 0.0F;
    }

    private static void buildMaze(TrickSession s, int seed) {
        ServerWorld w = s.roomWorld;
        BlockPos c = s.center;
        floor(w, c, -11, 11, -11, 11, Blocks.DEEPSLATE_TILES);
        // Clear the playable volume before generating a new maze arrangement.
        fill(w, c, -11, 11, -10, 10, 1, 4, Blocks.AIR);
        // Outer perimeter.
        for (int y = 1; y <= 4; y++) {
            for (int i = -11; i <= 11; i++) {
                if (i != -8) set(w, c.add(i, y, -11), Blocks.POLISHED_DEEPSLATE);
                if (i != 8) set(w, c.add(i, y, 11), Blocks.POLISHED_DEEPSLATE);
                set(w, c.add(-11, y, i), Blocks.POLISHED_DEEPSLATE);
                set(w, c.add(11, y, i), Blocks.POLISHED_DEEPSLATE);
            }
        }

        final int size = 5;
        boolean[][] visited = new boolean[size][size];
        boolean[][] eastOpen = new boolean[size][size];
        boolean[][] southOpen = new boolean[size][size];
        Random random = new Random(seed * 7919L + s.playerId.hashCode());
        ArrayDeque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{0, 0});
        visited[0][0] = true;
        while (!stack.isEmpty()) {
            int[] cell = stack.peek();
            List<int[]> choices = new ArrayList<>();
            int x = cell[0], z = cell[1];
            if (x + 1 < size && !visited[z][x + 1]) choices.add(new int[]{x + 1, z, 0});
            if (x - 1 >= 0 && !visited[z][x - 1]) choices.add(new int[]{x - 1, z, 1});
            if (z + 1 < size && !visited[z + 1][x]) choices.add(new int[]{x, z + 1, 2});
            if (z - 1 >= 0 && !visited[z - 1][x]) choices.add(new int[]{x, z - 1, 3});
            if (choices.isEmpty()) {
                stack.pop();
                continue;
            }
            int[] next = choices.get(random.nextInt(choices.size()));
            int nx = next[0], nz = next[1];
            switch (next[2]) {
                case 0 -> eastOpen[z][x] = true;
                case 1 -> eastOpen[z][nx] = true;
                case 2 -> southOpen[z][x] = true;
                case 3 -> southOpen[nz][x] = true;
            }
            visited[nz][nx] = true;
            stack.push(new int[]{nx, nz});
        }

        // Cell centres are four blocks apart; closed edges become solid three-block walls.
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int cx = (col - 2) * 4;
                int cz = (row - 2) * 4;
                if (col < size - 1 && !eastOpen[row][col]) {
                    for (int dz = -1; dz <= 1; dz++) wallColumn(w, c.add(cx + 2, 0, cz + dz), 1, 3, Blocks.POLISHED_DEEPSLATE);
                }
                if (row < size - 1 && !southOpen[row][col]) {
                    for (int dx = -1; dx <= 1; dx++) wallColumn(w, c.add(cx + dx, 0, cz + 2), 1, 3, Blocks.POLISHED_DEEPSLATE);
                }
            }
        }
        set(w, c.add(-8, 1, -11), Blocks.RED_CARPET);
        set(w, c.add(8, 1, 11), Blocks.RED_CARPET);
        set(w, c.add(-8, 2, -11), Blocks.SOUL_LANTERN);
        set(w, c.add(8, 2, 11), Blocks.SOUL_LANTERN);
        s.mazeSeed = seed;
        s.spawnPosition = pos(c, -8, 1, -8);
        s.spawnYaw = 0.0F;
    }

    public static void regenerateMaze(TrickSession session) {
        buildMaze(session, session.mazeSeed + 1);
    }

    private static void room(ServerWorld w, BlockPos c, int halfX, int halfZ, int height,
                             Block floor, Block wall, boolean roof) {
        floor(w, c, -halfX, halfX, -halfZ, halfZ, floor);
        walls(w, c, -halfX, halfX, -halfZ, halfZ, height, wall, roof);
    }

    private static void floor(ServerWorld w, BlockPos c, int minX, int maxX, int minZ, int maxZ, Block block) {
        fill(w, c, minX, maxX, minZ, maxZ, 0, 0, block);
    }

    private static void walls(ServerWorld w, BlockPos c, int minX, int maxX, int minZ, int maxZ,
                              int height, Block block, boolean roof) {
        for (int y = 1; y <= height; y++) {
            for (int x = minX; x <= maxX; x++) {
                set(w, c.add(x, y, minZ), block);
                set(w, c.add(x, y, maxZ), block);
            }
            for (int z = minZ; z <= maxZ; z++) {
                set(w, c.add(minX, y, z), block);
                set(w, c.add(maxX, y, z), block);
            }
        }
        if (roof) fill(w, c, minX, maxX, minZ, maxZ, height + 1, height + 1, block);
    }

    private static void opening(ServerWorld w, BlockPos c, int x, int y, int z, int height, Direction direction) {
        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            for (int dy = 0; dy < height; dy++) set(w, c.add(x, y + dy, z), Blocks.AIR);
        } else {
            for (int dy = 0; dy < height; dy++) set(w, c.add(x, y + dy, z), Blocks.AIR);
        }
    }

    private static void door(ServerWorld w, BlockPos pos, Direction facing) {
        // Keep the door visibly ajar so players can pass through the server-side trigger.
        var lower = Blocks.DARK_OAK_DOOR.getDefaultState()
                .with(DoorBlock.FACING, facing)
                .with(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .with(DoorBlock.OPEN, true);
        var upper = lower.with(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        w.setBlockState(pos, lower, Block.NOTIFY_LISTENERS);
        w.setBlockState(pos.up(), upper, Block.NOTIFY_LISTENERS);
    }

    private static void wallColumn(ServerWorld w, BlockPos base, int width, int height, Block block) {
        for (int x = 0; x < width; x++) {
            for (int y = 1; y <= height; y++) set(w, base.add(x, y, 0), block);
        }
    }

    public static void fill(ServerWorld w, BlockPos c, int minX, int maxX, int minZ, int maxZ,
                             int minY, int maxY, Block block) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) set(w, c.add(x, y, z), block);
            }
        }
    }

    public static void set(ServerWorld w, BlockPos pos, Block block) {
        w.setBlockState(pos, block.getDefaultState(), Block.NOTIFY_LISTENERS);
    }

    private static Vec3d pos(BlockPos c, double x, double y, double z) {
        return new Vec3d(c.getX() + x + 0.5, c.getY() + y, c.getZ() + z + 0.5);
    }
}
