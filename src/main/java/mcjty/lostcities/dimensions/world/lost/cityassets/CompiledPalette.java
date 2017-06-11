package mcjty.lostcities.dimensions.world.lost.cityassets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * More efficient representation of a palette useful for a single chunk
 */
public class CompiledPalette {

    private final Map<Character, Object> palette = new HashMap<>();
    private final Map<IBlockState, IBlockState> damagedToBlock = new HashMap<>();
    private final Map<Character, String> mobIds = new HashMap<>();

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
                            Object s = palette.get(c);
                            palette.put(entry.getKey(), s);
                            dirty = true;
                        }
                    }
                }
            }
        }

        for (Palette p : palettes) {
            for (Map.Entry<IBlockState, IBlockState> entry : p.getDamaged().entrySet()) {
                IBlockState c = entry.getKey();
                damagedToBlock.put(c, entry.getValue());
            }
            for (Map.Entry<Character, String> entry : p.getMobIds().entrySet()) {
                Character c = entry.getKey();
                mobIds.put(c, entry.getValue());
            }
        }
    }

    public Set<Character> getCharacters() {
        return palette.keySet();
    }

    public IBlockState getStraight(char c) {
        try {
            Object o = palette.get(c);
            if (o instanceof IBlockState) {
                return (IBlockState) o;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public IBlockState get(char c) {
        try {
            Object o = palette.get(c);
            if (o instanceof IBlockState) {
                return (IBlockState) o;
            } else {
                return ((Supplier<IBlockState>) o).get();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isGlass(IBlockState b) {
        Block block = b.getBlock();
        return block == Blocks.GLASS || block == Blocks.GLASS_PANE || block == Blocks.STAINED_GLASS || block == Blocks.STAINED_GLASS_PANE;
    }

    public IBlockState canBeDamagedToIronBars(IBlockState b) {
        return damagedToBlock.get(b);
    }

    public String getMobId(Character c) { return mobIds.get(c); }

    public boolean isEasyToDestroy(IBlockState b) {
        return isGlass(b);
    }

    public boolean isLiquid(IBlockState b) {
        return b != null && (b.getBlock() instanceof BlockLiquid || b.getBlock() instanceof BlockDynamicLiquid);
    }
}
