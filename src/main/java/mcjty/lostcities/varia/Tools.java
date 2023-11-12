package mcjty.lostcities.varia;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Tools {

    private static final Set<String> DONE = new HashSet<>();

    private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_MAPPER = new Function<Map.Entry<Property<?>, Comparable<?>>, String>() {
        @Override
        public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> entry) {
            if (entry == null) {
                return "<NULL>";
            } else {
                Property<?> property = entry.getKey();
                return property.getName() + "=" + this.getName(property, entry.getValue());
            }
        }

        private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
            return property.getName((T)comparable);
        }
    };

    public static String stateToString(BlockState state) {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(ForgeRegistries.BLOCKS.getKey(state.getBlock()));
        if (!state.getValues().isEmpty()) {
            stringbuilder.append('[');
            stringbuilder.append(state.getValues().entrySet().stream().map(PROPERTY_MAPPER).collect(Collectors.joining(",")));
            stringbuilder.append(']');
        }

        return stringbuilder.toString();
    }

    public static BlockState stringToState(String s) {
        if (s.contains("[")) {
            try {
                BlockStateParser.BlockResult parser = BlockStateParser.parseForBlock(WorldTools.getOverworld().holderLookup(Registries.BLOCK), new StringReader(s), false);
                return parser.blockState();
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
        }

        String converted = BlockStateData.upgradeBlock(s);
        Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(converted));
        if (value == null) {
            throw new RuntimeException("Cannot find block: '" + s + "'!");
        }
        return value.defaultBlockState();
    }

    public static <T> T getRandomFromList(RandomSource random, List<T> list, Function<T, Float> weightGetter) {
        if (list.isEmpty()) {
            return null;
        }
        List<T> elements = new ArrayList<>();
        float totalweight = 0;
        for (T pair : list) {
            elements.add(pair);
            totalweight += weightGetter.apply(pair);
        }
        float r = random.nextFloat() * totalweight;
        for (T pair : elements) {
            r -= weightGetter.apply(pair);
            if (r <= 0) {
                return pair;
            }
        }
        return elements.get(elements.size() - 1);
    }

    public static <T> T getRandomFromList(Random random, List<T> list, Function<T, Float> weightGetter) {
        if (list.isEmpty()) {
            return null;
        }
        List<T> elements = new ArrayList<>();
        float totalweight = 0;
        for (T pair : list) {
            elements.add(pair);
            totalweight += weightGetter.apply(pair);
        }
        float r = random.nextFloat() * totalweight;
        for (T pair : elements) {
            r -= weightGetter.apply(pair);
            if (r <= 0) {
                return pair;
            }
        }
        return null;
    }

    public static Iterable<Holder<Block>> getBlocksForTag(TagKey<Block> rl) {
        @SuppressWarnings("deprecation") DefaultedRegistry<Block> registry = BuiltInRegistries.BLOCK;
        return registry.getTagOrEmpty(rl);
    }

    public static boolean hasTag(Block block, TagKey<Block> tag) {
        //noinspection deprecation
        return BuiltInRegistries.BLOCK.getHolderOrThrow(block.builtInRegistryHolder().key()).is(tag);
    }

    public static int getSeaLevel(LevelReader level) {
        if (level instanceof WorldGenLevel wgLevel) {
            if (wgLevel.getChunkSource() instanceof ServerChunkCache scc) {
                return scc.getGenerator().getSeaLevel();
            }
        }
        //noinspection deprecation
        return level.getSeaLevel();
    }
}
