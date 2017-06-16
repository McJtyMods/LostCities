package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.gui.GuiLostCityConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LostWorldTypeATG extends WorldType {

    public LostWorldTypeATG() {
        super("lostcities_atg");
    }

    private BiomeProvider biomeProvider = null;
    private IChunkGenerator atgGenerator = null;

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        if (atgGenerator == null) {
            for (WorldType type : WorldType.WORLD_TYPES) {
                if ("atg".equals(type.getName())) {
                    WorldType orig = world.getWorldInfo().getTerrainType();
                    world.getWorldInfo().setTerrainType(type);
                    atgGenerator = type.getChunkGenerator(world, generatorOptions);
                    world.getWorldInfo().setTerrainType(orig);
                    break;
                }
            }
        }
        return new LostCityChunkGenerator(world, atgGenerator);
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        if (biomeProvider == null) {
            for (WorldType type : WorldType.WORLD_TYPES) {
                if ("atg".equals(type.getName())) {
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
    public boolean isCustomizable() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(Minecraft mc, GuiCreateWorld guiCreateWorld) {
        mc.displayGuiScreen(new GuiLostCityConfiguration(guiCreateWorld));
    }
}
