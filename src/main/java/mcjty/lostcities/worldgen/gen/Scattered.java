package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.QualityRandom;
import mcjty.lostcities.worldgen.ChunkDriver;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.highway.HighwayInfo;
import mcjty.lostcities.worldgen.lost.*;
import mcjty.lostcities.worldgen.lost.cityassets.*;
import mcjty.lostcities.worldgen.lost.regassets.data.ScatteredReference;
import mcjty.lostcities.worldgen.lost.regassets.data.ScatteredSettings;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Scattered {

    private static final ConcurrentMap<ScatteredPlanKey, ScatteredPlan> PLANS = new ConcurrentHashMap<>();

    public static void cleanCache() {
        PLANS.clear();
    }

    public static boolean avoidScattered(LostCityTerrainFeature feature, BuildingInfo info) {
        if (BuildingInfo.isCityRaw(info.coord, feature.provider, info.profile)) {
            return true;
        }
        if (info.hasBridge(feature.provider)) {
            return true;
        }
        return Highway.hasHighway(info.coord, feature.provider, feature.profile);
    }

    public static void generateScattered(LostCityTerrainFeature feature, BuildingInfo info, ScatteredSettings scatteredSettings) {
        int chunkX = info.coord.chunkX();
        int chunkZ = info.coord.chunkZ();
        IDimensionInfo provider = feature.provider;

        // First normalize the coordinates to scatter area sized coordinates. Add a large amount to make sure the coordinates are positive
        int ax = (chunkX + 2000000) / scatteredSettings.getAreasize();
        int az = (chunkZ + 2000000) / scatteredSettings.getAreasize();
        ScatteredPlanKey key = new ScatteredPlanKey(provider.getType(), provider.getSeed(), ax, az);
        ScatteredPlan plan = PLANS.computeIfAbsent(key, ignored -> calculatePlan(feature, scatteredSettings, ax, az));

        if (!plan.valid() || chunkX < plan.tlChunkX() || chunkZ < plan.tlChunkZ()
                || chunkX >= plan.tlChunkX() + plan.width() || chunkZ >= plan.tlChunkZ() + plan.depth()) {
            return;
        }

        QualityRandom scatteredRandom = plan.createGenerationRandom();
        Building building;
        if (plan.multiBuilding() == null) {
            building = AssetRegistries.BUILDINGS.getOrThrow(provider.getWorld(), plan.singleBuildingName());
        } else {
            int relx = chunkX - plan.tlChunkX();
            int relz = chunkZ - plan.tlChunkZ();
            MultiBuildingCoordinate original = toOriginalCoordinate(relx, relz, plan.multiBuilding(), plan.transform());
            String buildingName = plan.multiBuilding().getBuilding(original.x(), original.z());
            building = AssetRegistries.BUILDINGS.getOrThrow(provider.getWorld(), buildingName);
        }
        generateScatteredBuilding(feature, info, building, scatteredRandom, plan.lowestLevel(),
                plan.scattered().getTerrainfix(), plan.scattered().getSupportpart(), plan.transform());
    }

    private static ScatteredPlan calculatePlan(LostCityTerrainFeature feature, ScatteredSettings scatteredSettings, int ax, int az) {
        IDimensionInfo provider = feature.provider;
        int areaSize = scatteredSettings.getAreasize();
        long randomSeed = provider.getSeed() + ax * 5564338337L + az * 25564337621L;
        QualityRandom scatteredRandom = new QualityRandom(randomSeed);
        if (scatteredRandom.nextFloat() >= scatteredSettings.getChance() * provider.getProfile().SCATTERED_CHANCE_MULTIPLIER) {
            return ScatteredPlan.INVALID;
        }

        int areaOriginX = ax * areaSize - 2000000;
        int areaOriginZ = az * areaSize - 2000000;
        ChunkCoord selectionCoord = new ChunkCoord(provider.getType(), areaOriginX, areaOriginZ);
        ScatteredSelection selection = selectRandomScattered(feature, selectionCoord, scatteredSettings, scatteredRandom);
        if (selection == null || selection.reference() == null) {
            return ScatteredPlan.INVALID;
        }

        ScatteredReference reference = selection.reference();
        ScatteredBuilding scattered = AssetRegistries.SCATTERED.getOrThrow(provider.getWorld(), reference.getName());
        MultiBuilding multiBuilding = scattered.getMultibuilding() == null ? null
                : AssetRegistries.MULTI_BUILDINGS.getOrThrow(provider.getWorld(), scattered.getMultibuilding());
        int originalWidth = multiBuilding == null ? 1 : multiBuilding.getDimX();
        int originalDepth = multiBuilding == null ? 1 : multiBuilding.getDimZ();
        if (originalWidth > areaSize || originalDepth > areaSize) {
            return ScatteredPlan.INVALID;
        }

        PlacementCandidate placement;
        int positionXBound = 0;
        int positionZBound = 0;
        int highwayCandidateCount = 0;
        if (reference.isNearHighway()) {
            List<PlacementCandidate> candidates = findHighwayPlacements(feature, reference, scattered,
                    areaOriginX, areaOriginZ, areaSize, originalWidth, originalDepth);
            if (candidates.isEmpty()) {
                return ScatteredPlan.INVALID;
            }
            highwayCandidateCount = candidates.size();
            placement = candidates.get(scatteredRandom.nextInt(highwayCandidateCount));
        } else {
            positionXBound = areaSize - originalWidth + 1;
            positionZBound = areaSize - originalDepth + 1;
            int tlChunkX = areaOriginX + scatteredRandom.nextInt(positionXBound);
            int tlChunkZ = areaOriginZ + scatteredRandom.nextInt(positionZBound);
            TerrainStats terrain = calculateTerrainStats(feature, reference, tlChunkX, tlChunkZ,
                    originalWidth, originalDepth);
            if (terrain == null) {
                return ScatteredPlan.INVALID;
            }
            placement = new PlacementCandidate(tlChunkX, tlChunkZ, originalWidth, originalDepth,
                    Transform.ROTATE_NONE, null, terrain);
        }

        String singleBuildingName = null;
        int singleBuildingCount = 0;
        if (multiBuilding == null) {
            List<String> buildings = scattered.getBuildings();
            if (buildings == null) {
                throw new RuntimeException("Missing buildings for scattered '" + reference.getName() + "'!");
            }
            singleBuildingCount = buildings.size();
            if (buildings.size() == 1) {
                singleBuildingName = buildings.get(0);
            } else {
                singleBuildingName = buildings.get(scatteredRandom.nextInt(buildings.size()));
            }
        }

        int lowestLevel;
        if (placement.highwayHeight() != null) {
            lowestLevel = placement.highwayHeight() + scattered.getHeightoffset();
        } else if (multiBuilding == null) {
            lowestLevel = handleScatteredTerrain(feature, scattered, placement.terrain().singleHeightmap());
            if (lowestLevel < -4000) {
                LostCityProfile profile = feature.provider.getProfile();
                if (profile.isCavern()) {
                    lowestLevel = profile.GROUNDLEVEL;
                } else {
                    lowestLevel = provider.getWorld().getMinBuildHeight() + 2;  // @todo is this right?
                }
            }
        } else {
            lowestLevel = handleScatteredTerrainMulti(feature, scattered, placement.terrain().minimum(),
                    placement.terrain().maximum(), placement.terrain().average());
        }
        return new ScatteredPlan(true, randomSeed, selection.randomBound(), positionXBound, positionZBound,
                highwayCandidateCount, placement.width(), placement.depth(), placement.tlChunkX(), placement.tlChunkZ(),
                lowestLevel, placement.transform(), singleBuildingCount, singleBuildingName, scattered, multiBuilding);
    }

    private static List<PlacementCandidate> findHighwayPlacements(LostCityTerrainFeature feature,
                                                                   ScatteredReference reference,
                                                                   ScatteredBuilding scattered,
                                                                   int areaOriginX, int areaOriginZ, int areaSize,
                                                                   int originalWidth, int originalDepth) {
        List<PlacementCandidate> candidates = new ArrayList<>();
        Transform[] transforms = scattered.isRotatable()
                ? new Transform[]{Transform.ROTATE_NONE, Transform.ROTATE_90, Transform.ROTATE_180, Transform.ROTATE_270}
                : new Transform[]{Transform.ROTATE_NONE};
        for (Transform transform : transforms) {
            int width = rotatesDimensions(transform) ? originalDepth : originalWidth;
            int depth = rotatesDimensions(transform) ? originalWidth : originalDepth;
            for (int x = areaOriginX; x <= areaOriginX + areaSize - width; x++) {
                for (int z = areaOriginZ; z <= areaOriginZ + areaSize - depth; z++) {
                    Integer highwayHeight = getConnectedHighwayHeight(feature, x, z, width, depth,
                            connectionDirection(transform));
                    if (highwayHeight == null) {
                        continue;
                    }
                    TerrainStats terrain = calculateTerrainStats(feature, reference, x, z, width, depth);
                    if (terrain != null) {
                        candidates.add(new PlacementCandidate(x, z, width, depth, transform, highwayHeight, terrain));
                    }
                }
            }
        }
        return candidates;
    }

    @Nullable
    private static Integer getConnectedHighwayHeight(LostCityTerrainFeature feature, int x, int z,
                                                     int width, int depth, Direction connection) {
        Integer commonHeight = null;
        int edgeLength = connection.getAxis() == Direction.Axis.Z ? width : depth;
        for (int i = 0; i < edgeLength; i++) {
            ChunkCoord highwayCoord = switch (connection) {
                case NORTH -> new ChunkCoord(feature.provider.getType(), x + i, z - 1);
                case SOUTH -> new ChunkCoord(feature.provider.getType(), x + i, z + depth);
                case WEST -> new ChunkCoord(feature.provider.getType(), x - 1, z + i);
                case EAST -> new ChunkCoord(feature.provider.getType(), x + width, z + i);
                default -> throw new IllegalStateException("Unsupported horizontal direction " + connection);
            };
            HighwayInfo highwayInfo = Highway.getHighwayInfo(highwayCoord, feature.provider, feature.profile);
            int level = connection.getAxis() == Direction.Axis.Z ? highwayInfo.xLevel() : highwayInfo.zLevel();
            if (level < 0) {
                return null;
            }
            BuildingInfo highwayBuildingInfo = BuildingInfo.getBuildingInfo(highwayCoord, feature.provider);
            if (highwayBuildingInfo.isTunnel(level)) {
                return null;
            }
            int height = highwayBuildingInfo.groundLevel + level * LostCityTerrainFeature.FLOORHEIGHT;
            if (commonHeight != null && commonHeight != height) {
                return null;
            }
            commonHeight = height;
        }
        return commonHeight;
    }

    @Nullable
    private static TerrainStats calculateTerrainStats(LostCityTerrainFeature feature, ScatteredReference reference,
                                                       int tlChunkX, int tlChunkZ, int width, int depth) {
        int minheight = Integer.MAX_VALUE;
        int maxheight = Integer.MIN_VALUE;
        int avgheight = 0;
        ChunkHeightmap singleHeightmap = null;
        for (int x = tlChunkX; x < tlChunkX + width; x++) {
            for (int z = tlChunkZ; z < tlChunkZ + depth; z++) {
                ChunkCoord coord = new ChunkCoord(feature.provider.getType(), x, z);
                if (!isValidScatterBiome(feature, reference, coord)) {
                    return null;
                }
                BuildingInfo tinfo = BuildingInfo.getBuildingInfo(coord, feature.provider);
                if (avoidScattered(feature, tinfo)) {
                    return null;
                }
                ChunkHeightmap heightmap = feature.getHeightmap(coord, feature.provider.getWorld());
                if (width == 1 && depth == 1) {
                    singleHeightmap = heightmap;
                }
                int height = heightmap.getHeight();
                heightmap.calculateAccurateHeight(feature.provider.getWorld(), x, z);
                if (!reference.isAllowVoid() && !(feature.profile.isDefault() || feature.profile.isCavern())
                        && height <= feature.provider.getWorld().getMinBuildHeight() + 3) {
                    return null;
                }
                minheight = Math.min(minheight, heightmap.getMinHeight());
                maxheight = Math.max(maxheight, heightmap.getMaxHeight());
                avgheight += height;
            }
        }
        if (reference.getMaxheightdiff() != null && maxheight - minheight > reference.getMaxheightdiff()) {
            return null;
        }
        return new TerrainStats(minheight, maxheight, avgheight / (width * depth), singleHeightmap);
    }

    private static boolean rotatesDimensions(Transform transform) {
        return transform == Transform.ROTATE_90 || transform == Transform.ROTATE_270;
    }

    private static Direction connectionDirection(Transform transform) {
        return switch (transform) {
            case ROTATE_NONE -> Direction.NORTH;
            case ROTATE_90 -> Direction.EAST;
            case ROTATE_180 -> Direction.SOUTH;
            case ROTATE_270 -> Direction.WEST;
            default -> throw new IllegalArgumentException("Scattered buildings only support rotations");
        };
    }

    private static MultiBuildingCoordinate toOriginalCoordinate(int x, int z, MultiBuilding multiBuilding,
                                                                Transform transform) {
        return switch (transform) {
            case ROTATE_NONE -> new MultiBuildingCoordinate(x, z);
            case ROTATE_90 -> new MultiBuildingCoordinate(z, multiBuilding.getDimZ() - 1 - x);
            case ROTATE_180 -> new MultiBuildingCoordinate(multiBuilding.getDimX() - 1 - x,
                    multiBuilding.getDimZ() - 1 - z);
            case ROTATE_270 -> new MultiBuildingCoordinate(multiBuilding.getDimX() - 1 - z, x);
            default -> throw new IllegalArgumentException("Scattered buildings only support rotations");
        };
    }

    @Nullable
    private static ScatteredSelection selectRandomScattered(LostCityTerrainFeature feature, ChunkCoord selectionCoord, ScatteredSettings scatteredSettings, Random rand) {
        List<ScatteredReference> list = scatteredSettings.getList();
        if (list.isEmpty()) {
            return null;
        }

        int totalweight = 0;
        List<ScatteredReference> filteredList = new ArrayList<>();
        for (ScatteredReference reference : list) {
            if (isValidScatterBiome(feature, reference, selectionCoord)) {
                totalweight += reference.getWeight();
                filteredList.add(reference);
            }
        }
        if (filteredList.isEmpty()) {
            return null;
        }

        int randomBound = totalweight + scatteredSettings.getWeightnone();
        int rndweight = rand.nextInt(randomBound);
        ScatteredReference reference = null;
        for (ScatteredReference scatteredReference : filteredList) {
            int weight = scatteredReference.getWeight();
            if (rndweight <= weight) {
                reference = scatteredReference;
                break;
            }
            rndweight -= weight;
        }
        return new ScatteredSelection(reference, randomBound);
    }

    private static boolean isValidScatterBiome(LostCityTerrainFeature feature, ScatteredReference reference, ChunkCoord coord) {
        if (reference.getBiomeMatcher() != null) {
            BiomeInfo biome = BiomeInfo.getBiomeInfo(feature.provider, coord);
            return reference.getBiomeMatcher().test(biome.getMainBiome());
        }
        return true;
    }

    private static void generateScatteredBuilding(LostCityTerrainFeature feature, BuildingInfo info, Building building,
                                                  Random rand, int lowestLevel,
                                                  ScatteredBuilding.TerrainFix terrainFix,
                                                  @Nullable String supportPartName, Transform transform) {
        IDimensionInfo provider = feature.provider;

        int height = lowestLevel;
        int floors;
        int minfloors = building.getMinFloors();
        if (minfloors <= 0) {
            minfloors = 1;
        }
        int maxfloors = building.getMaxFloors();
        if (maxfloors <= 0) {
            maxfloors = 1;
        }
        if (minfloors >= maxfloors) {
            floors = minfloors;
        } else {
            floors = minfloors + rand.nextInt(maxfloors - minfloors + 1);
        }
        // TODO top condition is wrong due to floor calculation being different
        String belowFloor = "<none>";
        for (int f = 0; f < floors; f++) {
            ConditionContext conditionContext = new ConditionContext(lowestLevel, f, 0, floors, "<none>", belowFloor, building.getName(), info.coord) {
                @Override
                public boolean isBuilding() {
                    return true;
                }

                @Override
                public boolean isSphere() {
                    return CitySphere.isInSphere(info.coord, info.getCenter(0), provider);
                }

                @Override
                public ResourceLocation getBiome() {
                    Holder<Biome> biome = provider.getWorld().getBiome(info.getCenter(0));
                    return biome.unwrap().map(ResourceKey::location, b -> provider.getWorld().registryAccess().registryOrThrow(Registries.BIOME).getKey(b));
                }
            };
            ChunkDriver driver = feature.getDriver();
            BlockState air = Blocks.AIR.defaultBlockState();
            BlockState liquid = feature.liquid;
            String randomPart = building.getRandomPart(rand, conditionContext);
            BuildingPart part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), randomPart);
            belowFloor = randomPart;
            randomPart = building.getRandomPart2(rand, conditionContext);
            BuildingPart part2 = AssetRegistries.PARTS.get(provider.getWorld(), randomPart);    // Null is legal

            if (f == 0) {
                switch (terrainFix) {
                    case NONE -> {
                    }
                    case CLEAR -> {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                feature.clearRange(info, x, z, lowestLevel, lowestLevel + 50, false);
                            }
                        }
                    }
                    case REPEATSLICE -> {
                        BuildingPart supportPart = supportPartName == null ? part
                                : AssetRegistries.PARTS.getOrThrow(provider.getWorld(), supportPartName);
                        CompiledPalette compiledPalette = feature.computePalette(info, supportPart);
                        for (int x = 0; x < supportPart.getXSize(); x++) {
                            for (int z = 0; z < supportPart.getZSize(); z++) {
                                char c = supportPart.getPaletteChar(x, 0, z);
                                if (c != ' ') {
                                    int rx = transform.rotateX(x, z);
                                    int rz = transform.rotateZ(x, z);
                                    int y = lowestLevel - 1;
                                    driver.current(rx, y, rz);
                                    BlockState b = driver.getBlock();
                                    while (b == air || b == liquid) {
                                        driver.block(compiledPalette.get(c));
                                        driver.decY();
                                        b = driver.getBlock();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            height = feature.generatePart(info, part, transform, 0, height, 0, LostCityTerrainFeature.HardAirSetting.AIR);
            if (part2 != null) {
                feature.generatePart(info, part2, transform, 0, height, 0, LostCityTerrainFeature.HardAirSetting.AIR);
            }
        }
    }

    private static int handleScatteredTerrain(LostCityTerrainFeature feature, ScatteredBuilding scattered, ChunkHeightmap heightmap) {
        int lowestLevel = switch (scattered.getTerrainheight()) {
            case LOWEST -> heightmap.getHeight();
            case AVERAGE -> heightmap.getHeight();
            case HIGHEST -> heightmap.getHeight();
            case OCEAN -> ((ServerChunkCache) feature.provider.getWorld().getChunkSource()).getGenerator().getSeaLevel();
        };
        lowestLevel += scattered.getHeightoffset();
        return lowestLevel;
    }

    private static int handleScatteredTerrainMulti(LostCityTerrainFeature feature, ScatteredBuilding scattered,
                                                   int minimum, int maximum, int average) {
        int lowestLevel = switch (scattered.getTerrainheight()) {
            case LOWEST -> minimum;
            case AVERAGE -> average;
            case HIGHEST -> maximum;
            case OCEAN -> ((ServerChunkCache) feature.provider.getWorld().getChunkSource()).getGenerator().getSeaLevel();
        };
        lowestLevel += scattered.getHeightoffset();
        return lowestLevel;
    }

    private record ScatteredPlanKey(ResourceKey<Level> dimension, long seed, int areaX, int areaZ) {
    }

    private record ScatteredSelection(@Nullable ScatteredReference reference, int randomBound) {
    }

    private record TerrainStats(int minimum, int maximum, int average,
                                @Nullable ChunkHeightmap singleHeightmap) {
    }

    private record PlacementCandidate(int tlChunkX, int tlChunkZ, int width, int depth, Transform transform,
                                      @Nullable Integer highwayHeight, TerrainStats terrain) {
    }

    private record MultiBuildingCoordinate(int x, int z) {
    }

    private record ScatteredPlan(boolean valid, long randomSeed, int selectionBound,
                                 int positionXBound, int positionZBound, int highwayCandidateCount,
                                 int width, int depth, int tlChunkX, int tlChunkZ, int lowestLevel,
                                 Transform transform,
                                 int singleBuildingCount, @Nullable String singleBuildingName,
                                 @Nullable ScatteredBuilding scattered, @Nullable MultiBuilding multiBuilding) {

        private static final ScatteredPlan INVALID = new ScatteredPlan(false, 0L, 0,
                0, 0, 0, 0, 0, 0, 0, 0, Transform.ROTATE_NONE,
                0, null, null, null);

        private QualityRandom createGenerationRandom() {
            QualityRandom random = new QualityRandom(randomSeed);
            random.nextFloat();
            random.nextInt(selectionBound);
            if (highwayCandidateCount > 0) {
                random.nextInt(highwayCandidateCount);
            } else {
                random.nextInt(positionXBound);
                random.nextInt(positionZBound);
            }
            if (singleBuildingCount > 1) {
                random.nextInt(singleBuildingCount);
            }
            return random;
        }
    }
}
