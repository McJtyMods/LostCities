package mcjty.lostcities.network;


import mcjty.lostcities.LostCities;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
    private static int ID = 12;
    private static int packetId = 0;

    public static SimpleChannel INSTANCE = null;

    public static int nextPacketID() {
        return packetId++;
    }

    public PacketHandler() {
    }

    public static int nextID() {
        return ID++;
    }

    public static void registerMessages(String channelName) {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(LostCities.MODID, channelName), () -> "1.0", s -> true, s -> true);
        registerMessages();
    }

    public static void registerMessages() {
        // Server side
        INSTANCE.registerMessage(nextID(), PacketRequestProfile.class,
                PacketRequestProfile::toBytes,
                PacketRequestProfile::new,
                PacketRequestProfile::handle);

        // Client side
        INSTANCE.registerMessage(nextID(), PacketReturnProfileToClient.class,
                PacketReturnProfileToClient::toBytes,
                PacketReturnProfileToClient::new,
                PacketReturnProfileToClient::handle);
    }
}
