package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.worldgen.ChunkDriver;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.Orientation;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.cityassets.CompiledPalette;
import mcjty.lostcities.worldgen.lost.cityassets.Palette;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Bridges {

    public static void generateBridges(LostCityTerrainFeature feature, BuildingInfo info) {
        if (info.getHighwayXLevel() == 0 || info.getHighwayZLevel() == 0) {
            // If there is a highway at level 0 we cannot generate bridge parts. If there
            // is no highway or a highway at level 1 then bridge sections can generate just fine
            return;
        }
        BuildingPart bt = info.hasXBridge(info.provider);
        if (bt != null) {
            generateBridge(feature, info, bt, Orientation.X);
        } else {
            bt = info.hasZBridge(info.provider);
            if (bt != null) {
                generateBridge(feature, info, bt, Orientation.Z);
            }
        }
    }

    private static void generateBridge(LostCityTerrainFeature feature, BuildingInfo info, BuildingPart bt, Orientation orientation) {
        CompiledPalette compiledPalette = feature.computePalette(info, bt);
        ChunkDriver driver = feature.driver;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                driver.current(x, info.profile.GROUNDLEVEL + 1, z);
                int l = 0;
                while (l < bt.getSliceCount()) {
                    Character c = orientation == Orientation.X ? bt.getPaletteChar(x, l, z) : bt.getPaletteChar(z, l, x); // @todo general rotation system?
                    BlockState b = compiledPalette.get(c);
                    Palette.Info inf = compiledPalette.getInfo(c);
                    if (inf != null) {
                        if (inf.isTorch()) {
                            if (info.profile.GENERATE_LIGHTING) {
                                info.addTorchTodo(driver.getCurrentCopy());
                            } else {
                                b = Blocks.AIR.defaultBlockState();        // No torch!
                            }
                        }
                    }
                    driver.add(b);
                    l++;
                }
            }
        }

        Character support = bt.getMetaChar(ILostCities.META_SUPPORT);
        if (info.profile.BRIDGE_SUPPORTS && support != null) {
            BlockState sup = compiledPalette.get(support);
            BuildingInfo minDir = orientation.getMinDir().get(info);
            BuildingInfo maxDir = orientation.getMaxDir().get(info);
            if (minDir.hasBridge(info.provider, orientation) != null && maxDir.hasBridge(info.provider, orientation) != null) {
                // Needs support
                for (int y = info.waterLevel - 10; y <= info.groundLevel; y++) {
                    driver.current(7, y, 7).block(sup);
                    driver.current(7, y, 8).block(sup);
                    driver.current(8, y, 7).block(sup);
                    driver.current(8, y, 8).block(sup);
                }
            }
            if (minDir.hasBridge(info.provider, orientation) == null) {
                // Connection to the side section
                if (orientation == Orientation.X) {
                    int x = 0;
                    driver.current(x, info.profile.GROUNDLEVEL, 6);
                    for (int z = 6; z <= 9; z++) {
                        driver.block(sup).incZ();
                    }
                } else {
                    int z = 0;
                    driver.current(6, info.profile.GROUNDLEVEL, z);
                    for (int x = 6; x <= 9; x++) {
                        driver.block(sup).incX();
                    }
                }
            }
            if (maxDir.hasBridge(info.provider, orientation) == null) {
                // Connection to the side section
                if (orientation == Orientation.X) {
                    int x = 15;
                    driver.current(x, info.profile.GROUNDLEVEL, 6);
                    for (int z = 6; z <= 9; z++) {
                        driver.block(sup).incZ();
                    }
                } else {
                    int z = 15;
                    driver.current(6, info.profile.GROUNDLEVEL, z);
                    for (int x = 6; x <= 9; x++) {
                        driver.block(sup).incX();
                    }
                }
            }
        }
    }
}
