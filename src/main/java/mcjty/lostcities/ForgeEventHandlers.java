package mcjty.lostcities;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.varia.CustomTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onPlayerSleepInBedEvent(PlayerSleepInBedEvent event) {
        if (LostCityConfiguration.DIMENSION_ID == -1) {
            return;
        }

        World world = event.getEntityPlayer().getEntityWorld();
        if (world.isRemote) {
            return;
        }
        BlockPos bedLocation = event.getPos();
        IBlockState state = world.getBlockState(bedLocation);
        EnumFacing direction = Blocks.BED.getBedDirection(state, world, bedLocation);
        Block b1 = world.getBlockState(bedLocation.down()).getBlock();
        Block b2 = world.getBlockState(bedLocation.offset(direction.getOpposite()).down()).getBlock();

        if (b1 == Blocks.DIAMOND_BLOCK && b2 == Blocks.DIAMOND_BLOCK) {
            // Check if the bed is surrounded by 6 skulls
            if (world.getBlockState(bedLocation.offset(direction)).getBlock() != Blocks.SKULL) {
                return;
            }
            if (world.getBlockState(bedLocation.offset(direction.rotateY())).getBlock() != Blocks.SKULL) {
                return;
            }
            if (world.getBlockState(bedLocation.offset(direction.rotateYCCW())).getBlock() != Blocks.SKULL) {
                return;
            }
            if (world.getBlockState(bedLocation.offset(direction.getOpposite(), 2)).getBlock() != Blocks.SKULL) {
                return;
            }
            if (world.getBlockState(bedLocation.offset(direction.getOpposite()).offset(direction.getOpposite().rotateY())).getBlock() != Blocks.SKULL) {
                return;
            }
            if (world.getBlockState(bedLocation.offset(direction.getOpposite()).offset(direction.getOpposite().rotateYCCW())).getBlock() != Blocks.SKULL) {
                return;
            }

            if (world.provider.getDimension() == LostCityConfiguration.DIMENSION_ID) {
                event.setResult(Event.Result.DENY);
                BlockPos top = DimensionManager.getWorld(0).getTopSolidOrLiquidBlock(bedLocation);
                CustomTeleporter.teleportToDimension(event.getEntityPlayer(), 0, top);
            } else {
                event.setResult(Event.Result.DENY);
                WorldServer worldServer = event.getEntity().getEntityWorld().getMinecraftServer().worldServerForDimension(LostCityConfiguration.DIMENSION_ID);
                BlockPos top = worldServer.getTopSolidOrLiquidBlock(bedLocation);
                CustomTeleporter.teleportToDimension(event.getEntityPlayer(), LostCityConfiguration.DIMENSION_ID, top);
            }
        }
    }

}
