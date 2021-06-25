package mcjty.lostcities.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CommandSaveProfile implements Command<CommandSource> {

    private static final CommandSaveProfile CMD = new CommandSaveProfile();

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("saveprofile")
                .requires(cs -> cs.hasPermission(0))
                .then(Commands.argument("profile", StringArgumentType.word())
                    .executes(CMD));
    }


    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String name = context.getArgument("profile", String.class);
        LostCityProfile profile = LostCityConfiguration.standardProfiles.get(name);
        if (profile == null) {
            context.getSource().sendSuccess(new StringTextComponent(TextFormatting.RED + "Could not find profile '" + name + "'!"), true);
            return 0;
        }
        JsonObject jsonObject = profile.toJson(false);
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try {
            try (PrintWriter writer = new PrintWriter(new File(name + ".json"))) {
                writer.print(gson.toJson(jsonObject));
                writer.flush();
            }
        } catch (FileNotFoundException e) {
            context.getSource().sendSuccess(new StringTextComponent(TextFormatting.RED + "Error saving profile '" + name + "'!"), true);
            return 0;
        }
        context.getSource().sendSuccess(new StringTextComponent(TextFormatting.GREEN + "Saved profile '" + name + "'!"), true);
        return 0;
    }
}
