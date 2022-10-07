package mcjty.lostcities.playerdata;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PropertiesDispatcher implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private PlayerSpawnSet playerSpawnSet = null;
    private LazyOptional<PlayerSpawnSet> opt = LazyOptional.of(this::createPlayerSpawnSet);

    @Nonnull
    private PlayerSpawnSet createPlayerSpawnSet() {
        if (playerSpawnSet == null) {
            playerSpawnSet = new PlayerSpawnSet();
        }
        return playerSpawnSet;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == PlayerProperties.PLAYER_SPAWN_SET) {
            return opt.cast();
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerSpawnSet().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerSpawnSet().loadNBTData(nbt);
    }
}
