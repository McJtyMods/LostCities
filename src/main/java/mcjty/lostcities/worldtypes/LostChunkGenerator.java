package mcjty.lostcities.worldtypes;

import net.minecraft.world.World;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.NetherChunkGenerator;
import net.minecraft.world.gen.NetherGenSettings;

public class LostChunkGenerator extends NetherChunkGenerator {

    public LostChunkGenerator(World world, BiomeProvider biomeProvider, NetherGenSettings settings) {
        super(world, biomeProvider, settings);
    }
}
