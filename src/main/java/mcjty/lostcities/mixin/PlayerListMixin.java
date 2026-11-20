package mcjty.lostcities.mixin;

import mcjty.lostcities.setup.CavernSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "load", at = @At("RETURN"))
    private void lostcities$useCavernSpawn(ServerPlayer player, CallbackInfoReturnable<Optional<CompoundTag>> callback) {
        if (callback.getReturnValue().isEmpty()) {
            BlockPos spawn = CavernSpawn.resolveWorldSpawn(player.serverLevel());
            if (spawn != null) {
                player.moveTo(Vec3.atBottomCenterOf(spawn), player.getYRot(), player.getXRot());
            }
        }
    }
}
