package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.editor.Editor;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ComponentFactory;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.awt.*;

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
            context.getSource().sendFailure(Component.literal("Error finding part '" + name + "'!").withStyle(ChatFormatting.RED));
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

        Editor.startEditing( part, player, start, level, dimInfo);

        return 0;
    }

}
