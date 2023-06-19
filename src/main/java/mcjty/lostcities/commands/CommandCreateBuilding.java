package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ComponentFactory;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import static mcjty.lostcities.worldgen.LostCityTerrainFeature.FLOORHEIGHT;

public class CommandCreateBuilding implements Command<CommandSourceStack> {

    private static final CommandCreateBuilding CMD = new CommandCreateBuilding();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("createbuilding")
                .requires(cs -> cs.hasPermission(1))
                .then(Commands.argument("name", ResourceLocationArgument.id())
                        .suggests(ModCommands.getBuildingSuggestionProvider())
                        .then(Commands.argument("floors", IntegerArgumentType.integer(1, 20))
                                .then(Commands.argument("cellars", IntegerArgumentType.integer(0, 10)).executes(CMD))));
    }


    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation name = context.getArgument("name", ResourceLocation.class);
        Integer floors = context.getArgument("floors", Integer.class);
        Integer cellars = context.getArgument("cellars", Integer.class);
        Building building = AssetRegistries.BUILDINGS.get(context.getSource().getLevel(), name);
        if (building == null) {
            context.getSource().sendFailure(ComponentFactory.literal("Cannot find building: " + name + "!"));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = (ServerLevel) player.level();
        BlockPos bottom = player.blockPosition().below();

        IDimensionInfo dimInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(level);
        if (dimInfo == null) {
            context.getSource().sendFailure(ComponentFactory.literal("This dimension doesn't support Lost Cities!"));
            return 0;
        }
        BuildingInfo info = BuildingInfo.getBuildingInfo(bottom.getX() >> 4, bottom.getZ() >> 4, dimInfo);
        info.setBuildingType(building, cellars, floors, bottom.getY());

        Character borderBlock = info.getCityStyle().getBorderBlock();
        CompiledPalette palette = info.getCompiledPalette();
        char fillerBlock = info.getBuilding().getFillerBlock();

        ChunkPos cp = new ChunkPos(bottom);

        int height = bottom.getY();
        for (int y = height ; y < level.getMaxBuildHeight() ; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    level.setBlock(cp.getBlockAt(x, y, z), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }

        for (int f = -info.cellars; f <= info.getNumFloors(); f++) {
            BuildingPart part = info.getFloor(f);

            generatePart(level, cp, info, part, height);
            part = info.getFloorPart2(f);
            if (part != null) {
                generatePart(level, cp, info, part, height);
            }

            height += FLOORHEIGHT;    // We currently only support 6 here
        }

        return 0;
    }

    private static void generatePart(Level level, ChunkPos cp, BuildingInfo info, IBuildingPart part, int oy) {
        CompiledPalette compiledPalette = info.getCompiledPalette();
        // Cache the combined palette?
        Palette partPalette = part.getLocalPalette(level);
        Palette buildingPalette = info.getBuilding().getLocalPalette(level);
        if (partPalette != null || buildingPalette != null) {
            compiledPalette = new CompiledPalette(compiledPalette, partPalette, buildingPalette);
        }

        boolean nowater = part.getMetaBoolean("nowater");
        BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();

        for (int x = 0; x < part.getXSize(); x++) {
            for (int z = 0; z < part.getZSize(); z++) {
                char[] vs = part.getVSlice(x, z);
                if (vs != null) {
                    int rx = cp.getBlockX(x);
                    int rz = cp.getBlockZ(z);
                    current.set(rx, oy, rz);
                    int len = vs.length;
                    for (char c : vs) {
                        BlockState b = compiledPalette.get(c);
                        if (b == null) {
                            throw new RuntimeException("Could not find entry '" + c + "' in the palette for part '" + part.getName() + "'!");
                        }
                        level.setBlock(current, b, Block.UPDATE_CLIENTS);
                        current.setY(current.getY() + 1);
                    }
                }
            }
        }
    }


}
