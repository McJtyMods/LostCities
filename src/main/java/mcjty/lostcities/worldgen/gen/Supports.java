package mcjty.lostcities.worldgen.gen;

import mcjty.lostcities.worldgen.ChunkDriver;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.Transform;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.cityassets.CompiledPalette;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class Supports {

    private Supports() {
    }

    /**
     * Repeat a support part down from {@code topY}. Every column stops independently at the
     * first non-empty world block, so terrain under one column does not truncate the others.
     */
    static void generatePart(LostCityTerrainFeature feature, BuildingInfo info, BuildingPart part,
                             Transform transform, int topY) {
        int sliceCount = part.getSliceCount();
        if (sliceCount == 0) {
            throw new RuntimeException("Support part '" + part.getName() + "' has no slices!");
        }

        ChunkDriver driver = feature.getDriver();
        CompiledPalette palette = feature.computePalette(info, part);
        int minY = info.provider.getWorld().getMinY();

        for (int x = 0; x < part.getXSize(); x++) {
            for (int z = 0; z < part.getZSize(); z++) {
                char[] slices = part.getVSlice(x, z);
                if (slices == null) {
                    continue;
                }

                int rx = transform.rotateX(x, z);
                int rz = transform.rotateZ(x, z);
                driver.current(rx, topY, rz);
                int partY = sliceCount - 1;
                while (driver.getY() >= minY && LostCityTerrainFeature.isEmpty(driver.getBlock())) {
                    char c = slices[partY];
                    BlockState state = palette.get(c);
                    if (state == null) {
                        throw new RuntimeException("Could not find entry '" + c + "' in the palette for support part '"
                                + part.getName() + "'!");
                    }
                    if (!state.isAir() && !state.is(Blocks.STRUCTURE_VOID)) {
                        driver.block(feature.transformBlockState(transform, state));
                    }
                    driver.decY();
                    if (--partY < 0) {
                        partY = sliceCount - 1;
                    }
                }
            }
        }
    }
}
