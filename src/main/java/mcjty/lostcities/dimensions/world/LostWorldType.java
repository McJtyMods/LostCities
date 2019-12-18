package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.gui.GuiLostCityConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.OverworldGenSettings;

public class LostWorldType extends WorldType {

    public LostWorldType() {
        super("lostcities");
    }

    public LostWorldType(String name) {
        super(name);

    }

    @Override
    public double getHorizon(World world) {
        LostCityProfile profile = WorldTypeTools.getProfile(world);
        if (profile.HORIZON < 0) {
            return super.getHorizon(world);
        } else {
            return profile.HORIZON;
        }
    }

    // @todo 1.14
//    @Override
//    public int getSpawnFuzz(WorldServer world, MinecraftServer server) {
//        LostCityProfile profile = WorldTypeTools.getProfile(world);
//        switch (profile.LANDSCAPE_TYPE) {
//            case DEFAULT:
//            case FLOATING:
//                return super.getSpawnFuzz(world, server);
//            case SPACE:
//            case CAVERN:
//                return 0;
//        }
//        return super.getSpawnFuzz(world, server);
//    }


    @Override
    public ChunkGenerator<?> createChunkGenerator(World world) {
        // @todo 1.14
//        return new LostCityChunkGenerator(world, world.getSeed());
        return new LostCityChunkGenerator(world, getInternalBiomeProvider(world), new OverworldGenSettings());
    }

    protected BiomeProvider getInternalBiomeProvider(World world) {
        // todo 1.14
//        return super.getBiomeProvider(world);
        return null;
    }

    // @todo 1.14
//    @Override
//    public BiomeProvider getBiomeProvider(World world) {
//        LostCityProfile profile = WorldTypeTools.getProfile(world);
//        if (profile.ALLOWED_BIOME_FACTORS.length == 0) {
//            return getInternalBiomeProvider(world);
//        } else {
//            String[] outsideAllowedbiomeFactors = profile.ALLOWED_BIOME_FACTORS;
//            String[] outsideManualBiomeMappings = profile.MANUAL_BIOME_MAPPINGS;
//            BiomeSelectionStrategy outsideStrategy = null;
//            if (profile.isSpace() && profile.CITYSPHERE_LANDSCAPE_OUTSIDE && !profile.CITYSPHERE_OUTSIDE_PROFILE.isEmpty()) {
//                LostCityProfile outProfile = LostCityConfiguration.profiles.get(profile.CITYSPHERE_OUTSIDE_PROFILE);
//                outsideAllowedbiomeFactors = outProfile.ALLOWED_BIOME_FACTORS;
//                outsideManualBiomeMappings = outProfile.MANUAL_BIOME_MAPPINGS;
//                outsideStrategy = outProfile.BIOME_SELECTION_STRATEGY;
//            }
//            return new LostWorldFilteredBiomeProvider(world, super.getBiomeProvider(world),
//                    profile.ALLOWED_BIOME_FACTORS,
//                    profile.MANUAL_BIOME_MAPPINGS,
//                    profile.BIOME_SELECTION_STRATEGY,
//                    outsideAllowedbiomeFactors,
//                    outsideManualBiomeMappings,
//                    outsideStrategy);
//        }
//    }


    @Override
    public boolean hasCustomOptions() {
        return true;
    }

    @Override
    public void onCustomizeButton(Minecraft mc, CreateWorldScreen gui) {
        mc.displayGuiScreen(new GuiLostCityConfiguration(gui));
    }



    // @todo 1.14
//    @Override
//    public int getMinimumSpawnHeight(World world) {
//        LostCityProfile profile = WorldTypeTools.getProfile(world);
//        switch (profile.LANDSCAPE_TYPE) {
//            case DEFAULT:
//            case FLOATING:
//                return super.getMinimumSpawnHeight(world);
//            case SPACE:
//            case CAVERN:
//                return profile.GROUNDLEVEL;
//        }
//        return super.getMinimumSpawnHeight(world);
//    }
}
