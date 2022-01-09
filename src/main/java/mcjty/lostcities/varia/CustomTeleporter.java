package mcjty.lostcities.varia;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class CustomTeleporter extends PortalForcer {

    public CustomTeleporter(ServerLevel world, double x, double y, double z) {
        super(world);
        this.worldServer = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private final ServerLevel worldServer;
    private double x, y, z;

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        this.worldServer.getBlockState(new BlockPos((int) this.x, (int) this.y, (int) this.z));

        entity.setPos(this.x, this.y, this.z);
        entity.setDeltaMovement(0, 0, 0);
        return entity;
    }

    public static void teleportToDimension(Player player, ServerLevel dimension, BlockPos pos){
        teleportToDimension(player, dimension, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    public static void teleportToDimension(Player player, ServerLevel dimension, double x, double y, double z) {
        player.changeDimension(dimension, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                entity = repositionEntity.apply(false);
                entity.teleportTo(x, y, z);
                return entity;
            }
        });
    }

}
