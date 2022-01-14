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

    /**
     * On dedicated servers the dimensionInfo cache is no problem. The server starts only once
     * and will have the correct dimension info and for the clients it doesn't matter.
     * However, to make sure that on a single player world this cache is cleared when the player
     * exits the world and creates a new one we keep a static flag which is incremented whenever
     * the player exits the world. That is then used to help clear this cache
     */
    private final Map<RegistryKey<World>, IDimensionInfo> dimensionInfo = new HashMap<>();
    public static int globalDimensionInfoDirtyCounter = 0;
    private int dimensionInfoDirtyCounter = -1;

    public static ConfiguredFeature<?, ?> LOSTCITY_CONFIGURED_FEATURE;

    public static void registerConfiguredFeatures() {
        Registry<ConfiguredFeature<?, ?>> registry = WorldGenRegistries.CONFIGURED_FEATURE;

        LOSTCITY_CONFIGURED_FEATURE = Registration.LOSTCITY_FEATURE
                .configured(NoFeatureConfig.NONE)
                .decorated(Placement.RANGE.configured(new TopSolidRangeConfig(1, 0, 1)));

        Registry.register(registry, new ResourceLocation(LostCities.MODID, "configured_feature"), LOSTCITY_CONFIGURED_FEATURE);
    }


    public LostCityFeature() {
        super(NoFeatureConfig.CODEC);
    }

    @Override
    public boolean place(ISeedReader world, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (world instanceof WorldGenRegion) {
            IDimensionInfo diminfo = getDimensionInfo(world);
            if (diminfo != null) {
                WorldGenRegion region = (WorldGenRegion) world;
                int chunkX = region.getCenterX();
                int chunkZ = region.getCenterZ();
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
        if (globalDimensionInfoDirtyCounter != dimensionInfoDirtyCounter) {
            // Force clear of cache
            dimensionInfo.clear();
            dimensionInfoDirtyCounter = globalDimensionInfoDirtyCounter;
        }
        RegistryKey<World> type = world.getLevel().dimension();
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
