package mcjty.lostcities.varia;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Tools {

    private static Set<String> done = new HashSet<>();

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
        stringbuilder.append(state.getBlock().getRegistryName());
        if (!state.getValues().isEmpty()) {
            stringbuilder.append('[');
            stringbuilder.append(state.getValues().entrySet().stream().map(PROPERTY_MAPPER).collect(Collectors.joining(",")));
            stringbuilder.append(']');
        }

        return stringbuilder.toString();
    }

    public static BlockState stringToState(String s) {
        if (s.contains("[")) {
            BlockStateParser parser = new BlockStateParser(new StringReader(s), false);
            try {
                parser.parse(false);
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
            return parser.getState();
        }

        String converted = BlockStateData.upgradeBlock(s);
        Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(converted));
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
            return biomeRegistry.flatMap(r -> r.getResourceKey(biome).map(ResourceKey::location)).orElse(null);
        } else {
            return biome.getRegistryName();
        }
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
}
