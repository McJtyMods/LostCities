package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.editor.EditModeData;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.ComponentFactory;
import mcjty.lostcities.worldgen.IDimensionInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.List;

public class CommandLocatePart implements Command<CommandSourceStack> {

    private static final CommandLocatePart CMD = new CommandLocatePart();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("locatepart")
                .requires(cs -> cs.hasPermission(1))
                .then(Commands.argument("name", ResourceLocationArgument.id()).suggests(
                        ModCommands.getPartSuggestionProvider()
                ).executes(CMD));
    }


    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation name = context.getArgument("name", ResourceLocation.class);

        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos start = player.blockPosition();

        ServerLevel level = player.getLevel();
        IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(level);
        if (dimInfo == null) {
            context.getSource().sendFailure(ComponentFactory.literal("This dimension doesn't support Lost Cities!"));
            return 0;
        }
        if (!dimInfo.getProfile().EDITMODE) {
            context.getSource().sendFailure(ComponentFactory.literal("This world was not created with edit mode enabled. This command is not possible!"));
            return 0;
        }

        ChunkPos cp = new ChunkPos(start);
        // Abuse BlockPos as ChunkPos
        int cnt = 0;
        for (BlockPos.MutableBlockPos mpos : BlockPos.spiralAround(new BlockPos(cp.x, 0, cp.z), 15, Direction.EAST, Direction.SOUTH)) {
            List<EditModeData.PartData> data = EditModeData.getData().getPartData(new ChunkCoord(level.dimension(), mpos.getX(), mpos.getZ()));
            for (EditModeData.PartData pd : data) {
                if (pd.partName().equals(name.toString())) {
                    context.getSource().sendSuccess(ComponentFactory.literal("Found at " + (mpos.getX() * 16 + 8) + "," + pd.y() + "," + (mpos.getZ() * 16 + 8)), false);
                    cnt++;
                    if (cnt > 6) {
                        break;
                    }
                }
            }
        }
        return 0;
    }
}
