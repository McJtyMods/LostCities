package mcjty.lostcities.commands;

import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.WorldTypeTools;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.CitySphere;
import mcjty.lostcities.dimensions.world.lost.Railway;
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
        return "lc_debug";
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
            LostCityChunkGenerator provider = WorldTypeTools.getChunkGenerator(sender.getEntityWorld().provider.getDimension());
            BuildingInfo info = BuildingInfo.getBuildingInfo(position.getX() >> 4, position.getZ() >> 4, provider);
            System.out.println("profile = " + info.profile.getName());
            System.out.println("provider.hasMansion = " + info.provider.hasMansion(info.chunkX, info.chunkZ));
            System.out.println("buildingType = " + info.buildingType.getName());
            System.out.println("floors = " + info.getNumFloors());
            System.out.println("floorsBelowGround = " + info.floorsBelowGround);
            System.out.println("cityLevel = " + info.cityLevel);
            System.out.println("cityGroundLevel = " + info.getCityGroundLevel());
            System.out.println("isCity = " + info.isCity);
            System.out.println("chunkX = " + info.chunkX);
            System.out.println("chunkZ = " + info.chunkZ);
            System.out.println("getCityStyle() = " + BuildingInfo.getChunkCharacteristics(info.chunkX, info.chunkZ, info.provider).cityStyle.getName());
            System.out.println("streetType = " + info.streetType);
            System.out.println("ruinHeight = " + info.ruinHeight);
            System.out.println("getHighwayXLevel() = " + info.getHighwayXLevel());
            System.out.println("getHighwayZLevel() = " + info.getHighwayZLevel());
            System.out.println("getChestTodo().size() = " + info.getLootTodo().size());
            System.out.println("getMobSpawnerTodo().size() = " + info.getMobSpawnerTodo().size());

            float reldist = CitySphere.getRelativeDistanceToCityCenter(info.chunkX, info.chunkZ, provider);
            System.out.println("reldist = " + reldist);

            Railway.RailChunkInfo railInfo = Railway.getRailChunkType(info.chunkX, info.chunkZ, info.provider, info.profile);
            System.out.println("railInfo.getType() = " + railInfo.getType());
            System.out.println("railInfo.getLevel() = " + railInfo.getLevel());
            System.out.println("railInfo.getDirection() = " + railInfo.getDirection());
            System.out.println("railInfo.getRails() = " + railInfo.getRails());

            CitySphere sphere = CitySphere.getCitySphere(info.chunkX, info.chunkZ, provider);
            System.out.println("sphere.cityCenter = " + sphere.getCenter());
            System.out.println("sphere.isEnabled() = " + sphere.isEnabled());
            System.out.println("sphere.radius = " + sphere.getRadius());
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
