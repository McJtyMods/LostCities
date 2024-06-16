package mcjty.lostcities.varia;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public class CustomTeleporter {

    public static void teleportToDimension(Player player, ServerLevel dimension, BlockPos pos){
        teleportToDimension(player, dimension, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    public static void teleportToDimension(Player player, ServerLevel dimension, double x, double y, double z) {
        player.changeDimension(new DimensionTransition(dimension, new Vec3(x, y, z), Vec3.ZERO, 0.0f, 0.0f, DimensionTransition.PLAY_PORTAL_SOUND));
    }

}
