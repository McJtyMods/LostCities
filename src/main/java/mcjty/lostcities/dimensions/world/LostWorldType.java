package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.config.BiomeSelectionStrategy;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.gui.GuiLostCityConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

    @Override
    public int getSpawnFuzz(WorldServer world, MinecraftServer server) {
        LostCityProfile profile = WorldTypeTools.getProfile(world);
        switch (profile.LANDSCAPE_TYPE) {
            case DEFAULT:
            case FLOATING:
                return super.getSpawnFuzz(world, server);
            case SPACE:
            case CAVERN:
                return 0;
        }
        return super.getSpawnFuzz(world, server);
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        return new LostCityChunkGenerator(world, world.getSeed());
    }

    protected BiomeProvider getInternalBiomeProvider(World world) {
        return super.getBiomeProvider(world);
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        LostCityProfile profile = WorldTypeTools.getProfile(world);
        if (profile.ALLOWED_BIOME_FACTORS.length == 0) {
            return getInternalBiomeProvider(world);
        } else {
            String[] outsideAllowedbiomeFactors = profile.ALLOWED_BIOME_FACTORS;
            String[] outsideManualBiomeMappings = profile.MANUAL_BIOME_MAPPINGS;
            BiomeSelectionStrategy outsideStrategy = null;
            if (profile.isSpace() && profile.CITYSPHERE_LANDSCAPE_OUTSIDE && !profile.CITYSPHERE_OUTSIDE_PROFILE.isEmpty()) {
                LostCityProfile outProfile = LostCityConfiguration.profiles.get(profile.CITYSPHERE_OUTSIDE_PROFILE);
                outsideAllowedbiomeFactors = outProfile.ALLOWED_BIOME_FACTORS;
                outsideManualBiomeMappings = outProfile.MANUAL_BIOME_MAPPINGS;
                outsideStrategy = outProfile.BIOME_SELECTION_STRATEGY;
            }
            return new LostWorldFilteredBiomeProvider(world, super.getBiomeProvider(world),
                    profile.ALLOWED_BIOME_FACTORS,
                    profile.MANUAL_BIOME_MAPPINGS,
                    profile.BIOME_SELECTION_STRATEGY,
                    outsideAllowedbiomeFactors,
                    outsideManualBiomeMappings,
                    outsideStrategy);
        }
    }

    @Override
    public boolean isCustomizable() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(Minecraft mc, GuiCreateWorld guiCreateWorld) {
        mc.displayGuiScreen(new GuiLostCityConfiguration(guiCreateWorld));
    }



    @Override
    public int getMinimumSpawnHeight(World world) {
        LostCityProfile profile = WorldTypeTools.getProfile(world);
        switch (profile.LANDSCAPE_TYPE) {
            case DEFAULT:
            case FLOATING:
                return super.getMinimumSpawnHeight(world);
            case SPACE:
            case CAVERN:
                return profile.GROUNDLEVEL;
        }
        return super.getMinimumSpawnHeight(world);
    }
}
