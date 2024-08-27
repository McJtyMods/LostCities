package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.worldgen.ChunkDriver;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.BiomeInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.CompiledPalette;
import mcjty.lostcities.worldgen.lost.cityassets.StuffObject;
import mcjty.lostcities.worldgen.lost.regassets.StuffSettingsRE;
import mcjty.lostcities.worldgen.lost.regassets.data.BlockMatcher;
import mcjty.lostcities.worldgen.lost.regassets.data.ResourceLocationMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class Stuff {

    public static void generateStuff(LostCityTerrainFeature feature, BuildingInfo info) {
        feature.rand.setSeed(info.coord.chunkX() * 2570174657L + info.coord.chunkZ() * 101754695981L);
        BiomeInfo biome = BiomeInfo.getBiomeInfo(feature.provider, info.coord);
        CompiledPalette palette = info.getCompiledPalette();
        for (String tag : info.getCityStyle().getStuffTags()) {
            List<StuffObject> stuffs = AssetRegistries.STUFF_BY_TAG.get(tag);
            if (stuffs != null) {
                for (StuffObject stuff : stuffs) {
                    StuffSettingsRE settings = stuff.getSettings();
                    Boolean inBuilding = settings.isInBuilding();
                    if (inBuilding != null && inBuilding == info.hasBuilding) {
                        ResourceLocationMatcher buildingMatcher = settings.getBuildingMatcher();
                        if (buildingMatcher.isAny() || buildingMatcher.test(info.buildingType.getId())) {
                            if (settings.getBiomeMatcher().test(biome.getMainBiome())) {
                                actuallyGenerateStuff(feature, info, settings, palette, inBuilding == Boolean.TRUE);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean testBlock(ChunkDriver driver, BlockMatcher matcher, int x, int y, int z) {
        if (matcher.isAny()) {
            return true;
        }
        return matcher.test(driver.getBlock(x, y, z));
    }

    private static void actuallyGenerateStuff(LostCityTerrainFeature feature, BuildingInfo info, StuffSettingsRE settings, CompiledPalette palette, boolean inBuilding) {
        ChunkDriver driver = feature.driver;
        WorldGenLevel level = info.provider.getWorld();
        int attempts = settings.getAttempts();
        Integer minheight = settings.getMinheight();
        Integer maxheight = settings.getMaxheight();
        if (minheight == null) {
            minheight = info.groundLevel;
            if (inBuilding && info.hasBuilding) {
                int lowestLevel = info.getCityGroundLevel() - info.cellars * LostCityTerrainFeature.FLOORHEIGHT;
                minheight = lowestLevel;
            }
        }
        if (maxheight == null) {
            maxheight = minheight + 20;
            if (inBuilding && info.hasBuilding) {
                maxheight = info.getCityGroundLevel() + info.getNumFloors() * LostCityTerrainFeature.FLOORHEIGHT + 10; // 10 margine above highest floor
            }
        }
        int mincount = settings.getMincount();
        int maxcount = settings.getMaxcount();
        RandomSource rand = feature.rand;
        int count = rand.nextInt(maxcount - mincount) + mincount;
        for (int j = 0; j < count; j++) {
            for (int i = 0; i < attempts; i++) {
                int x = rand.nextInt(16);
                int y = rand.nextInt(maxheight - minheight) + minheight;
                int z = rand.nextInt(16);
                String blocks = settings.getColumn();
                if (testBlock(driver, settings.getBlockMatcher(), x, y-1, z) && testBlock(driver, settings.getUpperBlockMatcher(), x, y + blocks.length(), z)) {
                    Boolean isSeesky = settings.isSeesky();
                    if (isSeesky == null || isSeesky == level.canSeeSky(info.getRelativePos(x, y, z))) {
                        // Iterate over all characters of the block
                        boolean ok = true;
                        for (int k = 0; k < blocks.length(); k++) {
                            if (driver.getBlock(x, y + k, z) != feature.air) {
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            driver.current(x, y, z);
                            for (int k = 0; k < blocks.length(); k++) {
                                BlockState block = palette.get(blocks.charAt(k));
                                driver.add(block);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
