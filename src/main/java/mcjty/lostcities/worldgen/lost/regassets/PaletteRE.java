package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.worldgen.lost.regassets.data.PaletteEntry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A palette of materials as used by building parts
 */
public class PaletteRE implements IAsset<PaletteRE> {

    public static final Codec<PaletteRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(PaletteEntry.CODEC).fieldOf("palette").forGetter(l -> l.paletteEntries)
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
    public ResourceLocation getRegistryName() {
        return name;
    }
}