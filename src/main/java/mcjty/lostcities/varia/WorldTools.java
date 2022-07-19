package mcjty.lostcities.varia;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

public class WorldTools {

    public static boolean chunkLoaded(Level world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }
        return world.hasChunkAt(pos);
//        return world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) != null && world.getChunkFromBlockCoords(pos).isLoaded();
    }

    public static ServerLevel getOverworld() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getLevel(Level.OVERWORLD);
    }

    public static ServerLevel getOverworld(Level world) {
        MinecraftServer server = world.getServer();
        return server.getLevel(Level.OVERWORLD);
    }

    public static ServerLevel loadWorld(ResourceKey<Level> type) {
        ServerLevel world = getWorld(type);
        if (world == null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            return server.getLevel(type);
        }
        return world;
    }

    public static ServerLevel getWorld(ResourceKey<Level> type) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getLevel(type);
    }

    public static ServerLevel getWorld(Level world, ResourceKey<Level> type) {
        MinecraftServer server = world.getServer();
        return server.getLevel(type);
    }

}
