package mcjty.lostcities.editor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.WorldTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * In a world created in editmode this structure will contain information about all generated parts
 */
public class EditModeData extends SavedData {

    public static final String NAME = "lostcity_editdata";

    public record PartData(String partName, int y) { }
    private record CoordWithPart(ChunkCoord coord, PartData part) {
        public static final Codec<CoordWithPart> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.DIMENSION).fieldOf("level").forGetter(s -> s.coord.dimension()),
                Codec.INT.fieldOf("x").forGetter(s -> s.coord.chunkX()),
                Codec.INT.fieldOf("z").forGetter(s -> s.coord.chunkZ()),
                Codec.STRING.fieldOf("part").forGetter(s -> s.part.partName()),
                Codec.INT.fieldOf("y").forGetter(s -> s.part.y())
        ).apply(instance, (level, x, z, part, y) -> new CoordWithPart(new ChunkCoord(level, x, z), new PartData(part, y))));
    }
    private final Map<ChunkCoord, List<PartData>> partData = new HashMap<>();

    private static final Codec<EditModeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CoordWithPart.CODEC.listOf().fieldOf("data").forGetter(d -> {
                List<CoordWithPart> result = new ArrayList<>();
                d.partData.forEach((coord, list) -> list.forEach(part -> result.add(new CoordWithPart(coord, part))));
                return result;
            })
    ).apply(instance, EditModeData::new));

    private static final SavedDataType<EditModeData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("lostcities", NAME),
            EditModeData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    @Nonnull
    public static EditModeData getData() {
        ServerLevel overworld = WorldTools.getOverworld();
        SavedDataStorage storage = overworld.getServer().getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    private EditModeData() {
    }

    private EditModeData(List<CoordWithPart> partData) {
        for (CoordWithPart d : partData) {
            addPartData(d.coord, d.part.y, d.part.partName);
        }
    }

    public void addPartData(ChunkCoord pos, int y, String partName) {
        partData.computeIfAbsent(pos, p -> new ArrayList<>()).add(new PartData(partName, y));
        setDirty();
    }

    public List<PartData> getPartData(ChunkCoord pos) {
        return partData.getOrDefault(pos, Collections.emptyList());
    }
}
