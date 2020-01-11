package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.CityStyle;
import mcjty.lostcities.worldgen.lost.cityassets.PredefinedCity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
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

    public static PredefinedCity getPredefinedCity(int chunkX, int chunkZ, DimensionType type) {
        if (predefinedCityMap == null) {
            predefinedCityMap = new HashMap<>();
            for (PredefinedCity city : AssetRegistries.PREDEFINED_CITIES.getIterable()) {
                predefinedCityMap.put(new ChunkCoord(city.getDimension(), city.getChunkX(), city.getChunkZ()), city);
            }
        }
        if (predefinedCityMap.isEmpty()) {
            return null;
        }
        return predefinedCityMap.get(new ChunkCoord(type, chunkX, chunkZ));
    }

    public static PredefinedCity.PredefinedBuilding getPredefinedBuilding(int chunkX, int chunkZ, DimensionType type) {
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
        return predefinedBuildingMap.get(new ChunkCoord(type, chunkX, chunkZ));
    }

    public static PredefinedCity.PredefinedStreet getPredefinedStreet(int chunkX, int chunkZ, DimensionType type) {
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
        return predefinedStreetMap.get(new ChunkCoord(type, chunkX, chunkZ));
    }


    public static boolean isCityCenter(int chunkX, int chunkZ, IDimensionInfo provider) {
        PredefinedCity city = getPredefinedCity(chunkX, chunkZ, provider.getType());
        if (city != null) {
            return true;
        }
        Random rand = new Random(provider.getSeed() + chunkZ * 797003437L + chunkX * 295075153L);
        rand.nextFloat();
        rand.nextFloat();
        if (provider.getProfile().isSpace()) {
            // @todo config
            CitySphere sphere = CitySphere.getCitySphere(chunkX, chunkZ, provider);
            if (!sphere.isEnabled()) {
                // No sphere
                return rand.nextFloat() < provider.getOutsideProfile().CITY_CHANCE;
            }
            if (sphere.getCenter().getChunkX() == chunkX && sphere.getCenter().getChunkZ() == chunkZ) {
                // This chunk is the center of a city
                return rand.nextFloat() < provider.getProfile().CITY_CHANCE;
            }
            return false;
        } else {
            return rand.nextFloat() < provider.getProfile().CITY_CHANCE;
        }
    }

    /**
     * Return the radius of the city with the given center
     */
    public static float getCityRadius(int chunkX, int chunkZ, IDimensionInfo provider) {
        PredefinedCity city = getPredefinedCity(chunkX, chunkZ, provider.getType());
        if (city != null) {
            return city.getRadius();
        }
        Random rand = new Random(provider.getSeed() + chunkZ * 100001653L + chunkX * 295075153L);
        rand.nextFloat();
        rand.nextFloat();
        LostCityProfile profile = provider.getProfile();
        int cityRange = profile.CITY_MAXRADIUS - profile.CITY_MINRADIUS;
        if (cityRange < 1) {
            cityRange = 1;
        }
        if (profile.isSpace() && profile.CITYSPHERE_LANDSCAPE_OUTSIDE) {
            if (CitySphere.intersectsWithCitySphere(chunkX, chunkZ, provider)) {
                return profile.CITY_MINRADIUS + rand.nextInt(cityRange);
            } else {
                return provider.getOutsideProfile().CITY_MINRADIUS + rand.nextInt(provider.getOutsideProfile().CITY_MAXRADIUS - provider.getOutsideProfile().CITY_MINRADIUS);
            }
        } else {
            return profile.CITY_MINRADIUS + rand.nextInt(cityRange);
        }
    }

    // Call this on a city center to get the style of that city
    public static String getCityStyleForCityCenter(int chunkX, int chunkZ, IDimensionInfo provider) {
        PredefinedCity city = getPredefinedCity(chunkX, chunkZ, provider.getType());
        if (city != null) {
            if (city.getCityStyle() != null) {
                return city.getCityStyle();
            }
            // Otherwise we chose a random city style
        }
        Random rand = new Random(provider.getSeed() + chunkZ * 899809363L + chunkX * 256203221L);
        rand.nextFloat();
        rand.nextFloat();
        return provider.getWorldStyle().getRandomCityStyle(provider, chunkX, chunkZ, rand);
    }

    // Calculate the citystyle based on all surrounding cities
    public static CityStyle getCityStyle(int chunkX, int chunkZ, IDimensionInfo provider, LostCityProfile profile) {
        Random rand = new Random(provider.getSeed() + chunkZ * 593441843L + chunkX * 217645177L);
        rand.nextFloat();
        rand.nextFloat();

        int offset = (profile.CITY_MAXRADIUS+15) / 16;
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
            cityStyleName = provider.getWorldStyle().getRandomCityStyle(provider, chunkX, chunkZ, rand);
        } else {
            cityStyleName = Tools.getRandomFromList(rand, styles);
        }
        return AssetRegistries.CITYSTYLES.get(cityStyleName);
    }

    public static float getCityFactor(int chunkX, int chunkZ, IDimensionInfo provider, LostCityProfile profile) {
        DimensionType type = provider.getType();
        // If we have a predefined building here we force a high city factor

        PredefinedCity.PredefinedBuilding predefinedBuilding = getPredefinedBuilding(chunkX, chunkZ, type);
        if (predefinedBuilding != null) {
            return 1.0f;
        }
        PredefinedCity.PredefinedStreet predefinedStreet = getPredefinedStreet(chunkX, chunkZ, type);
        if (predefinedStreet != null) {
            return 1.0f;
        }

        predefinedBuilding = getPredefinedBuilding(chunkX-1, chunkZ, type);
        if (predefinedBuilding != null && predefinedBuilding.isMulti()) {
            return 1.0f;
        }
        predefinedBuilding = getPredefinedBuilding(chunkX-1, chunkZ-1, type);
        if (predefinedBuilding != null && predefinedBuilding.isMulti()) {
            return 1.0f;
        }
        predefinedBuilding = getPredefinedBuilding(chunkX, chunkZ-1, type);
        if (predefinedBuilding != null && predefinedBuilding.isMulti()) {
            return 1.0f;
        }

        for (int cx = -4 ; cx <= 4 ; cx++) {
            for (int cz = -4 ; cz <= 4 ; cz++) {
                // @todo 1.14
//                if (provider.hasMansion(chunkX+cx, chunkZ+cz)) {
//                    return 0.0f;
//                }
            }
        }

        for (int cx = -2 ; cx <= 2 ; cx++) {
            for (int cz = -2 ; cz <= 2 ; cz++) {
                // @todo 1.14
//                if (provider.hasOceanMonument(chunkX+cx, chunkZ+cz)) {
//                    return 0.0f;
//                }
            }
        }

        float factor = 0;
        int offset = (profile.CITY_MAXRADIUS+15) / 16;
        for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
            for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                LostCityProfile pro = BuildingInfo.getProfile(cx, cz, provider);
                // Only count cities that are in the same 'profile' as this one
                if (pro == profile && isCityCenter(cx, cz, provider)) {
                    float radius = getCityRadius(cx, cz, provider);
                    float sqdist = (cx * 16 - chunkX * 16) * (cx * 16 - chunkX * 16) + (cz * 16 - chunkZ * 16) * (cz * 16 - chunkZ * 16);
                    if (sqdist < radius * radius) {
                        float dist = (float) Math.sqrt(sqdist);
                        factor += (radius - dist) / radius;
                    }
                }
            }
        }

        // @todo 1.14: do we need this?
//        for (int cx = -1 ; cx <= 1 ; cx++) {
//            for (int cz = -1 ; cz <= 1 ; cz++) {
//                Biome[] biomes = BiomeInfo.getBiomeInfo(provider, new ChunkCoord(type, chunkX + cx, chunkZ + cz)).getBiomes();
//                if (isTooHighForBuilding(biomes)) {
//                    return 0;
//                }
//            }
//        }

        float foundFactor = profile.CITY_DEFAULT_BIOME_FACTOR;
        Biome biome = BiomeInfo.getBiomeInfo(provider, new ChunkCoord(type, chunkX, chunkZ)).getMainBiome();
        Map<ResourceLocation, Float> map = profile.getBiomeFactorMap();
        ResourceLocation object = biome.getRegistryName();
        Float f;
        try {
            f = map.get(object);
        } catch(NullPointerException e) {
            throw new RuntimeException("Biome '" + biome.getTranslationKey() + "' (" + biome.getRegistryName().getPath() + ") could not be found in the biome registry! This is likely a bug in the mod providing that biome!", e);
        }
        if (f != null) {
            foundFactor = f;
        }

        return Math.min(Math.max(factor * foundFactor, 0), 1);
    }

    public static boolean isTooHighForBuilding(Biome[] biomes) {
        // @todo 1.14
//        return biomes[55].getBaseHeight() > 4 || biomes[54].getBaseHeight() > 4 || biomes[56].getBaseHeight() > 4
//                || biomes[5].getBaseHeight() > 4 || biomes[95].getBaseHeight() > 4;
        return false;
    }

}
