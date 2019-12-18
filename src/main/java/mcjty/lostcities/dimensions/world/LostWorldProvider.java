package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.config.BiomeSelectionStrategy;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.ModDimensions;
import mcjty.lostcities.setup.ModSetup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.OverworldGenSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LostWorldProvider extends Dimension {

    public LostWorldProvider(World worldIn) {
        super(worldIn, ModDimensions.lostDimensionType);
    }

    // @todo 1.14
//    @Override
//    @Nonnull
//    public String getSaveFolder() {
//        if (getDimension() == LostCityConfiguration.DIMENSION_ID) {
//            return "LOST";
//        } else {
//            return "LOST" + getDimension();
//        }
//    }

    @Override
    @Nonnull
    public ChunkGenerator createChunkGenerator() {
//        return new LostCityChunkGenerator(world, (world.getSeed() >> 3) ^ 34328884229L);
        // @todo 1.14
        return new LostCityChunkGenerator(world, null, new OverworldGenSettings());
    }

    // @todo 1.14
//    private BiomeProvider getInternalBiomeProvider(World world) {
//        if (biomeProvider == null) {
//            for (WorldType type : WorldType.WORLD_TYPES) {
//                if ("BIOMESOP".equals(type.getName())) {
//                    WorldType orig = world.getWorldInfo().getTerrainType();
//                    world.getWorldInfo().setTerrainType(type);
//                    biomeProvider = type.getBiomeProvider(world);
//                    world.getWorldInfo().setTerrainType(orig);
//                    break;
//                }
//            }
//        }
//        return biomeProvider;
//    }

    // @todo 1.14
//    @Override
    protected void init() {
//        super.init();

        String profileName = ModDimensions.dimensionProfileMap.get(world.getDimension().getType());
        LostCityProfile profile = LostCityConfiguration.profiles.get(profileName);
        if (profile == null) {
            profile = WorldTypeTools.getProfile(world);
        }
        BiomeProvider biomeProvider;
        if (ModSetup.biomesoplenty && LostCityConfiguration.DIMENSION_BOP) {
            biomeProvider = null; // @todo 1.14 getInternalBiomeProvider(world);
        } else {
//            biomeProvider = new BiomeProvider(world.getWorldInfo());
            biomeProvider = null;
        }
        if (profile.ALLOWED_BIOME_FACTORS.length == 0) {
            // @todo 1.14
//            this.biomeProvider = biomeProvider;
        } else {
            String[] outsideAllowedbiomeFactors = profile.ALLOWED_BIOME_FACTORS;
            String[] outsideManualBiomeMapping = profile.MANUAL_BIOME_MAPPINGS;
            BiomeSelectionStrategy outsideStrategy = null;
            if (profile.isSpace() && profile.CITYSPHERE_LANDSCAPE_OUTSIDE && !profile.CITYSPHERE_OUTSIDE_PROFILE.isEmpty()) {
                LostCityProfile outProfile = LostCityConfiguration.profiles.get(profile.CITYSPHERE_OUTSIDE_PROFILE);
                outsideAllowedbiomeFactors = outProfile.ALLOWED_BIOME_FACTORS;
                outsideManualBiomeMapping = outProfile.MANUAL_BIOME_MAPPINGS;
                outsideStrategy = outProfile.BIOME_SELECTION_STRATEGY;
            }
            // @todo 1.14
//            this.biomeProvider = new LostWorldFilteredBiomeProvider(world, biomeProvider,
//                    profile.ALLOWED_BIOME_FACTORS,
//                    profile.MANUAL_BIOME_MAPPINGS,
//                    profile.BIOME_SELECTION_STRATEGY,
//                    outsideAllowedbiomeFactors,
//                    outsideManualBiomeMapping,
//                    outsideStrategy);
        }
    }


    // @todo 1.14 for all below
    @Nullable
    @Override
    public BlockPos findSpawn(ChunkPos chunkPosIn, boolean checkValid) {
        return null;
    }

    @Nullable
    @Override
    public BlockPos findSpawn(int posX, int posZ, boolean checkValid) {
        return null;
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0;
    }

    @Override
    public boolean isSurfaceWorld() {
        return false;
    }

    @Override
    public Vec3d getFogColor(float celestialAngle, float partialTicks) {
        return null;
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    @Override
    public boolean doesXZShowFog(int x, int z) {
        return false;
    }
}
