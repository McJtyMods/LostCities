package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.gui.GuiLCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
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
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent event) {
        if (event.getGui() instanceof CreateWorldScreen && lostCitiesButton != null) {
            CreateWorldScreen screen = (CreateWorldScreen) event.getGui();
            lostCitiesButton.visible = screen.inMoreWorldOptionsDisplay;
            if (lostCitiesButton.visible) {
                Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation(LostCities.MODID, "textures/gui/configicon.png"));
                screen.blit(event.getMatrixStack(), screen.width - 100, 30, 70, 70, 256, 256, 256, 256, 256, 256);
            }
        }
    }

    @SubscribeEvent
    public void onGuiPost(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof CreateWorldScreen) {
            CreateWorldScreen screen = (CreateWorldScreen) event.getGui();
            lostCitiesButton = new Button(screen.width - 100, 10, 70, 20, new StringTextComponent("Cities"), p_onPress_1_ -> {
//                WorldType worldType = WorldType.WORLD_TYPES[screen.selectedIndex];
                Minecraft.getInstance().displayGuiScreen(new GuiLCConfig(screen /* @todo 1.16, worldType*/));
            });
            event.addWidget(lostCitiesButton);
        }
    }
}
