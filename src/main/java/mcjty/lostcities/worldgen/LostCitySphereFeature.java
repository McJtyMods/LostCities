package mcjty.lostcities.worldgen;

import mcjty.lostcities.setup.Registration;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;

public class LostCitySphereFeature extends Feature<NoneFeatureConfiguration> {

    public LostCitySphereFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (level instanceof WorldGenRegion) {
            IDimensionInfo diminfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(level);
            if (diminfo != null) {
                WorldGenRegion region = (WorldGenRegion) level;
                ChunkPos center = region.getCenter();
                Holder<Biome> biome = region.getBiome(center.getMiddleBlockPosition(60));
                if (biome.is(Tags.Biomes.IS_VOID)) {
                    return false;
                }

                int chunkX = center.x;
                int chunkZ = center.z;
                diminfo.setWorld(level);
                diminfo.getFeature().generateSpheres(region, region.getChunk(chunkX, chunkZ));
                return true;
            }
        }
        return false;
    }
}
