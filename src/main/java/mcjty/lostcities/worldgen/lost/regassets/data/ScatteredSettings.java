package mcjty.lostcities.worldgen.lost.regassets.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class ScatteredSettings {

    public static final Codec<ScatteredSettings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("areasize").forGetter(l -> l.areasize),
                    Codec.FLOAT.fieldOf("chance").forGetter(l -> l.chance),
                    Codec.INT.fieldOf("weightnone").forGetter(l -> l.weightnone),
                    Codec.list(ScatteredReference.CODEC).fieldOf("list").forGetter((l -> l.list))
            ).apply(instance, ScatteredSettings::new));

    private final int areasize;
    private final float chance;
    private final int weightnone;       // The weight that nothing is selected
    private final List<ScatteredReference> list;
    private int totalweight;

    public ScatteredSettings(int areasize, float chance, int weightnone, List<ScatteredReference> list) {
        this.areasize = areasize;
        this.chance = chance;
        this.weightnone = weightnone;
        this.list = list;
        totalweight = 0;
        for (ScatteredReference reference : list) {
            totalweight += reference.getWeight();
        }
    }

    public int getAreasize() {
        return areasize;
    }

    public int getWeightnone() {
        return weightnone;
    }

    public float getChance() {
        return chance;
    }

    public int getTotalweight() {
        return totalweight;
    }

    public List<ScatteredReference> getList() {
        return list;
    }
}
