package mcjty.lostcities.varia;

import net.minecraft.block.*;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.RailShape;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.BlockStateFlatteningMap;
import net.minecraft.util.datafix.fixes.ItemStackDataFlattening;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tools {




    public static BlockState stringToState(String s) {
        if ("minecraft:double_stone_slab".equals(s)) {
            return Blocks.SMOOTH_STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.DOUBLE);
        }
        String original = s;
        int meta = 0;
        if (s.contains("@")) {
            // Temporary fix to just remove the meta to get things rolling
            String[] split = s.split("@");
            meta = Integer.parseInt(split[1]);
            s = split[0];
        }

        if (s.equals("minecraft:stone_brick_stairs")) {
            return getStairsState(meta, Blocks.STONE_BRICK_STAIRS.getDefaultState());
        } else if (s.equals("minecraft:quartz_stairs")) {
            return getStairsState(meta, Blocks.QUARTZ_STAIRS.getDefaultState());
        } else if (s.equals("minecraft:stone_stairs")) {
            return getStairsState(meta, Blocks.STONE_STAIRS.getDefaultState());
        } else if (s.equals("minecraft:rail")) {
            return getRailState(meta, Blocks.RAIL.getDefaultState());
        } else if (s.equals("minecraft:golden_rail")) {
            return getPoweredRailState(meta, Blocks.POWERED_RAIL.getDefaultState());
        } else if (s.equals("minecraft:stone_slab")) {
            return getStoneSlabState(meta, Blocks.SMOOTH_STONE_SLAB.getDefaultState());
        } else if (s.equals("minecraft:redstone_torch")) {
            return getRedstoneTorchState(meta);
        }

        String converted = ItemStackDataFlattening.updateItem(s, meta);
        if (converted != null) {
            s = converted;
        } else {
            converted = BlockStateFlatteningMap.updateName(s);
            if (converted != null) {
                s = converted;
            }
        }
        Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(s));
        System.out.println("original = " + original + ", value = " + value);
        if (value == null) {
            throw new RuntimeException("Cannot find block: '" + s + "'!");
        }
        return value.getDefaultState();
    }

    private static BlockState getRedstoneTorchState(int meta) {
        switch (meta) {
            case 1: return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.EAST);
            case 2: return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.WEST);
            case 3: return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.SOUTH);
            case 4: return Blocks.REDSTONE_WALL_TORCH.getDefaultState().with(RedstoneWallTorchBlock.FACING, Direction.NORTH);
            case 5: return Blocks.REDSTONE_TORCH.getDefaultState();
        }

        return Blocks.REDSTONE_TORCH.getDefaultState();
    }

    private static BlockState getStoneSlabState(int meta, BlockState state) {
        state.with (SlabBlock.TYPE, (meta & 8) > 0 ? SlabType.TOP : SlabType.BOTTOM);
        return state;
    }

    private static BlockState getRailState(int meta, BlockState state) {
        return state.with(RailBlock.SHAPE, getRailShape(meta, false));
    }

    private static BlockState getPoweredRailState(int meta, BlockState state) {
        return state.with(PoweredRailBlock.SHAPE, getRailShape(meta, true))
                .with(PoweredRailBlock.POWERED, (meta & 8) > 0);
    }

    private static BlockState getStairsState(int meta, BlockState state) {
        return state
                .with(StairsBlock.FACING, getStairsDirection(meta))
                .with(StairsBlock.HALF, getStairsHalf(meta));
    }

    private static Direction getStairsDirection(int meta) {
        int index = 5 - (meta & 3);
        return Direction.values()[MathHelper.abs(index % Direction.values().length)];
    }

    private static Half getStairsHalf(int meta) {
        return (meta & 4) > 0 ? Half.TOP : Half.BOTTOM;
    }

    private static RailShape getRailShape(int meta, boolean powered) {
        if (powered) {
            meta = meta & 7;
        }
        switch (meta) {
            case 0: return RailShape.NORTH_SOUTH;
            case 1: return RailShape.EAST_WEST;
            case 2: return RailShape.ASCENDING_EAST;
            case 3: return RailShape.ASCENDING_WEST;
            case 4: return RailShape.ASCENDING_NORTH;
            case 5: return RailShape.ASCENDING_SOUTH;
            case 6: return RailShape.SOUTH_EAST;
            case 7: return RailShape.SOUTH_WEST;
            case 8: return RailShape.NORTH_WEST;
            case 9: return RailShape.NORTH_EAST;
        }
        return RailShape.NORTH_SOUTH;
    }

    public static String stateToString(BlockState state) {
        // @todo 1.14
        return state.getBlock().getRegistryName().toString();
    }

    public static String getRandomFromList(Random random, List<Pair<Float, String>> list) {
        if (list.isEmpty()) {
            return null;
        }
        List<Pair<Float, String>> elements = new ArrayList<>();
        float totalweight = 0;
        for (Pair<Float, String> pair : list) {
            elements.add(pair);
            totalweight += pair.getKey();
        }
        float r = random.nextFloat() * totalweight;
        for (Pair<Float, String> pair : elements) {
            r -= pair.getKey();
            if (r <= 0) {
                return pair.getRight();
            }
        }
        return null;
    }
}
