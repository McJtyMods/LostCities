package mcjty.lostcities.editor;

import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.WorldTools;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * In a world created in editmode this structure will contain information about all generated parts
 */
public class EditModeData extends SavedData {

    public static final String NAME = "LostCityEditData";

    public static record PartData(String partName, int y) { }
    private final Map<ChunkCoord, List<PartData>> partData = new HashMap<>();

    @Nonnull
    public static EditModeData getData() {
        ServerLevel overworld = WorldTools.getOverworld();
        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(EditModeData::new, EditModeData::new, NAME);
    }

    public EditModeData() {
    }

    public EditModeData(CompoundTag nbt) {
        ListTag data = nbt.getList("data", Tag.TAG_COMPOUND);
        for (Tag t : data) {
            CompoundTag pdTag = (CompoundTag) t;
            ResourceKey<Level> level = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(pdTag.getString("level")));
            int chunkX = pdTag.getInt("x");
            int chunkZ = pdTag.getInt("z");
            ChunkCoord pos = new ChunkCoord(level, chunkX, chunkZ);
            String part = pdTag.getString("part");
            int y = pdTag.getInt("y");
            addPartData(pos, y, part);
        }
    }

    public void addPartData(ChunkCoord pos, int y, String partName) {
        partData.computeIfAbsent(pos, p -> new ArrayList<>()).add(new PartData(partName, y));
    }

    public List<PartData> getPartData(ChunkCoord pos) {
        return partData.getOrDefault(pos, Collections.emptyList());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag data = new ListTag();
        partData.forEach((pos, list) -> {
            for (PartData pd : list) {
                CompoundTag pdTag = new CompoundTag();
                pdTag.putString("level", pos.dimension().location().toString());
                pdTag.putInt("x", pos.chunkX());
                pdTag.putInt("z", pos.chunkX());
                pdTag.putString("part", pd.partName());
                pdTag.putInt("y", pd.y());
                data.add(pdTag);
            }
        });
        tag.put("data", data);
        return tag;
    }
}
