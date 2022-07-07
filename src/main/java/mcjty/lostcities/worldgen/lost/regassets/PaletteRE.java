package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A palette of materials as used by building parts
 */
public class PaletteRE {

    public static class PaletteEntry {
        private String chr;
        private String block;
        private String variant;
        private String frompalette;
        private List<Pair<Integer, String>> blocks;
        private String damaged;

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

        public static PaletteEntry blocks(List<Pair<Integer, String>> blocks) {
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

        public List<Pair<Integer, String>> getBlocks() {
            return blocks;
        }

        public String getDamaged() {
            return damaged;
        }

        public PaletteEntry(String chr, Optional<String> block, Optional<String> variant, Optional<String> frompalette,
                            Optional<List<Pair<Integer, String>>> blocks, Optional<String> damaged) {
            this.chr = chr;
            this.block = block.isPresent() ? block.get() : null;
            this.variant = variant.isPresent() ? variant.get() : null;
            this.frompalette = frompalette.isPresent() ? frompalette.get() : null;
            this.blocks = blocks.isPresent() ? blocks.get() : null;
            this.damaged = damaged.isPresent() ? damaged.get() : null;
        }
    }

    public static final Codec<PaletteEntry> PALETTE_ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("chr").forGetter(l -> l.chr),
                    Codec.STRING.optionalFieldOf("block").forGetter(l -> Optional.ofNullable(l.block)),
                    Codec.STRING.optionalFieldOf("variant").forGetter(l -> Optional.ofNullable(l.variant)),
                    Codec.STRING.optionalFieldOf("frompalette").forGetter(l -> Optional.ofNullable(l.frompalette)),
                    Codec.list(Codec.pair(Codec.INT, Codec.STRING)).optionalFieldOf("blocks").forGetter(l -> Optional.ofNullable(l.blocks)),
                    Codec.STRING.optionalFieldOf("damaged").forGetter(l -> Optional.ofNullable(l.damaged))
            ).apply(instance, PaletteEntry::new));

    // @todo CODEC is not yet complete!
    public static final Codec<PaletteRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(PALETTE_ENTRY_CODEC).fieldOf("palette").forGetter(l -> l.paletteEntries)
            ).apply(instance, PaletteRE::new));

    private final List<PaletteEntry> paletteEntries = new ArrayList<>();

    public PaletteRE(List<PaletteEntry> entries) {
        paletteEntries.addAll(entries);
    }

    public List<PaletteEntry> getPaletteEntries() {
        return paletteEntries;
    }
}
