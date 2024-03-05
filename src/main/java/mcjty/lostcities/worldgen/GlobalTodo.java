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
import net.minecraft.world.level.block.entity.BlockEntity;
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
    // This is for spawners and is more important (and persisted)
    private Map<BlockPos, Pair<BlockState, ResourceLocation>> todoSpawners = new HashMap<>();
    // This is generic block entity data that still has to be placed in the world
    private Map<BlockPos, Pair<BlockState, CompoundTag>> todoBlockEntities = new HashMap<>();
    // Todo blocks that require POI
    private Map<BlockPos, BlockState> todoPoi = new HashMap<>();

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
        ListTag blockEntities = new ListTag();
        todoBlockEntities.forEach((pos, pair) -> {
            CompoundTag blockEntityTag = new CompoundTag();
            blockEntityTag.put("pos", NbtUtils.writeBlockPos(pos));
            blockEntityTag.put("state", NbtUtils.writeBlockState(pair.getLeft()));
            blockEntityTag.put("tag", pair.getRight());
            blockEntities.add(blockEntityTag);
        });
        tag.put("blockentities", blockEntities);
        ListTag poi = new ListTag();
        todoPoi.entrySet().forEach(entry -> {
            CompoundTag pTag = new CompoundTag();
            pTag.put("pos", NbtUtils.writeBlockPos(entry.getKey()));
            pTag.put("state", NbtUtils.writeBlockState(entry.getValue()));
            poi.add(pTag);
        });
        return tag;
    }

    public void addTodo(BlockPos pos, Consumer<ServerLevel> code) {
        todo.put(pos, code);
    }

    public void addSpawnerTodo(BlockPos pos, BlockState spawnerState, ResourceLocation randomEntity) {
        todoSpawners.put(pos, Pair.of(spawnerState, randomEntity));
    }

    public void addBlockEntityTodo(BlockPos pos, BlockState state, CompoundTag tag) {
        todoBlockEntities.put(pos, Pair.of(state, tag));
    }

    public void addPoi(BlockPos pos, BlockState state) {
        todoPoi.put(pos, state);
    }

    public void executeAndClearTodo(ServerLevel level) {
        Map<BlockPos, Consumer<ServerLevel>> copy = this.todo;
        this.todo = new HashMap<>();
        copy.forEach((pos, code) -> code.accept(level));

        var copySpawners = this.todoSpawners;
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

        var copyBlockEntities = this.todoBlockEntities;
        this.todoBlockEntities = new HashMap<>();
        copyBlockEntities.forEach((pos, pair) -> {
            BlockState state = pair.getLeft();
            CompoundTag tag = pair.getRight();
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                be.load(tag);
            }
        });

        var copyPoi = this.todoPoi;
        this.todoPoi = new HashMap<>();
        copyPoi.entrySet().forEach(entry -> {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            if (!level.getPoiManager().getType(pos).isPresent()) {
                if (level.getBlockState(pos).getBlock() == state.getBlock()) {
                    level.setBlock(pos, state, Block.UPDATE_ALL);
                }
            }
        });
    }
}