package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.regassets.StyleRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import mcjty.lostcities.worldgen.lost.regassets.data.PaletteSelector;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Style implements ILostCityAsset {

    private final ResourceLocation name;

    private final List<List<Pair<Float, String>>> randomPaletteChoices = new ArrayList<>();

    public Style(StyleRE object) {
        name = object.getRegistryName();
        for (List<PaletteSelector> array : object.getRandomPaletteChoices()) {
            List<Pair<Float, String>> palettes = new ArrayList<>();
            for (PaletteSelector selector : array) {
                float factor = selector.factor();
                String palette = selector.palette();
                palettes.add(Pair.of(factor, palette));
            }
            randomPaletteChoices.add(palettes);
        }
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public ResourceLocation getId() {
        return name;
    }

    public Palette getRandomPalette(IDimensionInfo provider, Random random) {
        Palette palette = new Palette("__random__");
        for (List<Pair<Float, String>> pairs : randomPaletteChoices) {
            float totalweight = 0;
            for (Pair<Float, String> pair : pairs) {
                totalweight += pair.getKey();
            }
            float r = random.nextFloat() * totalweight;
            Palette tomerge = null;
            for (Pair<Float, String> pair : pairs) {
                r -= pair.getKey();
                if (r <= 0) {
                    tomerge = AssetRegistries.PALETTES.get(provider.getWorld(), pair.getRight());
                    if (tomerge == null) {
                        throw new RuntimeException("Palette '" + pair.getRight() + "' is missing!");
                    }
                    break;
                }
            }
            palette.merge(tomerge);
        }

        return palette;
    }
}
