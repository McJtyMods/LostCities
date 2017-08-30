package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.dimensions.world.lost.cityassets.CityStyle;
import mcjty.lostcities.dimensions.world.lost.cityassets.PredefinedCity;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Tools;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * A city is defined as a big sphere. Buildings are where the radius is less then 70%
 */
public class City {

    private static Map<ChunkCoord, PredefinedCity> predefinedCityMap = null;
    private static Map<ChunkCoord, PredefinedCity.PredefinedBuilding> predefinedBuildingMap = null;
    private static Map<ChunkCoord, PredefinedCity.PredefinedStreet> predefinedStreetMap = null;

    public static void cleanCache() {
        predefinedCityMap = null;
        predefinedBuildingMap = null;
        predefinedStreetMap = null;
    }

    private static PredefinedCity getPredefinedCity(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        if (predefinedCityMap == null) {
            predefinedCityMap = new HashMap<>();
            for (PredefinedCity city : AssetRegistries.PREDEFINED_CITIES.getIterable()) {
                predefinedCityMap.put(new ChunkCoord(city.getDimension(), city.getChunkX(), city.getChunkZ()), city);
            }
        }
        if (predefinedCityMap.isEmpty()) {
            return null;
        }
        return predefinedCityMap.get(new ChunkCoord(provider.dimensionId, chunkX, chunkZ));
    }

    public static PredefinedCity.PredefinedBuilding getPredefinedBuilding(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        if (predefinedBuildingMap == null) {
            predefinedBuildingMap = new HashMap<>();
            for (PredefinedCity city : AssetRegistries.PREDEFINED_CITIES.getIterable()) {
                for (PredefinedCity.PredefinedBuilding building : city.getPredefinedBuildings()) {
                    predefinedBuildingMap.put(new ChunkCoord(city.getDimension(),
                            city.getChunkX() + building.getRelChunkX(), city.getChunkZ() + building.getRelChunkZ()), building);
                }
            }
        }
        if (predefinedBuildingMap.isEmpty()) {
            return null;
        }
        return predefinedBuildingMap.get(new ChunkCoord(provider.dimensionId, chunkX, chunkZ));
    }

    public static PredefinedCity.PredefinedStreet getPredefinedStreet(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        if (predefinedStreetMap == null) {
            predefinedStreetMap = new HashMap<>();
            for (PredefinedCity city : AssetRegistries.PREDEFINED_CITIES.getIterable()) {
                for (PredefinedCity.PredefinedStreet street : city.getPredefinedStreets()) {
                    predefinedStreetMap.put(new ChunkCoord(city.getDimension(),
                            city.getChunkX() + street.getRelChunkX(), city.getChunkZ() + street.getRelChunkZ()), street);
                }
            }
        }
        if (predefinedStreetMap.isEmpty()) {
            return null;
        }
        return predefinedStreetMap.get(new ChunkCoord(provider.dimensionId, chunkX, chunkZ));
    }

    public static boolean isCityCenter(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        PredefinedCity city = getPredefinedCity(chunkX, chunkZ, provider);
        if (city != null) {
            return true;
        }
        Random rand = new Random(provider.seed + chunkZ * 797003437L + chunkX * 295075153L);
        rand.nextFloat();
        rand.nextFloat();
        return rand.nextFloat() < provider.profile.CITY_CHANCE;
    }

    public static float getCityRadius(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        PredefinedCity city = getPredefinedCity(chunkX, chunkZ, provider);
        if (city != null) {
            return city.getRadius();
        }
        Random rand = new Random(provider.seed + chunkZ * 100001653L + chunkX * 295075153L);
        rand.nextFloat();
        rand.nextFloat();
        return provider.profile.CITY_MINRADIUS + rand.nextInt(provider.profile.CITY_MAXRADIUS - provider.profile.CITY_MINRADIUS);
    }

    // Call this on a city center to get the style of that city
    public static String getCityStyleForCityCenter(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        PredefinedCity city = getPredefinedCity(chunkX, chunkZ, provider);
        if (city != null) {
            if (city.getCityStyle() != null) {
                return city.getCityStyle();
            }
            // Otherwise we chose a random city style
        }
        Random rand = new Random(provider.seed + chunkZ * 899809363L + chunkX * 256203221L);
        rand.nextFloat();
        rand.nextFloat();
        return provider.worldStyle.getRandomCityStyle(provider, chunkX, chunkZ, rand);
    }

    // Calculate the citystyle based on all surrounding cities
    public static CityStyle getCityStyle(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        Random rand = new Random(provider.seed + chunkZ * 593441843L + chunkX * 217645177L);
        rand.nextFloat();
        rand.nextFloat();

        int offset = (provider.profile.CITY_MAXRADIUS+15) / 16;
        List<Pair<Float, String>> styles = new ArrayList<>();
        for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
            for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                if (isCityCenter(cx, cz, provider)) {
                    float radius = getCityRadius(cx, cz, provider);
                    float sqdist = (cx * 16 - chunkX * 16) * (cx * 16 - chunkX * 16) + (cz * 16 - chunkZ * 16) * (cz * 16 - chunkZ * 16);
                    if (sqdist < radius * radius) {
                        float dist = (float) Math.sqrt(sqdist);
                        float factor = (radius - dist) / radius;
                        styles.add(Pair.of(factor, getCityStyleForCityCenter(chunkX, chunkZ, provider)));
                    }
                }
            }
        }
        String cityStyleName;
        if (styles.isEmpty()) {
            cityStyleName = provider.worldStyle.getRandomCityStyle(provider, chunkX, chunkZ, rand);
        } else {
            cityStyleName = Tools.getRandomFromList(rand, styles);
        }
        return AssetRegistries.CITYSTYLES.get(cityStyleName);
    }

    public static float getCityFactor(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        // If we have a predefined building here we force a high city factor

        PredefinedCity.PredefinedBuilding predefinedBuilding = getPredefinedBuilding(chunkX, chunkZ, provider);
        if (predefinedBuilding != null) {
            return 1.0f;
        }
        PredefinedCity.PredefinedStreet predefinedStreet = getPredefinedStreet(chunkX, chunkZ, provider);
        if (predefinedStreet != null) {
            return 1.0f;
        }

        predefinedBuilding = getPredefinedBuilding(chunkX-1, chunkZ, provider);
        if (predefinedBuilding != null && predefinedBuilding.isMulti()) {
            return 1.0f;
        }
        predefinedBuilding = getPredefinedBuilding(chunkX-1, chunkZ-1, provider);
        if (predefinedBuilding != null && predefinedBuilding.isMulti()) {
            return 1.0f;
        }
        predefinedBuilding = getPredefinedBuilding(chunkX, chunkZ-1, provider);
        if (predefinedBuilding != null && predefinedBuilding.isMulti()) {
            return 1.0f;
        }

        for (int cx = -4 ; cx <= 4 ; cx++) {
            for (int cz = -4 ; cz <= 4 ; cz++) {
                if (provider.hasMansion(chunkX+cx, chunkZ+cz)) {
                    return 0.0f;
                }
            }
        }
//        if (provider.isInsideStructure(provider.worldObj, "LostMansion", new BlockPos(chunkX*16, 50, chunkZ*16))) {
//            return 0.0f;
//        }
//        if (provider.isInsideStructure(provider.worldObj, "LostMansion", new BlockPos(chunkX*16+15, 50, chunkZ*16))) {
//            return 0.0f;
//        }
//        if (provider.isInsideStructure(provider.worldObj, "LostMansion", new BlockPos(chunkX*16, 50, chunkZ*16+15))) {
//            return 0.0f;
//        }
//        if (provider.isInsideStructure(provider.worldObj, "LostMansion", new BlockPos(chunkX*16+15, 50, chunkZ*16+15))) {
//            return 0.0f;
//        }

        long seed = provider.seed;
        float factor = 0;
        int offset = (provider.profile.CITY_MAXRADIUS+15) / 16;
        for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
            for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                if (isCityCenter(cx, cz, provider)) {
                    float radius = getCityRadius(cx, cz, provider);
                    float sqdist = (cx * 16 - chunkX * 16) * (cx * 16 - chunkX * 16) + (cz * 16 - chunkZ * 16) * (cz * 16 - chunkZ * 16);
                    if (sqdist < radius * radius) {
                        float dist = (float) Math.sqrt(sqdist);
                        factor += (radius - dist) / radius;
                    }
                }
            }
        }

        Float foundFactor = null;
        for (int cx = -1 ; cx <= 1 ; cx++) {
            for (int cz = -1 ; cz <= 1 ; cz++) {
                Biome[] biomes = BiomeInfo.getBiomeInfo(provider, new ChunkCoord(provider.dimensionId, chunkX + cx, chunkZ + cz)).getBiomes();
                if (isTooHighForBuilding(biomes)) {
                    return 0;
                }
            }
        }

        Biome[] biomes = BiomeInfo.getBiomeInfo(provider, new ChunkCoord(provider.dimensionId, chunkX, chunkZ)).getBiomes();
        for (Biome biome : biomes) {
            Map<String, Float> map = provider.profile.getBiomeFactorMap();
            ResourceLocation object = Biome.REGISTRY.getNameForObject(biome);
            Float f = map.get(object.toString());
            if (f != null) {
                foundFactor = f;
                break;
            }
        }

        if (foundFactor == null) {
            factor = factor * provider.profile.CITY_DEFAULT_BIOME_FACTOR;
        } else {
            factor = factor * foundFactor;
        }
        if (factor < 0) {
            return 0;
        } else if (factor > 1) {
            return 1;
        }
        return factor;
    }

    public static boolean isTooHighForBuilding(Biome[] biomes) {
        return biomes[55].getBaseHeight() > 4 || biomes[54].getBaseHeight() > 4 || biomes[56].getBaseHeight() > 4
                || biomes[5].getBaseHeight() > 4 || biomes[95].getBaseHeight() > 4;
    }

}
