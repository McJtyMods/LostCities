package mcjty.lostcities.varia;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import jdk.nashorn.api.scripting.JSObject;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class JSonTools {

    public static Optional<JsonElement> getElement(JsonObject element, String name) {
        JsonElement el = element.get(name);
        if (el != null) {
            return Optional.of(el);
        } else {
            return Optional.empty();
        }
    }

    public static Stream<Pair<String,String>> asPairs(JsonElement element) {
        Stream.Builder<Pair<String, String>> builder = Stream.builder();
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            builder.add(Pair.of(entry.getKey(), entry.getValue().getAsString()));
        }
        return builder.build();
    }

    public static Stream<JsonElement> asArrayOrSingle(JsonElement element) {
        if (element.isJsonArray()) {
            Stream.Builder<JsonElement> builder = Stream.builder();
            for (JsonElement el : element.getAsJsonArray()) {
                builder.add(el);
            }
            return builder.build();
        } else {
            return Stream.of(element);
        }
    }

    public static void addPairs(JsonObject parent, String name, Map<String, String> pairs) {
        if (pairs != null) {
            JsonObject object = new JsonObject();
            for (Map.Entry<String, String> entry : pairs.entrySet()) {
                object.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
            }
            parent.add(name, object);
        }
    }

    public static void addArrayOrSingle(JsonObject parent, String name, Collection<String> strings) {
        if (strings != null) {
            if (strings.size() == 1) {
                parent.add(name, new JsonPrimitive(strings.iterator().next()));
            } else {
                JsonArray array = new JsonArray();
                for (String value : strings) {
                    array.add(new JsonPrimitive(value));
                }
                parent.add(name, array);
            }
        }
    }

    public static void addIntArrayOrSingle(JsonObject parent, String name, Collection<Integer> integers) {
        if (integers != null) {
            if (integers.size() == 1) {
                parent.add(name, new JsonPrimitive(integers.iterator().next()));
            } else {
                JsonArray array = new JsonArray();
                for (Integer value : integers) {
                    array.add(new JsonPrimitive(value));
                }
                parent.add(name, array);
            }
        }
    }
}
