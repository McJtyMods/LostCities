package mcjty.lostcities.worldgen.lost;

import net.minecraft.world.World;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.OceanMonumentStructure;

public class LostStructureOceanMonument extends OceanMonumentStructure {

    public LostStructureOceanMonument() {
        super(NoFeatureConfig.field_236558_a_);
    }

    public boolean hasStructure(World world, int chunkX, int chunkZ) {
//        return canSpawnStructureAtCoords(chunkX, chunkZ);
        // @todo 1.14
        return false;
    }
}
