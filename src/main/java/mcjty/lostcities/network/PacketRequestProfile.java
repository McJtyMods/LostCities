package mcjty.lostcities.network;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRequestProfile {

    private RegistryKey<World> dimension;

    public PacketRequestProfile(PacketBuffer buf) {
        dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, buf.readResourceLocation());
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeResourceLocation(dimension.getLocation());
    }

    public PacketRequestProfile() {
    }

    public PacketRequestProfile(RegistryKey<World> dimension) {
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
