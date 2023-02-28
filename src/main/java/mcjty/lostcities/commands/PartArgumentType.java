package mcjty.lostcities.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.BuildingPart;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PartArgumentType extends ResourceLocationArgument {

    public static PartArgumentType part() {
        return new PartArgumentType();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        Stream<BuildingPart> stream = StreamSupport.stream(AssetRegistries.PARTS.getIterable().spliterator(), false);
        return SharedSuggestionProvider.suggest(stream.map(b -> b.getId().toString()), builder);
    }
}
