package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.ModDimensions;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class WorldTypeTools {

    // A map which maps dimension id to the profile
    private static Map<DimensionType, LostCityProfile> profileMap = new HashMap<>();

    // This map is constructed dynamically to allow having to avoid having an instanceof
    // on LostCityChunkGenerator which breaks on sponge
    private static Map<DimensionType, WeakReference<LostCityChunkGenerator>> chunkGeneratorMap = new HashMap<>();

    // To prevent the client from asking the profile to the server too much
    private static long clientTimeout = -1;

    public static void cleanCache() {
        profileMap.clear();
        clientTimeout = -1;
    }

    public static void cleanChunkGeneratorMap() {
        chunkGeneratorMap.clear();
    }

    public static void registerChunkGenerator(DimensionType dimension, LostCityChunkGenerator chunkGenerator) {
        chunkGeneratorMap.put(dimension, new WeakReference<LostCityChunkGenerator>(chunkGenerator));
    }

    @Nullable
    public static LostCityChunkGenerator getChunkGenerator(DimensionType dimension) {
        if (chunkGeneratorMap.containsKey(dimension)) {
            WeakReference<LostCityChunkGenerator> reference = chunkGeneratorMap.get(dimension);
            return reference.get();
        }
        return null;
    }

    public static LostCityProfile getProfile(IWorld world) {
        if (profileMap.containsKey(world.getDimension().getType())) {
            return profileMap.get(world.getDimension().getType());
        }

        if (world instanceof ServerWorld) {
            LostCityProfile profile = getProfileOnServer(world);
            profileMap.put(world.getDimension().getType(), profile);
            return profile;
        } else {
            // We don't know the information yet so we ask the server. We set a timeout to make sure this
            // message is not sent all the time
            long time = System.currentTimeMillis();
            if (clientTimeout == -1 || clientTimeout + 2000 > time) {
                // @todo 1.14
//                PacketHandler.INSTANCE.sendToServer(new PacketRequestProfile(world.provider.getDimension()));
                clientTimeout = time;
            }
            if (ModDimensions.dimensionProfileMap.keySet().contains(world.getDimension().getType())) {
                // Don't put in cache because we might want to ask again
                return LostCityConfiguration.profiles.get(ModDimensions.dimensionProfileMap.get(world.getDimension().getType()));
            } else {
                // Don't put in cache because we might want to ask again
                return LostCityConfiguration.profiles.get(LostCityConfiguration.DEFAULT_PROFILE);
            }
        }
    }

    // Called client side when we get an answer from the server about our profile
    public static void setProfileFromServer(DimensionType dimension, String profileName) {
        LostCityProfile profile = LostCityConfiguration.profiles.get(profileName);
        if (profile == null) {
            throw new RuntimeException("Cannot find profile '" + profileName + "' that the server is using! Please make client configs for Lost Cities compatible");
        }
        profileMap.put(dimension, profile);
    }

    /**
     * If possible return the LostCityChunkGenerator that belongs to this world. Return
     * null if it is not a Lost City world
     */
    @Nullable
    public static LostCityChunkGenerator getLostCityChunkGenerator(World world) {
        return getChunkGenerator(world.getDimension().getType());
//        WorldServer worldServer = (WorldServer) world;
//        IChunkGenerator chunkGenerator = worldServer.getChunkProvider().chunkGenerator;
//        // @todo not compatible with Sponge! No clue how to solve atm
//        // XXX
//        if (!(chunkGenerator instanceof LostCityChunkGenerator)) {
//            return null;
//        }
//        return (LostCityChunkGenerator) chunkGenerator;
    }

    public static boolean isLostCities(IWorld world) {
        if (ModDimensions.dimensionProfileMap.containsKey(world.getDimension().getType())) {
            return true;
        }
        if (world.getDimension().getType() != DimensionType.OVERWORLD) {
            return false;
        }
        return world.getWorld().getWorldType() instanceof LostWorldType || world.getWorld().getWorldType() instanceof LostWorldTypeBOP;
    }

    private static LostCityProfile getProfileOnServer(IWorld world) {
        if (ModDimensions.dimensionProfileMap.containsKey(world.getDimension().getType())) {
            LostCityProfile profile = LostCityConfiguration.profiles.get(ModDimensions.dimensionProfileMap.get(world.getDimension().getType()));
            if (profile != null) {
                return profile;
            }
        }
        CompoundNBT generatorOptions = world.getWorldInfo().getGeneratorOptions();
        LostCityProfile p;
        if (generatorOptions == null || generatorOptions.isEmpty()) {
            p = LostCityConfiguration.profiles.get(LostCityConfiguration.DEFAULT_PROFILE);
            if (p == null) {
                throw new RuntimeException("Something went wrong! Profile '" + LostCityConfiguration.DEFAULT_PROFILE + "' is missing!");
            }
        } else {
            p = null;
            // @todo 1.14
//            JsonParser parser = new JsonParser();
//            JsonElement parsed = parser.parse(generatorOptions);
//            String profileName;
//            if (parsed.getAsJsonObject().has("profile")) {
//                profileName = parsed.getAsJsonObject().get("profile").getAsString();
//            } else {
//                profileName = LostCityConfiguration.DEFAULT_PROFILE;
//            }
//            p = LostCityConfiguration.profiles.get(profileName);
//            if (p == null) {
//                throw new RuntimeException("Something went wrong! Profile '" + profileName + "' is missing!");
//            }
        }
        return p;
    }
}
