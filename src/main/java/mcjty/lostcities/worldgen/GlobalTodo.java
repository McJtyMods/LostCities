package mcjty.lostcities.worldgen;

import mcjty.lostcities.setup.Config;
import mcjty.lostcities.varia.TodoQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class GlobalTodo extends SavedData implements IGlobalTodo {

    public static final String NAME = "LostCityTodo";
    // Todo is not persisted. It's currently only for saplings
    private TodoQueue<Consumer<ServerLevel>> todo = new TodoQueue<>();
    // This is for spawners and is more important (and persisted)
    private TodoQueue<Pair<BlockState, ResourceLocation>> todoSpawners = new TodoQueue<>();
    // This is generic block entity data that still has to be placed in the world
    private TodoQueue<Pair<BlockState, CompoundTag>> todoBlockEntities = new TodoQueue<>();
    // Todo blocks that require POI
    private TodoQueue<BlockState> todoPoi = new TodoQueue<>();

    @Nonnull
    public static IGlobalTodo getData(Level world) {
        if (world.isClientSide) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionDataStorage storage = ((ServerLevel) world).getDataStorage();
//        return storage.computeIfAbsent(GlobalTodo::new, GlobalTodo::new, NAME);
        return storage.computeIfAbsent(GlobalTodoV2::new, GlobalTodoV2::new, NAME);
    }

    public GlobalTodo() {
    }

    private static ServerLevel getOverworld() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getLevel(Level.OVERWORLD);
    }

    public GlobalTodo(CompoundTag nbt) {
        ListTag spawners = nbt.getList("spawners", Tag.TAG_COMPOUND);
        for (Tag spawner : spawners) {
            CompoundTag spawnerTag = (CompoundTag) spawner;
            BlockPos pos = NbtUtils.readBlockPos(spawnerTag, "pos").get();
            BlockState state = NbtUtils.readBlockState(getOverworld().holderLookup(Registries.BLOCK), spawnerTag.getCompound("state"));
            ResourceLocation entity = ResourceLocation.parse(spawnerTag.getString("entity"));
            addSpawnerTodo(pos, state, entity);
        }
        ListTag blockEntities = nbt.getList("blockentities", Tag.TAG_COMPOUND);
        for (Tag blockEntity : blockEntities) {
            CompoundTag blockEntityTag = (CompoundTag) blockEntity;
            BlockPos pos = NbtUtils.readBlockPos(blockEntityTag, "pos").get();
            BlockState state = NbtUtils.readBlockState(getOverworld().holderLookup(Registries.BLOCK), blockEntityTag.getCompound("state"));
            CompoundTag tag = blockEntityTag.getCompound("tag");
            addBlockEntityTodo(pos, state, tag);
        }
        ListTag poi = nbt.getList("poi", Tag.TAG_COMPOUND);
        for (Tag p : poi) {
            CompoundTag pTag = (CompoundTag) p;
            BlockPos pos = NbtUtils.readBlockPos(pTag, "pos").get();    // @todo 1.21 cleanup
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), pTag.getCompound("state"));
            addPoi(pos, state);
        }
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider holderLookup) {
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
        todoPoi.forEach((pos, state) -> {
            CompoundTag pTag = new CompoundTag();
            pTag.put("pos", NbtUtils.writeBlockPos(pos));
            pTag.put("state", NbtUtils.writeBlockState(state));
            poi.add(pTag);
        });
        tag.put("poi", poi);
        return tag;
    }

    @Override
    public void addTodo(BlockPos pos, Consumer<ServerLevel> code) {
        todo.add(pos, code);
        setDirty();
    }

    @Override
    public void addSpawnerTodo(BlockPos pos, BlockState spawnerState, ResourceLocation randomEntity) {
        todoSpawners.add(pos, Pair.of(spawnerState, randomEntity));
        setDirty();
    }

    @Override
    public void addBlockEntityTodo(BlockPos pos, BlockState state, CompoundTag tag) {
        todoBlockEntities.add(pos, Pair.of(state, tag));
        setDirty();
    }

    @Override
    public void addPoi(BlockPos pos, BlockState state) {
        todoPoi.add(pos, state);
        setDirty();
    }

    @Override
    public void executeAndClearTodo(ServerLevel level) {
        int todoSize = Config.TODO_QUEUE_SIZE.get();

        this.todo.forEach(todoSize, (pos, code) -> code.accept(level));

        this.todoSpawners.forEach(todoSize, (pos, pair) -> {
            BlockState spawnerState = pair.getLeft();
            ResourceLocation randomEntity = pair.getRight();
            if (level.getBlockState(pos).getBlock() == spawnerState.getBlock()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(pos, spawnerState, Block.UPDATE_CLIENTS);
                LostCityTerrainFeature.createSpawner(level, pos, randomEntity);
            }
        });

        this.todoBlockEntities.forEach(todoSize, (pos, pair) -> {
            CompoundTag tag = pair.getRight();
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null) {
                be.loadWithComponents(tag, null);   // @todo 1.21 FIX THIS
            }
        });

        this.todoPoi.forEach(todoSize, (pos, state) -> {
            if (level.getPoiManager().getType(pos).isEmpty()) {
                if (level.getBlockState(pos).getBlock() == state.getBlock()) {
                    level.setBlock(pos, state, Block.UPDATE_ALL);
                }
            }
        });
        setDirty();
    }
}
