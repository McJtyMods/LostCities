package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.dimensions.world.lost.cityassets.CityStyle;
import mcjty.lostcities.dimensions.world.lost.cityassets.PredefinedCity;
import mcjty.lostcities.dimensions.world.lost.cityassets.PredefinedSphere;
import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CitySphere {

    private static Map<ChunkCoord, CitySphere> citySphereCache = new HashMap<>();
    private static Map<ChunkCoord, PredefinedSphere> predefinedSphereMap = Collections.emptyMap();

    public static final CitySphere EMPTY = new CitySphere(new ChunkCoord(0, 0, 0), 0.0f, new BlockPos(0, 0, 0), false);

    private final ChunkCoord center;
    private final BlockPos centerPos;
    private final float radius;
    private final boolean enabled;

    private Biome biome;

    private boolean monorailNorthCandidate;
    private boolean monorailSouthCandidate;
    private boolean monorailWestCandidate;
    private boolean monorailEastCandidate;

    private char glassBlock = 0;
    private char baseBlock = 0;
    private char sideBlock = 0;

    private CitySphere(ChunkCoord center, float radius, BlockPos centerPos, boolean enabled) {
        this.enabled = enabled;
        this.center = center;
        this.radius = radius;
        this.centerPos = centerPos;
    }

    public static void initSphere(CitySphere sphere, LostCityChunkGenerator provider) {
        if (sphere.getBaseBlock() != 0) {
            return;
        }

        ChunkCoord center = sphere.getCenter();
        BuildingInfo info = BuildingInfo.getBuildingInfo(center.getChunkX(), center.getChunkZ(), provider);
        CityStyle cs = info.getCityStyle();

        Random rand = new Random(info.provider.seed + center.getChunkX() * 837971201L + center.getChunkZ() * 961744153L);
        rand.nextFloat();
        rand.nextFloat();

        Character glass = info.getCompiledPalette().get(cs.getSphereGlassBlock(), rand);
        Character base = info.getCompiledPalette().get(cs.getSphereBlock(), rand);
        Character side = info.getCompiledPalette().get(cs.getSphereSideBlock(), rand);
        sphere.setBlocks(glass, base, side);
    }

    public ChunkCoord getCenter() {
        return center;
    }

    public BlockPos getCenterPos() {
        return centerPos;
    }

    public float getRadius() {
        return radius;
    }

    @Nullable
    public Biome getBiome() {
        return biome;
    }

    public void setBlocks(char glassBlock, char baseBlock, char sideBlock) {
        this.glassBlock = glassBlock;
        this.baseBlock = baseBlock;
        this.sideBlock = sideBlock;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public char getGlassBlock() {
        return glassBlock;
    }

    public char getBaseBlock() {
        return baseBlock;
    }

    public char getSideBlock() {
        return sideBlock;
    }

    public static void cleanCache() {
        citySphereCache.clear();
        predefinedSphereMap = Collections.emptyMap();
    }

    public static PredefinedSphere getPredefinedSphere(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        if (predefinedSphereMap == null) {
            predefinedSphereMap = new HashMap<>();
            for (PredefinedSphere sphere : AssetRegistries.PREDEFINED_SPHERES.getIterable()) {
                predefinedSphereMap.put(new ChunkCoord(sphere.getDimension(), sphere.getChunkX(), sphere.getChunkZ()), sphere);
            }
        }
        if (predefinedSphereMap.isEmpty()) {
            return null;
        }
        return predefinedSphereMap.get(new ChunkCoord(provider.dimensionId, chunkX, chunkZ));
    }


    /**
     * Return true if there is a horizontal monorail here. This is the case if this chunk is on a city center multiple
     * (i.e. multiple of 16) and if there are cities left and right that both want a monorail in the correct direction
     */
    public static boolean hasHorizontalMonorail(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        if ((chunkZ & 0xf) == 8) {
            // There is a city center on this vertical chunk coordinate
            // Find the first city on the right (with a limit)
            // @todo optimize this by caching the result of this scan?
            boolean result = false;
            for (int cx = chunkX+1 ; cx < chunkX+64 ; cx++) {
                if ((cx & 0xf) == 8) {
                    // This could be a city center
                    CitySphere sphere = getCitySphere(cx, chunkZ, provider);
                    if (sphere.isEnabled()) {
                        if (!sphere.monorailWestCandidate) {
                            return false;
                        } else {
                            result = true;
                            break;  // No need to continue. We found our city
                        }
                    }
                }
            }
            if (!result) {
                return false;
            }
            result = false;
            // Find the first city on the left (with a limit)
            for (int cx = chunkX-1 ; cx > chunkX-64 ; cx--) {
                if ((cx & 0xf) == 8) {
                    // This could be a city center
                    CitySphere sphere = getCitySphere(cx, chunkZ, provider);
                    if (sphere.isEnabled()) {
                        if (!sphere.monorailEastCandidate) {
                            return false;
                        } else {
                            result = true;
                            break;  // No need to continue. We found our city
                        }
                    }
                }
            }
            return result;
        } else {
            return false;
        }
    }

    public static boolean hasVerticalMonorail(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        if ((chunkX & 0xf) == 8) {
            boolean result = false;
            for (int cz = chunkZ+1 ; cz < chunkZ+64 ; cz++) {
                if ((cz & 0xf) == 8) {
                    // This could be a city center
                    CitySphere sphere = getCitySphere(chunkX, cz, provider);
                    if (sphere.isEnabled()) {
                        if (!sphere.monorailNorthCandidate) {
                            return false;
                        } else {
                            result = true;
                            break;  // No need to continue. We found our city
                        }
                    }
                }
            }
            if (!result) {
                return false;
            }
            result = false;
            for (int cz = chunkZ-1 ; cz > chunkZ-64 ; cz--) {
                if ((cz & 0xf) == 8) {
                    // This could be a city center
                    CitySphere sphere = getCitySphere(chunkX, cz, provider);
                    if (sphere.isEnabled()) {
                        if (!sphere.monorailSouthCandidate) {
                            return false;
                        } else {
                            result = true;
                            break;  // No need to continue. We found our city
                        }
                    }
                }
            }
            return result;
        } else {
            return false;
        }
    }

    /**
     * From the center
     */
    private static float getSphereRadius(ChunkCoord center, LostCityChunkGenerator provider, Random rand) {
        PredefinedCity city = City.getPredefinedCity(center.getChunkX(), center.getChunkZ(), provider);
        LostCityProfile profile = provider.getProfile();
        if (city != null) {
            return city.getRadius() * profile.CITYSPHERE_FACTOR;
        }
        return profile.CITY_MINRADIUS + rand.nextInt(profile.CITY_MAXRADIUS - profile.CITY_MINRADIUS) * profile.CITYSPHERE_FACTOR;
    }


    /**
     * Return true if a given chunk is fully enclosed in a city sphere
     */
    public static boolean fullyInsideCitySpere(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        CitySphere sphere = CitySphere.getCitySphere(chunkX, chunkZ, provider);
        if (!sphere.isEnabled()) {
            return false;
        }
        float radius = sphere.getRadius();
        BlockPos cc = sphere.getCenterPos();
        double sqradiusOffset = (radius-2) * (radius-2);
        int cx = cc.getX();
        int cz = cc.getZ();
        if (squaredDistance(cx, cz, chunkX*16, chunkZ*16) > sqradiusOffset) {
            return false;
        }
        if (squaredDistance(cx, cz, chunkX*16+15, chunkZ*16) > sqradiusOffset) {
            return false;
        }
        if (squaredDistance(cx, cz, chunkX*16, chunkZ*16+15) > sqradiusOffset) {
            return false;
        }
        if (squaredDistance(cx, cz, chunkX*16+15, chunkZ*16+15) > sqradiusOffset) {
            return false;
        }
        return true;
    }

    /**
     * Return true if a given chunk is partially or fully enclosed in a city sphere
     */
    public static boolean intersectsWithCitySphere(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        CitySphere sphere = CitySphere.getCitySphere(chunkX, chunkZ, provider);
        if (!sphere.isEnabled()) {
            return false;
        }
        float radius = sphere.getRadius();
        BlockPos cc = sphere.getCenterPos();
        return intersectChunkWithSphere(chunkX, chunkZ, radius, cc);
    }

    private static boolean intersectChunkWithSphere(int chunkX, int chunkZ, float radius, BlockPos cc) {
        double sqradiusOffset = radius * radius;
        int cx = cc.getX();
        int cz = cc.getZ();
        if (squaredDistance(cx, cz, chunkX*16, chunkZ*16) <= sqradiusOffset) {
            return true;
        }
        if (squaredDistance(cx, cz, chunkX*16+15, chunkZ*16) <= sqradiusOffset) {
            return true;
        }
        if (squaredDistance(cx, cz, chunkX*16, chunkZ*16+15) <= sqradiusOffset) {
            return true;
        }
        if (squaredDistance(cx, cz, chunkX*16+15, chunkZ*16+15) <= sqradiusOffset) {
            return true;
        }
        return false;
    }


    /**
     * Return true if a given chunk is partially enclosed in a city sphere (i.e. on the sphere border)
     */
    public static boolean onCitySphereBorder(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        CitySphere sphere = CitySphere.getCitySphere(chunkX, chunkZ, provider);
        if (!sphere.isEnabled()) {
            return false;
        }
        float radius = sphere.getRadius();
        BlockPos cc = sphere.getCenterPos();
        double sqradiusOffset = radius * radius;
        int cx = cc.getX();
        int cz = cc.getZ();
        int cnt = 0;
        if (squaredDistance(cx, cz, chunkX*16, chunkZ*16) <= sqradiusOffset) {
            cnt++;
        }
        if (squaredDistance(cx, cz, chunkX*16+15, chunkZ*16) <= sqradiusOffset) {
            cnt++;
        }
        if (squaredDistance(cx, cz, chunkX*16, chunkZ*16+15) <= sqradiusOffset) {
            cnt++;
        }
        if (squaredDistance(cx, cz, chunkX*16+15, chunkZ*16+15) <= sqradiusOffset) {
            cnt++;
        }
        return cnt > 0 && cnt < 4;
    }



    public static double squaredDistance(int cx, int cz, int x, int z) {
        return (cx-x)*(cx-x) + (cz-z)*(cz-z);
    }

    /**
     * Return a city sphere for this city center chunk
     */
    private static CitySphere getSphereAtCenter(ChunkCoord center, LostCityChunkGenerator provider, @Nullable PredefinedSphere predef) {
        int chunkX = center.getChunkX();
        int chunkZ = center.getChunkZ();
        Random rand = new Random(provider.seed + chunkX * 961744153L + chunkZ * 837971201L);
        rand.nextFloat();
        rand.nextFloat();
        CitySphere citySphere;
        // This information is for city spheres. This information is only relevant
        // in the chunk representing the center of the city
        boolean enabled = rand.nextFloat() < provider.getProfile().CITYSPHERE_CHANCE;
        float radius = predef != null ? predef.getRadius() : getSphereRadius(center, provider, rand);
        BlockPos centerPosition = predef != null ? new BlockPos(predef.getCenterX(), 0, predef.getCenterZ()) : getSphereCenterPosition(center, provider, rand);
        citySphere = new CitySphere(center, radius, centerPosition, enabled);
        if (enabled) {
            citySphere.monorailNorthCandidate = rand.nextFloat() < provider.getProfile().CITYSPHERE_MONORAIL_CHANCE;
            citySphere.monorailSouthCandidate = rand.nextFloat() < provider.getProfile().CITYSPHERE_MONORAIL_CHANCE;
            citySphere.monorailWestCandidate = rand.nextFloat() < provider.getProfile().CITYSPHERE_MONORAIL_CHANCE;
            citySphere.monorailEastCandidate = rand.nextFloat() < provider.getProfile().CITYSPHERE_MONORAIL_CHANCE;
            if (predef != null && predef.getBiome() != null) {
                citySphere.biome = Biome.REGISTRY.getObject(new ResourceLocation(predef.getBiome()));
                if (citySphere.biome == null) {
                    LostCities.logger.warn("Could not find biome '" + predef.getBiome() + "'!");
                }
            }
        }
        return citySphere;
    }

    /**
     * Given a chunk coordinate return the nearest city sphere that affects this chunk. This can return city
     * spheres that are disabled so always test for that! If this returns EMPTY there is no sphere at all
     */
    @Nonnull
    public static CitySphere getCitySphere(int chunkX, int chunkZ, LostCityChunkGenerator provider) {
        ChunkCoord coord = new ChunkCoord(provider.dimensionId, chunkX, chunkZ);
        if (!citySphereCache.containsKey(coord)) {
            for (Map.Entry<ChunkCoord, PredefinedSphere> entry : predefinedSphereMap.entrySet()) {
                PredefinedSphere predef = entry.getValue();
                if (predef.getDimension() == provider.dimensionId) {
                    if (intersectChunkWithSphere(chunkX, chunkZ, predef.getRadius(), new BlockPos(predef.getCenterX(), 0, predef.getCenterZ()))) {
                        ChunkCoord center = new ChunkCoord(provider.dimensionId, predef.getChunkX(), predef.getChunkZ());
                        CitySphere sphere = getSphereAtCenter(center, provider, predef);
                        updateCache(coord, sphere);
                        return sphere;
                    }
                }
            }

            CitySphere sphere;
            if (provider.getProfile().CITYSPHERE_ONLY_PREDEFINED) {
                sphere = CitySphere.EMPTY;
            } else {
                int cx = (chunkX & ~0xf) + 8;
                int cz = (chunkZ & ~0xf) + 8;
                ChunkCoord center = new ChunkCoord(provider.dimensionId, cx, cz);
                sphere = getSphereAtCenter(center, provider, null);
            }
            updateCache(coord, sphere);
            return sphere;
        } else {
            return citySphereCache.get(coord);
        }
    }

    private static void updateCache(ChunkCoord coord, CitySphere sphere) {
        citySphereCache.put(coord, sphere);
        BlockPos centerPos = sphere.getCenterPos();
        int radius = (int) sphere.getRadius();
        if (radius < 0.0001f) {
            citySphereCache.put(sphere.center, sphere);
            return;
        }
        for (int cx = centerPos.getX() - radius-16 ; cx <= centerPos.getX() + radius+16 ; cx += 16) {
            for (int cz = centerPos.getZ() - radius-16 ; cz <= centerPos.getZ()+radius+16 ; cz += 16) {
                ChunkCoord cc = new ChunkCoord(sphere.getCenter().getDimension(), cx >> 4, cz >> 4);
                if (intersectChunkWithSphere(cc.getChunkX(), cc.getChunkZ(), radius, centerPos)) {
                    citySphereCache.put(cc, sphere);
                }
            }
        }
    }

    /**
     * Given a sphere center, return the actual position of the center
     */
    private static BlockPos getSphereCenterPosition(ChunkCoord center, LostCityChunkGenerator provider, Random rand) {
        int cx = center.getChunkX() * 16 + rand.nextInt(16) - 8;
        int cz = center.getChunkZ() * 16 + rand.nextInt(16) - 8;
        return new BlockPos(cx, 0, cz);
    }
}
