package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * More efficient representation of a palette useful for a single chunk
 */
public class CompiledPalette {

    private final Map<Character, Object> palette = new HashMap<>();

    public CompiledPalette(Style style, Palette originalPalette) {
        for (Map.Entry<Character, Object> entry : originalPalette.palette.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                IBlockState state = style.get((String) value);
                if (state != null) {
                    palette.put(entry.getKey(), state);
                } else {
                    palette.put(entry.getKey(), Blocks.AIR.getDefaultState());
                }
            } else {
                if (value == null) {
                    throw new RuntimeException("Invalid palette entry for '" + entry.getKey() + "'!");
                }
                palette.put(entry.getKey(), value);
            }
        }

    }

    public IBlockState get(char c, BuildingInfo info) {
        try {
            Object o = palette.get(c);
            if (o instanceof IBlockState) {
                return (IBlockState) o;
            } else {
                return ((Function<BuildingInfo, IBlockState>) o).apply(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
