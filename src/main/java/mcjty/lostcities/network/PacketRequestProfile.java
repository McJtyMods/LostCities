package mcjty.lostcities.network;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRequestProfile {

    private final ResourceKey<Level> dimension;

    public PacketRequestProfile(FriendlyByteBuf buf) {
        dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension.location());
    }

    public PacketRequestProfile(ResourceKey<Level> dimension) {
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
