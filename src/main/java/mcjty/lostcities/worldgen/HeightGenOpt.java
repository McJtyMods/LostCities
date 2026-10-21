package mcjty.lostcities.worldgen;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;

public class HeightGenOpt {

    public static int getBaseHeight(NoiseBasedChunkGenerator generator, int x, int z, WorldGenLevel level, RandomState rnd) {
        NoiseGeneratorSettings noise = generator.generatorSettings().value();
        NoiseSettings settings = noise.noiseSettings().clampToHeightAccessor(level);
        if (settings.height() <= 0) {
            return level.getMinY();
        }

        DensityVolume volume = new DensityVolume(1, settings.height(), 1, x, settings.minY(), z);
        Aquifer.FluidStatus fluid = new Aquifer.FluidStatus(noise.seaLevel(), noise.defaultFluid());
        try (NoiseChunk chunk = new NoiseChunk(rnd, null, noise, (bx, by, bz) -> fluid, Blender.empty(), volume)) {
            DensitySampler.Bound density = chunk.cachingSamplers().get(noise.noiseRouter().finalDensity());
            for (int y = settings.minY() + settings.height() - 1; y >= settings.minY(); y--) {
                BlockState state = chunk.aquifer().computeSubstance(x, y, z, density.sampleValue(x, y, z));
                if ((state == null ? noise.defaultBlock() : state).is(BlockTags.BLOCKS_MOTION_IN_HEIGHTMAP)) {
                    return y + 1;
                }
            }
        }
        return level.getMinY();
    }
}
