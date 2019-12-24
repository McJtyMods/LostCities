package mcjty.lostcities.network;

import mcjty.lostcities.config.LostCityProfile;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class PacketProfileToServer {

    private String profileName;
    @Nullable private LostCityProfile profile;

    public PacketProfileToServer(PacketBuffer buf) {
        profileName = buf.readString(32767);
        if (buf.readBoolean()) {
            profile = new LostCityProfile(profileName, buf);
        }
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeString(profileName);
        buf.writeBoolean(profile != null);
        if (profile != null) {
            profile.toBytes(buf);
        }
    }

    public PacketProfileToServer() {
    }

    public PacketProfileToServer(String profileName, @Nullable LostCityProfile profile) {
        this.profileName = profileName;
        this.profile = profile;
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // @todo 1.14
//            WorldTypeTools.setProfileFromServer(dimension, profile);
        });
        ctx.get().setPacketHandled(true);
    }
}
