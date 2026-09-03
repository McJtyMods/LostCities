package mcjty.lostcities.mixin;

import mcjty.lostcities.setup.ForgeEventHandlers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric replacement for NeoForge's new-world spawn-position event.
 */
@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "setInitialSpawn", at = @At("HEAD"), cancellable = true)
    private static void lostcities$onSetInitialSpawn(ServerLevel level, ServerLevelData levelData, boolean spawnBonusChest,
                                                     boolean isDebug, LevelLoadListener levelLoadListener, CallbackInfo ci) {
        if (!isDebug && ForgeEventHandlers.INSTANCE.onCreateSpawnPoint(level, levelData)) {
            ci.cancel();
        }
    }
}
