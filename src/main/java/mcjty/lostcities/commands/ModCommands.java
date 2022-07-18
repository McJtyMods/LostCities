package mcjty.lostcities.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcjty.lostcities.LostCities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.commands.ResetChunksCommand;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> commands = dispatcher.register(
                Commands.literal(LostCities.MODID)
                        .then(CommandCreateBuilding.register(dispatcher))
                        .then(CommandDebug.register(dispatcher))
                        .then(CommandMap.register(dispatcher))
                        .then(CommandSaveProfile.register(dispatcher))
                        .then(CommandCreatePart.register(dispatcher))
                        .then(CommandLocatePart.register(dispatcher))
                        .then(CommandEditPart.register(dispatcher))
                        .then(CommandListParts.register(dispatcher))
                        .then(CommandExportPart.register(dispatcher))
        );

        dispatcher.register(Commands.literal("lost").redirect(commands));
        ResetChunksCommand.register(dispatcher);
    }

}
