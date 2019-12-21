package mcjty.lostcities.setup;

import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEventHandlers {

    public static void init(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEventHandlers());

    }

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

    @SubscribeEvent
    public void onGuiPost(GuiScreenEvent.InitGuiEvent.Post event) {
// @disabled for now
        //        if (event.getGui() instanceof CreateWorldScreen) {
//            CreateWorldScreen screen = (CreateWorldScreen) event.getGui();
//            event.addWidget(new Button(screen.width / 2 - 75, 167, 150, 20, "Lost Cities", p_onPress_1_ -> {
//
//            }));
//        }
    }

}
