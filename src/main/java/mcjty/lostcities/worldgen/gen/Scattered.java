package mcjty.lostcities.worldgen.gen;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Scattered {
    public static boolean avoidScattered(LostCityTerrainFeature feature, BuildingInfo info) {
        if (info.isCity) {
            return true;
        }
        if (info.hasBridge(feature.provider)) {
            return true;
        }
        return Highway.hasHighway(info.coord, feature.provider, feature.profile);
    }

    public static void generateScattered(LostCityTerrainFeature feature, BuildingInfo info, ScatteredSettings scatteredSettings, ChunkHeightmap heightmap) {
        int chunkX = info.chunkX;
        int chunkZ = info.chunkZ;
        IDimensionInfo provider = feature.provider;

        // First normalize the coordinates to scatter area sized coordinates. Add a large amount to make sure the coordinates are positive
        int ax = (chunkX + 2000000) / scatteredSettings.getAreasize();
        int az = (chunkZ + 2000000) / scatteredSettings.getAreasize();

        QualityRandom scatteredRandom = new QualityRandom(provider.getSeed() + ax * 5564338337L + az * 25564337621L);

        if (scatteredRandom.nextFloat() < scatteredSettings.getChance()) {
            // No scattered structure in this area
            return;
        }

        // Find the right type of scattered asset for this area
        ScatteredReference reference = selectRandomScattered(feature, info, scatteredSettings, scatteredRandom);
        if (reference == null) {
            // Nothing matches
            return;
        }
        ScatteredBuilding scattered = AssetRegistries.SCATTERED.getOrThrow(provider.getWorld(), reference.getName());

        // Find the size of the scattered building
        int w;
        int h;
        MultiBuilding multiBuilding;
        if (scattered.getMultibuilding() != null) {
            multiBuilding = AssetRegistries.MULTI_BUILDINGS.getOrThrow(provider.getWorld(), scattered.getMultibuilding());
            w = multiBuilding.getDimX();
            h = multiBuilding.getDimZ();
        } else {
            w = h = 1;
            multiBuilding = null;
        }

        // Find the position of the building in the world
        int tlChunkX = (ax * scatteredSettings.getAreasize() - 2000000) + scatteredRandom.nextInt(scatteredSettings.getAreasize() - w + 1);
        int tlChunkZ = (az * scatteredSettings.getAreasize() - 2000000) + scatteredRandom.nextInt(scatteredSettings.getAreasize() - h + 1);

        if (chunkX < tlChunkX || chunkZ < tlChunkZ || chunkX >= (tlChunkX + w) || chunkZ >= (tlChunkZ + h)) {
            return;
        }

        // First test the conditions for all the relevant chunks (does this need to be cached?)
        int minheight = Integer.MAX_VALUE;
        int maxheight = Integer.MIN_VALUE;
        int avgheight = 0;
        for (int x = tlChunkX; x < tlChunkX + w; x++) {
            for (int z = tlChunkZ; z < tlChunkZ + h; z++) {
                ChunkCoord coord = new ChunkCoord(provider.getType(), x, z);
                if (!isValidScatterBiome(feature, reference, coord)) {
                    return;
                }
                BuildingInfo tinfo = BuildingInfo.getBuildingInfo(coord, provider);
                if (avoidScattered(feature, tinfo)) {
                    return;
                }
                ChunkHeightmap hm = feature.getHeightmap(coord, provider.getWorld());
                if (!reference.isAllowVoid()) {
                    if (!(feature.profile.isDefault() || feature.profile.isCavern())) {
                        // We are in a world that can have void chunks. Check if this chunk is a void chunk
                        if (hm.getHeight() <= feature.provider.getWorld().getMinBuildHeight() + 3) {
                            return;
                        }
                    }
                }
                minheight = Math.min(minheight, hm.getHeight());
                maxheight = Math.max(maxheight, hm.getHeight());
                avgheight += hm.getHeight();
            }
        }
        // Check the height difference
        if (reference.getMaxheightdiff() != null) {
            int diff = maxheight - minheight;
            if (diff > reference.getMaxheightdiff()) {
                return;
            }
        }

        avgheight /= w * h;

        // We need to generate a part of the building
        if (multiBuilding == null) {
            // A single building
            List<String> buildings = scattered.getBuildings();
            if (buildings == null) {
                throw new RuntimeException("Missing buildings for scattered '" + reference.getName() + "'!");
            }
            String buildingName;
            if (buildings.size() == 1) {
                buildingName = buildings.get(0);
            } else {
                buildingName = buildings.get(scatteredRandom.nextInt(buildings.size()));
            }
            Building building = AssetRegistries.BUILDINGS.getOrThrow(provider.getWorld(), buildingName);
            int lowestLevel = handleScatteredTerrain(feature, scattered, heightmap);
            generateScatteredBuilding(feature, info, building, scatteredRandom, lowestLevel, scattered.getTerrainfix());
        } else {
            int lowestLevel = handleScatteredTerrainMulti(feature, scattered, minheight, maxheight, avgheight);
            int relx = chunkX - tlChunkX;
            int relz = chunkZ - tlChunkZ;
            String buildingName = multiBuilding.getBuilding(relx, relz);
            Building building = AssetRegistries.BUILDINGS.getOrThrow(provider.getWorld(), buildingName);
            generateScatteredBuilding(feature, info, building, scatteredRandom, lowestLevel, scattered.getTerrainfix());
        }
    }

    @Nullable
    private static ScatteredReference selectRandomScattered(LostCityTerrainFeature feature, BuildingInfo info, ScatteredSettings scatteredSettings, Random rand) {
        List<ScatteredReference> list = scatteredSettings.getList();
        if (list.isEmpty()) {
            return null;
        }

        int totalweight = 0;
        List<ScatteredReference> filteredList = new ArrayList<>();
        for (ScatteredReference reference : list) {
            if (isValidScatterBiome(feature, reference, info.coord)) {
                totalweight += reference.getWeight();
                filteredList.add(reference);
            }
        }
        if (filteredList.isEmpty()) {
            return null;
        }

        int rndweight = rand.nextInt(totalweight + scatteredSettings.getWeightnone());
        ScatteredReference reference = null;
        for (ScatteredReference scatteredReference : filteredList) {
            int weight = scatteredReference.getWeight();
            if (rndweight <= weight) {
                reference = scatteredReference;
                break;
            }
            rndweight -= weight;
        }
        return reference;
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
        int chunkX = info.chunkX;
        int chunkZ = info.chunkZ;

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
        for (int f = 0; f < floors; f++) {
            ConditionContext conditionContext = new ConditionContext(lowestLevel, f, 0, floors, "<none>", building.getName(),
                    chunkX, chunkZ) {
                @Override
                public boolean isBuilding() {
                    return true;
                }

                @Override
                public boolean isSphere() {
                    return CitySphere.isInSphere(info.coord, new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8), provider);
                }

                @Override
                public ResourceLocation getBiome() {
                    Holder<Biome> biome = provider.getWorld().getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
                    return biome.unwrap().map(ResourceKey::location, b -> provider.getWorld().registryAccess().registryOrThrow(Registries.BIOME).getKey(b));
                }
            };
            ChunkDriver driver = feature.driver;
            BlockState air = Blocks.AIR.defaultBlockState();
            BlockState liquid = feature.liquid;
            String randomPart = building.getRandomPart(rand, conditionContext);
            BuildingPart part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), randomPart);
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

            height += feature.generatePart(info, part, Transform.ROTATE_NONE, 0, height, 0, LostCityTerrainFeature.HardAirSetting.AIR);
            if (part2 != null) {
                feature.generatePart(info, part2, Transform.ROTATE_NONE, 0, height, 0, LostCityTerrainFeature.HardAirSetting.AIR);
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

    private static int handleScatteredTerrainMulti(LostCityTerrainFeature feature, ScatteredBuilding scattered, int minimum, int maximum, int average) {
        int lowestLevel = switch (scattered.getTerrainheight()) {
            case LOWEST -> minimum;
            case AVERAGE -> maximum;
            case HIGHEST -> average;
            case OCEAN -> ((ServerChunkCache) feature.provider.getWorld().getChunkSource()).getGenerator().getSeaLevel();
        };
        lowestLevel += scattered.getHeightoffset();
        return lowestLevel;
    }
}
