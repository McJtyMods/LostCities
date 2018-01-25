package mcjty.lostcities.network;

import io.netty.buffer.ByteBuf;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.WorldTypeTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestProfile implements IMessage {

    private int dimension;

    @Override
    public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
    }

    public PacketRequestProfile() {
    }

    public PacketRequestProfile(int dimension) {
        this.dimension = dimension;
    }

    public static class Handler implements IMessageHandler<PacketRequestProfile, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestProfile message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketRequestProfile message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            LostCityProfile profile = WorldTypeTools.getProfile(DimensionManager.getWorld(message.dimension));
            PacketHandler.INSTANCE.sendTo(new PacketReturnProfileToClient(message.dimension, profile.getName()), player);
        }
    }

}
