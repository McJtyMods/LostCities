package mcjty.lostcities.mixin;

import mcjty.lostcities.setup.CavernSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(PlayerSpawnFinder.class)
public abstract class PlayerSpawnFinderMixin {

    @Inject(method = "findSpawn", at = @At("HEAD"), cancellable = true)
    private static void lostcities$useCavernSpawn(ServerLevel level, BlockPos suggestion,
                                                   CallbackInfoReturnable<CompletableFuture<Vec3>> callback) {
        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }
        MinecraftServer server = level.getServer();
        if (!server.isSameThread()) {
            // Login preparation can run on the network thread. Terrain checks
            // and any repair to the shared spawn belong on the server thread.
            callback.setReturnValue(server.submit(() -> PlayerSpawnFinder.findSpawn(level, suggestion))
                    .thenCompose(Function.identity()));
            return;
        }
        // Saved player positions and valid personal respawns bypass this finder.
        BlockPos spawn = CavernSpawn.resolveWorldSpawn(level);
        if (spawn != null) {
            callback.setReturnValue(CompletableFuture.completedFuture(Vec3.atBottomCenterOf(spawn)));
        }
    }
}
