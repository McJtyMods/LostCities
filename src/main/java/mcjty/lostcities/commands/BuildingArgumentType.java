package mcjty.lostcities.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.Building;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BuildingArgumentType extends ResourceLocationArgument {

    public static BuildingArgumentType building() {
        return new BuildingArgumentType();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Stream<Building> stream = StreamSupport.stream(AssetRegistries.BUILDINGS.getIterable().spliterator(), false);
        return SharedSuggestionProvider.suggest(stream.map(b -> b.getId().toString()), builder);
    }
}
