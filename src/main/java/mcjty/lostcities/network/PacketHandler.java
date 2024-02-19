package mcjty.lostcities.network;


import mcjty.lostcities.LostCities;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static int ID = 12;
    private static int packetId = 0;

    private static SimpleChannel INSTANCE = null;

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
        INSTANCE.registerMessage(nextID(), PacketRequestProfile.class,
                PacketRequestProfile::toBytes,
                PacketRequestProfile::new,
                PacketRequestProfile::handle);
        INSTANCE.registerMessage(nextID(), PacketReturnProfileToClient.class,
                PacketReturnProfileToClient::toBytes,
                PacketReturnProfileToClient::new,
                PacketReturnProfileToClient::handle);
    }
}
