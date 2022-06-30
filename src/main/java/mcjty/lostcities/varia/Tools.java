package mcjty.lostcities.varia;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Tools {

    public static BlockState stringToState(String s) {
        if ("minecraft:double_stone_slab".equals(s)) {
            return Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);
        }
        int meta = 0;
        if (s.contains("[")) {
            BlockStateParser parser = new BlockStateParser(new StringReader(s), false);
            try {
                parser.parse(false);
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
            return parser.getState();
        }
        if (s.contains("@")) {
            // Temporary fix to just remove the meta to get things rolling
            String[] split = s.split("@");
            meta = Integer.parseInt(split[1]);
            s = split[0];
        }

        if (s.equals("minecraft:stone_brick_stairs")) {
            return getStairsState(meta, Blocks.STONE_BRICK_STAIRS.defaultBlockState());
        } else if (s.equals("minecraft:quartz_stairs")) {
            return getStairsState(meta, Blocks.QUARTZ_STAIRS.defaultBlockState());
        } else if (s.equals("minecraft:stone_stairs")) {
            return getStairsState(meta, Blocks.STONE_STAIRS.defaultBlockState());
        } else if (s.equals("minecraft:rail")) {
            return getRailState(meta, Blocks.RAIL.defaultBlockState());
        } else if (s.equals("minecraft:golden_rail")) {
            return getPoweredRailState(meta, Blocks.POWERED_RAIL.defaultBlockState());
        } else if (s.equals("minecraft:stone_slab")) {
            return getStoneSlabState(meta, Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        } else if (s.equals("minecraft:redstone_torch")) {
            return getRedstoneTorchState(meta);
        } else if (s.equals("minecraft:ladder")) {
            return getLadderState(meta);
        }

        String converted = ItemStackTheFlatteningFix.updateItem(s, meta);
        if (converted != null) {
            s = converted;
        } else {
            converted = BlockStateData.upgradeBlock(s);
            if (converted != null) {
                s = converted;
            }
        }
        Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(s));
//        System.out.println("original = " + original + ", value = " + value);
        if (value == null) {
            throw new RuntimeException("Cannot find block: '" + s + "'!");
        }
        return value.defaultBlockState();
    }

    @Nullable
    public static ResourceLocation getBiomeId(Biome biome) {
        // @todo use IWorld.registryAccess()
        if (biome.getRegistryName() == null) {
            Optional<? extends Registry<Biome>> biomeRegistry = RegistryAccess.builtinCopy().registry(Registry.BIOME_REGISTRY);
            return biomeRegistry.map(r -> r.getResourceKey(biome).map(ResourceKey::location).orElse(null)).orElse(null);
        } else {
            return biome.getRegistryName();
        }
    }

    private static BlockState getLadderState(int meta) {
        Direction direction = Direction.values()[Mth.abs(meta % Direction.values().length)];
        if (direction.getAxis() == Direction.Axis.Y) {
            direction = Direction.NORTH;
        }
        return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, direction);
    }

    private static BlockState getRedstoneTorchState(int meta) {
        return switch (meta) {
            case 1 -> Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.EAST);
            case 2 -> Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.WEST);
            case 3 -> Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.SOUTH);
            case 4 -> Blocks.REDSTONE_WALL_TORCH.defaultBlockState().setValue(RedstoneWallTorchBlock.FACING, Direction.NORTH);
            case 5 -> Blocks.REDSTONE_TORCH.defaultBlockState();
            default -> Blocks.REDSTONE_TORCH.defaultBlockState();
        };

    }

    private static BlockState getStoneSlabState(int meta, BlockState state) {
        state.setValue(SlabBlock.TYPE, (meta & 8) > 0 ? SlabType.TOP : SlabType.BOTTOM);
        return state;
    }

    private static BlockState getRailState(int meta, BlockState state) {
        return state.setValue(RailBlock.SHAPE, getRailShape(meta, false));
    }

    private static BlockState getPoweredRailState(int meta, BlockState state) {
        return state.setValue(PoweredRailBlock.SHAPE, getRailShape(meta, true))
                .setValue(PoweredRailBlock.POWERED, (meta & 8) > 0);
    }

    private static BlockState getStairsState(int meta, BlockState state) {
        return state
                .setValue(StairBlock.FACING, getStairsDirection(meta))
                .setValue(StairBlock.HALF, getStairsHalf(meta));
    }

    private static Direction getStairsDirection(int meta) {
        int index = 5 - (meta & 3);
        return Direction.values()[Mth.abs(index % Direction.values().length)];
    }

    private static Half getStairsHalf(int meta) {
        return (meta & 4) > 0 ? Half.TOP : Half.BOTTOM;
    }

    private static RailShape getRailShape(int meta, boolean powered) {
        if (powered) {
            meta = meta & 7;
        }
        return switch (meta) {
            case 0 -> RailShape.NORTH_SOUTH;
            case 1 -> RailShape.EAST_WEST;
            case 2 -> RailShape.ASCENDING_EAST;
            case 3 -> RailShape.ASCENDING_WEST;
            case 4 -> RailShape.ASCENDING_NORTH;
            case 5 -> RailShape.ASCENDING_SOUTH;
            case 6 -> RailShape.SOUTH_EAST;
            case 7 -> RailShape.SOUTH_WEST;
            case 8 -> RailShape.NORTH_WEST;
            case 9 -> RailShape.NORTH_EAST;
            default -> RailShape.NORTH_SOUTH;
        };
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
