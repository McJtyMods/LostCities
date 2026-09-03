package mcjty.lostcities.setup;

import mcjty.lostcities.gui.GuiLCConfig;
import mcjty.lostcities.gui.LostCitySetup;
import mcjty.lostcities.varia.ComponentFactory;
import mcjty.lostcities.worldgen.LostCityFeature;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;

public final class ClientEventHandlers {

    private ClientEventHandlers() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof CreateWorldScreen createWorldScreen) {
                Button lostCitiesButton = Button.builder(ComponentFactory.literal("Cities"), button ->
                        Minecraft.getInstance().gui.setScreen(new GuiLCConfig(createWorldScreen))
                ).bounds(screen.width - 100, 40, 70, 20).build();
                lostCitiesButton.visible = false;
                Screens.getWidgets(screen).add(lostCitiesButton);
                ScreenEvents.afterTick(screen).register(ignored ->
                        lostCitiesButton.visible = createWorldScreen.tabManager.getCurrentTab() instanceof CreateWorldScreen.MoreTab);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LostCitySetup.CLIENT_SETUP.reset();
            Config.reset();
            LostCityFeature.globalDimensionInfoDirtyCounter++;
        });
    }
}
