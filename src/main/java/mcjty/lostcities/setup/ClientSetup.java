package mcjty.lostcities.setup;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new ClientEventHandlers());
    }
}
