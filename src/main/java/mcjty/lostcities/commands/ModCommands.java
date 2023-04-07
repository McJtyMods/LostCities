package mcjty.lostcities.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.Building;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.commands.ResetChunksCommand;

import javax.annotation.Nonnull;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> commands = dispatcher.register(
                Commands.literal(LostCities.MODID)
                        .then(CommandCreateBuilding.register(dispatcher))
                        .then(CommandDebug.register(dispatcher))
                        .then(CommandMap.register(dispatcher))
                        .then(CommandSaveProfile.register(dispatcher))
                        .then(CommandCreatePart.register(dispatcher))
                        .then(CommandLocatePart.register(dispatcher))
                        .then(CommandEditPart.register(dispatcher))
                        .then(CommandListParts.register(dispatcher))
                        .then(CommandExportPart.register(dispatcher))
        );

        dispatcher.register(Commands.literal("lost").redirect(commands));
        ResetChunksCommand.register(dispatcher);
    }

    @Nonnull
    static SuggestionProvider<CommandSourceStack> getPartSuggestionProvider() {
        return (context, builder) -> {
            Stream<BuildingPart> stream = StreamSupport.stream(AssetRegistries.PARTS.getIterable().spliterator(), false);
            return SharedSuggestionProvider.suggest(stream.map(b -> b.getId().toString()), builder);
        };
    }

    @Nonnull
    static SuggestionProvider<CommandSourceStack> getBuildingSuggestionProvider() {
        return (context, builder) -> {
            Stream<Building> stream = StreamSupport.stream(AssetRegistries.BUILDINGS.getIterable().spliterator(), false);
            return SharedSuggestionProvider.suggest(stream.map(b -> b.getId().toString()), builder);
        };
    }
}
