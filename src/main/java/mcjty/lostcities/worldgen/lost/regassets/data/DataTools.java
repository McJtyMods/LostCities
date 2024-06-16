package mcjty.lostcities.worldgen.lost.regassets.data;

import mcjty.lostcities.LostCities;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class DataTools {

    public static Optional<String> toNullable(Character c) {
        if (c == null) {
            return Optional.empty();
        } else {
            return Optional.of(Character.toString(c));
        }
    }

    public static Character getNullableChar(Optional<String> opt) {
        return opt.isPresent() ? opt.get().charAt(0) : null;
    }

    public static String toName(ResourceLocation rl) {
        if (rl.getNamespace().equals(LostCities.MODID)) {
            return rl.getPath();
        } else {
            return rl.toString();
        }
    }

    public static ResourceLocation fromName(String name) {
        if (name.contains(":")) {
            return ResourceLocation.parse(name);
        } else {
            return ResourceLocation.fromNamespaceAndPath(LostCities.MODID, name);
        }
    }
}
