package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.config.ProfileSetup;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.ForgeEventHandlers;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class LostCityFeature extends Feature<NoneFeatureConfiguration> {

    /**
     * On dedicated servers the dimensionInfo cache is no problem. The server starts only once
     * and will have the correct dimension info and for the clients it doesn't matter.
     * However, to make sure that on a single player world this cache is cleared when the player
     * exits the world and creates a new one we keep a static flag which is incremented whenever
     * the player exits the world. That is then used to help clear this cache
     */
    private final Map<ResourceKey<Level>, IDimensionInfo> dimensionInfo = new HashMap<>();
    public static int globalDimensionInfoDirtyCounter = 0;
    private int dimensionInfoDirtyCounter = -1;

    public LostCityFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (level instanceof WorldGenRegion) {
            IDimensionInfo diminfo = getDimensionInfo(level);
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
                try {
                    diminfo.getFeature().generate(region, region.getChunk(chunkX, chunkZ));
                } catch (Exception e) {
                    LostCities.getLogger().error("Error generating chunk {},{}: {}", chunkX, chunkZ, e.getMessage(), e);
                    ErrorLogger.logChunkInfo(chunkX, chunkZ, diminfo);
                    ErrorLogger.report("There was an error generating a chunk. See log for details!");
                }
                return true;
            }
        }
        return false;
    }

    @Nullable
    public IDimensionInfo getDimensionInfo(WorldGenLevel world) {
        if (globalDimensionInfoDirtyCounter != dimensionInfoDirtyCounter) {
            // Force clear of cache
            cleanUp();
        }
        ResourceKey<Level> type = world.getLevel().dimension();
        String profileName = Config.getProfileForDimension(type);
        if (profileName != null) {
            if (!dimensionInfo.containsKey(type)) {
                LostCityProfile profile = ProfileSetup.STANDARD_PROFILES.get(profileName);
                if (profile == null) {
                    return null;
                }
                LostCityProfile outsideProfile = profile.CITYSPHERE_OUTSIDE_PROFILE == null ? null : ProfileSetup.STANDARD_PROFILES.get(profile.CITYSPHERE_OUTSIDE_PROFILE);
                IDimensionInfo diminfo = new DefaultDimensionInfo(world, profile, outsideProfile);
                dimensionInfo.put(type, diminfo);
            }
            return dimensionInfo.get(type);
        }
        return null;
    }

    public void cleanUp() {
        LostCities.lostCitiesImp.cleanUp();
        ForgeEventHandlers.cleanUp();
        AssetRegistries.reset();
        dimensionInfo.clear();
        dimensionInfoDirtyCounter = globalDimensionInfoDirtyCounter;
    }
}
