package mcjty.lostcities.worldtypes;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.OverworldBiomeProviderSettings;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.NetherGenSettings;

public class CavernWorldType extends WorldType {

    @SuppressWarnings("FieldCanBeLocal")
    private static CavernWorldType worldType;

    public static void init() {
        worldType = new CavernWorldType("lc_cavern");
    }

    public CavernWorldType(String name) {
        super(name);
    }

    @Override
    public ChunkGenerator<?> createChunkGenerator(World world) {
        return new CavernChunkGenerator(world, new LostBiomeProvider(new OverworldBiomeProviderSettings(world.getWorldInfo())), new NetherGenSettings());
    }


}
