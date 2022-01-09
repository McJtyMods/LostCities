package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CommandMap implements Command<CommandSourceStack> {

    private static final CommandMap CMD = new CommandMap();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("map")
                .requires(cs -> cs.hasPermission(0))
                .executes(CMD);
    }


    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (player != null) {
            BlockPos position = player.blockPosition();
            IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.getDimensionInfo(player.getLevel());
            if (dimInfo != null) {
                ChunkPos pos = new ChunkPos(position);
                for (int z = pos.z - 40 ; z <= pos.z + 40 ; z++) {
                    String buf = "";
                    for (int x = pos.x - 40 ; x <= pos.x + 40 ; x++) {
                        BuildingInfo info = BuildingInfo.getBuildingInfo(pos.x + x, pos.z + z, dimInfo);
                        if (info.isCity && info.hasBuilding) {
                            buf += "B";
                        } else if (info.isCity) {
                            buf += "+";
                        } else if (info.highwayXLevel >= 0 || info.highwayZLevel >= 0) {
                            buf += ".";
                        } else {
                            buf += " ";
                        }
                    }
                    System.out.println(buf);
                }
            }
        }
        return 0;
    }
}
