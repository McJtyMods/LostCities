package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.LostCitiesTerrainGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * More efficient representation of a palette useful for a single chunk
 */
public class CompiledPalette {

    private final Map<Character, Object> palette = new HashMap<>();
    private final Map<Character, Character> damagedToBlock = new HashMap<>();
    private final Map<Character, String> mobIds = new HashMap<>();
    private final Map<Character, String> lootTables = new HashMap<>();

    public CompiledPalette(Palette... palettes) {
        // First add the straight palette entries
        for (Palette p : palettes) {
            for (Map.Entry<Character, Object> entry : p.palette.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof IBlockState) {
                    palette.put(entry.getKey(), (char) Block.BLOCK_STATE_IDS.get((IBlockState) value));
                } else if (value instanceof Pair[]) {
                    Pair<Float, IBlockState>[] r = (Pair<Float, IBlockState>[]) value;
                    Pair<Float, Character>[] randomBlocks = new Pair[r.length];
                    for (int i = 0 ; i < r.length ; i++) {
                        randomBlocks[i] = Pair.of(r[i].getLeft(), (char) Block.BLOCK_STATE_IDS.get(r[i].getRight()));
                    }
                    palette.put(entry.getKey(), randomBlocks);
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
            for (Map.Entry<Character, String> entry : p.getLootTables().entrySet()) {
                Character c = entry.getKey();
                lootTables.put(c, entry.getValue());
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
                Pair<Float, Character>[] randomBlocks = (Pair<Float, Character>[]) o;
                return Block.BLOCK_STATE_IDS.getByValue(randomBlocks[0].getRight());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return true if this is a simple character that can have only one value in the palette
     */
    public boolean isSimple(char c) {
        Object o = palette.get(c);
        return o instanceof Character;
    }

    public Character get(char c) {
        try {
            Object o = palette.get(c);
            if (o instanceof Character) {
                return (Character) o;
            } else if (o == null) {
                return null;
            } else {
                Pair<Float, Character>[] randomBlocks = (Pair<Float, Character>[]) o;
                float r = LostCitiesTerrainGenerator.globalRandom.nextFloat();
                for (Pair<Float, Character> pair : randomBlocks) {
                    r -= pair.getKey();
                    if (r <= 0) {
                        return pair.getRight();
                    }
                }
                return LostCitiesTerrainGenerator.airChar;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Character canBeDamagedToIronBars(Character b) {
        return damagedToBlock.get(b);
    }

    public String getMobId(Character c) { return mobIds.get(c); }

    public String getLootTable(Character c) {
        return lootTables.get(c);
    }
}
