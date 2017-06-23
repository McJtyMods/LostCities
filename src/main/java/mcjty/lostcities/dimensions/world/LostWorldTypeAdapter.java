package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.api.IChunkPrimerFactory;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.gui.GuiLostCityConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.world.gen.IChunkGenerator;

public class LostWorldTypeAdapter extends WorldType {

    public LostWorldTypeAdapter(String other) {
        super("lostcities_" + other);
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
            return new LostWorldFilteredBiomeProvider(getInternalBiomeProvider(world), profile.ALLOWED_BIOME_FACTORS);
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
}
