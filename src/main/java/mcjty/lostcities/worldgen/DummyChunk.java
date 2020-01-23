package mcjty.lostcities.worldgen;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.lighting.WorldLightManager;

import javax.annotation.Nullable;

/**
 * This chunk only serves to maintain a heightmap
 */
public class DummyChunk extends ChunkPrimer {

    private final ChunkHeightmap heightmap;

    public DummyChunk(ChunkPos pos, ChunkHeightmap heightmap) {
        super(pos, UpgradeData.EMPTY);
        this.heightmap = heightmap;
    }

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        heightmap.update(pos.getX() & 0xf, pos.getY(), pos.getZ() & 0xf, state);
        return super.setBlockState(pos, state, isMoving);
    }

    @Override
    public void addLightValue(short packedPosition, int lightValue) {
        // Do nothing
    }

    @Override
    public void addLightPosition(BlockPos lightPos) {
        // Do nothing
    }

    @Override
    public void setLightManager(WorldLightManager p_217306_1_) {
        super.setLightManager(p_217306_1_);
    }
}
