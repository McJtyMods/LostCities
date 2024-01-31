package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.CityStyle;
import mcjty.lostcities.worldgen.lost.cityassets.PredefinedCity;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import mcjty.lostcities.worldgen.lost.regassets.data.PredefinedBuilding;
import mcjty.lostcities.worldgen.lost.regassets.data.PredefinedStreet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * A city is defined as a big sphere. Buildings are where the radius is less then 70%
 */
public class City {

    private static Map<ChunkCoord, PredefinedCity> predefinedCityMap = null;
    private static Map<ChunkCoord, PredefinedBuilding> predefinedBuildingMap = null;
    private static Map<ChunkCoord, PredefinedStreet> predefinedStreetMap = null;

    // If cityChance == -1 then this is used to control where cities are
    private static final Map<ResourceKey<Level>, CityRarityMap> CITY_RARITY_MAP = new HashMap<>();

    public static void cleanCache() {
        predefinedCityMap = null;
        predefinedBuildingMap = null;
        predefinedStreetMap = null;
        CITY_RARITY_MAP.clear();
    }

    public static CityRarityMap getCityRarityMap(ResourceKey<Level> level, long seed, double scale, double offset, double innerScale) {
        return CITY_RARITY_MAP.computeIfAbsent(level, k -> new CityRarityMap(seed, scale, offset, innerScale));
    }

    public static PredefinedCity getPredefinedCity(int chunkX, int chunkZ, ResourceKey<Level> type) {
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

    public static PredefinedBuilding getPredefinedBuilding(int chunkX, int chunkZ, ResourceKey<Level> type) {
        if (predefinedBuildingMap == null) {
            predefinedBuildingMap = new HashMap<>();
            for (PredefinedCity city : AssetRegistries.PREDEFINED_CITIES.getIterable()) {
                for (PredefinedBuilding building : city.getPredefinedBuildings()) {
                    predefinedBuildingMap.put(new ChunkCoord(city.getDimension(),
                            city.getChunkX() + building.relChunkX(), city.getChunkZ() + building.relChunkZ()), building);
                }
            }
        }
        if (predefinedBuildingMap.isEmpty()) {
            return null;
        }
        return predefinedBuildingMap.get(new ChunkCoord(type, chunkX, chunkZ));
    }

    public static PredefinedStreet getPredefinedStreet(int chunkX, int chunkZ, ResourceKey<Level> type) {
        if (predefinedStreetMap == null) {
            predefinedStreetMap = new HashMap<>();
            for (PredefinedCity city : AssetRegistries.PREDEFINED_CITIES.getIterable()) {
                for (PredefinedStreet street : city.getPredefinedStreets()) {
                    predefinedStreetMap.put(new ChunkCoord(city.getDimension(),
                            city.getChunkX() + street.relChunkX(), city.getChunkZ() + street.relChunkZ()), street);
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
        Random cityCenterRandom = new Random(chunkZ * 797003437L + chunkX * 295075153L);
//        cityCenterRandom.nextFloat();
//        cityCenterRandom.nextFloat();
        if ((provider.getProfile().isSpace() || provider.getProfile().isSpheres())) {
            // @todo config
            CitySphere sphere = CitySphere.getCitySphere(chunkX, chunkZ, provider);
            if (!sphere.isEnabled()) {
                // No sphere
                return cityCenterRandom.nextDouble() < provider.getOutsideProfile().CITY_CHANCE;
            }
            if (sphere.getCenter().chunkX() == chunkX && sphere.getCenter().chunkZ() == chunkZ) {
                // This chunk is the center of a city
                return cityCenterRandom.nextDouble() < provider.getProfile().CITY_CHANCE;
            }
            return false;
        } else {
            return cityCenterRandom.nextDouble() < provider.getProfile().CITY_CHANCE;
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
        Random cityRadiusRandom = new Random(chunkZ * 100001653L + chunkX * 295075153L);
//        cityRadiusRandom.nextFloat();
//        cityRadiusRandom.nextFloat();
        LostCityProfile profile = provider.getProfile();
        int cityRange = profile.CITY_MAXRADIUS - profile.CITY_MINRADIUS;
        if (cityRange < 1) {
            cityRange = 1;
        }
        if (profile.isSpace() || profile.isSpheres()) {
            if (CitySphere.intersectsWithCitySphere(chunkX, chunkZ, provider)) {
                return profile.CITY_MINRADIUS + cityRadiusRandom.nextInt(cityRange);
            } else {
                return provider.getOutsideProfile().CITY_MINRADIUS + cityRadiusRandom.nextInt(provider.getOutsideProfile().CITY_MAXRADIUS - provider.getOutsideProfile().CITY_MINRADIUS);
            }
        } else {
            return profile.CITY_MINRADIUS + cityRadiusRandom.nextInt(cityRange);
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
        Random cityStyleForCenterRandom = new Random(chunkZ * 899809363L + chunkX * 256203221L);
//        cityStyleForCenterRandom.nextFloat();
//        cityStyleForCenterRandom.nextFloat();
        return provider.getWorldStyle().getRandomCityStyle(provider, chunkX, chunkZ, cityStyleForCenterRandom);
    }

    // Calculate the citystyle based on all surrounding cities
    public static CityStyle getCityStyle(int chunkX, int chunkZ, IDimensionInfo provider, LostCityProfile profile) {
        List<Pair<Float, String>> styles = new ArrayList<>();
        Random cityStyleRandom = new Random(provider.getSeed() + chunkZ * 593441843L + chunkX * 217645177L);
//        cityStyleRandom.nextFloat();
//        cityStyleRandom.nextFloat();

        if (profile.CITY_CHANCE < 0) {
            WorldGenLevel world = provider.getWorld();
            CityRarityMap rarityMap = getCityRarityMap(world.getLevel().dimension(), world.getSeed(),
                    profile.CITY_PERLIN_SCALE, profile.CITY_PERLIN_OFFSET, profile.CITY_PERLIN_INNERSCALE);
            float factor = rarityMap.getCityFactor(chunkX, chunkZ);
            if (factor < profile.CITY_STYLE_THRESHOLD) {
                styles.add(Pair.of(factor, profile.CITY_STYLE_ALTERNATIVE));
            } else {
                styles.add(Pair.of(factor, getCityStyleForCityCenter(chunkX, chunkZ, provider)));
            }
        } else {
            int offset = (profile.CITY_MAXRADIUS + 15) / 16;
            for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
                for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                    if (isCityCenter(cx, cz, provider)) {
                        float radius = getCityRadius(cx, cz, provider);
                        float sqdist = (cx * 16 - chunkX * 16) * (cx * 16 - chunkX * 16) + (cz * 16 - chunkZ * 16) * (cz * 16 - chunkZ * 16);
                        if (sqdist < radius * radius) {
                            float dist = (float) Math.sqrt(sqdist);
                            float factor = (radius - dist) / radius;
                            if (factor < profile.CITY_STYLE_THRESHOLD) {
                                styles.add(Pair.of(factor, profile.CITY_STYLE_ALTERNATIVE));
                            } else {
                                styles.add(Pair.of(factor, getCityStyleForCityCenter(chunkX, chunkZ, provider)));
                            }
                        }
                    }
                }
            }
        }

        String cityStyleName;
        if (styles.isEmpty()) {
            cityStyleName = provider.getWorldStyle().getRandomCityStyle(provider, chunkX, chunkZ, cityStyleRandom);
        } else {
            Pair<Float, String> fromList = Tools.getRandomFromList(cityStyleRandom, styles, Pair::getLeft);
            if (fromList == null) {
                cityStyleName = null;
            } else {
                cityStyleName = fromList.getRight();
            }
        }
        return AssetRegistries.CITYSTYLES.get(provider.getWorld(), cityStyleName);
    }

    public static float getCityFactor(int chunkX, int chunkZ, IDimensionInfo provider, LostCityProfile profile) {
        ResourceKey<Level> type = provider.getType();
        // If we have a predefined building here we force a high city factor

        PredefinedBuilding predefinedBuilding = getPredefinedBuilding(chunkX, chunkZ, type);
        if (predefinedBuilding != null) {
            return 1.0f;
        }
        PredefinedStreet predefinedStreet = getPredefinedStreet(chunkX, chunkZ, type);
        if (predefinedStreet != null) {
            return 1.0f;
        }

        predefinedBuilding = getPredefinedBuilding(chunkX-1, chunkZ, type);
        if (predefinedBuilding != null && predefinedBuilding.multi()) {
            return 1.0f;
        }
        predefinedBuilding = getPredefinedBuilding(chunkX-1, chunkZ-1, type);
        if (predefinedBuilding != null && predefinedBuilding.multi()) {
            return 1.0f;
        }
        predefinedBuilding = getPredefinedBuilding(chunkX, chunkZ-1, type);
        if (predefinedBuilding != null && predefinedBuilding.multi()) {
            return 1.0f;
        }

        float factor = 0;
        if (profile.CITY_CHANCE < 0) {
            CityRarityMap rarityMap = getCityRarityMap(provider.dimension(), provider.getSeed(),
                    profile.CITY_PERLIN_SCALE, profile.CITY_PERLIN_OFFSET, profile.CITY_PERLIN_INNERSCALE);
            factor = rarityMap.getCityFactor(chunkX, chunkZ);
        } else {
            int offset = (profile.CITY_MAXRADIUS + 15) / 16;
            for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
                for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                    LostCityProfile pro = BuildingInfo.getProfile(cx, cz, provider);
                    // Only count cities that are in the same 'profile' as this one
                    if (pro == profile) {
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
            }
        }

        if (factor > 0.0001 && provider.getWorld() != null) {
            WorldStyle worldStyle = AssetRegistries.WORLDSTYLES.get(provider.getWorld(), profile.getWorldStyle());
            float multiplier = worldStyle.getCityChanceMultiplier(provider, chunkX, chunkZ);
            factor *= multiplier;
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

        return Math.min(Math.max(factor, 0), 1);
    }
}
