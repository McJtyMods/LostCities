package mcjty.lostcities.worldgen;

import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.jetbrains.annotations.Nullable;

/**
 * This chunk only serves to maintain a heightmap
 */
public class DummyChunk extends ProtoChunk {
    // @todo 1.18
    public DummyChunk(ChunkPos p_188167_, UpgradeData p_188168_, LevelHeightAccessor p_188169_, Registry<Biome> p_188170_, @Nullable BlendingData p_188171_) {
        super(p_188167_, p_188168_, p_188169_, p_188170_, p_188171_);
    }

    // @todo 1.18
    public DummyChunk(ChunkPos p_188173_, UpgradeData p_188174_, @Nullable LevelChunkSection[] p_188175_, ProtoChunkTicks<Block> p_188176_, ProtoChunkTicks<Fluid> p_188177_, LevelHeightAccessor p_188178_, Registry<Biome> p_188179_, @Nullable BlendingData p_188180_) {
        super(p_188173_, p_188174_, p_188175_, p_188176_, p_188177_, p_188178_, p_188179_, p_188180_);
    }
// @todo 1.18
//    private final ChunkHeightmap heightmap;
//
//    public DummyChunk(ChunkPos p_188167_, UpgradeData p_188168_, LevelHeightAccessor p_188169_, Registry<Biome> p_188170_, @org.jetbrains.annotations.Nullable BlendingData p_188171_) {
//        super(p_188167_, p_188168_, p_188169_, p_188170_, p_188171_);
//    }
//
//    public DummyChunk(ChunkPos p_188173_, UpgradeData p_188174_, @org.jetbrains.annotations.Nullable LevelChunkSection[] p_188175_, ProtoChunkTicks<Block> p_188176_, ProtoChunkTicks<Fluid> p_188177_, LevelHeightAccessor p_188178_, Registry<Biome> p_188179_, @org.jetbrains.annotations.Nullable BlendingData p_188180_) {
//        super(p_188173_, p_188174_, p_188175_, p_188176_, p_188177_, p_188178_, p_188179_, p_188180_);
//    }
//
//    public DummyChunk(ChunkPos pos, ChunkHeightmap heightmap) {
//        super(pos, UpgradeData.EMPTY, null, );
//        this.heightmap = heightmap;
//    }
//
//    @Nullable
//    @Override
//    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
//        heightmap.update(pos.getX() & 0xf, pos.getY(), pos.getZ() & 0xf, state);
//        return super.setBlockState(pos, state, isMoving);
//    }
//
//    @Override
//    public BlockState getBlockState(BlockPos pos) {
//        if (pos.getY() >= heightmap.getHeight(pos.getX() & 0xf, pos.getZ() & 0xf)) {
//            return Blocks.AIR.defaultBlockState();
//        } else {
//            return Blocks.STONE.defaultBlockState();
//        }
////        return super.getBlockState(pos);
//    }
//
//    @Override
//    public int getHeight(Heightmap.Types type, int x, int z) {
//        return heightmap.getHeight(x & 0xf, z & 0xf);
//    }
//
//    @Override
//    public void addLight(short packedPosition, int lightValue) {
//        // Do nothing
//    }
//
//    @Override
//    public void addLight(BlockPos lightPos) {
//        // Do nothing
//    }
//
//    @Override
//    public void setLightEngine(LevelLightEngine p_217306_1_) {
//        super.setLightEngine(p_217306_1_);
//    }
}
