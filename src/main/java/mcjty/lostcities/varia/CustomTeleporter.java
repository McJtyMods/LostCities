package mcjty.lostcities.varia;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;

public class CustomTeleporter extends Teleporter {

    public CustomTeleporter(ServerWorld world, double x, double y, double z) {
        super(world);
        this.worldServer = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private final ServerWorld worldServer;
    private double x, y, z;

    @Override
    public boolean placeInPortal(@Nonnull Entity entity, float rotationYaw) {
        this.worldServer.getBlockState(new BlockPos((int) this.x, (int) this.y, (int) this.z));

        entity.setPosition(this.x, this.y, this.z);
        entity.setMotion(0, 0, 0);
        return true;
    }

    public static void teleportToDimension(PlayerEntity player, DimensionType dimension, BlockPos pos){
        teleportToDimension(player, dimension, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    public static void teleportToDimension(PlayerEntity player, DimensionType dimension, double x, double y, double z) {
        DimensionType oldDimension = player.getEntityWorld().getDimension().getType();
        ServerPlayerEntity entityPlayerMP = (ServerPlayerEntity) player;
        MinecraftServer server = player.getEntityWorld().getServer();
        ServerWorld worldServer = server.getWorld(dimension);
        player.addExperienceLevel(0);

        if (worldServer == null || worldServer.getServer() == null){ //Dimension doesn't exist
            throw new IllegalArgumentException("Dimension: "+dimension+" doesn't exist!");
        }

        // @todo 1.14
//        worldServer.getServer().getPlayerList().transferPlayerToDimension(entityPlayerMP, dimension, new CustomTeleporter(worldServer, x, y, z));
//        player.setPositionAndUpdate(x, y, z);
//        if (oldDimension == 1) {
//            // For some reason teleporting out of the end does weird things.
//            player.setPositionAndUpdate(x, y, z);
//            worldServer.spawnEntity(player);
//            worldServer.updateEntityWithOptionalForce(player, false);
//        }
    }

}
