package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.cityassets.*;
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

    record PreDefBuildingOffset(PredefinedBuilding building, int offsetX, int offsetZ) {}

    private static Map<ChunkCoord, PredefinedCity> predefinedCityMap = null;
    private static Map<ChunkCoord, PredefinedBuilding> predefinedBuildingMap = null;
    private static Map<ChunkCoord, PredefinedStreet> predefinedStreetMap = null;

    // If cityChance == -1 then this is used to control where cities are
    private static final Map<ResourceKey<Level>, CityRarityMap> CITY_RARITY_MAP = new HashMap<>();
    private static final Map<ChunkCoord, CityStyle> CITY_STYLE_MAP = new HashMap<>();
    private static Map<ChunkCoord, PreDefBuildingOffset> OCCUPIED_CHUNKS_BUILDING = null;
    private static Map<ChunkCoord, PredefinedStreet> OCCUPIED_CHUNKS_STREET = null;

    public static void cleanCache() {
        predefinedCityMap = null;
        predefinedBuildingMap = null;
        predefinedStreetMap = null;
        CITY_RARITY_MAP.clear();
        CITY_STYLE_MAP.clear();
        OCCUPIED_CHUNKS_BUILDING = null;
        OCCUPIED_CHUNKS_STREET = null;
    }

    public static CityRarityMap getCityRarityMap(ResourceKey<Level> level, long seed, double scale, double offset, double innerScale) {
        return CITY_RARITY_MAP.computeIfAbsent(level, k -> new CityRarityMap(seed, scale, offset, innerScale));
    }

    public static PredefinedCity getPredefinedCity(ChunkCoord coord) {
        if (predefinedCityMap == null) {
            predefinedCityMap = new HashMap<>();
            for (PredefinedCity city : AssetRegistries.PREDEFINED_CITIES.getIterable()) {
                predefinedCityMap.put(new ChunkCoord(city.getDimension(), city.getChunkX(), city.getChunkZ()), city);
            }
        }
        if (predefinedCityMap.isEmpty()) {
            return null;
        }
        return predefinedCityMap.get(coord);
    }

    public static PredefinedBuilding getPredefinedBuildingAtTopLeft(ChunkCoord coord) {
        calculateMap();
        return predefinedBuildingMap.get(coord);
    }

    public static PreDefBuildingOffset getPredefinedBuilding(IDimensionInfo provider, ChunkCoord coord) {
        calculateOccupied(provider);
        return OCCUPIED_CHUNKS_BUILDING.get(coord);
    }

    public static PredefinedStreet getPredefinedStreet(IDimensionInfo provider, ChunkCoord coord) {
        calculateOccupied(provider);
        return OCCUPIED_CHUNKS_STREET.get(coord);
    }

    // Return true if a chunk is occupied (by a predefined building or street)
    public static boolean isChunkOccupied(IDimensionInfo provider, ChunkCoord coord) {
        calculateOccupied(provider);
        return OCCUPIED_CHUNKS_BUILDING.containsKey(coord) || OCCUPIED_CHUNKS_STREET.containsKey(coord);
    }

    private static void calculateOccupied(IDimensionInfo provider) {
        if (OCCUPIED_CHUNKS_BUILDING == null) {
            OCCUPIED_CHUNKS_BUILDING = new HashMap<>();
            calculateMap();
            for (Map.Entry<ChunkCoord, PredefinedBuilding> entry : predefinedBuildingMap.entrySet()) {
                PredefinedBuilding pb = entry.getValue();
                ChunkCoord root = entry.getKey();
                if (pb.multi()) {
                    MultiBuilding building = AssetRegistries.MULTI_BUILDINGS.getOrThrow(provider.getWorld(), pb.building());
                    // Add all occupied chunkcoords for the building to the occupied set
                    for (int x = 0 ; x < building.getDimX() ; x++) {
                        for (int z = 0 ; z < building.getDimZ() ; z++) {
                            OCCUPIED_CHUNKS_BUILDING.put(root.offset(x, z), new PreDefBuildingOffset(pb, x, z));
                        }
                    }
                } else {
                    OCCUPIED_CHUNKS_BUILDING.put(root, new PreDefBuildingOffset(pb, 0, 0));
                }
            }
        }
        if (OCCUPIED_CHUNKS_STREET == null) {
            OCCUPIED_CHUNKS_STREET = new HashMap<>();
            for (PredefinedCity city : AssetRegistries.PREDEFINED_CITIES.getIterable()) {
                for (PredefinedStreet street : city.getPredefinedStreets()) {
                    OCCUPIED_CHUNKS_STREET.put(new ChunkCoord(city.getDimension(),
                            city.getChunkX() + street.relChunkX(), city.getChunkZ() + street.relChunkZ()), street);
                }
            }
        }
    }

    private static void calculateMap() {
        if (predefinedBuildingMap == null) {
            predefinedBuildingMap = new HashMap<>();
            for (PredefinedCity city : AssetRegistries.PREDEFINED_CITIES.getIterable()) {
                for (PredefinedBuilding building : city.getPredefinedBuildings()) {
                    predefinedBuildingMap.put(new ChunkCoord(city.getDimension(),
                            city.getChunkX() + building.relChunkX(), city.getChunkZ() + building.relChunkZ()), building);
                }
            }
        }
    }

    public static PredefinedStreet getPredefinedStreet(ChunkCoord coord) {
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
        return predefinedStreetMap.get(coord);
    }


    public static boolean isCityCenter(ChunkCoord coord, IDimensionInfo provider) {
        PredefinedCity city = getPredefinedCity(coord);
        if (city != null) {
            return true;
        }
        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
        Random cityCenterRandom = new Random(chunkZ * 797003437L + chunkX * 295075153L);
        if ((provider.getProfile().isSpace() || provider.getProfile().isSpheres())) {
            // @todo config
            CitySphere sphere = CitySphere.getCitySphere(coord, provider);
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
    public static float getCityRadius(ChunkCoord coord, IDimensionInfo provider) {
        PredefinedCity city = getPredefinedCity(coord);
        if (city != null) {
            return city.getRadius();
        }
        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
        Random cityRadiusRandom = new Random(chunkZ * 100001653L + chunkX * 295075153L);
        LostCityProfile profile = provider.getProfile();
        int cityRange = profile.CITY_MAXRADIUS - profile.CITY_MINRADIUS;
        if (cityRange < 1) {
            cityRange = 1;
        }
        if (profile.isSpace() || profile.isSpheres()) {
            if (CitySphere.intersectsWithCitySphere(coord, provider)) {
                return profile.CITY_MINRADIUS + cityRadiusRandom.nextInt(cityRange);
            } else {
                return provider.getOutsideProfile().CITY_MINRADIUS + cityRadiusRandom.nextInt(provider.getOutsideProfile().CITY_MAXRADIUS - provider.getOutsideProfile().CITY_MINRADIUS);
            }
        } else {
            return profile.CITY_MINRADIUS + cityRadiusRandom.nextInt(cityRange);
        }
    }

    // Call this on a city center to get the style of that city
    public static String getCityStyleForCityCenter(ChunkCoord coord, IDimensionInfo provider) {
        PredefinedCity city = getPredefinedCity(coord);
        if (city != null) {
            if (city.getCityStyle() != null) {
                return city.getCityStyle();
            }
            // Otherwise we chose a random city style
        }
        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
        Random cityStyleForCenterRandom = new Random(chunkZ * 899809363L + chunkX * 256203221L);
        return provider.getWorldStyle().getRandomCityStyle(provider, coord, cityStyleForCenterRandom);
    }

    // Calculate the citystyle based on all surrounding cities
    public static CityStyle getCityStyle(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        return CITY_STYLE_MAP.computeIfAbsent(coord, k -> getCityStyleInt(coord, provider, profile));
    }

    private static CityStyle getCityStyleInt(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        List<Pair<Float, String>> styles = new ArrayList<>();
        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
        Random cityStyleRandom = new Random(provider.getSeed() + chunkZ * 593441843L + chunkX * 217645177L);

        if (profile.CITY_CHANCE < 0) {
            WorldGenLevel world = provider.getWorld();
            CityRarityMap rarityMap = getCityRarityMap(world.getLevel().dimension(), world.getSeed(),
                    profile.CITY_PERLIN_SCALE, profile.CITY_PERLIN_OFFSET, profile.CITY_PERLIN_INNERSCALE);
            float factor = rarityMap.getCityFactor(chunkX, chunkZ);
            if (factor < profile.CITY_STYLE_THRESHOLD) {
                styles.add(Pair.of(factor, profile.CITY_STYLE_ALTERNATIVE));
            } else {
                styles.add(Pair.of(factor, getCityStyleForCityCenter(coord, provider)));
            }
        } else {
            int offset = (profile.CITY_MAXRADIUS + 15) / 16;
            for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
                for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                    ChunkCoord c = new ChunkCoord(provider.getType(), cx, cz);
                    if (isCityCenter(c, provider)) {
                        float radius = getCityRadius(c, provider);
                        float sqdist = (cx * 16 - (chunkX << 4)) * (cx * 16 - (chunkX << 4)) + (cz * 16 - (chunkZ << 4)) * (cz * 16 - (chunkZ << 4));
                        if (sqdist < radius * radius) {
                            float dist = (float) Math.sqrt(sqdist);
                            float factor = (radius - dist) / radius;
                            if (factor < profile.CITY_STYLE_THRESHOLD) {
                                styles.add(Pair.of(factor, profile.CITY_STYLE_ALTERNATIVE));
                            } else {
                                styles.add(Pair.of(factor, getCityStyleForCityCenter(coord, provider)));
                            }
                        }
                    }
                }
            }
        }

        String cityStyleName;
        if (styles.isEmpty()) {
            cityStyleName = provider.getWorldStyle().getRandomCityStyle(provider, coord, cityStyleRandom);
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

    public static float getCityFactor(ChunkCoord coord, IDimensionInfo provider, LostCityProfile profile) {
        ResourceKey<Level> type = provider.getType();
        // If we have a predefined building here we force a high city factor

        PredefinedBuilding predefinedBuilding = getPredefinedBuildingAtTopLeft(coord);
        if (predefinedBuilding != null) {
            return 1.0f;
        }
        PredefinedStreet predefinedStreet = getPredefinedStreet(coord);
        if (predefinedStreet != null) {
            return 1.0f;
        }

        predefinedBuilding = getPredefinedBuildingAtTopLeft(coord.west());
        if (predefinedBuilding != null && predefinedBuilding.multi()) {
            return 1.0f;
        }
        predefinedBuilding = getPredefinedBuildingAtTopLeft(coord.northWest());
        if (predefinedBuilding != null && predefinedBuilding.multi()) {
            return 1.0f;
        }
        predefinedBuilding = getPredefinedBuildingAtTopLeft(coord.north());
        if (predefinedBuilding != null && predefinedBuilding.multi()) {
            return 1.0f;
        }

        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
        float factor = 0;
        if (profile.CITY_CHANCE < 0) {
            CityRarityMap rarityMap = getCityRarityMap(provider.dimension(), provider.getSeed(),
                    profile.CITY_PERLIN_SCALE, profile.CITY_PERLIN_OFFSET, profile.CITY_PERLIN_INNERSCALE);
            factor = rarityMap.getCityFactor(chunkX, chunkZ);
        } else {
            int offset = (profile.CITY_MAXRADIUS + 15) / 16;
            for (int cx = chunkX - offset; cx <= chunkX + offset; cx++) {
                for (int cz = chunkZ - offset; cz <= chunkZ + offset; cz++) {
                    ChunkCoord c = new ChunkCoord(type, cx, cz);
                    LostCityProfile pro = BuildingInfo.getProfile(c, provider);
                    // Only count cities that are in the same 'profile' as this one
                    if (pro == profile) {
                        if (isCityCenter(c, provider)) {
                            float radius = getCityRadius(c, provider);
                            float sqdist = (cx * 16 - (chunkX << 4)) * (cx * 16 - (chunkX << 4)) + (cz * 16 - (chunkZ << 4)) * (cz * 16 - (chunkZ << 4));
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
            float multiplier = worldStyle.getCityChanceMultiplier(provider, coord);
            factor *= multiplier;
        }

        if (factor > 0.0001 && provider.getWorld() != null) {
            // Check if the terrain is not too low or high for building
            ChunkHeightmap heightmap = provider.getHeightmap(coord);
            if (heightmap == null) {
                return 0;
            }
            if (heightmap.getHeight() < profile.CITY_MINHEIGHT) {
                return 0;
            }
            if (heightmap.getHeight() > profile.CITY_MAXHEIGHT) {
                return 0;
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

        return Math.min(Math.max(factor, 0), 1);
    }
}
