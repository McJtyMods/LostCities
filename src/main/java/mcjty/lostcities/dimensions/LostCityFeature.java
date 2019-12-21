package mcjty.lostcities.dimensions;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.setup.Config;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LostCityFeature extends Feature<NoFeatureConfig> {

    private Map<DimensionType, IDimensionInfo> dimensionInfo = new HashMap<>();

    public LostCityFeature() {
        super(NoFeatureConfig::deserialize);
    }

    @Override
    public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (world instanceof WorldGenRegion) {
            DimensionType type = world.getDimension().getType();
            String profileName = Config.getProfileForDimension(type);
            if (profileName != null) {
                WorldGenRegion region = (WorldGenRegion) world;
                int chunkX = region.getMainChunkX();
                int chunkZ = region.getMainChunkZ();
                if (!dimensionInfo.containsKey(type)) {
                    LostCityProfile profile = LostCityConfiguration.standardProfiles.get(profileName);
                    IDimensionInfo diminfo = new DefaultDimensionInfo(world, profile);
                    dimensionInfo.put(type, diminfo);
                }
                IDimensionInfo diminfo = dimensionInfo.get(type);
                diminfo.getFeature().generate(region, region.getChunk(chunkX, chunkZ));
                return true;
            }
        }
        return false;
    }
}
