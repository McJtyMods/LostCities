package mcjty.lostcities.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ComponentFactory;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.cityassets.CompiledPalette;
import mcjty.lostcities.worldgen.lost.cityassets.Palette;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public class CommandExportPart implements Command<CommandSourceStack> {

    private static final CommandExportPart CMD = new CommandExportPart();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("exportpart")
                .requires(cs -> cs.hasPermission(1))
                .then(Commands.argument("name", StringArgumentType.word()).executes(CMD));
    }


    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = context.getArgument("name", String.class);
        BuildingPart part = AssetRegistries.PARTS.get(context.getSource().getLevel(), name);
        if (part == null) {
            context.getSource().sendFailure(new TextComponent("Error finding part '" + name + "'!").withStyle(ChatFormatting.RED));
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

        BuildingInfo info = BuildingInfo.getBuildingInfo(start.getX() >> 4, start.getZ() >> 4, dimInfo);
        CompiledPalette palette = info.getCompiledPalette();
        Palette partPalette = part.getLocalPalette(level);
        Palette buildingPalette = info.getBuilding().getLocalPalette(level);
        if (partPalette != null || buildingPalette != null) {
            palette = new CompiledPalette(palette, partPalette, buildingPalette);
        }

        Set<BlockState> unknowns = new HashSet<>();

        for (int y = 0 ; y < part.getSliceCount() ; y++) {
            System.out.println("    [");
            for (int z = 0; z < part.getZSize(); z++) {
                StringBuilder b = new StringBuilder("      \"");
                for (int x = 0; x < part.getXSize(); x++) {
                    BlockPos pos = new BlockPos(info.chunkX*16+x, start.getY()+y, info.chunkZ*16+z);
                    BlockState state = level.getBlockState(pos);
                    Character c = palette.find(state);
                    if (c == null) {
                        Character character = part.getC(x, y, z);
                        if (palette.isMatch(character, state)) {
                            c = character;
                        } else {
                            c = '?';
                            unknowns.add(state);
                        }
                    }
                    b.append(c);
                }
                b.append('"');
                if (z < part.getZSize()-1) {
                    b.append(',');
                }
                System.out.println(b);
            }
            System.out.println("    ],");
        }

        for (BlockState unknown : unknowns) {
            System.out.println("unknown = " + unknown);
        }

        return 0;
    }
}
    /*
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        try {
            int cntSlices = Integer.parseInt(args[1]);

            JsonArray array = new JsonArray();

            EntityPlayer player = (EntityPlayer) sender;
            BlockPos start = player.getPosition().down();

            String palettechars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{}\\|`~:;',./<>?";
            int idx = 0;
            Map<BlockState, Character> mapping = new HashMap<>();
            Palette palette = new Palette("old");
            Palette paletteNew = new Palette("new");
            LostCityChunkGenerator provider = WorldTypeTools.getChunkGenerator(sender.getEntityWorld().provider.getDimension());
            BuildingInfo info = BuildingInfo.getBuildingInfo(start.getX() >> 4, start.getZ() >> 4, provider);
            for (Character character : info.getCompiledPalette().getCharacters()) {
                BlockState state = info.getCompiledPalette().getStraight(character);
                if (state != null) {
                    palette.addMapping(character, state);
                    mapping.put(state, character);
                }
            }

            List<Slice> slices = new ArrayList<>();
            for (int f = 0 ; f < cntSlices ; f++) {
                Slice slice = new Slice();
                slices.add(slice);
                int cx = (start.getX() >> 4) * 16;
                int cy = start.getY() + f;
                int cz = (start.getZ() >> 4) * 16;
                BlockPos.Mutable pos = new BlockPos.Mutable(cx, cy, cz);
                for (int x = 0 ; x < 16 ; x++) {
                    for (int z = 0 ; z < 16 ; z++) {
                        pos.setPos(cx + x, cy, cz + z);
                        BlockState state = server.getEntityWorld().getBlockState(pos);
                        Character character = mapping.get(state);
                        if (character == null) {
                            while (true) {
                                character = state.getBlock() == Blocks.AIR ? ' ' : palettechars.charAt(idx);
                                idx++;
                                if (!palette.getPalette().containsKey(character) && !paletteNew.getPalette().containsKey(character)) {
                                    break;
                                }
                            }
                            paletteNew.addMapping(character, state);
                            mapping.put(state, character);
                        }
                        slice.sequence[z*16+x] = "" + character;
                    }
                }

            }

            String[] sl = new String[cntSlices];
            for (int i = 0 ; i < cntSlices ; i++) {
                sl[i] = StringUtils.join(slices.get(i).sequence);
            }

            BuildingPart part = new BuildingPart("part", 16, 16, sl);
            array.add(part.writeToJSon());

            array.add(palette.writeToJSon());
            array.add(paletteNew.writeToJSon());

//            AssetRegistries.STYLES.writeToJson(array);
//            AssetRegistries.CITYSTYLES.writeToJson(array);
//            AssetRegistries.PALETTES.writeToJson(array);
//            AssetRegistries.PARTS.writeToJson(array);
//            AssetRegistries.BUILDINGS.writeToJson(array);
//            AssetRegistries.MULTI_BUILDINGS.writeToJson(array);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try(PrintWriter writer = new PrintWriter(new File(args[0]))) {
                writer.print(gson.toJson(array));
                writer.flush();
            }
        } catch (FileNotFoundException e) {
            sender.sendMessage(new StringTextComponent("Error writing to file '" + args[0] + "'!"));
        } catch (Exception e) {
            e.printStackTrace();
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

    public static class Slice {
        String sequence[] = new String[256];
    }
}
*/