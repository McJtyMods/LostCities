package mcjty.lostcities.varia;

import net.minecraft.network.chat.*;

public class ComponentFactory {

    public static TranslatableComponent translatable(String key) {
        return new TranslatableComponent(key);
    }

    public static MutableComponent literal(String text) {
        return new TextComponent(text);
    }

    public static Component empty() {
        return TextComponent.EMPTY;
    }

    public static MutableComponent keybind(String keybind) {
        return new KeybindComponent(keybind);
    }
}
