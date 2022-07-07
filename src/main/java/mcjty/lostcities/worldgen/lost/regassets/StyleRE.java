package mcjty.lostcities.worldgen.lost.regassets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StyleRE implements IForgeRegistryEntry<StyleRE> {

    public static class PaletteSelector {
        private float factor;
        private String palette;

        public float getFactor() {
            return factor;
        }

        public String getPalette() {
            return palette;
        }

        public PaletteSelector(float factor, String palette) {
            this.factor = factor;
            this.palette = palette;
        }
    }

    public static final Codec<PaletteSelector> PALETTE_SELECTOR_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("factor").forGetter(l -> l.factor),
                    Codec.STRING.fieldOf("palette").forGetter(l -> l.palette)
            ).apply(instance, PaletteSelector::new));

    public static final Codec<StyleRE> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Codec.list(PALETTE_SELECTOR_CODEC)).fieldOf("randompalettes").forGetter(l -> l.randomPaletteChoices)
            ).apply(instance, StyleRE::new));

    private ResourceLocation name;

    private final List<List<PaletteSelector>> randomPaletteChoices;

    public StyleRE(List<List<PaletteSelector>> randomPaletteChoices) {
        this.randomPaletteChoices = randomPaletteChoices;
    }

    public List<List<PaletteSelector>> getRandomPaletteChoices() {
        return randomPaletteChoices;
    }

    @Override
    public StyleRE setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<StyleRE> getRegistryType() {
        return StyleRE.class;
    }
}
