package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.api.ILostSphere;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.CityStyle;
import mcjty.lostcities.worldgen.lost.cityassets.PredefinedCity;
import mcjty.lostcities.worldgen.lost.cityassets.PredefinedSphere;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CitySphere implements ILostSphere {

    private static final Map<ChunkCoord, CitySphere> CITY_SPHERE_CACHE = new HashMap<>();

    public static final CitySphere EMPTY = new CitySphere(new ChunkCoord(Level.OVERWORLD, 0, 0), 0.0f, new BlockPos(0, 0, 0), false);

    private final ChunkCoord center;
    private final BlockPos centerPos;
    private final float radius;
    private final boolean enabled;

    private boolean monorailNorthCandidate;
    private boolean monorailSouthCandidate;
    private boolean monorailWestCandidate;
    private boolean monorailEastCandidate;

    private BlockState glassBlock = Blocks.AIR.defaultBlockState();
    private BlockState baseBlock = Blocks.AIR.defaultBlockState();
    private BlockState sideBlock = Blocks.AIR.defaultBlockState();

    private CitySphere(ChunkCoord center, float radius, BlockPos centerPos, boolean enabled) {
        this.enabled = enabled;
        this.center = center;
        this.radius = radius;
        this.centerPos = centerPos;
    }

    public static void initSphere(CitySphere sphere, IDimensionInfo provider) {
        if (sphere.getBaseBlock() != Blocks.AIR.defaultBlockState()) {
            return;
        }

        ChunkCoord center = sphere.getCenter();
        BuildingInfo info = BuildingInfo.getBuildingInfo(center, provider);
        CityStyle cs = info.getCityStyle();

        Random rand = new Random(info.provider.getSeed() + center.chunkX() * 837971201L + center.chunkZ() * 961744153L);

        BlockState glass = info.getCompiledPalette().get(cs.getSphereGlassBlock(), rand);
        BlockState base = info.getCompiledPalette().get(cs.getSphereBlock(), rand);
        BlockState side = info.getCompiledPalette().get(cs.getSphereSideBlock(), rand);
        sphere.setBlocks(glass, base, side);
    }

    public static boolean isInSphere(ChunkCoord coord, BlockPos pos, IDimensionInfo provider) {
        boolean sphere = false;
        if (provider.getProfile().isSpace() || provider.getProfile().isSpheres()) {
            CitySphere citySphere = getCitySphere(coord, provider);
            if (citySphere.isEnabled()) {
                double sqdist = squaredDistance(citySphere.getCenterPos().getX(), citySphere.getCenterPos().getZ(), pos.getX(), pos.getZ());
                if (sqdist <= citySphere.getRadius() * citySphere.getRadius()) {
                    sphere = true;
                }
            }
        }
        return sphere;
    }

    /**
     * Given a chunk coordinate return the relative distance (number between 0 and 1) for the nearest city sphere.
     * This only works for space type worlds!
     */
    public static float getRelativeDistanceToCityCenter(ChunkCoord coord, IDimensionInfo provider) {
        CitySphere sphere = getCitySphere(coord, provider);
        BlockPos centerPos = sphere.getCenterPos();
        float radius = sphere.getRadius();
        int cx = coord.chunkX()*16+8;
        int cz = coord.chunkZ()*16+8;
        int sqdist = (cx - centerPos.getX()) * (cx - centerPos.getX()) + (cz - centerPos.getZ()) * (cz - centerPos.getZ());
        return (float) (Math.sqrt(sqdist) / radius);
    }

    private static boolean hasNonStationMonoRail(ChunkCoord coord, IDimensionInfo provider) {
        if (!fullyInsideCitySpere(coord, provider)) {
            return hasHorizontalMonorail(coord, provider) || hasVerticalMonorail(coord, provider);
        }
        return false;
    }

    public static boolean hasMonorailStation(ChunkCoord coord, IDimensionInfo provider) {
        if (fullyInsideCitySpere(coord, provider)) {
            // If there is a non enclosed monorail nearby we generate a station
            return hasNonStationMonoRail(coord.west(), provider) ||
                    hasNonStationMonoRail(coord.east(), provider) ||
                    hasNonStationMonoRail(coord.north(), provider) ||
                    hasNonStationMonoRail(coord.south(), provider);
        }
        return false;
    }

    @Override
    public ChunkCoord getCenter() {
        return center;
    }

    @Override
    public BlockPos getCenterPos() {
        return centerPos;
    }

    @Override
    public float getRadius() {
        return radius;
    }

    public void setBlocks(BlockState glassBlock, BlockState baseBlock, BlockState sideBlock) {
        this.glassBlock = glassBlock;
        this.baseBlock = baseBlock;
        this.sideBlock = sideBlock;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public BlockState getGlassBlock() {
        return glassBlock;
    }

    public BlockState getBaseBlock() {
        return baseBlock;
    }

    public BlockState getSideBlock() {
        return sideBlock;
    }

    public static void cleanCache() {
        CITY_SPHERE_CACHE.clear();
    }

    /**
     * Return true if there is a horizontal monorail here. This is the case if this chunk is on a city center multiple
     * (i.e. multiple of 16) and if there are cities left and right that both want a monorail in the correct direction
     */
    public static boolean hasHorizontalMonorail(ChunkCoord coord, IDimensionInfo provider) {
        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
        if ((chunkZ & 0xf) == 8) {
            // There is a city center on this vertical chunk coordinate
            // Find the first city on the right (with a limit)
            // @todo optimize this by caching the result of this scan?
            boolean result = false;
            for (int cx = chunkX+1 ; cx < chunkX+64 ; cx++) {
                if ((cx & 0xf) == 8) {
                    // This could be a city center
                    ChunkCoord c = new ChunkCoord(provider.getType(), cx, chunkZ);
                    CitySphere sphere = getCitySphere(c, provider);
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
                    ChunkCoord c = new ChunkCoord(provider.getType(), cx, chunkZ);
                    CitySphere sphere = getCitySphere(c, provider);
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

    public static boolean hasVerticalMonorail(ChunkCoord coord, IDimensionInfo provider) {
        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
        if ((chunkX & 0xf) == 8) {
            boolean result = false;
            for (int cz = chunkZ+1 ; cz < chunkZ+64 ; cz++) {
                if ((cz & 0xf) == 8) {
                    // This could be a city center
                    ChunkCoord c = new ChunkCoord(provider.getType(), chunkX, cz);
                    CitySphere sphere = getCitySphere(c, provider);
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
                    ChunkCoord c = new ChunkCoord(provider.getType(), chunkX, cz);
                    CitySphere sphere = getCitySphere(c, provider);
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
    private static float getSphereRadius(ChunkCoord center, IDimensionInfo provider, Random rand) {
        PredefinedCity city = City.getPredefinedCity(center);
        LostCityProfile profile = provider.getProfile();
        if (city != null) {
            return city.getRadius() * profile.CITYSPHERE_FACTOR;
        }
        return profile.CITY_MINRADIUS + rand.nextInt(profile.CITY_MAXRADIUS - profile.CITY_MINRADIUS) * profile.CITYSPHERE_FACTOR;
    }


    /**
     * Return true if a given chunk is fully enclosed in a city sphere
     */
    public static boolean fullyInsideCitySpere(ChunkCoord coord, IDimensionInfo provider) {
        CitySphere sphere = getCitySphere(coord, provider);
        if (!sphere.isEnabled()) {
            return false;
        }
        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
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
    public static boolean intersectsWithCitySphere(ChunkCoord coord, IDimensionInfo provider) {
        CitySphere sphere = getCitySphere(coord, provider);
        if (!sphere.isEnabled()) {
            return false;
        }
        float radius = sphere.getRadius();
        BlockPos cc = sphere.getCenterPos();
        return intersectChunkWithSphere(coord.chunkX(), coord.chunkZ(), radius, cc);
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
    public static boolean onCitySphereBorder(ChunkCoord coord, IDimensionInfo provider) {
        CitySphere sphere = getCitySphere(coord, provider);
        if (!sphere.isEnabled()) {
            return false;
        }
        int chunkX = coord.chunkX();
        int chunkZ = coord.chunkZ();
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
    private static CitySphere getSphereAtCenter(ChunkCoord center, IDimensionInfo provider, @Nullable PredefinedSphere predef) {
        int chunkX = center.chunkX();
        int chunkZ = center.chunkZ();
        Random rand = new Random(provider.getSeed() + chunkX * 961744153L + chunkZ * 837971201L);
        CitySphere citySphere;
        // This information is for city spheres. This information is only relevant
        // in the chunk representing the center of the city
        LostCityProfile profile = provider.getProfile();
        boolean enabled = predef != null || rand.nextFloat() < profile.CITYSPHERE_CHANCE;
        float radius = predef != null ? predef.getRadius() : getSphereRadius(center, provider, rand);
        BlockPos centerPosition = predef != null ? new BlockPos(predef.getCenterX(), profile.GROUNDLEVEL, predef.getCenterZ()) : getSphereCenterPosition(center, provider, rand);
        citySphere = new CitySphere(center, radius, centerPosition, enabled);
        if (enabled) {
            citySphere.monorailNorthCandidate = rand.nextFloat() < profile.CITYSPHERE_MONORAIL_CHANCE;
            citySphere.monorailSouthCandidate = rand.nextFloat() < profile.CITYSPHERE_MONORAIL_CHANCE;
            citySphere.monorailWestCandidate = rand.nextFloat() < profile.CITYSPHERE_MONORAIL_CHANCE;
            citySphere.monorailEastCandidate = rand.nextFloat() < profile.CITYSPHERE_MONORAIL_CHANCE;
        }
        return citySphere;
    }

    public static boolean isCitySphereCenter(ChunkCoord coord, IDimensionInfo provider) {
        CitySphere sphere = getCitySphere(coord, provider);
        return sphere.isEnabled() && sphere.getCenter().chunkX() == coord.chunkX() && sphere.getCenter().chunkZ() == coord.chunkZ();
    }

    /**
     * Given a chunk coordinate return the nearest city sphere that affects this chunk. This can return city
     * spheres that are disabled so always test for that! If this returns EMPTY there is no sphere at all
     */
    @Nonnull
    public static synchronized CitySphere getCitySphere(ChunkCoord coord, IDimensionInfo provider) {
        if (!CITY_SPHERE_CACHE.containsKey(coord)) {
            for (PredefinedSphere predef : AssetRegistries.PREDEFINED_SPHERES.getIterable()) {
                if (predef.getDimension() == provider.getType()) {
                    if (intersectChunkWithSphere(coord.chunkX(), coord.chunkZ(), predef.getRadius(), new BlockPos(predef.getCenterX(), 0, predef.getCenterZ()))) {
                        ChunkCoord center = new ChunkCoord(provider.getType(), predef.getChunkX(), predef.getChunkZ());
                        CitySphere sphere = getSphereAtCenter(center, provider, predef);
                        updateCache(coord, sphere);
                        return sphere;
                    }
                }
            }

            CitySphere sphere;
            if (provider.getProfile().CITYSPHERE_ONLY_PREDEFINED) {
                sphere = EMPTY;
            } else {
                int cx = (coord.chunkX() & ~0xf) + 8;
                int cz = (coord.chunkZ() & ~0xf) + 8;
                ChunkCoord center = new ChunkCoord(provider.getType(), cx, cz);
                sphere = getSphereAtCenter(center, provider, null);
            }
            updateCache(coord, sphere);
            return sphere;
        } else {
            return CITY_SPHERE_CACHE.get(coord);
        }
    }

    private static void updateCache(ChunkCoord coord, CitySphere sphere) {
        CITY_SPHERE_CACHE.put(coord, sphere);
        BlockPos centerPos = sphere.getCenterPos();
        int radius = (int) sphere.getRadius();
        if (radius < 0.0001f) {
            CITY_SPHERE_CACHE.put(sphere.center, sphere);
            return;
        }
        for (int cx = centerPos.getX() - radius-16 ; cx <= centerPos.getX() + radius+16 ; cx += 16) {
            for (int cz = centerPos.getZ() - radius-16 ; cz <= centerPos.getZ()+radius+16 ; cz += 16) {
                ChunkCoord cc = new ChunkCoord(sphere.getCenter().dimension(), cx >> 4, cz >> 4);
                if (intersectChunkWithSphere(cc.chunkX(), cc.chunkZ(), radius, centerPos)) {
                    CITY_SPHERE_CACHE.put(cc, sphere);
                }
            }
        }
    }

    /**
     * Given a sphere center, return the actual position of the center
     */
    private static BlockPos getSphereCenterPosition(ChunkCoord center, IDimensionInfo provider, Random rand) {
        int cx = (center.chunkX() << 4) + rand.nextInt(16) - 8;
        int cz = (center.chunkZ() << 4) + rand.nextInt(16) - 8;
        return new BlockPos(cx, provider.getProfile().GROUNDLEVEL, cz);
    }
}
