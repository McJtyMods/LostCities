package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.lost.regassets.PaletteRE;
import mcjty.lostcities.worldgen.lost.regassets.data.BlockEntry;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import mcjty.lostcities.worldgen.lost.regassets.data.PaletteEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A palette of materials as used by building parts
 */
public class Palette implements ILostCityAsset {

    private final ResourceLocation name;
    private final Map<Character, PE> palette = new HashMap<>();
    private final Map<BlockState, BlockState> damaged = new HashMap<>();

    public Palette(PaletteRE object) {
        name = object.getRegistryName();
        parsePaletteArray(object);
    }

    public Palette(String name) {
        this.name = ResourceLocation.fromNamespaceAndPath(LostCities.MODID, name);
    }

    public void merge(Palette other) {
        palette.putAll(other.palette);
        damaged.putAll(other.damaged);
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public ResourceLocation getId() {
        return name;
    }

    public Map<BlockState, BlockState> getDamaged() {
        return damaged;
    }

    public Map<Character, PE> getPalette() {
        return palette;
    }

    public void parsePaletteArray(PaletteRE paletteRE) {
        for (PaletteEntry entry : paletteRE.getPaletteEntries()) {
            Character c = entry.getChr().charAt(0);
            BlockState dmg = null;
            if (entry.getDamaged() != null) {
                dmg = Tools.stringToState(entry.getDamaged());
            }
            Info info = new Info(entry.getMob(), entry.getLoot(), entry.getTorch() == null ? false : entry.getTorch(),
                    entry.getTag());

            if (entry.getBlock() != null) {
                String block = entry.getBlock();
                BlockState state = Tools.stringToState(block);
                palette.put(c, new PE(state, info));
                if (dmg != null) {
                    damaged.put(state, dmg);
                }
            } else if (entry.getVariant() != null) {
                String variantName = entry.getVariant();
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                ServerLevel level = server.getLevel(Level.OVERWORLD);
                Variant variant = AssetRegistries.VARIANTS.getOrThrow(level, variantName);
                List<Pair<Integer, BlockState>> blocks = variant.getBlocks();
                if (dmg != null) {
                    for (Pair<Integer, BlockState> pair : blocks) {
                        damaged.put(pair.getRight(), dmg);
                    }
                }
                addMappingViaState(c, blocks, info);
            } else if (entry.getFrompalette() != null) {
                String value = entry.getFrompalette();
                palette.put(c, new PE(value, info));
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
                addMappingViaState(c, blocks, info);
            } else {
                throw new RuntimeException("Illegal palette " + name + "!");
            }
        }
    }

    private Palette addMappingViaState(char c, List<Pair<Integer, BlockState>> randomBlocks, Info info) {
        palette.put(c, new PE(randomBlocks.toArray(new Pair[randomBlocks.size()]), info));
        return this;
    }

    public record Info(String mobId, String loot, boolean isTorch, CompoundTag tag) {
        public boolean isSpecial() {
            return mobId != null || loot != null || isTorch || tag != null;
        }
    }

    public record PE(Object blocks, Info info) {
    }

}
