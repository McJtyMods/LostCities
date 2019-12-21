package mcjty.lostcities.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRequestProfile {

    private DimensionType dimension;

    public PacketRequestProfile(PacketBuffer buf) {
        dimension = DimensionType.getById(buf.readInt());
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(dimension.getId());
    }

    public PacketRequestProfile() {
    }

    public PacketRequestProfile(DimensionType dimension) {
        this.dimension = dimension;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // @todo 1.14
//            ServerPlayerEntity player = ctx.get().getSender();
//            LostCityProfile profile = WorldTypeTools.getProfile(WorldTools.getWorld(dimension));
//            PacketHandler.INSTANCE.sendTo(new PacketReturnProfileToClient(dimension, profile.getName()), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.get().setPacketHandled(true);
    }
}
