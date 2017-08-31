package mcjty.lostcities.dimensions.world.lost;

import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureOceanMonument;

public class LostStructureOceanMonument extends StructureOceanMonument {

    public boolean hasStructure(World world, int chunkX, int chunkZ) {
        this.world = world;
        return canSpawnStructureAtCoords(chunkX, chunkZ);
    }
}
