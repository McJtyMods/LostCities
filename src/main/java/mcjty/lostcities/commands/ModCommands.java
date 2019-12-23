package mcjty.lostcities.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcjty.lostcities.LostCities;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> cmdList = dispatcher.register(
                Commands.literal(LostCities.MODID)
                        .then(CommandDebug.register(dispatcher))
                        .then(CommandMap.register(dispatcher))
        );

        dispatcher.register(Commands.literal("lost").redirect(cmdList));
    }

}
