package mcjty.lostcities.gui;

import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.worldgen.ChunkHeightmap;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.LostCityTerrainFeature;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.WorldStyle;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.util.Random;

public class NullDimensionInfo implements IDimensionInfo {

    private final String[] biomeMap = new String[] {
            "ddddddddddddddddddddddppppppppppppppp==ppppppppppp",
            "ddddddddddddddddddddpppppppppppppppp==pppppppppppp",
            "ddddddddddddddddddddpppppppppppppp===ppppppppppppp",
            "pddddddddddddddddpppppppppppppppppp==ppppppppppppp",
            "pppdddddddppppppppppppppppppppppppp==ppppppppppppp",
            "pppppppppppppppppppppppppppppppppppp==pppppppppp--",
            "ppppppppppppppppppppppppppppppppppppp==ppppppp----",
            "pppppppppppppppppppppppppppppppppppppp==ppppp-----",
            "pppppppppppppppppppppppppppppppppppppp===pppp-----",
            "ppppppppppppppppppppppppppppppppppppppp===ppppp---",
            "pppppppppppppppppppppppppppppppppppppppp==--pp----",
            "pppppppppppppppppppppppppppppppppppppppp*---------",
            "pppppppppppppppppppppppppppppppppppppp****--------",
            "ppppppppppppppppppppppppppppppppppppp***----------",
            "pppppppppppppppppppppppppppppppppppp**------------",
            "ppppppppppppppppppppppppppppppppppppp**-----------",
            "ppppppppppppppppppppppppppppppppppppppp*----------",
            "pppppppppppppppppppppppppppppppppppppp**----------",
            "ppppp###pppppppppppppppppppppppppppppp**----------",
            "ppppp####ppppppp#####pppppppppppppppppp*----------",
            "pppppp#####pp##+++#####ppppppppppppp*****---------",
            "pppppppp#####++++####pppppppppppppp**------pp----p",
            "ppppppppp##++++++###pppppppppppppppp***---pppp--pp",
            "ppppppppp###+++++++#####ppppppppppppp---pppppppppp",
            "pppppppp##p##+++++++###ppppppppppppppppppppppppppp",
            "pppppppppp#####++++####ppppppppppppppppppppppppppp",
            "pppppppppppp###+++++###ppppppppppppppppppppppppppp",
            "ppppppppppppp####++++####ppppppppppppppppppppppppp",
            "pppppppppppppp####++######pppppppppppppppppppppppp",
            "ppppppppppppppp#+++####ppppppppppppppppppppppppppp",
            "ppppppppppppp####pp#####pppppppppppppppppppppppppp",
            "pppppppppp#####ppppppppppppppppppppppppppppppppppp",
            "ppppppppppp###pppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp",
            "pppppppppppppppppppppppppppppppppppppppppppppppppp"
    };

    private final LostCityProfile profile;
    private final WorldStyle style;
    private final Random random;
    private final long seed;

    private final Registry<Biome> biomeRegistry;
    private final LostCityTerrainFeature feature;

    public NullDimensionInfo(LostCityProfile profile, long seed) {
        this.profile = profile;
        style = AssetRegistries.WORLDSTYLES.get("standard");
        this.seed = seed;
        random = new Random(seed);
        feature = new LostCityTerrainFeature(this, profile, getRandom());
        feature.setupStates(profile);
        biomeRegistry = DynamicRegistries.func_239770_b_().func_230521_a_(Registry.BIOME_KEY).get();
    }

    @Override
    public void setWorld(ISeedReader world) {
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public ISeedReader getWorld() {
        return null;
    }

    @Override
    public RegistryKey<World> getType() {
        return World.OVERWORLD;
    }

    @Override
    public LostCityProfile getProfile() {
        return profile;
    }

    @Override
    public LostCityProfile getOutsideProfile() {
        return null;
    }

    @Override
    public WorldStyle getWorldStyle() {
        return style;
    }

    @Override
    public Random getRandom() {
        return random;
    }

    @Override
    public LostCityTerrainFeature getFeature() {
        return feature;
    }

    @Override
    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ) {
        ChunkHeightmap heightmap = new ChunkHeightmap(profile.LANDSCAPE_TYPE, profile.GROUNDLEVEL, getFeature().base);
        char b = getBiomeChar(chunkX, chunkZ);
        int y = 65;
        switch (b) {
            case 'p': y = 65; break;
            case '-': y = 60; break;
            case '=': y = 65; break;
            case '#': y = 95; break;
            case '+': y = 125; break;
            case '*': y = 65; break;
            case 'd': y = 65; break;
        }
        for (int x = 0 ; x < 16 ; x++) {
            for (int z = 0 ; z < 16 ; z++) {
                heightmap.update(x, y, z, getFeature().base);
            }
        }
        return heightmap;
    }

    public char getBiomeChar(int chunkX, int chunkZ) {
        if (chunkX >= 0 && chunkX < 50 && chunkZ >= 0 && chunkZ < 50) {
            return biomeMap[chunkZ].charAt(chunkX);
        } else {
            return 'p';
        }
    }

//    @Override
//    public Biome[] getBiomes(int chunkX, int chunkZ) {
//        Biome[] biomes = new Biome[10*10];
//        Biome biome = Biomes.PLAINS;
//        char b = getBiomeChar(chunkX, chunkZ);
//        switch (b) {
//            case 'p': biome = Biomes.PLAINS; break;
//            case '-': biome = Biomes.OCEAN; break;
//            case '=': biome = Biomes.RIVER; break;
//            case '#': biome = Biomes.MOUNTAIN_EDGE; break;
//            case '+': biome = Biomes.MOUNTAINS; break;
//            case '*': biome = Biomes.BEACH; break;
//            case 'd': biome = Biomes.DESERT; break;
//        }
//        for (int i = 0 ; i < biomes.length ; i++) {
//            biomes[i] = biome;
//        }
//        return biomes;
//    }

    @Override
    public Biome getBiome(BlockPos pos) {
        RegistryKey<Biome> biome = Biomes.PLAINS;
        ChunkPos cp = new ChunkPos(pos);
        char b = getBiomeChar(cp.x, cp.z);
        switch (b) {
            case 'p': biome = Biomes.PLAINS; break;
            case '-': biome = Biomes.OCEAN; break;
            case '=': biome = Biomes.RIVER; break;
            case '#': biome = Biomes.MOUNTAIN_EDGE; break;
            case '+': biome = Biomes.MOUNTAINS; break;
            case '*': biome = Biomes.BEACH; break;
            case 'd': biome = Biomes.DESERT; break;
        }
        return biomeRegistry.getOrDefault(biome.getLocation());
    }
}
