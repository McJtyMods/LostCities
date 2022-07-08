package mcjty.lostcities.worldgen.lost.cityassets;

import com.google.gson.JsonObject;
import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.lost.regassets.PaletteRE;
import mcjty.lostcities.worldgen.lost.regassets.data.BlockEntry;
import mcjty.lostcities.worldgen.lost.regassets.data.PaletteEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * A palette of materials as used by building parts
 */
public class Palette implements ILostCityAsset {

    private String name;
    private final Map<Character, Object> palette = new HashMap<>();
    private final Map<BlockState, BlockState> damaged = new HashMap<>();
    private final Map<Character, String> mobIds = new HashMap<>(); // For spawners
    private final Map<Character, String> lootTables = new HashMap<>(); // For chests
    private final Set<Character> torches = new HashSet<>(); // For torches

    public Palette(PaletteRE object) {
        name = object.getRegistryName().getPath(); // @todo temporary. Needs to be fully qualified
        parsePaletteArray(object);
    }

    public Palette(String name) {
        this.name = name;
    }

    public void merge(Palette other) {
        palette.putAll(other.palette);
        damaged.putAll(other.damaged);
        mobIds.putAll(other.mobIds);
        lootTables.putAll(other.lootTables);
        torches.addAll(other.torches);
    }

    @Override
    public String getName() {
        return name;
    }

    public Map<BlockState, BlockState> getDamaged() {
        return damaged;
    }

    public Map<Character, String> getMobIds() {
        return mobIds;
    }

    public Map<Character, String> getLootTables() {
        return lootTables;
    }

    public Set<Character> getTorches() {
        return torches;
    }

    public Map<Character, Object> getPalette() {
        return palette;
    }

    @Override
    public void readFromJSon(JsonObject object) {
    }

    public void parsePaletteArray(PaletteRE paletteRE) {
        for (PaletteEntry entry : paletteRE.getPaletteEntries()) {
            Character c = entry.getChr().charAt(0);
            BlockState dmg = null;
            if (entry.getDamaged() != null) {
                dmg = Tools.stringToState(entry.getDamaged());
            }
            if (entry.getLoot() != null) {
                lootTables.put(c, entry.getLoot());
            }
            if (entry.getMob() != null) {
                mobIds.put(c, entry.getMob());
            }
            if (entry.getTorch() != null && entry.getTorch()) {
                torches.add(c);
            }
            if (entry.getBlock() != null) {
                String block = entry.getBlock();
                BlockState state = Tools.stringToState(block);
                palette.put(c, state);
                if (dmg != null) {
                    damaged.put(state, dmg);
                }
            } else if (entry.getVariant() != null) {
                String variantName = entry.getVariant();
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                ServerLevel level = server.getLevel(Level.OVERWORLD);
                Variant variant = AssetRegistries.VARIANTS.get(level, variantName);
                if (variant == null) {
                    throw new RuntimeException("Variant '" + variantName + "' is missing!");
                }
                List<Pair<Integer, BlockState>> blocks = variant.getBlocks();
                if (dmg != null) {
                    for (Pair<Integer, BlockState> pair : blocks) {
                        damaged.put(pair.getRight(), dmg);
                    }
                }
                addMappingViaState(c, blocks);
            } else if (entry.getFrompalette() != null) {
                String value = entry.getFrompalette();
                palette.put(c, value);
            } else if (entry.getBlocks() != null) {
                List<BlockEntry> entryBlocks = entry.getBlocks();
                List<Pair<Integer, BlockState>> blocks = new ArrayList<>();
                for (BlockEntry ob : entryBlocks) {
                    Integer f = ob.random();
                    String block = ob.block();
                    BlockState state = Tools.stringToState(block);
                    blocks.add(Pair.of(f, state));
                    if (dmg != null) {
                        damaged.put(state, dmg);
                    }
                }
                addMappingViaState(c, blocks);
            } else {
                throw new RuntimeException("Illegal palette " + name + "!");
            }
        }
    }

    private Palette addMappingViaState(char c, List<Pair<Integer, BlockState>> randomBlocks) {
        palette.put(c, randomBlocks.toArray(new Pair[randomBlocks.size()]));
        return this;
    }
}
