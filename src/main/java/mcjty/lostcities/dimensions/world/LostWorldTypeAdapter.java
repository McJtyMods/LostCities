package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.api.IChunkPrimerFactory;
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

public class LostWorldTypeAdapter extends WorldType {

    public LostWorldTypeAdapter(String other) {
        super("lc_" + other);
        this.otherWorldtype = other;
    }

    private final String otherWorldtype;
    private BiomeProvider biomeProvider = null;
    private IChunkGenerator otherGenerator = null;
    private IChunkPrimerFactory factory = null;

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        if (otherGenerator == null) {
            for (WorldType type : WorldType.WORLD_TYPES) {
                if (otherWorldtype.equals(type.getName())) {
                    WorldType orig = world.getWorldInfo().getTerrainType();
                    world.getWorldInfo().setTerrainType(type);
                    otherGenerator = type.getChunkGenerator(world, generatorOptions);
                    world.getWorldInfo().setTerrainType(orig);
                    if (otherGenerator instanceof IChunkPrimerFactory) {
                        factory = (IChunkPrimerFactory) otherGenerator;
                    }
                    break;
                }
            }
        }
        return new LostCityChunkGenerator(world, factory);
    }

    private BiomeProvider getInternalBiomeProvider(World world) {
        if (biomeProvider == null) {
            for (WorldType type : WorldType.WORLD_TYPES) {
                if (otherWorldtype.equals(type.getName())) {
                    WorldType orig = world.getWorldInfo().getTerrainType();
                    world.getWorldInfo().setTerrainType(type);
                    biomeProvider = type.getBiomeProvider(world);
                    world.getWorldInfo().setTerrainType(orig);
                    break;
                }
            }
        }
        return biomeProvider;
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        LostCityProfile profile = LostWorldType.getProfile(world);
        if (profile.ALLOWED_BIOME_FACTORS.length == 0) {
            return getInternalBiomeProvider(world);
        } else {
            String[] outsideAllowedbiomeFactors = profile.ALLOWED_BIOME_FACTORS;
            if (profile.isSpace() && profile.CITYSPHERE_LANDSCAPE_OUTSIDE && !profile.CITYSPHERE_OUTSIDE_PROFILE.isEmpty()) {
                LostCityProfile outProfile = LostCityConfiguration.profiles.get(profile.CITYSPHERE_OUTSIDE_PROFILE);
                outsideAllowedbiomeFactors = outProfile.ALLOWED_BIOME_FACTORS;
            }
            return new LostWorldFilteredBiomeProvider(world, getInternalBiomeProvider(world),
                    profile.ALLOWED_BIOME_FACTORS, outsideAllowedbiomeFactors);
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
    public int getSpawnFuzz(WorldServer world, MinecraftServer server) {
        LostCityProfile profile = LostWorldType.getProfile(world);
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
    public int getMinimumSpawnHeight(World world) {
        LostCityProfile profile = LostWorldType.getProfile(world);
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
