package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.api.IChunkPrimerFactory;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class LostWorldTypeAdapter extends LostWorldType {

    public LostWorldTypeAdapter(String other) {
        super("lc_" + other);
        this.otherWorldtype = other;
    }

    private final String otherWorldtype;
    private BiomeProvider biomeProvider = null;
    private IChunkGenerator otherGenerator = null;
    private IChunkPrimerFactory factory = null;


    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        if (otherGenerator == null) {
            for (WorldType type : WorldType.WORLD_TYPES) {
                if (otherWorldtype.equals(type.getName())) {
                    WorldType orig = world.getWorldInfo().getTerrainType();
                    world.getWorldInfo().setTerrainType(type);
                    otherGenerator = type.getChunkGenerator(world, generatorOptions);
                    world.getWorldInfo().setTerrainType(orig);
                    if (otherGenerator instanceof IChunkPrimerFactory) {
                        factory = (IChunkPrimerFactory) otherGenerator;
                    }
                    break;
                }
            }
        }
        return new LostCityChunkGenerator(world, factory);
    }

    @Override
    protected BiomeProvider getInternalBiomeProvider(World world) {
        if (biomeProvider == null) {
            for (WorldType type : WorldType.WORLD_TYPES) {
                if (otherWorldtype.equals(type.getName())) {
                    WorldType orig = world.getWorldInfo().getTerrainType();
                    world.getWorldInfo().setTerrainType(type);
                    biomeProvider = type.getBiomeProvider(world);
                    world.getWorldInfo().setTerrainType(orig);
                    break;
                }
            }
        }
        return biomeProvider;
    }
}
