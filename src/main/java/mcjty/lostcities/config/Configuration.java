package mcjty.lostcities.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nonnull;
import java.util.*;

public class Configuration {

    public static class Value<T> {
        private final Component comment;
        private T value;
        private final T min;
        private final T max;
        private final Comparator<T> comparator;

        public Value(String comment, T value, T min, T max, @Nonnull Comparator<T> comparator) {
            this.comment = new TextComponent(comment);
            this.value = value;
            this.min = min;
            this.max = max;
            this.comparator = comparator;
        }

        public void set(T val) {
            value = val;
        }

        public T get() {
            return value;
        }

        public Component getComment() {
            return comment;
        }

        // Return true if we had to change the value
        public boolean constrain() {
            if (comparator.compare(value, min) < 0) {
                value = min;
                return true;
            }
            if (comparator.compare(value, max) > 0) {
                value = max;
                return true;
            }
            return false;
        }
    }

    public static class Category {
        private final Map<String, Value> valueMap = new HashMap<>();
    }

    private final Map<String, Category> categoryMap = new HashMap<>();

    public Value getValue(String name) {
        String[] split = name.split("\\.");
        Category category = categoryMap.get(split[0]);
        if (category == null) {
            throw new RuntimeException("Could not find category '" + split[0] + "'!");
        }
        Value value = category.valueMap.get(split[1]);
        if (value == null) {
            throw new RuntimeException("Could not find value '" + name + "'!");
        }
        return value;
    }

    public <T> T get(String name) {
        String[] split = name.split("\\.");
        Category category = categoryMap.get(split[0]);
        if (category == null) {
            throw new RuntimeException("Could not find category '" + split[0] + "'!");
        }
        Value value = category.valueMap.get(split[1]);
        if (value == null) {
            throw new RuntimeException("Could not find value '" + name + "'!");
        }
        return (T) value.value;
    }

    public <T> void set(String name, T val) {
        String[] split = name.split("\\.");
        Category category = categoryMap.get(split[0]);
        if (category == null) {
            throw new RuntimeException("Could not find category '" + split[0] + "'!");
        }
        Value value = category.valueMap.get(split[1]);
        if (value == null) {
            throw new RuntimeException("Could not find value '" + name + "'!");
        }
        value.value = val;
    }

    public JsonObject toJson(boolean readonly) {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, Category> entry : categoryMap.entrySet()) {
            JsonObject categoryObject = new JsonObject();
            if (readonly) {
                categoryObject.addProperty("__readonly__", "This profile is read only and cannot be modified! If you want to make a new profile based on this then you can make a copy to a new name");
            }
            Category category = entry.getValue();
            for (Map.Entry<String, Value> valueEntry : category.valueMap.entrySet()) {
                String key = valueEntry.getKey();
                Value value = valueEntry.getValue();
                if (value.value instanceof Number) {
                    categoryObject.addProperty(key, (Number) value.value);
                } else if (value.value instanceof Boolean) {
                    categoryObject.addProperty(key, (Boolean) value.value);
                } else if (value.value instanceof String) {
                    categoryObject.addProperty(key, (String) value.value);
                } else if (value.value instanceof String[]) {
                    JsonArray array = new JsonArray();
                    for (String s : ((String[]) value.value)) {
                        array.add(new JsonPrimitive(s));
                    }
                    categoryObject.add(key, array);
                }
            }
            root.add(entry.getKey(), categoryObject);
        }
        return root;
    }

    public void fromJson(JsonObject root) {
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String category = entry.getKey();
            addCustomCategoryComment(category, "<none>");
            for (Map.Entry<String, JsonElement> elementEntry : entry.getValue().getAsJsonObject().entrySet()) {
                JsonElement valueEl = elementEntry.getValue();
                if (valueEl.isJsonPrimitive()) {
                    if (valueEl.getAsJsonPrimitive().isNumber()) {
                        getFloat(elementEntry.getKey(), category, valueEl.getAsFloat(), 0.0f, 0.0f, "");
                    } else if (valueEl.getAsJsonPrimitive().isString()) {
                        getString(elementEntry.getKey(), category, valueEl.getAsString(), "");
                    } else if (valueEl.getAsJsonPrimitive().isBoolean()) {
                        getBoolean(elementEntry.getKey(), category, valueEl.getAsBoolean(), "");
                    }
                } else if (valueEl.isJsonArray()) {
                    List<String> list = new ArrayList<>();
                    for (JsonElement element : valueEl.getAsJsonArray()) {
                        list.add(element.getAsString());
                    }
                    getStringList(elementEntry.getKey(), category, list.toArray(new String[list.size()]), "");
                }
            }
        }
    }

    public void addCustomCategoryComment(String category, String description) {
        if (!categoryMap.containsKey(category)) {
            categoryMap.put(category, new Category());
        }
    }

    private <T> Category getValueCategory(String name, String category, T defaultValue, String description, T min, T max,
                                          @Nonnull Comparator<T> comparator) {
        Category cat = categoryMap.get(category);
        if (cat == null) {
            throw new IllegalStateException("Missing category: " + category);
        }
        if (!cat.valueMap.containsKey(name)) {
            cat.valueMap.put(name, new Value<>(description, defaultValue, min, max, comparator));
        }
        return cat;
    }

    public float getFloat(String name, String category, float defaultValue, float minValue, float maxValue, String description) {
        Category cat = getValueCategory(name, category, defaultValue, description, minValue, maxValue, Float::compareTo);
        Object value = cat.valueMap.get(name).value;
        if (value instanceof Float) {
            return (Float) value;
        } else {
            return ((Integer) value).floatValue();
        }
    }

    public boolean getBoolean(String name, String category, boolean defaultValue, String description) {
        Category cat = getValueCategory(name, category, defaultValue, description, null, null, (o1, o2) -> 0);
        return (Boolean) cat.valueMap.get(name).value;
    }

    public int getInt(String name, String category, int defaultValue, int minValue, int maxValue, String description) {
        Category cat = getValueCategory(name, category, defaultValue, description, minValue, maxValue, Integer::compareTo);
        Object value = cat.valueMap.get(name).value;
        if (value instanceof Float) {
            return ((Float) value).intValue();
        } else {
            return (Integer) value;
        }
    }

    public String getString(String name, String category, String defaultValue, String description) {
        Category cat = getValueCategory(name, category, defaultValue, description, null, null, (o1, o2) -> 0);
        return (String) cat.valueMap.get(name).value;
    }

    public String getString(String name, String category, String defaultValue, String description, String[] strings) {
        Category cat = getValueCategory(name, category, defaultValue, description, null, null, (o1, o2) -> 0);
        return (String) cat.valueMap.get(name).value;
    }

    public String[] getStringList(String name, String category, String[] defaultValue, String description) {
        Category cat = getValueCategory(name, category, defaultValue, description, null, null, (o1, o2) -> 0);
        return (String[]) cat.valueMap.get(name).value;
    }

    public boolean hasKey(String category, String name) {
        return categoryMap.get(category).valueMap.containsKey(name);
    }

    public Collection<Object> getCategory(String categoryGeneral) {
        // @todo
        return Collections.emptyList();
    }

}
