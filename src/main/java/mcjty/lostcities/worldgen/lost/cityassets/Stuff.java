package mcjty.lostcities.worldgen.lost.cityassets;

import mcjty.lostcities.api.ILostCityAsset;
import mcjty.lostcities.worldgen.lost.regassets.StuffSettingsRE;
import mcjty.lostcities.worldgen.lost.regassets.data.DataTools;
import net.minecraft.resources.ResourceLocation;

public class Stuff implements ILostCityAsset {

    private final ResourceLocation name;
    private final StuffSettingsRE settings;

    public Stuff(StuffSettingsRE settings) {
        this.settings = settings;
        this.name = settings.getRegistryName();
    }

    @Override
    public String getName() {
        return DataTools.toName(name);
    }

    @Override
    public ResourceLocation getId() {
        return name;
    }

    public StuffSettingsRE getSettings() {
        return settings;
    }
}
