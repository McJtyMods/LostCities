package mcjty.lostcities.dimensions.world.lost.cityassets;

import mcjty.lostcities.dimensions.world.terraingen.LostCitiesTerrainGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * More efficient representation of a palette useful for a single chunk
 */
public class CompiledPalette {

    private final Map<Character, Object> palette = new HashMap<>();
    private final Map<Character, Character> damagedToBlock = new HashMap<>();
    private final Map<Character, Info> information = new HashMap<>();

    public static class Info {
        private final String mobId;
        private final String loot;
        private final Map<String, Integer> torchOrientations;

        public Info(String mobId, String loot, Map<String, Integer> torchOrientations) {
            this.mobId = mobId;
            this.loot = loot;
            this.torchOrientations = torchOrientations;
        }

        public String getMobId() {
            return mobId;
        }

        public String getLoot() {
            return loot;
        }

        public Map<String, Integer> getTorchOrientations() {
            return torchOrientations;
        }
    }


    public CompiledPalette(CompiledPalette other, Palette... palettes) {
        this.palette.putAll(other.palette);
        this.damagedToBlock.putAll(other.damagedToBlock);
        this.information.putAll(other.information);
        addPalettes(palettes);
    }

    public CompiledPalette(Palette... palettes) {
        addPalettes(palettes);
    }

    private int addEntries(char[] randomBlocks, int idx, char c, int cnt) {
        for (int i = 0 ; i < cnt ; i++) {
            if (idx >= randomBlocks.length) {
                return idx;
            }
            randomBlocks[idx++] = c;
        }
        return idx;
    }

    public void addPalettes(Palette[] palettes) {
        // First add the straight palette entries
        for (Palette p : palettes) {
            for (Map.Entry<Character, Object> entry : p.palette.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof IBlockState) {
                    palette.put(entry.getKey(), (char) Block.BLOCK_STATE_IDS.get((IBlockState) value));
                } else if (value instanceof Pair[]) {
                    Pair<Integer, IBlockState>[] r = (Pair<Integer, IBlockState>[]) value;
                    char[] randomBlocks = new char[128];
                    int idx = 0;
                    for (Pair<Integer, IBlockState> pair : r) {
                        idx = addEntries(randomBlocks, idx, (char) Block.BLOCK_STATE_IDS.get(pair.getRight()), pair.getLeft());
                        if (idx >= randomBlocks.length) {
                            break;
                        }
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
                information.put(c, new Info(entry.getValue(), null, null));
            }
            for (Map.Entry<Character, String> entry : p.getLootTables().entrySet()) {
                Character c = entry.getKey();
                information.put(c, new Info(null, entry.getValue(), null));
            }
            for (Map.Entry<Character, Map<String, Integer>> entry : p.getTorchOrientations().entrySet()) {
                Character c = entry.getKey();
                information.put(c, new Info(null, null, entry.getValue()));
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
                char[] randomBlocks = (char[]) o;
                return Block.BLOCK_STATE_IDS.getByValue(randomBlocks[0]);
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

    // Same as get(c) but with a predefined random generator that is predictable
    public Character get(char c, Random rand) {
        try {
            Object o = palette.get(c);
            if (o instanceof Character) {
                return (Character) o;
            } else if (o == null) {
                return null;
            } else {
                char[] randomBlocks = (char[]) o;
                return randomBlocks[rand.nextInt(128)];
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
            } else if (o == null) {
                return null;
            } else {
                char[] randomBlocks = (char[]) o;
                return randomBlocks[LostCitiesTerrainGenerator.fastrand128()];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Character canBeDamagedToIronBars(Character b) {
        return damagedToBlock.get(b);
    }

    public Info getInfo(Character c) { return information.get(c); }
}
