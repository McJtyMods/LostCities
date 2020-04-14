package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.CitySphere;
import mcjty.lostcities.worldgen.lost.Railway;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class CommandDebug implements Command<CommandSource> {

    private static final CommandDebug CMD = new CommandDebug();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("debug")
                .requires(cs -> cs.hasPermissionLevel(0))
                .executes(CMD);
    }


    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        if (player != null) {
            BlockPos position = player.getPosition();
            IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.getDimensionInfo(player.getServerWorld());
            if (dimInfo != null) {
                BuildingInfo info = BuildingInfo.getBuildingInfo(position.getX() >> 4, position.getZ() >> 4, dimInfo);
                System.out.println("profile = " + info.profile.getName());
//            System.out.println("provider.hasMansion = " + info.provider.hasMansion(info.chunkX, info.chunkZ));
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
                System.out.println("tunnel0 = " + info.isTunnel(0));
                System.out.println("tunnel1 = " + info.isTunnel(1));
                System.out.println("getHighwayXLevel() = " + info.getHighwayXLevel());
                System.out.println("getHighwayZLevel() = " + info.getHighwayZLevel());
                System.out.println("getChestTodo().size() = " + info.getLootTodo().size());
                System.out.println("getMobSpawnerTodo().size() = " + info.getMobSpawnerTodo().size());

                float reldist = CitySphere.getRelativeDistanceToCityCenter(info.chunkX, info.chunkZ, dimInfo);
                System.out.println("reldist = " + reldist);

                Railway.RailChunkInfo railInfo = Railway.getRailChunkType(info.chunkX, info.chunkZ, info.provider, info.profile);
                System.out.println("railInfo.getType() = " + railInfo.getType());
                System.out.println("railInfo.getLevel() = " + railInfo.getLevel());
                System.out.println("railInfo.getDirection() = " + railInfo.getDirection());
                System.out.println("railInfo.getRails() = " + railInfo.getRails());

                CitySphere sphere = CitySphere.getCitySphere(info.chunkX, info.chunkZ, dimInfo);
                System.out.println("sphere.cityCenter = " + sphere.getCenter());
                System.out.println("sphere.isEnabled() = " + sphere.isEnabled());
                System.out.println("sphere.radius = " + sphere.getRadius());

                ChunkHeightmap heightmap = dimInfo.getFeature().getHeightmap(info.chunkX, info.chunkZ, player.getServerWorld());
                int avg = 0;
                for (int x = 0 ; x < 16 ; x++) {
                    for (int z = 0 ; z < 16 ; z++) {
                        avg += heightmap.getHeight(x, z);
                    }
                }
                avg /= 16*16;
                System.out.println("Average chunk height (heightmap): " + avg);

                System.out.println("dimInfo.getProfile().BUILDING_MINFLOORS = " + dimInfo.getProfile().BUILDING_MINFLOORS);
                System.out.println("dimInfo.getProfile().BUILDING_MAXFLOORS = " + dimInfo.getProfile().BUILDING_MAXFLOORS);
                System.out.println("dimInfo.getProfile().CITY_CHANCE = " + dimInfo.getProfile().CITY_CHANCE);
            }
        }
        return 0;
    }
}
