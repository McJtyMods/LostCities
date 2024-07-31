package mcjty.lostcities.worldgen;

import mcjty.lostcities.setup.Config;
import mcjty.lostcities.varia.TodoQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GlobalTodoV2 extends SavedData implements IGlobalTodo {

    public static final String NAME = "LostCityTodo";

    record TodoQueues(TodoQueue<Consumer<ServerLevel>> todo,                      // Todo is not persisted. It's currently only for saplings
                      TodoQueue<Pair<BlockState, ResourceLocation>> todoSpawners, // This is for spawners and is more important (and persisted)
                      TodoQueue<Pair<BlockState, CompoundTag>> todoBlockEntities, // This is generic block entity data that still has to be placed in the world
                      TodoQueue<BlockState> todoPoi) {                            // Todo blocks that require POI
        // Return true if all queues are empty
        public boolean isEmpty() {
            return todo.isEmpty() && todoSpawners.isEmpty() && todoBlockEntities.isEmpty() && todoPoi.isEmpty();
        }
    }

    private Map<ChunkPos, TodoQueues> todoQueues = new HashMap<>();

    @Nonnull
    public static GlobalTodoV2 getData(Level world) {
        if (world.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionDataStorage storage = ((ServerLevel) world).getDataStorage();
        return storage.computeIfAbsent(GlobalTodoV2::new, GlobalTodoV2::new, NAME);
    }

    public GlobalTodoV2() {
    }

    public GlobalTodoV2(CompoundTag nbt) {
        ListTag spawners = nbt.getList("spawners", Tag.TAG_COMPOUND);
        for (Tag spawner : spawners) {
            CompoundTag spawnerTag = (CompoundTag) spawner;
            BlockPos pos = NbtUtils.readBlockPos(spawnerTag.getCompound("pos"));
            BlockState state = NbtUtils.readBlockState(spawnerTag.getCompound("state"));
            ResourceLocation entity = new ResourceLocation(spawnerTag.getString("entity"));
            addSpawnerTodo(pos, state, entity);
        }
        ListTag blockEntities = nbt.getList("blockentities", Tag.TAG_COMPOUND);
        for (Tag blockEntity : blockEntities) {
            CompoundTag blockEntityTag = (CompoundTag) blockEntity;
            BlockPos pos = NbtUtils.readBlockPos(blockEntityTag.getCompound("pos"));
            BlockState state = NbtUtils.readBlockState(blockEntityTag.getCompound("state"));
            CompoundTag tag = blockEntityTag.getCompound("tag");
            addBlockEntityTodo(pos, state, tag);
        }
        ListTag poi = nbt.getList("poi", Tag.TAG_COMPOUND);
        for (Tag p : poi) {
            CompoundTag pTag = (CompoundTag) p;
            BlockPos pos = NbtUtils.readBlockPos(pTag.getCompound("pos"));
            BlockState state = NbtUtils.readBlockState(pTag.getCompound("state"));
            addPoi(pos, state);
        }
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag spawners = new ListTag();
        ListTag blockEntities = new ListTag();
        ListTag poi = new ListTag();
        todoQueues.forEach((chunkPos, queues) -> {
            queues.todoSpawners.forEach((pos, pair) -> {
                CompoundTag spawnerTag = new CompoundTag();
                spawnerTag.put("pos", NbtUtils.writeBlockPos(pos));
                spawnerTag.put("state", NbtUtils.writeBlockState(pair.getLeft()));
                spawnerTag.putString("entity", pair.getRight().toString());
                spawners.add(spawnerTag);
            });
            queues.todoBlockEntities.forEach((pos, pair) -> {
                CompoundTag blockEntityTag = new CompoundTag();
                blockEntityTag.put("pos", NbtUtils.writeBlockPos(pos));
                blockEntityTag.put("state", NbtUtils.writeBlockState(pair.getLeft()));
                blockEntityTag.put("tag", pair.getRight());
                blockEntities.add(blockEntityTag);
            });
            queues.todoPoi.forEach((pos, state) -> {
                CompoundTag pTag = new CompoundTag();
                pTag.put("pos", NbtUtils.writeBlockPos(pos));
                pTag.put("state", NbtUtils.writeBlockState(state));
                poi.add(pTag);
            });
        });
        tag.put("spawners", spawners);
        tag.put("blockentities", blockEntities);
        tag.put("poi", poi);
        return tag;
    }

    @Override
    public void addTodo(BlockPos pos, Consumer<ServerLevel> code) {
        ChunkPos chunkPos = new ChunkPos(pos);
        TodoQueues queues = todoQueues.computeIfAbsent(chunkPos, k -> new TodoQueues(new TodoQueue<>(), new TodoQueue<>(), new TodoQueue<>(), new TodoQueue<>()));
        queues.todo.add(pos, code);
        setDirty();
    }

    @Override
    public void addSpawnerTodo(BlockPos pos, BlockState spawnerState, ResourceLocation randomEntity) {
        ChunkPos chunkPos = new ChunkPos(pos);
        TodoQueues queues = todoQueues.computeIfAbsent(chunkPos, k -> new TodoQueues(new TodoQueue<>(), new TodoQueue<>(), new TodoQueue<>(), new TodoQueue<>()));
        queues.todoSpawners.add(pos, Pair.of(spawnerState, randomEntity));
        setDirty();
    }

    @Override
    public void addBlockEntityTodo(BlockPos pos, BlockState state, CompoundTag tag) {
        ChunkPos chunkPos = new ChunkPos(pos);
        TodoQueues queues = todoQueues.computeIfAbsent(chunkPos, k -> new TodoQueues(new TodoQueue<>(), new TodoQueue<>(), new TodoQueue<>(), new TodoQueue<>()));
        queues.todoBlockEntities.add(pos, Pair.of(state, tag));
        setDirty();
    }

    @Override
    public void addPoi(BlockPos pos, BlockState state) {
        ChunkPos chunkPos = new ChunkPos(pos);
        TodoQueues queues = todoQueues.computeIfAbsent(chunkPos, k -> new TodoQueues(new TodoQueue<>(), new TodoQueue<>(), new TodoQueue<>(), new TodoQueue<>()));
        queues.todoPoi.add(pos, state);
        setDirty();
    }

    @Override
    public void executeAndClearTodo(ServerLevel level) {
        int todoSize = Config.TODO_QUEUE_SIZE.get();

        // @todo process chunks based on their distance to the player
        Set<ChunkPos> todoToRemove = new HashSet<>();
        for (Map.Entry<ChunkPos, TodoQueues> entry : todoQueues.entrySet()) {
            TodoQueues queues = entry.getValue();
            todoSize -= queues.todo.forEach(todoSize, (pos, code) -> code.accept(level));
            todoSize -= queues.todoSpawners.forEach(todoSize, (pos, pair) -> {
                BlockState spawnerState = pair.getLeft();
                ResourceLocation randomEntity = pair.getRight();
                if (level.getBlockState(pos).getBlock() == spawnerState.getBlock()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.setBlock(pos, spawnerState, Block.UPDATE_CLIENTS);
                    LostCityTerrainFeature.createSpawner(level, pos, randomEntity);
                }
            });
            todoSize -= queues.todoBlockEntities.forEach(todoSize, (pos, pair) -> {
                CompoundTag tag = pair.getRight();
                BlockEntity be = level.getBlockEntity(pos);
                if (be != null) {
                    be.load(tag);
                }
            });
            todoSize -= queues.todoPoi.forEach(todoSize, (pos, state) -> {
                if (level.getPoiManager().getType(pos).isEmpty()) {
                    if (level.getBlockState(pos).getBlock() == state.getBlock()) {
                        level.setBlock(pos, state, Block.UPDATE_ALL);
                    }
                }
            });
            if (queues.isEmpty()) {
                todoToRemove.add(entry.getKey());
            }
            if (todoSize <= 0) {
                break;
            }
        }

        // Remove all empty todo queues
        todoToRemove.forEach(todoQueues::remove);

        setDirty();
    }
}