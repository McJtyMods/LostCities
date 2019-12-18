package mcjty.lostcities.network;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.WorldTypeTools;
import mcjty.lostcities.varia.WorldTools;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkDirection;
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
            ServerPlayerEntity player = ctx.get().getSender();
            LostCityProfile profile = WorldTypeTools.getProfile(WorldTools.getWorld(dimension));
            PacketHandler.INSTANCE.sendTo(new PacketReturnProfileToClient(dimension, profile.getName()), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.get().setPacketHandled(true);
    }
}
