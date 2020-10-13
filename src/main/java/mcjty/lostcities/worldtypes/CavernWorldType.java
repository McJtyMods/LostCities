package mcjty.lostcities.worldtypes;

public class CavernWorldType {} /* @todo 1.16 extends WorldType {

    @SuppressWarnings("FieldCanBeLocal")
    private static CavernWorldType worldType;

    public static void init() {
        worldType = new CavernWorldType("lc_cavern");
    }

    public CavernWorldType(String name) {
        super(name);
    }

    @Override
    public ChunkGenerator createChunkGenerator(World world) {
        return new CavernChunkGenerator(world, new LostBiomeProvider(new OverworldBiomeProviderSettings(world.getWorldInfo())), new NetherGenSettings());
    }


}
*/