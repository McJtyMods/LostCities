package mcjty.lostcities.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketReturnProfileToClient {

    private RegistryKey<World> dimension;
    private String profile;

    public PacketReturnProfileToClient(PacketBuffer buf) {
        dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
        profile = buf.readUtf(32767);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(dimension.location());
        buf.writeUtf(profile);
    }

    public PacketReturnProfileToClient() {
    }

    public PacketReturnProfileToClient(RegistryKey<World> dimension, String profileName) {
        this.dimension = dimension;
        this.profile = profileName;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // @todo 1.14
//            WorldTypeTools.setProfileFromServer(dimension, profile);
        });
        ctx.get().setPacketHandled(true);
    }
}
