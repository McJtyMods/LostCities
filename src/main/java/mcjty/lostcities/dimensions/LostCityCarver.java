package mcjty.lostcities.dimensions;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.varia.WorldTools;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.carver.EmptyCarverConfig;
import net.minecraft.world.gen.carver.WorldCarver;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LostCityCarver extends WorldCarver<EmptyCarverConfig> {

    private Map<DimensionType, IDimensionInfo> dimensionInfo = new HashMap<>();

    public LostCityCarver() {
        super(dynamic -> new EmptyCarverConfig(), 256);
    }

    @Override
    public boolean carve(IChunk chunk, Random rand, int seaLevel, int chunkX, int chunkZ, int p_212867_6_, int p_212867_7_, BitSet carvingMask, EmptyCarverConfig config) {
        DimensionType type = DimensionType.OVERWORLD; // @todo !!!!!!!!!!!!! chunk.getWorldForge().getDimension().getType();
        if (!dimensionInfo.containsKey(type)) {
            LostCityProfile profile = LostCityConfiguration.standardProfiles.get("default");
//            IDimensionInfo diminfo = new DefaultDimensionInfo(chunk.getWorldForge(), profile);
            // @todo !!!!!!!!!!!!
            IDimensionInfo diminfo = new DefaultDimensionInfo(WorldTools.getWorld(DimensionType.OVERWORLD), profile);
            dimensionInfo.put(type, diminfo);
        }
        IDimensionInfo diminfo = dimensionInfo.get(type);
        diminfo.getCarver().carve(chunk);
        return true;
    }

    @Override
    public boolean shouldCarve(Random rand, int chunkX, int chunkZ, EmptyCarverConfig config) {
        return true;
    }

    @Override
    protected boolean func_222708_a(double p_222708_1_, double p_222708_3_, double p_222708_5_, int p_222708_7_) {
        return false;
    }
}
