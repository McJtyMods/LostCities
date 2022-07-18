package mcjty.lostcities.commands;

import com.google.gson.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import mcjty.lostcities.editor.EditorInfo;
import mcjty.lostcities.setup.Registration;
import mcjty.lostcities.varia.ComponentFactory;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import mcjty.lostcities.worldgen.lost.cityassets.CompiledPalette;
import mcjty.lostcities.worldgen.lost.cityassets.Palette;
import mcjty.lostcities.worldgen.lost.regassets.BuildingPartRE;
import mcjty.lostcities.worldgen.lost.regassets.PaletteRE;
import mcjty.lostcities.worldgen.lost.regassets.data.PaletteEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
        ServerPlayer player = context.getSource().getPlayerOrException();
        EditorInfo editorInfo = EditorInfo.getEditorInfo(player.getUUID());
        if (editorInfo == null) {
            context.getSource().sendFailure(Component.literal("You are not editing anything!").withStyle(ChatFormatting.RED));
            return 0;
        }

        BuildingPart part = AssetRegistries.PARTS.get(context.getSource().getLevel(), editorInfo.getPartName());
        if (part == null) {
            context.getSource().sendFailure(Component.literal("Error finding part '" + editorInfo.getPartName() + "'!").withStyle(ChatFormatting.RED));
            return 0;
        }

        BlockPos start = editorInfo.getBottomLocation();

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

        Map<BlockState, Character> unknowns = new HashMap<>();

        List<List<String>> slices = new ArrayList<>();
        Set<Character> usedCharacters = new HashSet<>(palette.getCharacters());
        String possibleChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{}|;:'<>,.?/`~";

        for (int y = 0 ; y < part.getSliceCount() ; y++) {
            List<String> yslice = new ArrayList<>();
            for (int z = 0; z < part.getZSize(); z++) {
                StringBuilder b = new StringBuilder("");
                for (int x = 0; x < part.getXSize(); x++) {
                    BlockPos pos = new BlockPos(info.chunkX*16+x, start.getY()+y, info.chunkZ*16+z);
                    BlockState state = level.getBlockState(pos);
                    Character c = editorInfo.getPaleteEntry(state);
                    if (c == null) {
                        c = unknowns.get(state);
                    }
                    if (c == null) {
                        // New state!
                        c = (char) possibleChars.chars().filter(value -> !usedCharacters.contains((char)value)).findFirst().getAsInt();
                        unknowns.put(state, c);
                        usedCharacters.add(c);
                    }
                    b.append(c);
                }
                yslice.add(b.toString());
            }
            slices.add(yslice);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        JsonObject root = new JsonObject();

        if (!unknowns.isEmpty()) {
            List<PaletteEntry> entries = new ArrayList<>();
            for (Map.Entry<BlockState, Character> entry : unknowns.entrySet()) {
                entries.add(new PaletteEntry(Character.toString(entry.getValue()), Optional.of(Tools.stateToString(entry.getKey())),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
            }
            PaletteRE paletteRE = new PaletteRE(entries);
            DataResult<JsonElement> result = PaletteRE.CODEC.encodeStart(JsonOps.INSTANCE, paletteRE);
            root.add("__comment__", new JsonPrimitive("'missingpalette' represents all blockstates that it couldn't find in the palette. These have to be put in a palette. " +
                    "'exportedpart' is the actual exported part"));
            root.add("missingpalette", result.result().get());
        } else {
            root.add("__comment__", new JsonPrimitive("'exportedpart' is the actual exported part"));
        }

        BuildingPartRE buildingPartRE = new BuildingPartRE(part.getXSize(), part.getZSize(), slices,
                Optional.ofNullable(part.getRefPaletteName()), Optional.empty(), Optional.empty());
        DataResult<JsonElement> result = BuildingPartRE.CODEC.encodeStart(JsonOps.INSTANCE, buildingPartRE);
        root.add("exportedpart", result.result().get());

        String json = gson.toJson(root);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(name));
            writer.write(json);
            writer.close();
            context.getSource().sendSuccess(ComponentFactory.literal("Exported part to '" + name + "'!"), false);
        } catch (IOException e) {
            context.getSource().sendFailure(Component.literal("Error writing file '" + name + "'!").withStyle(ChatFormatting.RED));
        }

        return 0;
    }
}
