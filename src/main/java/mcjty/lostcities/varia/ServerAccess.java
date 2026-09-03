package mcjty.lostcities.varia;

import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public final class ServerAccess {
    private static MinecraftServer currentServer;

    private ServerAccess() {
    }

    public static void setServer(@Nullable MinecraftServer server) {
        currentServer = server;
    }

    @Nullable
    public static MinecraftServer getServer() {
        return currentServer;
    }
}
