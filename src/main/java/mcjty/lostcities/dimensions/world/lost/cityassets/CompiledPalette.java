package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.LostCitiesTerrainGenerator;
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
    private final Map<Character, Character> damagedToBlock = new HashMap<>();
    private final Map<Character, String> mobIds = new HashMap<>();

    public CompiledPalette(Palette... palettes) {
        // First add the straight palette entries
        for (Palette p : palettes) {
            for (Map.Entry<Character, Object> entry : p.palette.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof IBlockState) {
                    palette.put(entry.getKey(), (char) Block.BLOCK_STATE_IDS.get((IBlockState) value));
                } else if (!(value instanceof String)) {
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
                            if (s instanceof IBlockState) {
                                s = (char) Block.BLOCK_STATE_IDS.get((IBlockState) value);
                            }
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
                damagedToBlock.put((char) Block.BLOCK_STATE_IDS.get(c), (char) Block.BLOCK_STATE_IDS.get(entry.getValue()));
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
            } else if (o instanceof Character) {
                return Block.BLOCK_STATE_IDS.getByValue((Character) o);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Character get(char c) {
        try {
            Object o = palette.get(c);
            if (o instanceof Character) {
                return (Character) o;
            } else if (o instanceof IBlockState) {
                //return (IBlockState) o;
                throw new RuntimeException("BAH!");
            } else {
                IBlockState bs = ((Supplier<IBlockState>) o).get();
                return (char) Block.BLOCK_STATE_IDS.get(bs);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public IBlockState getState(char c) {
        try {
            Object o = palette.get(c);
            if (o instanceof Character) {
                return Block.BLOCK_STATE_IDS.getByValue((Character) o);
            } else if (o instanceof IBlockState) {
                return (IBlockState) o;
            } else {
                return ((Supplier<IBlockState>) o).get();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isGlass(Block block) {
        return block == Blocks.GLASS || block == Blocks.GLASS_PANE || block == Blocks.STAINED_GLASS || block == Blocks.STAINED_GLASS_PANE;
    }

    public static boolean isGlass(Character block) {
        return block == LostCitiesTerrainGenerator.glassChar || block == LostCitiesTerrainGenerator.glassPaneChar
                || block == LostCitiesTerrainGenerator.stainedGlassChar || block == LostCitiesTerrainGenerator.stainedGlassPaneChar;
    }

    public Character canBeDamagedToIronBars(Character b) {
        return damagedToBlock.get(b);
    }

    public String getMobId(Character c) { return mobIds.get(c); }
}
