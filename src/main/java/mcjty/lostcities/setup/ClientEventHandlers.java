package mcjty.lostcities.setup;

import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.gui.GuiLCConfig;
import mcjty.lostcities.gui.LostCitySetup;
import mcjty.lostcities.varia.ComponentFactory;
import mcjty.lostcities.worldgen.LostCityFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandlers {

    //
//    @SubscribeEvent
//    public void onFogEvent(EntityViewRenderEvent.FogColors event) {
//        if (WorldTypeTools.isLostCities(Minecraft.getInstance().world)) {
//            LostCityProfile profile = WorldTypeTools.getProfile(Minecraft.getInstance().world);
//            if (profile.FOG_RED >= 0) {
//                event.setRed(profile.FOG_RED);
//            }
//            if (profile.FOG_GREEN >= 0) {
//                event.setGreen(profile.FOG_GREEN);
//            }
//            if (profile.FOG_BLUE >= 0) {
//                event.setBlue(profile.FOG_BLUE);
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
//        if (WorldTypeTools.isLostCities(Minecraft.getInstance().world)) {
//            LostCityProfile profile = WorldTypeTools.getProfile(Minecraft.getInstance().world);
//            if (profile.FOG_DENSITY >= 0) {
//                event.setDensity(profile.FOG_DENSITY);
//                event.setCanceled(true);
//            }
//        }
//    }

    private Button lostCitiesButton = null;

    @SubscribeEvent
    public void onGuiDraw(ScreenEvent.Render event) {
        if (event.getScreen() instanceof CreateWorldScreen screen && lostCitiesButton != null) {
            lostCitiesButton.visible = screen.tabManager.getCurrentTab() instanceof CreateWorldScreen.MoreTab;
            if (lostCitiesButton.visible) {
                RenderSystem.setShaderTexture(0, new ResourceLocation(LostCities.MODID, "textures/gui/configicon.png"));
                GuiComponent.blit(event.getPoseStack(), screen.width - 100, 60, 70, 70, 256, 256, 256, 256, 256, 256);
            }
        }
    }

    @SubscribeEvent
    public void onGuiPost(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof CreateWorldScreen screen) {
            lostCitiesButton = Button.builder(ComponentFactory.literal("Cities"), p_onPress_1_ -> {
//                WorldType worldType = WorldType.WORLD_TYPES[screen.selectedIndex];
                Minecraft.getInstance().setScreen(new GuiLCConfig(screen /* @todo 1.16, worldType*/));
            }).bounds(screen.width - 100, 40, 70, 20).build();
            lostCitiesButton.visible = false;
            event.addListener(lostCitiesButton);
        }
    }

    // To clean up client-side and single player
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LostCitySetup.CLIENT_SETUP.reset();
        Config.reset();
        LostCityFeature.globalDimensionInfoDirtyCounter++;
    }
}
