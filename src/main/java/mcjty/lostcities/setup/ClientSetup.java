package mcjty.lostcities.setup;

import mcjty.lostcities.network.PacketReturnProfileToClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ClientSetup implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientEventHandlers.register();
        ClientPlayNetworking.registerGlobalReceiver(PacketReturnProfileToClient.TYPE,
                (payload, context) -> payload.handle());
    }
}
