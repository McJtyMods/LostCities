package mcjty.lostcities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

public class CommandSaveAssets implements ICommand {

    @Override
    public String getName() {
        return "saveassets";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return getName() + " <file>";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new File(args[0]));
            JsonArray array = new JsonArray();
            AssetRegistries.STYLES.writeToJson(array);
            AssetRegistries.CITYSTYLES.writeToJson(array);
            AssetRegistries.PALETTES.writeToJson(array);
            AssetRegistries.PARTS.writeToJson(array);
            AssetRegistries.BUILDINGS.writeToJson(array);
            AssetRegistries.MULTI_BUILDINGS.writeToJson(array);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            writer.print(gson.toJson(array));
            writer.flush();
        } catch (FileNotFoundException e) {
            sender.sendMessage(new TextComponentString("Error writing to file '" + args[0] + "'!"));
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
