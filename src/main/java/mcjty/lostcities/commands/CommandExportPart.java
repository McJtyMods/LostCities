package mcjty.lostcities.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.WorldTypeTools;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.cityassets.BuildingPart;
import mcjty.lostcities.dimensions.world.lost.cityassets.Palette;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class CommandExportPart implements ICommand {

    @Override
    public String getName() {
        return "lc_exportpart";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return getName() + " <file> <slices>";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        try {
            int cntSlices = Integer.parseInt(args[1]);

            JsonArray array = new JsonArray();

            EntityPlayer player = (EntityPlayer) sender;
            BlockPos start = player.getPosition().down();

            String palettechars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{}\\|`~:;',./<>?";
            int idx = 0;
            Map<IBlockState, Character> mapping = new HashMap<>();
            Palette palette = new Palette("old");
            Palette paletteNew = new Palette("new");
            LostCityChunkGenerator provider = WorldTypeTools.getChunkGenerator(sender.getEntityWorld().provider.getDimension());
            BuildingInfo info = BuildingInfo.getBuildingInfo(start.getX() >> 4, start.getZ() >> 4, provider);
            for (Character character : info.getCompiledPalette().getCharacters()) {
                IBlockState state = info.getCompiledPalette().getStraight(character);
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
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(cx, cy, cz);
                for (int x = 0 ; x < 16 ; x++) {
                    for (int z = 0 ; z < 16 ; z++) {
                        pos.setPos(cx + x, cy, cz + z);
                        IBlockState state = server.getEntityWorld().getBlockState(pos);
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
            sender.sendMessage(new TextComponentString("Error writing to file '" + args[0] + "'!"));
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
