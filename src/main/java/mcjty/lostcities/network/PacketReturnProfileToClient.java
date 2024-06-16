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

public record PacketReturnProfileToClient(ResourceKey<Level> dimension, String profile) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(LostCities.MODID, "returnprofile");
    public static final CustomPacketPayload.Type<PacketReturnProfileToClient> TYPE = new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketReturnProfileToClient> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), PacketReturnProfileToClient::dimension,
            ByteBufCodecs.STRING_UTF8, PacketReturnProfileToClient::profile,
            PacketReturnProfileToClient::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // @todo 1.14
//            WorldTypeTools.setProfileFromServer(dimension, profile);
        });
    }
}
