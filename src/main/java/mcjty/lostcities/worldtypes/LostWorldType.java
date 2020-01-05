package mcjty.lostcities.worldtypes;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.OverworldBiomeProviderSettings;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.NetherGenSettings;

public class LostWorldType extends WorldType {

    private static LostWorldType worldType;

    public static void init() {
        worldType = new LostWorldType("lc_cavern");
    }

    public LostWorldType(String name) {
        super(name);
    }

    @Override
    public ChunkGenerator<?> createChunkGenerator(World world) {
        return new LostChunkGenerator(world, new LostBiomeProvider(new OverworldBiomeProviderSettings().setWorldInfo(world.getWorldInfo())), new NetherGenSettings());
    }


}
