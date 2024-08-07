package mcjty.lostcities.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class CommandTestFill implements Command<CommandSourceStack> {

    private static final CommandTestFill CMD = new CommandTestFill();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("testfill")
                .requires(cs -> cs.hasPermission(0))
                .executes(CMD);
    }

    private static final Random random = new Random();

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // Fill a 3x3 area from the current position with stairs in a circular pattern. Do this for
        // several layers, each time with a different type of stair
        ServerLevel world = context.getSource().getLevel();
        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos pos = player.blockPosition();

        Block[] blocks = new Block[] {
                Blocks.SANDSTONE_STAIRS,
                Blocks.STONE_BRICK_STAIRS,
                Blocks.BRICK_STAIRS,
                Blocks.NETHER_BRICK_STAIRS,
                Blocks.QUARTZ_STAIRS,
                Blocks.RED_SANDSTONE_STAIRS,
                Blocks.PURPUR_STAIRS,
                Blocks.PRISMARINE_BRICK_STAIRS,
                Blocks.DARK_PRISMARINE_STAIRS,
                Blocks.POLISHED_ANDESITE_STAIRS,
                Blocks.POLISHED_DIORITE_STAIRS,
                Blocks.POLISHED_GRANITE_STAIRS,
                Blocks.END_STONE_BRICK_STAIRS,
                Blocks.BLACKSTONE_STAIRS,
                Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS,
                Blocks.CRIMSON_STAIRS,
                Blocks.WARPED_STAIRS,
                Blocks.SANDSTONE_SLAB,
                Blocks.STONE_BRICK_SLAB,
                Blocks.BRICK_SLAB,
                Blocks.NETHER_BRICK_SLAB,
                Blocks.QUARTZ_SLAB,
                Blocks.RED_SANDSTONE_SLAB,
                Blocks.PURPUR_SLAB,
                Blocks.PRISMARINE_BRICK_SLAB,
                Blocks.DARK_PRISMARINE_SLAB,
                Blocks.POLISHED_ANDESITE_SLAB,
                Blocks.POLISHED_DIORITE_SLAB,
                Blocks.POLISHED_GRANITE_SLAB,
                Blocks.END_STONE_BRICK_SLAB,
                Blocks.BLACKSTONE_SLAB,
                Blocks.POLISHED_BLACKSTONE_BRICK_SLAB,
                Blocks.CRIMSON_SLAB,
                Blocks.WARPED_SLAB,
                Blocks.ANDESITE_WALL,
                Blocks.DIORITE_WALL,
                Blocks.GRANITE_WALL,
                Blocks.SANDSTONE_WALL,
                Blocks.RED_SANDSTONE_WALL,
                Blocks.STONE_BRICK_WALL,
                Blocks.NETHER_BRICK_WALL,
                Blocks.BRICK_WALL,
                Blocks.PRISMARINE_WALL,
                Blocks.ACACIA_PLANKS,
                Blocks.BIRCH_PLANKS,
                Blocks.CRIMSON_PLANKS,
                Blocks.DARK_OAK_PLANKS,
                Blocks.JUNGLE_PLANKS,
                Blocks.OAK_PLANKS,
                Blocks.SPRUCE_PLANKS,
                Blocks.WARPED_PLANKS,
                Blocks.ACACIA_SLAB,
                Blocks.GLASS,
                Blocks.GLOWSTONE,
                Blocks.OAK_FENCE,
                Blocks.OAK_FENCE_GATE,
                Blocks.BLACK_STAINED_GLASS,
                Blocks.BLUE_STAINED_GLASS,
                Blocks.BROWN_STAINED_GLASS,
                Blocks.CYAN_STAINED_GLASS,
                Blocks.GRAY_STAINED_GLASS,
                Blocks.GREEN_STAINED_GLASS,
                Blocks.LIGHT_BLUE_STAINED_GLASS,
                Blocks.LIGHT_GRAY_STAINED_GLASS,
                Blocks.LIME_STAINED_GLASS,
                Blocks.MAGENTA_STAINED_GLASS,
                Blocks.ORANGE_STAINED_GLASS,
                Blocks.PINK_STAINED_GLASS,
                Blocks.PURPLE_STAINED_GLASS,
                Blocks.RED_STAINED_GLASS,
                Blocks.WHITE_STAINED_GLASS,
                Blocks.YELLOW_STAINED_GLASS,
                Blocks.BLACK_WOOL,
                Blocks.BLUE_WOOL,
                Blocks.BROWN_WOOL,
                Blocks.CYAN_WOOL,
                Blocks.GRAY_WOOL,
                Blocks.GREEN_WOOL,
                Blocks.LIGHT_BLUE_WOOL,
                Blocks.LIGHT_GRAY_WOOL,
                Blocks.LIME_WOOL,
                Blocks.MAGENTA_WOOL,
                Blocks.ORANGE_WOOL,
                Blocks.PINK_WOOL,
                Blocks.PURPLE_WOOL,
                Blocks.RED_WOOL,
                Blocks.WHITE_WOOL,
                Blocks.YELLOW_WOOL
        };

        // Fill a cube of 5x5x5 with random blocks from the Minecraft Blocks
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    Block block = blocks[random.nextInt(blocks.length)];
                    ImmutableList<BlockState> states = block.getStateDefinition().getPossibleStates();
                    BlockState state = states.get(random.nextInt(states.size()));

                    world.setBlock(p, state, Block.UPDATE_ALL);
                }
            }
        }

        return 0;
    }
}
