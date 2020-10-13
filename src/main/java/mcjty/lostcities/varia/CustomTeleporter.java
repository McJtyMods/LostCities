package mcjty.lostcities.varia;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.function.Function;

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
    public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        this.worldServer.getBlockState(new BlockPos((int) this.x, (int) this.y, (int) this.z));

        entity.setPosition(this.x, this.y, this.z);
        entity.setMotion(0, 0, 0);
        return entity;
    }

    public static void teleportToDimension(PlayerEntity player, RegistryKey<World> dimension, BlockPos pos){
        teleportToDimension(player, dimension, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    public static void teleportToDimension(PlayerEntity player, RegistryKey<World> dimension, double x, double y, double z) {
        RegistryKey<World> oldDimension = player.getEntityWorld().getDimensionKey();
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
