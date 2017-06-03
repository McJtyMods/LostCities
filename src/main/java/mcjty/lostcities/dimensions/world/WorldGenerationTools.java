package mcjty.lostcities.dimensions.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenerationTools {

    public static int findUpsideDownEmptySpot(World world, int x, int z) {
        for (int y = 90 ; y > 0 ; y--) {
            if (world.isAirBlock(new BlockPos(x, y, z)) && world.isAirBlock(new BlockPos(x, y+1, z)) && world.isAirBlock(new BlockPos(x, y+2, z))
                    && world.isAirBlock(new BlockPos(x, y+3, z)) && world.isAirBlock(new BlockPos(x, y+4, z))) {
                return y;
            }
        }
        return -1;
    }



    public static int findSuitableEmptySpot(World world, int x, int z) {
        int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
        if (y == -1) {
            return -1;
        }

        y--;            // y should now be at a solid or liquid block.

        if (y > world.getHeight() - 5) {
            y = world.getHeight() / 2;
        }


        IBlockState state = world.getBlockState(new BlockPos(x, y + 1, z));
        Block block = state.getBlock();
        while (block.getMaterial(state).isLiquid()) {
            y++;
            if (y > world.getHeight()-10) {
                return -1;
            }
            state = world.getBlockState(new BlockPos(x, y + 1, z));
            block = state.getBlock();
        }

        return y;
    }

    // Return true if this block is solid.
    public static boolean isSolid(World world, int x, int y, int z) {
        if (world.isAirBlock(new BlockPos(x, y, z))) {
            return false;
        }
        IBlockState state = world.getBlockState(new BlockPos(x, y, z));
        Block block = state.getBlock();
        return block.getMaterial(state).blocksMovement();
    }

    // Return true if this block is solid.
    public static boolean isAir(World world, int x, int y, int z) {
        if (world.isAirBlock(new BlockPos(x, y, z))) {
            return true;
        }
        Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        return block == null;
    }

    // Starting at the current height, go down and fill all air blocks with stone until a
    // non-air block is encountered.
    public static void fillEmptyWithStone(World world, int x, int y, int z) {
        while (y > 0 && !isSolid(world, x, y, z)) {
            world.setBlockState(new BlockPos(x, y, z), Blocks.STONE.getDefaultState(), 2);
            y--;
        }
    }
}
