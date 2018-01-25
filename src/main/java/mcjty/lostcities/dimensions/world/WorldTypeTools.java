package mcjty.lostcities.dimensions.world;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.network.PacketHandler;
import mcjty.lostcities.network.PacketRequestProfile;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.Map;

public class WorldTypeTools {

    // A map which maps dimension id to the profile
    private static Map<Integer, LostCityProfile> profileMap = new HashMap<>();

    // To prevent the client from asking the profile to the server too much
    private static long clientTimeout = -1;

    public static LostCityProfile getProfile(World world) {
        if (profileMap.containsKey(world.provider.getDimension())) {
            return profileMap.get(world.provider.getDimension());
        }

        if (world instanceof WorldServer) {
            LostCityProfile profile = getProfileOnServer(world);
            profileMap.put(world.provider.getDimension(), profile);
            return profile;
        } else {
            // We don't know the information yet so we ask the server. We set a timeout to make sure this
            // message is not sent all the time
            long time = System.currentTimeMillis();
            if (clientTimeout == -1 || clientTimeout + 2000 > time) {
                PacketHandler.INSTANCE.sendToServer(new PacketRequestProfile(world.provider.getDimension()));
                clientTimeout = time;
            }
            if (world.provider.getDimension() == LostCityConfiguration.DIMENSION_ID) {
                // Don't put in cache because we might want to ask again
                return LostCityConfiguration.profiles.get(LostCityConfiguration.DIMENSION_PROFILE);
            } else {
                // Don't put in cache because we might want to ask again
                return LostCityConfiguration.profiles.get(LostCityConfiguration.DEFAULT_PROFILE);
            }
        }
    }

    // Called client side when we get an answer from the server about our profile
    public static void setProfileFromServer(int dimension, String profile) {
        System.out.println("FROM SERVER: profile = " + profile + " for " + dimension);
        profileMap.put(dimension, LostCityConfiguration.profiles.get(profile));
    }

    private static LostCityProfile getProfileOnServer(World world) {
        if (world.provider.getDimension() == LostCityConfiguration.DIMENSION_ID) {
            LostCityProfile profile = LostCityConfiguration.profiles.get(LostCityConfiguration.DIMENSION_PROFILE);
            if (profile != null) {
                return profile;
            }
        }
        String generatorOptions = world.getWorldInfo().getGeneratorOptions();
        LostCityProfile p;
        if (generatorOptions == null || generatorOptions.trim().isEmpty()) {
            p = LostCityConfiguration.profiles.get(LostCityConfiguration.DEFAULT_PROFILE);
            if (p == null) {
                throw new RuntimeException("Something went wrong! Profile '" + LostCityConfiguration.DEFAULT_PROFILE + "' is missing!");
            }
        } else {
            JsonParser parser = new JsonParser();
            JsonElement parsed = parser.parse(generatorOptions);
            String profileName;
            if (parsed.getAsJsonObject().has("profile")) {
                profileName = parsed.getAsJsonObject().get("profile").getAsString();
            } else {
                profileName = LostCityConfiguration.DEFAULT_PROFILE;
            }
            p = LostCityConfiguration.profiles.get(profileName);
            if (p == null) {
                throw new RuntimeException("Something went wrong! Profile '" + profileName + "' is missing!");
            }
        }
        return p;
    }
}
