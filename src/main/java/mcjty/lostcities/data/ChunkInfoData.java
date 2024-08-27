package mcjty.lostcities.data;

import mcjty.lostcities.api.*;
import mcjty.lostcities.setup.CustomRegistries;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.WorldTools;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import net.minecraft.core.registries.Registries;
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

public class ChunkInfoData extends SavedData {

    public static final String NAME = "LostCityChunkData";

    private final Map<ChunkCoord, List<LostChunkCharacteristics>> chunkData = new HashMap<>();

    @Nonnull
    public static ChunkInfoData getData() {
        ServerLevel overworld = WorldTools.getOverworld();
        DimensionDataStorage storage = overworld.getDataStorage();
        return storage.computeIfAbsent(ChunkInfoData::new, ChunkInfoData::new, NAME);
    }

    public ChunkInfoData() {
    }

    public ChunkInfoData(CompoundTag nbt) {
        ListTag data = nbt.getList("data", Tag.TAG_COMPOUND);
        for (Tag t : data) {
            CompoundTag pdTag = (CompoundTag) t;
            ResourceKey<Level> level = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(pdTag.getString("level")));
            int chunkX = pdTag.getInt("x");
            int chunkZ = pdTag.getInt("z");
            ChunkCoord pos = new ChunkCoord(level, chunkX, chunkZ);
            String part = pdTag.getString("part");
            int y = pdTag.getInt("y");
            addPartData(pos, y, part);
        }
    }

    private LostChunkCharacteristics fromNbt(CompoundTag tag) {
        LostChunkCharacteristics characteristics = new LostChunkCharacteristics();
        characteristics.isCity = tag.getBoolean("city");
        characteristics.couldHaveBuilding = tag.getBoolean("couldHaveBuilding");
        characteristics.multiPos = new MultiPos(tag.getInt("multiX"), tag.getInt("multiZ"), tag.getInt("multiW"), tag.getInt("multiH"));
        characteristics.cityLevel = tag.getInt("cityLevel");
        characteristics.cityStyleId = new ResourceLocation(tag.getString("cityStyle"));
        characteristics.multiBuildingId = new ResourceLocation(tag.getString("multiBuilding"));
        characteristics.buildingTypeId = new ResourceLocation(tag.getString("buildingType"));
        return characteristics;
    }

    private CompoundTag toNbt(LostChunkCharacteristics characteristics) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("city", characteristics.isCity);
        tag.putBoolean("couldHaveBuilding", characteristics.couldHaveBuilding);
        tag.putInt("multiX", characteristics.multiPos.x());
        tag.putInt("multiZ", characteristics.multiPos.z());
        tag.putInt("multiW", characteristics.multiPos.w());
        tag.putInt("multiH", characteristics.multiPos.h());
        tag.putInt("cityLevel", characteristics.cityLevel);
        tag.putString("cityStyle", characteristics.cityStyleId.toString());
        tag.putString("multiBuilding", characteristics.multiBuildingId.toString());
        tag.putString("buildingType", characteristics.buildingTypeId.toString());
        return tag;
    }

    public void addPartData(ChunkCoord pos, int y, String partName) {
        chunkData.computeIfAbsent(pos, p -> new ArrayList<>()).add(new LostChunkCharacteristics(partName, y));
        setDirty();
    }

    public List<LostChunkCharacteristics> getChunkData(ChunkCoord pos) {
        return chunkData.getOrDefault(pos, Collections.emptyList());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag data = new ListTag();
        chunkData.forEach((pos, list) -> {
            for (LostChunkCharacteristics pd : list) {
                CompoundTag pdTag = new CompoundTag();
                pdTag.putString("level", pos.dimension().location().toString());
                pdTag.putInt("x", pos.chunkX());
                pdTag.putInt("z", pos.chunkZ());
                pdTag.putString("part", pd.partName());
                pdTag.putInt("y", pd.y());
                data.add(pdTag);
            }
        });
        tag.put("data", data);
        return tag;
    }
}
