package mcjty.lostcities.dimensions.world.terrain.lost;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;

public class Style {
    public IBlockState street;
    public IBlockState street2;
    public IBlockState glass;
    public IBlockState glass_full;
    public IBlockState quartz;
    public IBlockState bricks;
    public IBlockState bricks_variant;
    public IBlockState bricks_cracked;
    public IBlockState bricks_mossy;
    public IBlockState bricks_monster;

    public boolean isGlass(IBlockState b) {
        return b != null && (b == glass || b == glass_full);
    }

    public boolean canBeDamagedToIronBars(IBlockState b) {
        return b != null && (b == bricks || b == bricks_cracked || b == bricks_mossy
                || b == bricks_variant || b == quartz);
    }

    public boolean isEasyToDestroy(IBlockState b) {
        return b != null && (b == glass || b == glass_full);
    }

    public boolean isLiquid(IBlockState b) {
        return b != null && (b.getBlock() instanceof BlockLiquid || b.getBlock() instanceof BlockDynamicLiquid);
    }
}
