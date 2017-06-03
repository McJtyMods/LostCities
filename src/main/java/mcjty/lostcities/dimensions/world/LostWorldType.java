package mcjty.lostcities.dimensions.world;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunkGenerator;

public class LostWorldType extends WorldType {

    public LostWorldType() {
        super("lostcities");
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        return new LostCityChunkGenerator(world);
    }
}
