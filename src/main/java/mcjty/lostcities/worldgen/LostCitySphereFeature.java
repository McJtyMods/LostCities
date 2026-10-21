package mcjty.lostcities.worldgen;

import com.mojang.serialization.MapCodec;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.worldgen.gen.Spheres;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.common.Tags;

public class LostCitySphereFeature implements Feature {

    public static final MapCodec<LostCitySphereFeature> CODEC = MapCodec.unit(Registration.LOSTCITY_SPHERE_FEATURE);

    @Override
    public MapCodec<LostCitySphereFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        if (level instanceof WorldGenRegion region) {
            LostCityFeature lostCityFeature = Registration.LOSTCITY_FEATURE.get();
            return lostCityFeature.runWithDimensionInfo(level, diminfo -> {
                ChunkPos center = region.getCenter();
                Holder<Biome> biome = region.getBiome(center.getMiddleBlockPosition(60));
                if (biome.is(Tags.Biomes.IS_VOID)) {
                    return false;
                }

                int chunkX = center.x();
                int chunkZ = center.z();
                try (GenerationContext.Scope ignored = GenerationContext.open(level, diminfo.getSeed(), diminfo.getType(), chunkX, chunkZ)) {
                    Spheres.generateSpheres(diminfo.getFeature(), region, region.getChunk(chunkX, chunkZ));
                }
                return true;
            });
        }
        return false;
    }
}
