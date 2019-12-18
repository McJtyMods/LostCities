package mcjty.lostcities;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.WorldTypeTools;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandlers {

    @SubscribeEvent
    public void onFogEvent(EntityViewRenderEvent.FogColors event) {
        if (WorldTypeTools.isLostCities(Minecraft.getInstance().world)) {
            LostCityProfile profile = WorldTypeTools.getProfile(Minecraft.getInstance().world);
            if (profile.FOG_RED >= 0) {
                event.setRed(profile.FOG_RED);
            }
            if (profile.FOG_GREEN >= 0) {
                event.setGreen(profile.FOG_GREEN);
            }
            if (profile.FOG_BLUE >= 0) {
                event.setBlue(profile.FOG_BLUE);
            }
        }
    }

    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (WorldTypeTools.isLostCities(Minecraft.getInstance().world)) {
            LostCityProfile profile = WorldTypeTools.getProfile(Minecraft.getInstance().world);
            if (profile.FOG_DENSITY >= 0) {
                event.setDensity(profile.FOG_DENSITY);
                event.setCanceled(true);
            }
        }
    }
}
