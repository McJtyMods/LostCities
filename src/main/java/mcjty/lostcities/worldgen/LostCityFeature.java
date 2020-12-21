package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.Registration;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.provider.OverworldBiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LostCityFeature extends Feature<NoFeatureConfig> {

    private Map<RegistryKey<World>, IDimensionInfo> dimensionInfo = new HashMap<>();

    public static ConfiguredFeature<?, ?> LOSTCITY_CONFIGURED_FEATURE;

    public static void registerConfiguredFeatures() {
        Registry<ConfiguredFeature<?, ?>> registry = WorldGenRegistries.CONFIGURED_FEATURE;

        LOSTCITY_CONFIGURED_FEATURE = Registration.LOSTCITY_FEATURE
                .withConfiguration(NoFeatureConfig.NO_FEATURE_CONFIG)
                .withPlacement(Placement.RANGE.configure(new TopSolidRangeConfig(1, 0, 1)));

        Registry.register(registry, new ResourceLocation(LostCities.MODID, "configured_feature"), LOSTCITY_CONFIGURED_FEATURE);
    }


    public LostCityFeature() {
        super(NoFeatureConfig.field_236558_a_);
    }

    @Override
    public boolean generate(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (world instanceof WorldGenRegion) {
            IDimensionInfo diminfo = getDimensionInfo(world);
            if (diminfo != null) {
                WorldGenRegion region = (WorldGenRegion) world;
                int chunkX = region.getMainChunkX();
                int chunkZ = region.getMainChunkZ();
                diminfo.setWorld(world);
//                generator.getBiomeProvider() ->OverworldBiomeProvider
//                diminfo.getFeature().generateDummy(region, region.getChunk(chunkX, chunkZ));
                diminfo.getFeature().generate(region, region.getChunk(chunkX, chunkZ));
                return true;
            }
        }
        return false;
    }

    @Nullable
    public IDimensionInfo getDimensionInfo(ISeedReader world) {
        RegistryKey<World> type = world.getWorld().getDimensionKey();
        String profileName = Config.getProfileForDimension(type);
        if (profileName != null) {
            if (!dimensionInfo.containsKey(type)) {
                LostCityProfile profile = LostCityConfiguration.standardProfiles.get(profileName);
                IDimensionInfo diminfo = new DefaultDimensionInfo(world, profile);
                dimensionInfo.put(type, diminfo);
            }
            return dimensionInfo.get(type);
        }
        return null;
    }
}
