package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.editor.EditModeData;
import mcjty.lostcities.editor.Editor;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.ComponentFactory;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class CommandEditPart implements Command<CommandSourceStack> {

    private static final CommandEditPart CMD = new CommandEditPart();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("editpart")
                .requires(cs -> cs.hasPermission(1)).executes(CMD);
    }


    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
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
        for (EditModeData.PartData data : EditModeData.getData().getPartData(new ChunkCoord(level.dimension(), cp.x, cp.z))) {
            BuildingPart part = AssetRegistries.PARTS.get(level, data.partName());
            if (part == null) {
                context.getSource().sendFailure(ComponentFactory.literal("Unknown part '" + data.partName() + "' in this chunk!"));
                return 0;
            }
            if (data.y() <= start.getY() && start.getY() < data.y() + part.getSliceCount()) {
                context.getSource().sendSuccess(ComponentFactory.literal("Start editing part '" + data.partName() + "'!"), false);
                Editor.startEditing(part, player, new BlockPos(start.getX(), data.y(), start.getZ()), level, dimInfo);
                return 0;
            }
        }

        context.getSource().sendFailure(ComponentFactory.literal("Could not find a part to edit in this chunk!"));
        return 0;
    }
}
