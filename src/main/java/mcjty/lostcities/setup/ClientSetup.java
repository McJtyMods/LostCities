package mcjty.lostcities.setup;

import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.neoforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEventHandlers());
    }
}
