package mcjty.lostcities.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public interface IGlobalTodo {
    void addTodo(BlockPos pos, Consumer<ServerLevel> code);

    void addSpawnerTodo(BlockPos pos, BlockState spawnerState, ResourceLocation randomEntity);

    void addBlockEntityTodo(BlockPos pos, BlockState state, CompoundTag tag);

    void addPoi(BlockPos pos, BlockState state);

    void executeAndClearTodo(ServerLevel level);
}
