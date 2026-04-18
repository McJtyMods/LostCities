package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An entry in a palette
 */
public class PaletteEntry {

    public static final Codec<PaletteEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("char").forGetter(PaletteEntry::getChr),
                    Codec.STRING.optionalFieldOf("block").forGetter(l -> Optional.ofNullable(l.getBlock())),
                    Codec.STRING.optionalFieldOf("variant").forGetter(l -> Optional.ofNullable(l.getVariant())),
                    Codec.STRING.optionalFieldOf("frompalette").forGetter(l -> Optional.ofNullable(l.getFrompalette())),
                    Codec.list(BlockEntry.CODEC).optionalFieldOf("blocks").forGetter(l -> Optional.ofNullable(l.getBlocks())),
                    Codec.STRING.optionalFieldOf("damaged").forGetter(l -> Optional.ofNullable(l.getDamaged())),
                    Codec.STRING.optionalFieldOf("mob").forGetter(l -> Optional.ofNullable(l.getMob())),
                    Codec.STRING.optionalFieldOf("loot").forGetter(l -> Optional.ofNullable(l.getLoot())),
                    Codec.BOOL.optionalFieldOf("torch").forGetter(l -> Optional.ofNullable(l.getTorch())),
                    CompoundTag.CODEC.optionalFieldOf("tag").forGetter(l -> Optional.ofNullable(l.getTag()))
            ).apply(instance, PaletteEntry::new));

    // Used for reducing the memory usage when stored in the game client, we shouldn't even have duplicate pools as they're very expensive - Quan
    private static final Map<CompoundTag, CompoundTag> TAG_POOL = new HashMap<>();

    private String chr;
    private String block;
    private String variant;
    private String frompalette;
    private List<BlockEntry> blocks;
    private String damaged;
    private String mob;
    private String loot;
    private Boolean torch;
    private CompoundTag tag;

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

    public CompoundTag getTag() {
        return tag;
    }

    private static CompoundTag deduplicate(CompoundTag incomingTag) {
        if (incomingTag == null || incomingTag.isEmpty()) {
            return null;
        }
        // If the tag exists in the pool, return the existing one.
        // If not, put this one in and return it.
        return TAG_POOL.computeIfAbsent(incomingTag, t -> t);
    }

    public PaletteEntry(String chr, Optional<String> block, Optional<String> variant, Optional<String> frompalette,
                        Optional<List<BlockEntry>> blocks, Optional<String> damaged,
                        Optional<String> mob, Optional<String> loot, Optional<Boolean> torch,
                        Optional<CompoundTag> tag) {
        this.chr = chr.intern();
        this.block = block.map(String::intern).orElse(null);
        this.variant = variant.orElse(null);
        this.frompalette = frompalette.map(String::intern).orElse(null);
        this.blocks = blocks.orElse(null);
        this.damaged = damaged.map(String::intern).orElse(null);
        this.mob = mob.map(String::intern).orElse(null);
        this.loot = loot.map(String::intern).orElse(null);
        this.torch = torch.orElse(null);
        this.tag = deduplicate(tag.orElse(null));
    }

    @Override
    public String toString() {
        return "PaletteEntry{" +
                "chr='" + chr + '\'' +
                ", block='" + block + '\'' +
                ", variant='" + variant + '\'' +
                ", frompalette='" + frompalette + '\'' +
                ", blocks=" + blocks +
                ", damaged='" + damaged + '\'' +
                ", mob='" + mob + '\'' +
                ", loot='" + loot + '\'' +
                ", torch=" + torch +
                ", tag=" + tag +
                '}';
    }
}
