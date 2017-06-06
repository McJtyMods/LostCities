package mcjty.lostcities.dimensions.world.lost.cityassets;

import com.google.common.collect.Lists;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.lost.Style;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class CityStyle implements IAsset {

    private final String name;

    private final List<Function<CityInfo, Pair<Float, String>>> buildingSelector = new ArrayList<>();
    private final List<Function<CityInfo, Pair<Float, String>>> multiBuildingSelector = new ArrayList<>();
    private final List<List<Pair<Float, String>>> randomStyleChoices = new ArrayList<>();

    public CityStyle(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public CityStyle addBuilding(Function<CityInfo, Pair<Float, String>> function) {
        buildingSelector.add(function);
        return this;
    }

    public CityStyle addMultiBuilding(Function<CityInfo, Pair<Float, String>> function) {
        multiBuildingSelector.add(function);
        return this;
    }

    public CityStyle addRandomStyleChoice(Pair<Float, String>... styles) {
        randomStyleChoices.add(Lists.newArrayList(styles));
        return this;
    }

    public Style getRandomStyle(LostCityChunkGenerator provider, Random random) {
        Style style = new Style();
        for (List<Pair<Float, String>> pairs : randomStyleChoices) {
            float totalweight = 0;
            for (Pair<Float, String> pair : pairs) {
                totalweight += pair.getKey();
            }
            float r = random.nextFloat() * totalweight;
            Style tomerge = null;
            for (Pair<Float, String> pair : pairs) {
                r -= pair.getKey();
                if (r <= 0) {
                    tomerge = AssetRegistries.STYLES.get(pair.getRight());
                    if (tomerge == null) {
                        throw new RuntimeException("Style '" + pair.getRight() + "' is missing!");
                    }
                    break;
                }
            }
            style.merge(tomerge);
        }

        return style;
    }

    public String getRandomBuilding(LostCityChunkGenerator provider, Random random) {
        List<Pair<Float, String>> buildings = new ArrayList<>();
        float totalweight = 0;
        CityInfo cityInfo = new CityInfo(provider, random);
        for (Function<CityInfo, Pair<Float, String>> function : buildingSelector) {
            Pair<Float, String> pair = function.apply(cityInfo);
            if (pair != null) {
                buildings.add(pair);
                totalweight += pair.getKey();
            }
        }
        float r = random.nextFloat() * totalweight;
        for (Pair<Float, String> pair : buildings) {
            r -= pair.getKey();
            if (r <= 0) {
                return pair.getRight();
            }
        }
        return null;
    }

    public String getRandomMultiBuilding(LostCityChunkGenerator provider, Random random) {
        List<Pair<Float, String>> multiBuildings = new ArrayList<>();
        float totalweight = 0;
        CityInfo cityInfo = new CityInfo(provider, random);
        for (Function<CityInfo, Pair<Float, String>> function : multiBuildingSelector) {
            Pair<Float, String> pair = function.apply(cityInfo);
            if (pair != null) {
                multiBuildings.add(pair);
                totalweight += pair.getKey();
            }
        }
        float r = random.nextFloat() * totalweight;
        for (Pair<Float, String> pair : multiBuildings) {
            r -= pair.getKey();
            if (r <= 0) {
                return pair.getRight();
            }
        }
        return null;
    }


    public static class CityInfo {
        private final LostCityChunkGenerator provider;
        private final Random random;

        public CityInfo(LostCityChunkGenerator provider, Random random) {
            this.provider = provider;
            this.random = random;
        }

        public LostCityChunkGenerator getProvider() {
            return provider;
        }

        public Random getRandom() {
            return random;
        }
    }
}
