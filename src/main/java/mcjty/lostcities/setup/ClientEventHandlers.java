package mcjty.lostcities.setup;

import com.mojang.blaze3d.systems.RenderSystem;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.gui.GuiLCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.event.ScreenEvent;
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
    public void onGuiDraw(ScreenEvent.DrawScreenEvent event) {
        if (event.getScreen() instanceof CreateWorldScreen screen && lostCitiesButton != null) {
            lostCitiesButton.visible = screen.worldGenSettingsVisible;
            if (lostCitiesButton.visible) {
                RenderSystem.setShaderTexture(0, new ResourceLocation(LostCities.MODID, "textures/gui/configicon.png"));
                GuiComponent.blit(event.getPoseStack(), screen.width - 100, 30, 70, 70, 256, 256, 256, 256, 256, 256);
            }
        }
    }

    @SubscribeEvent
    public void onGuiPost(ScreenEvent.InitScreenEvent.Post event) {
        if (event.getScreen() instanceof CreateWorldScreen screen) {
            lostCitiesButton = new Button(screen.width - 100, 10, 70, 20, new TextComponent("Cities"), p_onPress_1_ -> {
//                WorldType worldType = WorldType.WORLD_TYPES[screen.selectedIndex];
                Minecraft.getInstance().setScreen(new GuiLCConfig(screen /* @todo 1.16, worldType*/));
            });
            event.addListener(lostCitiesButton);
        }
    }
}
