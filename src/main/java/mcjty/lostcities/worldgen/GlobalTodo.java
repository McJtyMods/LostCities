package mcjty.lostcities.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GlobalTodo extends SavedData {

    public static final String NAME = "LostCityTodo";
    // Todo is not persisted. It's currently only for saplings
    private Map<BlockPos, Consumer<ServerLevel>> todo = new HashMap<>();
    // This is for spawners and is more important
    private Map<BlockPos, Pair<BlockState, ResourceLocation>> todoSpawners = new HashMap<>();

    @Nonnull
    public static GlobalTodo getData(Level world) {
        if (world.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionDataStorage storage = ((ServerLevel) world).getDataStorage();
        return storage.computeIfAbsent(GlobalTodo::new, GlobalTodo::new, NAME);
    }

    public GlobalTodo() {
    }

    public GlobalTodo(CompoundTag nbt) {
        ListTag spawners = nbt.getList("spawners", Tag.TAG_COMPOUND);
        for (Tag spawner : spawners) {
            CompoundTag spawnerTag = (CompoundTag) spawner;
            BlockPos pos = NbtUtils.readBlockPos(spawnerTag.getCompound("pos"));
            BlockState state = NbtUtils.readBlockState(spawnerTag.getCompound("state"));
            ResourceLocation entity = new ResourceLocation(spawnerTag.getString("entity"));
            addSpawnerTodo(pos, state, entity);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag spawners = new ListTag();
        todoSpawners.forEach((pos, pair) -> {
            CompoundTag spawnerTag = new CompoundTag();
            spawnerTag.put("pos", NbtUtils.writeBlockPos(pos));
            spawnerTag.put("state", NbtUtils.writeBlockState(pair.getLeft()));
            spawnerTag.putString("entity", pair.getRight().toString());
            spawners.add(spawnerTag);
        });
        tag.put("spawners", spawners);
        return tag;
    }

    public void addTodo(BlockPos pos, Consumer<ServerLevel> code) {
        todo.put(pos, code);
    }

    public void addSpawnerTodo(BlockPos pos, BlockState spawnerState, ResourceLocation randomEntity) {
        todo.put(pos, level -> {
            if (level.getBlockState(pos).getBlock() == spawnerState.getBlock()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(pos, spawnerState, Block.UPDATE_CLIENTS);
                LostCityTerrainFeature.createSpawner(level, pos, randomEntity);
            }
        });
    }

    public void executeAndClearTodo(ServerLevel level) {
        Map<BlockPos, Consumer<ServerLevel>> copy = this.todo;
        this.todo = new HashMap<>();
        copy.forEach((pos, code) -> code.accept(level));

        Map<BlockPos, Pair<BlockState, ResourceLocation>> copySpawners = this.todoSpawners;
        this.todoSpawners = new HashMap<>();
        copySpawners.forEach((pos, pair) -> {
            BlockState spawnerState = pair.getLeft();
            ResourceLocation randomEntity = pair.getRight();
            if (level.getBlockState(pos).getBlock() == spawnerState.getBlock()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(pos, spawnerState, Block.UPDATE_CLIENTS);
                LostCityTerrainFeature.createSpawner(level, pos, randomEntity);
            }
        });
    }
}