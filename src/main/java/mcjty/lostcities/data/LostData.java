package mcjty.lostcities.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import javax.annotation.Nonnull;

public class LostData extends SavedData {

    public static final String NAME = "lostcities_data";

    private static Codec<LostData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("profile").forGetter(d -> d.selectedProfile),
            Codec.STRING.fieldOf("json").forGetter(d -> d.selectedJson)
            ).apply(instance, LostData::new));

    private static final SavedDataType<LostData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("lostcities", NAME),
            LostData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private String selectedProfile = "";
    private String selectedJson = "";

    @Nonnull
    public static LostData getData(Level level) {
        if (level.isClientSide()) {
            throw new RuntimeException("Don't access this client-side!");
        }
        MinecraftServer server = level.getServer();
        SavedDataStorage storage = server.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    public LostData() {
    }

    public LostData(String profile, String json) {
        selectedProfile = profile;
        selectedJson = json;
    }

    public void setProfile(String profile, String json) {
        selectedProfile = profile;
        selectedJson = json;
        setDirty();
    }

    public String getSelectedProfile() {
        return selectedProfile;
    }

    public String getSelectedJson() {
        return selectedJson;
    }
}
