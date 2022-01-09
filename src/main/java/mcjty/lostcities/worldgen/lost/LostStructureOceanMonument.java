package mcjty.lostcities.worldgen.lost;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.OceanMonumentFeature;

public class LostStructureOceanMonument extends OceanMonumentFeature {

    public LostStructureOceanMonument() {
        super(NoneFeatureConfiguration.CODEC);
    }

    public boolean hasStructure(Level world, int chunkX, int chunkZ) {
//        return canSpawnStructureAtCoords(chunkX, chunkZ);
        // @todo 1.14
        return false;
    }
}
