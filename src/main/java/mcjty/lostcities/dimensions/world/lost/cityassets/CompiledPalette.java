package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
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
    private final Map<Character, IBlockState> damagedToBlock = new HashMap<>();

    public CompiledPalette(Palette... palettes) {
        // First add the straight palette entries
        for (Palette p : palettes) {
            for (Map.Entry<Character, Object> entry : p.palette.entrySet()) {
                Object value = entry.getValue();
                if (!(value instanceof String)) {
                    if (value == null) {
                        throw new RuntimeException("Invalid palette entry for '" + entry.getKey() + "'!");
                    }
                    palette.put(entry.getKey(), value);
                }
            }
        }

        boolean dirty = true;
        while (dirty) {
            dirty = false;

            // Now add the palette entries that refer to other palette entries
            for (Palette p : palettes) {
                for (Map.Entry<Character, Object> entry : p.palette.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        char c = ((String) value).charAt(0);
                        if (palette.containsKey(c) && !palette.containsKey(entry.getKey())) {
                            palette.put(entry.getKey(), palette.get(c));
                            dirty = true;
                        }
                    }
                }
            }
        }

        for (Palette p : palettes) {
            for (Map.Entry<Character, IBlockState> entry : p.getDamaged().entrySet()) {
                Character c = entry.getKey();
                damagedToBlock.put(c, entry.getValue());
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

    public boolean isGlass(IBlockState b) {
        return b.getBlock() == Blocks.GLASS || b.getBlock() == Blocks.GLASS_PANE;
    }

    public IBlockState canBeDamagedToIronBars(IBlockState b) {
        return damagedToBlock.get(b);
    }

    public boolean isEasyToDestroy(IBlockState b) {
        return isGlass(b);
    }

    public boolean isLiquid(IBlockState b) {
        return b != null && (b.getBlock() instanceof BlockLiquid || b.getBlock() instanceof BlockDynamicLiquid);
    }
}
