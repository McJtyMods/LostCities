package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.Statistics;
import mcjty.lostcities.worldgen.IDimensionInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.WorldGenLevel;

public class CommandStats implements Command<CommandSourceStack> {

    private static final CommandStats CMD = new CommandStats();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("stats")
                .requires(cs -> cs.hasPermission(0))
                .executes(CMD);
    }


    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo((WorldGenLevel) player.level());
        if (dimInfo != null) {
            Statistics statistics = dimInfo.getFeature().getStatistics();
            float averageTime = statistics.getAverageTime();
            long minTime = statistics.getMinTime();
            long maxTime = statistics.getMaxTime();
            context.getSource().sendSuccess(() -> Component.literal("Average time: " + averageTime + "ms").withStyle(ChatFormatting.YELLOW), false);
            context.getSource().sendSuccess(() -> Component.literal("Min time: " + minTime + "ms").withStyle(ChatFormatting.YELLOW), false);
            context.getSource().sendSuccess(() -> Component.literal("Max time: " + maxTime + "ms").withStyle(ChatFormatting.YELLOW), false);
        } else {
            context.getSource().sendFailure(Component.literal("No dimension info found!").withStyle(ChatFormatting.RED));
        }
        return 0;
    }
}
