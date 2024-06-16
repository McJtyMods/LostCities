package mcjty.lostcities.network;

import mcjty.lostcities.LostCities;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketRequestProfile(ResourceKey<Level> dimension) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LostCities.MODID, "requestproofile");
    public static final CustomPacketPayload.Type<PacketRequestProfile> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRequestProfile> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), PacketRequestProfile::dimension,
            PacketRequestProfile::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // @todo 1.14
//            ServerPlayerEntity player = ctx.get().getSender();
//            LostCityProfile profile = WorldTypeTools.getProfile(WorldTools.getWorld(dimension));
//            PacketHandler.INSTANCE.sendTo(new PacketRequestProfile(dimension, profile.getName()), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
    }
}
