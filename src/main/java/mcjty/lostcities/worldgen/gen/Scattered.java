package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.QualityRandom;
import mcjty.lostcities.worldgen.ChunkDriver;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.*;
import mcjty.lostcities.worldgen.lost.cityassets.*;
import mcjty.lostcities.worldgen.lost.regassets.data.ScatteredReference;
import mcjty.lostcities.worldgen.lost.regassets.data.ScatteredSettings;
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
            String buildingName = plan.multiBuilding().getBuilding(relx, relz);
            building = AssetRegistries.BUILDINGS.getOrThrow(provider.getWorld(), buildingName);
        }
        generateScatteredBuilding(feature, info, building, scatteredRandom, plan.lowestLevel(), plan.scattered().getTerrainfix());
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
        int width = multiBuilding == null ? 1 : multiBuilding.getDimX();
        int depth = multiBuilding == null ? 1 : multiBuilding.getDimZ();
        if (width > areaSize || depth > areaSize) {
            return ScatteredPlan.INVALID;
        }

        int tlChunkX = areaOriginX + scatteredRandom.nextInt(areaSize - width + 1);
        int tlChunkZ = areaOriginZ + scatteredRandom.nextInt(areaSize - depth + 1);
        int minheight = Integer.MAX_VALUE;
        int maxheight = Integer.MIN_VALUE;
        int avgheight = 0;
        ChunkHeightmap singleHeightmap = null;
        for (int x = tlChunkX; x < tlChunkX + width; x++) {
            for (int z = tlChunkZ; z < tlChunkZ + depth; z++) {
                ChunkCoord coord = new ChunkCoord(provider.getType(), x, z);
                if (!isValidScatterBiome(feature, reference, coord)) {
                    return ScatteredPlan.INVALID;
                }
                BuildingInfo tinfo = BuildingInfo.getBuildingInfo(coord, provider);
                if (avoidScattered(feature, tinfo)) {
                    return ScatteredPlan.INVALID;
                }
                if (reference.isNearHighway()) {
                    if (!Highway.hasHighway(coord.east(), provider, feature.profile) &&
                            !Highway.hasHighway(coord.west(), provider, feature.profile) &&
                            !Highway.hasHighway(coord.north(), provider, feature.profile) &&
                            !Highway.hasHighway(coord.south(), provider, feature.profile)) {
                        return ScatteredPlan.INVALID;
                    }
                }
                ChunkHeightmap hm = feature.getHeightmap(coord, provider.getWorld());
                if (width == 1 && depth == 1) {
                    singleHeightmap = hm;
                }
                int height = hm.getHeight();
                hm.calculateAccurateHeight(provider.getWorld(), x, z);
                if (!reference.isAllowVoid()) {
                    if (!(feature.profile.isDefault() || feature.profile.isCavern())) {
                        // We are in a world that can have void chunks. Check if this chunk is a void chunk
                        if (height <= feature.provider.getWorld().getMinBuildHeight() + 3) {
                            return ScatteredPlan.INVALID;
                        }
                    }
                }
                minheight = Math.min(minheight, hm.getMinHeight());
                maxheight = Math.max(maxheight, hm.getMaxHeight());
                avgheight += height;
            }
        }
        // Check the height difference
        if (reference.getMaxheightdiff() != null) {
            int diff = maxheight - minheight;
            if (diff > reference.getMaxheightdiff()) {
                return ScatteredPlan.INVALID;
            }
        }

        avgheight /= width * depth;
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
        if (multiBuilding == null) {
            lowestLevel = handleScatteredTerrain(feature, scattered, selectionCoord, singleHeightmap);
            if (lowestLevel < -4000) {
                LostCityProfile profile = feature.provider.getProfile();
                if (profile.isCavern()) {
                    lowestLevel = profile.GROUNDLEVEL;
                } else {
                    lowestLevel = provider.getWorld().getMinBuildHeight() + 2;  // @todo is this right?
                }
            }
        } else {
            lowestLevel = handleScatteredTerrainMulti(feature, scattered, selectionCoord, minheight, maxheight, avgheight);
        }
        return new ScatteredPlan(true, randomSeed, selection.randomBound(), areaSize, width, depth, tlChunkX, tlChunkZ,
                lowestLevel, singleBuildingCount, singleBuildingName, scattered, multiBuilding);
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

    private static void generateScatteredBuilding(LostCityTerrainFeature feature, BuildingInfo info, Building building, Random rand, int lowestLevel, ScatteredBuilding.TerrainFix terrainFix) {
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
                        CompiledPalette compiledPalette = feature.computePalette(info, part);
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                char c = part.getPaletteChar(x, 0, z);
                                if (c != ' ') {
                                    int y = lowestLevel - 1;
                                    driver.current(x, y, z);
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

            height = feature.generatePart(info, part, Transform.ROTATE_NONE, 0, height, 0, LostCityTerrainFeature.HardAirSetting.AIR);
            if (part2 != null) {
                feature.generatePart(info, part2, Transform.ROTATE_NONE, 0, height, 0, LostCityTerrainFeature.HardAirSetting.AIR);
            }
        }
    }

    private static int handleScatteredTerrain(LostCityTerrainFeature feature, ScatteredBuilding scattered, ChunkCoord coord, ChunkHeightmap heightmap) {
        int lowestLevel = switch (scattered.getTerrainheight()) {
            case LOWEST -> heightmap.getHeight();
            case AVERAGE -> heightmap.getHeight();
            case HIGHEST -> heightmap.getHeight();
            case OCEAN -> ((ServerChunkCache) feature.provider.getWorld().getChunkSource()).getGenerator().getSeaLevel();
        };
        lowestLevel += scattered.getHeightoffset();
        return lowestLevel;
    }

    private static int handleScatteredTerrainMulti(LostCityTerrainFeature feature, ScatteredBuilding scattered, ChunkCoord coord, int minimum, int maximum, int average) {
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

    private record ScatteredPlan(boolean valid, long randomSeed, int selectionBound, int areaSize,
                                 int width, int depth, int tlChunkX, int tlChunkZ, int lowestLevel,
                                 int singleBuildingCount, @Nullable String singleBuildingName,
                                 @Nullable ScatteredBuilding scattered, @Nullable MultiBuilding multiBuilding) {

        private static final ScatteredPlan INVALID = new ScatteredPlan(false, 0L, 0, 0,
                0, 0, 0, 0, 0, 0, null, null, null);

        private QualityRandom createGenerationRandom() {
            QualityRandom random = new QualityRandom(randomSeed);
            random.nextFloat();
            random.nextInt(selectionBound);
            random.nextInt(areaSize - width + 1);
            random.nextInt(areaSize - depth + 1);
            if (singleBuildingCount > 1) {
                random.nextInt(singleBuildingCount);
            }
            return random;
        }
    }
}
