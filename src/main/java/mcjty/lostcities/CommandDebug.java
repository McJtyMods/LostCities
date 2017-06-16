package mcjty.lostcities;

import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.Highway;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandDebug implements ICommand {

    @Override
    public String getName() {
        return "lostdebug";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return getName();
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            BlockPos position = player.getPosition();
            ChunkProviderServer chunkProvider = ((WorldServer) player.getEntityWorld()).getChunkProvider();
            BuildingInfo info = BuildingInfo.getBuildingInfo(position.getX() >> 4, position.getZ() >> 4, (LostCityChunkGenerator) chunkProvider.chunkGenerator);
            System.out.println("info.buildingType = " + info.buildingType.getName());
            System.out.println("info.floors = " + info.getNumFloors());
            System.out.println("info.floorsBelowGround = " + info.floorsBelowGround);
            System.out.println("info.cityLevel = " + info.cityLevel);
            System.out.println("info.isCity = " + info.isCity);
            System.out.println("info.chunkX = " + info.chunkX);
            System.out.println("info.chunkZ = " + info.chunkZ);
            System.out.println("info.getCityStyle() = " + info.getCityStyle().getName());
            System.out.println("info.getHighwayXLevel() = " + info.getHighwayXLevel());
            System.out.println("info.getHighwayZLevel() = " + info.getHighwayZLevel());
            double a = BuildingInfo.getChunkHeight(info.chunkX, info.chunkZ, info.provider);
            System.out.println("Average height: " + a);
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(o.getName());
    }
}
