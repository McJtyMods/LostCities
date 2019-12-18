package mcjty.lostcities.network;

import mcjty.lostcities.dimensions.world.WorldTypeTools;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketReturnProfileToClient {

    private DimensionType dimension;
    private String profile;

    public PacketReturnProfileToClient(PacketBuffer buf) {
        dimension = DimensionType.getById(buf.readInt());
        profile = buf.readString(32767);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(dimension.getId());
        buf.writeString(profile);
    }

    public PacketReturnProfileToClient() {
    }

    public PacketReturnProfileToClient(DimensionType dimension, String profileName) {
        this.dimension = dimension;
        this.profile = profileName;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            WorldTypeTools.setProfileFromServer(dimension, profile);
        });
        ctx.get().setPacketHandled(true);
    }
}
