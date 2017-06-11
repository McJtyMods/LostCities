package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.LostCitiesTerrainGenerator;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.*;

public class LostCityChunkGenerator implements IChunkGenerator {

    public Random rand;
    public long seed;

    public World worldObj;
    public WorldType worldType;
    private final BaseTerrainGenerator terrainGenerator;

    private ChunkGeneratorSettings settings = null;

    public Biome[] biomesForGeneration;

    private MapGenBase caveGenerator = new MapGenCaves();


    private MapGenStronghold strongholdGenerator = new MapGenStronghold();
    private StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();
    private MapGenVillage villageGenerator = new MapGenVillage();
    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();

    public ChunkGeneratorSettings getSettings() {
        if (settings == null) {
            ChunkGeneratorSettings.Factory factory = new ChunkGeneratorSettings.Factory();
            settings = factory.build();
        }
        return settings;
    }

    // Holds ravine generator
    private MapGenBase ravineGenerator = new MapGenRavine();

    public IChunkGenerator otherGenerator = null;

    public LostCityChunkGenerator(World world, IChunkGenerator otherGenerator) {
        this(world);
        this.otherGenerator = otherGenerator;
    }

    public LostCityChunkGenerator(World world) {

        {
            caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
            strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, STRONGHOLD);

            villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, VILLAGE);
            mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
            scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(scatteredFeatureGenerator, SCATTERED_FEATURE);
            ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
            oceanMonumentGenerator = (StructureOceanMonument) TerrainGen.getModdedMapGen(oceanMonumentGenerator, OCEAN_MONUMENT);
        }


        this.worldObj = world;

        this.worldType = world.getWorldInfo().getTerrainType();

        this.seed = world.getSeed();
//        System.out.println("GenericChunkGenerator: seed = " + seed);
        this.rand = new Random((seed + 516) * 314);
//        this.rand = new Random(seed);

        int waterLevel = (byte) (LostCityConfiguration.GROUNDLEVEL - LostCityConfiguration.WATERLEVEL_OFFSET);
        world.setSeaLevel(waterLevel);

        terrainGenerator = new LostCitiesTerrainGenerator();
        terrainGenerator.setup(world, this);
    }

    @Override
    public Chunk provideChunk(int chunkX, int chunkZ) {
        this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);

        if (otherGenerator != null) {
            // For ATG, experimental
            BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, seed, this);
            if (!info.isCity) {
                return otherGenerator.provideChunk(chunkX, chunkZ);
            }
        }


        ChunkPrimer chunkprimer = new ChunkPrimer();

        this.biomesForGeneration = this.worldObj.getBiomeProvider().getBiomesForGeneration(this.biomesForGeneration, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);
        terrainGenerator.generate(chunkX, chunkZ, chunkprimer);
        this.biomesForGeneration = this.worldObj.getBiomeProvider().getBiomes(this.biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);
        terrainGenerator.replaceBlocksForBiome(chunkX, chunkZ, chunkprimer, this.biomesForGeneration);

        if (LostCityConfiguration.GENERATE_CAVES) {
            this.caveGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }
        if (LostCityConfiguration.GENERATE_RAVINES) {
            this.ravineGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        if (LostCityConfiguration.GENERATE_MINESHAFTS) {
            this.mineshaftGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        if (LostCityConfiguration.GENERATE_VILLAGES) {
            if (LostCityConfiguration.PREVENT_VILLAGES_IN_CITIES) {
                if (!BuildingInfo.isCity(chunkX, chunkZ, seed, this)) {
                    this.villageGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
                }
            } else {
                this.villageGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
            }
        }

        if (LostCityConfiguration.GENERATE_STRONGHOLDS) {
            this.strongholdGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        if (LostCityConfiguration.GENERATE_SCATTERED) {
            this.scatteredFeatureGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        if (LostCityConfiguration.GENERATE_OCEANMONUMENTS) {
            this.oceanMonumentGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        Chunk chunk = new Chunk(this.worldObj, chunkprimer, chunkX, chunkZ);
        byte[] abyte = chunk.getBiomeArray();

        for (int i = 0; i < abyte.length; ++i) {
            abyte[i] = (byte) Biome.getIdForBiome(this.biomesForGeneration[i]);
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        BlockFalling.fallInstantly = true;
        int x = chunkX * 16;
        int z = chunkZ * 16;
        World w = this.worldObj;
        Biome Biome = w.getBiomeForCoordsBody(new BlockPos(x + 16, 0, z + 16));
        this.rand.setSeed(w.getSeed());
        long i1 = this.rand.nextLong() / 2L * 2L + 1L;
        long j1 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkX * i1 + chunkZ * j1 ^ w.getSeed());
        boolean flag = false;

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(this, w, rand, chunkX, chunkZ, flag));

        ChunkPos cp = new ChunkPos(chunkX, chunkZ);

        if (LostCityConfiguration.GENERATE_MINESHAFTS) {
            this.mineshaftGenerator.generateStructure(w, this.rand, cp);
        }
        if (LostCityConfiguration.GENERATE_VILLAGES) {
            if (LostCityConfiguration.PREVENT_VILLAGES_IN_CITIES) {
                if (!BuildingInfo.isCity(chunkX, chunkZ, seed, this)) {
                    flag = this.villageGenerator.generateStructure(w, this.rand, cp);
                }
            } else {
                flag = this.villageGenerator.generateStructure(w, this.rand, cp);
            }
        }
        if (LostCityConfiguration.GENERATE_STRONGHOLDS) {
            this.strongholdGenerator.generateStructure(w, this.rand, cp);
        }
        if (LostCityConfiguration.GENERATE_SCATTERED) {
            this.scatteredFeatureGenerator.generateStructure(w, this.rand, cp);
        }
        if (LostCityConfiguration.GENERATE_OCEANMONUMENTS) {
            this.oceanMonumentGenerator.generateStructure(w, this.rand, cp);
        }

        int k1;
        int l1;
        int i2;

//            if (dimensionInformation.hasFeatureType(FeatureType.FEATURE_LAKES)) {
        if (Biome != Biomes.DESERT && Biome != Biomes.DESERT_HILLS && !flag && this.rand.nextInt(4) == 0
                && TerrainGen.populate(this, w, rand, chunkX, chunkZ, flag, PopulateChunkEvent.Populate.EventType.LAKE)) {
            k1 = x + this.rand.nextInt(16) + 8;
            l1 = this.rand.nextInt(256);
            i2 = z + this.rand.nextInt(16) + 8;
            (new WorldGenLakes(Blocks.WATER)).generate(w, this.rand, new BlockPos(k1, l1, i2));
        }

        if (TerrainGen.populate(this, w, rand, chunkX, chunkZ, flag, PopulateChunkEvent.Populate.EventType.LAVA) && !flag && this.rand.nextInt(8) == 0) {
            k1 = x + this.rand.nextInt(16) + 8;
            l1 = this.rand.nextInt(this.rand.nextInt(248) + 8);
            i2 = z + this.rand.nextInt(16) + 8;

            if (l1 < (LostCityConfiguration.GROUNDLEVEL - LostCityConfiguration.WATERLEVEL_OFFSET) || this.rand.nextInt(10) == 0) {
                (new WorldGenLakes(Blocks.LAVA)).generate(w, this.rand, new BlockPos(k1, l1, i2));
            }
        }

        boolean doGen = false;
        doGen = TerrainGen.populate(this, w, rand, chunkX, chunkZ, flag, PopulateChunkEvent.Populate.EventType.DUNGEON);
        for (k1 = 0; doGen && k1 < 8; ++k1) {
            l1 = x + this.rand.nextInt(16) + 8;
            i2 = this.rand.nextInt(256);
            int j2 = z + this.rand.nextInt(16) + 8;
            (new WorldGenDungeons()).generate(w, this.rand, new BlockPos(l1, i2, j2));
        }

        BlockPos pos = new BlockPos(x, 0, z);
        Biome.decorate(w, this.rand, pos);

        if (TerrainGen.populate(this, w, rand, chunkX, chunkZ, flag, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
            WorldEntitySpawner.performWorldGenSpawning(w, Biome, x + 8, z + 8, 16, 16, this.rand);
        }
        x += 8;
        z += 8;

        doGen = TerrainGen.populate(this, w, rand, chunkX, chunkZ, flag, PopulateChunkEvent.Populate.EventType.ICE);
        for (k1 = 0; doGen && k1 < 16; ++k1) {
            for (l1 = 0; l1 < 16; ++l1) {
                i2 = w.getPrecipitationHeight(new BlockPos(x + k1, 0, z + l1)).getY();

                if (w.canBlockFreeze(new BlockPos(k1 + x, i2 - 1, l1 + z), false)) {
                    w.setBlockState(new BlockPos(k1 + x, i2 - 1, l1 + z), Blocks.ICE.getDefaultState(), 2);
                }

                if (w.canSnowAt(new BlockPos(k1 + x, i2, l1 + z), true)) {
                    w.setBlockState(new BlockPos(k1 + x, i2, l1 + z), Blocks.SNOW_LAYER.getDefaultState(), 2);
                }
            }
        }

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(this, w, rand, chunkX, chunkZ, flag));

        BlockFalling.fallInstantly = false;
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        boolean flag = false;

        if (chunkIn.getInhabitedTime() < 3600L) {
            flag |= this.oceanMonumentGenerator.generateStructure(this.worldObj, this.rand, new ChunkPos(x, z));
        }

        return flag;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return getDefaultCreatures(creatureType, pos);
    }

    private List getDefaultCreatures(EnumCreatureType creatureType, BlockPos pos) {
        Biome Biome = this.worldObj.getBiomeForCoordsBody(pos);
        if (creatureType == EnumCreatureType.MONSTER) {
            if (this.scatteredFeatureGenerator.isInsideStructure(pos)) {
                return this.scatteredFeatureGenerator.getScatteredFeatureSpawnList();
            }
            if (this.oceanMonumentGenerator.isPositionInStructure(this.worldObj, pos)) {
                return this.oceanMonumentGenerator.getScatteredFeatureSpawnList();
            }
        }

        return Biome.getSpawnableList(creatureType);
    }

    @Nullable
    public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position, boolean p_180513_4_) {
        if ("Stronghold".equals(structureName) && this.strongholdGenerator != null) {
            return this.strongholdGenerator.getClosestStrongholdPos(worldIn, position, p_180513_4_);
//        } else if ("Mansion".equals(structureName) && this.woodlandMansionGenerator != null) {
//            return this.woodlandMansionGenerator.getClosestStrongholdPos(worldIn, position, p_180513_4_);
        } else if ("Monument".equals(structureName) && this.oceanMonumentGenerator != null) {
            return this.oceanMonumentGenerator.getClosestStrongholdPos(worldIn, position, p_180513_4_);
        } else if ("Village".equals(structureName) && this.villageGenerator != null) {
            return this.villageGenerator.getClosestStrongholdPos(worldIn, position, p_180513_4_);
        } else if ("Mineshaft".equals(structureName) && this.mineshaftGenerator != null) {
            return this.mineshaftGenerator.getClosestStrongholdPos(worldIn, position, p_180513_4_);
        } else {
            return "Temple".equals(structureName) && this.scatteredFeatureGenerator != null ? this.scatteredFeatureGenerator.getClosestStrongholdPos(worldIn, position, p_180513_4_) : null;
        }
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        if (LostCityConfiguration.GENERATE_MINESHAFTS) {
            this.mineshaftGenerator.generate(this.worldObj, x, z, null);
        }

        if (LostCityConfiguration.GENERATE_VILLAGES) {
            if (LostCityConfiguration.PREVENT_VILLAGES_IN_CITIES) {
                if (!BuildingInfo.isCity(x, z, seed, this)) {
                    this.villageGenerator.generate(this.worldObj, x, z, null);
                }
            } else {
                this.villageGenerator.generate(this.worldObj, x, z, null);
            }
        }

        if (LostCityConfiguration.GENERATE_STRONGHOLDS) {
            this.strongholdGenerator.generate(this.worldObj, x, z, null);
        }

        if (LostCityConfiguration.GENERATE_SCATTERED) {
            this.scatteredFeatureGenerator.generate(this.worldObj, x, z, null);
        }

        if (LostCityConfiguration.GENERATE_OCEANMONUMENTS) {
            this.oceanMonumentGenerator.generate(this.worldObj, x, z, null);
        }
    }

    @Override
    public boolean func_193414_a(World p_193414_1_, String p_193414_2_, BlockPos p_193414_3_) {
        if ("Stronghold".equals(p_193414_2_) && this.strongholdGenerator != null) {
            return this.strongholdGenerator.isInsideStructure(p_193414_3_);
//        } else if ("Mansion".equals(p_193414_2_) && this.woodlandMansionGenerator != null) {
//            return this.woodlandMansionGenerator.isInsideStructure(p_193414_3_);
        } else if ("Monument".equals(p_193414_2_) && this.oceanMonumentGenerator != null) {
            return this.oceanMonumentGenerator.isInsideStructure(p_193414_3_);
        } else if ("Village".equals(p_193414_2_) && this.villageGenerator != null) {
            return this.villageGenerator.isInsideStructure(p_193414_3_);
        } else if ("Mineshaft".equals(p_193414_2_) && this.mineshaftGenerator != null) {
            return this.mineshaftGenerator.isInsideStructure(p_193414_3_);
        } else {
            return "Temple".equals(p_193414_2_) && this.scatteredFeatureGenerator != null ? this.scatteredFeatureGenerator.isInsideStructure(p_193414_3_) : false;
        }
    }

}
