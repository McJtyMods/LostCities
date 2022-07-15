package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.editor.EditorInfo;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ComponentFactory;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.cityassets.CompiledPalette;
import mcjty.lostcities.worldgen.lost.cityassets.Palette;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CommandCreatePart implements Command<CommandSourceStack> {

    private static final CommandCreatePart CMD = new CommandCreatePart();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("createpart")
                .requires(cs -> cs.hasPermission(1))
                .then(Commands.argument("name", StringArgumentType.word()).executes(CMD));
    }


    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = context.getArgument("name", String.class);
        BuildingPart part = AssetRegistries.PARTS.get(context.getSource().getLevel(), name);
        if (part == null) {
            context.getSource().sendFailure(new TextComponent("Error finding part '" + name + "'!").withStyle(ChatFormatting.RED));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos start = player.blockPosition();

        ServerLevel level = player.getLevel();
        IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.getDimensionInfo(level);
        if (dimInfo == null) {
            context.getSource().sendFailure(ComponentFactory.literal("This dimension doesn't support Lost Cities!"));
            return 0;
        }

        BuildingInfo info = BuildingInfo.getBuildingInfo(start.getX() >> 4, start.getZ() >> 4, dimInfo);
        CompiledPalette palette = info.getCompiledPalette();
        Palette partPalette = part.getLocalPalette(level);
        Palette buildingPalette = info.getBuilding().getLocalPalette(level);
        if (partPalette != null || buildingPalette != null) {
            palette = new CompiledPalette(palette, partPalette, buildingPalette);
        }

        EditorInfo editorInfo = EditorInfo.createEditorInfo(player.getUUID(), name, start);

        CompiledPalette finalPalette = palette;

        player.level.getServer().doRunTask(new TickTask(3, () -> {
            System.out.println("CommandCreatePart.run");

            for (int y = 0; y < part.getSliceCount(); y++) {
                for (int x = 0; x < part.getXSize(); x++) {
                    for (int z = 0; z < part.getZSize(); z++) {
                        BlockPos pos = new BlockPos(info.chunkX * 16 + x, start.getY() + y, info.chunkZ * 16 + z);
                        Character character = part.getC(x, y, z);
                        BlockState state = finalPalette.get(character);
                        if (state != null) {
                            level.setBlock(pos, state, Block.UPDATE_ALL);
                        }
                    }
                }
            }
            for (int y = 0; y < part.getSliceCount(); y++) {
                for (int x = 0; x < part.getXSize(); x++) {
                    for (int z = 0; z < part.getZSize(); z++) {
                        BlockPos pos = new BlockPos(info.chunkX * 16 + x, start.getY() + y, info.chunkZ * 16 + z);
                        Character character = part.getC(x, y, z);
                        if (finalPalette.get(character) != null) {
                            BlockState state = level.getBlockState(pos);
                            editorInfo.addPaletteEntry(character, state);
                        }
                    }
                }
            }
        }));

        return 0;
    }
}
