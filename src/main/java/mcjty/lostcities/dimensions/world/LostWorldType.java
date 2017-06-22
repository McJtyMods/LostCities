package mcjty.lostcities.dimensions.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.gui.GuiLostCityConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class LostWorldType extends WorldType {

    public LostWorldType() {
        super("lostcities");
    }

    public LostWorldType(String name) {
        super(name);

    }

    public static LostCityProfile getProfile(World world) {
        String generatorOptions = world.getWorldInfo().getGeneratorOptions();
        LostCityProfile p;
        if (generatorOptions == null || generatorOptions.trim().isEmpty()) {
            p = LostCityConfiguration.profiles.get("default");
            if (p == null) {
                throw new RuntimeException("Something went wrong! Profile '" + "default" + "' is missing!");
            }
        } else {
            JsonParser parser = new JsonParser();
            JsonElement parsed = parser.parse(generatorOptions);
            String profileName;
            if (parsed.getAsJsonObject().has("profile")) {
                profileName = parsed.getAsJsonObject().get("profile").getAsString();
            } else {
                profileName = "default";
            }
            p = LostCityConfiguration.profiles.get(profileName);
            if (p == null) {
                throw new RuntimeException("Something went wrong! Profile '" + profileName + "' is missing!");
            }
        }
        return p;
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        return new LostCityChunkGenerator(world);
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        LostCityProfile profile = getProfile(world);
        if (profile.ALLOWED_BIOME_FACTORS.length == 0) {
            return super.getBiomeProvider(world);
        } else {
            return new LostWorldFilteredBiomeProvider(super.getBiomeProvider(world), profile.ALLOWED_BIOME_FACTORS);
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
