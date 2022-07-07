package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A palette of materials as used by building parts
 */
public class PaletteRE implements IForgeRegistryEntry<PaletteRE> {

    public static class BlockEntry {
        private int random;
        private String block;

        public int getRandom() {
            return random;
        }

        public String getBlock() {
            return block;
        }

        public BlockEntry(int random, String block) {
            this.random = random;
            this.block = block;
        }
    }

    public static class PaletteEntry {
        private String chr;
        private String block;
        private String variant;
        private String frompalette;
        private List<BlockEntry> blocks;
        private String damaged;
        private String mob;
        private String loot;
        private Boolean torch;

        public PaletteEntry() {
        }

        public static PaletteEntry block(String block) {
            PaletteEntry entry = new PaletteEntry();
            entry.block = block;
            return entry;
        }

        public static PaletteEntry variant(String variant) {
            PaletteEntry entry = new PaletteEntry();
            entry.variant = variant;
            return entry;
        }

        public static PaletteEntry frompalette(String frompalette) {
            PaletteEntry entry = new PaletteEntry();
            entry.frompalette = frompalette;
            return entry;
        }

        public static PaletteEntry blocks(List<BlockEntry> blocks) {
            PaletteEntry entry = new PaletteEntry();
            entry.blocks = blocks;
            return entry;
        }

        public String getChr() {
            return chr;
        }

        public String getBlock() {
            return block;
        }

        public String getVariant() {
            return variant;
        }

        public String getFrompalette() {
            return frompalette;
        }

        public List<BlockEntry> getBlocks() {
            return blocks;
        }

        public String getDamaged() {
            return damaged;
        }

        public String getMob() {
            return mob;
        }

        public String getLoot() {
            return loot;
        }

        public Boolean getTorch() {
            return torch;
        }

        public PaletteEntry(String chr, Optional<String> block, Optional<String> variant, Optional<String> frompalette,
                            Optional<List<BlockEntry>> blocks, Optional<String> damaged,
                            Optional<String> mob, Optional<String> loot, Optional<Boolean> torch) {
            this.chr = chr;
            this.block = block.isPresent() ? block.get() : null;
            this.variant = variant.isPresent() ? variant.get() : null;
            this.frompalette = frompalette.isPresent() ? frompalette.get() : null;
            this.blocks = blocks.isPresent() ? blocks.get() : null;
            this.damaged = damaged.isPresent() ? damaged.get() : null;
            this.mob = mob.isPresent() ? mob.get() : null;
            this.loot = loot.isPresent() ? loot.get() : null;
            this.torch = torch.isPresent() ? torch.get() : null;
        }
    }

    public static final Codec<BlockEntry> BLOCK_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("random").forGetter(l -> l.random),
                    Codec.STRING.fieldOf("block").forGetter(l -> l.block)
            ).apply(instance, BlockEntry::new));

    public static final Codec<PaletteEntry> PALETTE_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("char").forGetter(l -> l.chr),
                    Codec.STRING.optionalFieldOf("block").forGetter(l -> Optional.ofNullable(l.block)),
                    Codec.STRING.optionalFieldOf("variant").forGetter(l -> Optional.ofNullable(l.variant)),
                    Codec.STRING.optionalFieldOf("frompalette").forGetter(l -> Optional.ofNullable(l.frompalette)),
                    Codec.list(BLOCK_ENTRY_CODEC).optionalFieldOf("blocks").forGetter(l -> Optional.ofNullable(l.blocks)),
                    Codec.STRING.optionalFieldOf("damaged").forGetter(l -> Optional.ofNullable(l.damaged)),
                    Codec.STRING.optionalFieldOf("mob").forGetter(l -> Optional.ofNullable(l.mob)),
                    Codec.STRING.optionalFieldOf("loot").forGetter(l -> Optional.ofNullable(l.loot)),
                    Codec.BOOL.optionalFieldOf("torch").forGetter(l -> Optional.ofNullable(l.torch))
            ).apply(instance, PaletteEntry::new));

    public static final Codec<PaletteRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(PALETTE_ENTRY_CODEC).fieldOf("palette").forGetter(l -> l.paletteEntries)
            ).apply(instance, PaletteRE::new));

    private ResourceLocation name;
    private final List<PaletteEntry> paletteEntries = new ArrayList<>();

    public PaletteRE(List<PaletteEntry> entries) {
        paletteEntries.addAll(entries);
    }

    public List<PaletteEntry> getPaletteEntries() {
        return paletteEntries;
    }

    @Override
    public PaletteRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<PaletteRE> getRegistryType() {
        return PaletteRE.class;
    }
}
