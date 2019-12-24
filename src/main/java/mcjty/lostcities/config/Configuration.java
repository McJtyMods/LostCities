package mcjty.lostcities.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.*;

public class Configuration {

    public static class Value<T> {
        private final String name;
        private final String comment;
        private final T value;

        public Value(String name, String comment, T value) {
            this.name = name;
            this.comment = comment;
            this.value = value;
        }
    }

    public static class Category {
        private final String name;
        private final String comment;
        private final Map<String, Value> valueMap = new HashMap<>();

        public Category(String name, String comment) {
            this.name = name;
            this.comment = comment;
        }
    }

    private final Map<String, Category> categoryMap = new HashMap<>();

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        for (Map.Entry<String, Category> entry : categoryMap.entrySet()) {
            JsonObject categoryObject = new JsonObject();
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
            categoryMap.put(category, new Category(category, description));
        }
    }

    private <T> Category getValueCategory(String name, String category, T defaultValue, String description) {
        Category cat = categoryMap.get(category);
        if (cat == null) {
            throw new IllegalStateException("Missing category: " + category);
        }
        if (!cat.valueMap.containsKey(name)) {
            cat.valueMap.put(name, new Value<>(name, description, defaultValue));
        }
        return cat;
    }

    public float getFloat(String name, String category, float defaultValue, float minValue, float maxValue, String description) {
        Category cat = getValueCategory(name, category, defaultValue, description);
        Object value = cat.valueMap.get(name).value;
        if (value instanceof Float) {
            return (Float) value;
        } else {
            return ((Integer) value).floatValue();
        }
    }

    public boolean getBoolean(String name, String category, boolean defaultValue, String description) {
        Category cat = getValueCategory(name, category, defaultValue, description);
        return (Boolean) cat.valueMap.get(name).value;
    }

    public int getInt(String name, String category, int defaultValue, int minValue, int maxValue, String description) {
        Category cat = getValueCategory(name, category, defaultValue, description);
        Object value = cat.valueMap.get(name).value;
        if (value instanceof Float) {
            return ((Float) value).intValue();
        } else {
            return (Integer) value;
        }
    }

    public String getString(String name, String category, String defaultValue, String description) {
        Category cat = getValueCategory(name, category, defaultValue, description);
        return (String) cat.valueMap.get(name).value;
    }

    public String getString(String name, String category, String defaultValue, String description, String[] strings) {
        Category cat = getValueCategory(name, category, defaultValue, description);
        return (String) cat.valueMap.get(name).value;
    }

    public String[] getStringList(String name, String category, String[] defaultValue, String description) {
        Category cat = getValueCategory(name, category, defaultValue, description);
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
