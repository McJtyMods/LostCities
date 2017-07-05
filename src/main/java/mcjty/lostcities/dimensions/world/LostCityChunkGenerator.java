package mcjty.lostcities.dimensions.world;

import mcjty.lib.compat.CompatChunkGenerator;
import mcjty.lib.compat.CompatMapGenStructure;
import mcjty.lib.tools.EntityTools;
import mcjty.lostcities.api.IChunkPrimerFactory;
import mcjty.lostcities.api.ILostChunkGenerator;
import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.dimensions.world.lost.cityassets.ConditionContext;
import mcjty.lostcities.dimensions.world.lost.cityassets.WorldStyle;
import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockVine;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.*;

public class LostCityChunkGenerator implements CompatChunkGenerator, ILostChunkGenerator {

    public LostCityProfile profile; // Current profile
    public WorldStyle worldStyle;

    public Random rand;
    public long seed;
    public int dimensionId;

    public World worldObj;
    public WorldType worldType;
    public final LostCitiesTerrainGenerator terrainGenerator;

    private ChunkProviderSettings settings = null;

    private Biome[] biomesForGeneration;

    private MapGenBase caveGenerator;

    // Sometimes we have to precalculate primers for a chunk before the
    // chunk is generated. In that case we cache them here so that when the
    // chunk is really generated it will find it and use that instead of
    // making that primer again
    // @todo, make this cache timed so that primers expire if they are not used quickly enough?
    private Map<ChunkCoord, ChunkPrimer> cachedPrimers = new HashMap<>();
    private Map<ChunkCoord, ChunkHeightmap> cachedHeightmaps = new HashMap<>();

    private MapGenStronghold strongholdGenerator = new MapGenStronghold();
    private StructureOceanMonument oceanMonumentGenerator = new StructureOceanMonument();
    private MapGenVillage villageGenerator = new MapGenVillage();
    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
    private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();

    public ChunkProviderSettings getSettings() {
        if (settings == null) {
            ChunkProviderSettings.Factory factory = new ChunkProviderSettings.Factory();
            settings = factory.build();
        }
        return settings;
    }

    // Holds ravine generator
    private MapGenBase ravineGenerator = new MapGenRavine();

    public IChunkPrimerFactory otherGenerator = null;

    public LostCityChunkGenerator(World world, IChunkPrimerFactory otherGenerator) {
        this(world, world.getSeed());
        this.otherGenerator = otherGenerator;
    }

    public LostCityChunkGenerator(World world, long seed) {

        {
            caveGenerator = new LostGenCaves(this);
            caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
            strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, STRONGHOLD);

            villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, VILLAGE);
            mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
            scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(scatteredFeatureGenerator, SCATTERED_FEATURE);
            ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
            oceanMonumentGenerator = (StructureOceanMonument) TerrainGen.getModdedMapGen(oceanMonumentGenerator, OCEAN_MONUMENT);
        }

        dimensionId = world.provider.getDimension();
        profile = LostWorldType.getProfile(world);

        System.out.println("LostCityChunkGenerator.LostCityChunkGenerator: profile=" + profile.getName());
        worldStyle = AssetRegistries.WORLDSTYLES.get(profile.getWorldStyle());
        if (worldStyle == null) {
            throw new RuntimeException("Unknown worldstyle '" + profile.getWorldStyle() + "'!");
        }

        String generatorOptions = profile.GENERATOR_OPTIONS;
        if (generatorOptions != null && !generatorOptions.isEmpty()) {
            this.settings = ChunkProviderSettings.Factory.jsonToFactory(generatorOptions).build();
        }

        this.worldObj = world;

        this.worldType = world.getWorldInfo().getTerrainType();

        this.seed = seed;
        this.rand = new Random((seed + 516) * 314);

        int waterLevel = (byte) (profile.GROUNDLEVEL - profile.WATERLEVEL_OFFSET);
        world.setSeaLevel(waterLevel);

        terrainGenerator = new LostCitiesTerrainGenerator(this);
        terrainGenerator.setup(world);
    }

    public ChunkPrimer generatePrimer(int chunkX, int chunkZ) {
        this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer();

        if (otherGenerator != null) {
            // For ATG, experimental
            otherGenerator.fillChunk(chunkX, chunkZ, chunkprimer);
        } else {
            terrainGenerator.doCoreChunk(chunkX, chunkZ, chunkprimer);
        }
        return chunkprimer;
    }

    // Get a heightmap for a chunk. If needed calculate (and cache) a primer
    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ) {
        ChunkCoord key = new ChunkCoord(worldObj.provider.getDimension(), chunkX, chunkZ);
        if (cachedHeightmaps.containsKey(key)) {
            return cachedHeightmaps.get(key);
        } else if (cachedPrimers.containsKey(key)) {
            ChunkHeightmap heightmap = new ChunkHeightmap(cachedPrimers.get(key));
            cachedHeightmaps.put(key, heightmap);
            return heightmap;
        } else {
            ChunkPrimer primer = generatePrimer(chunkX, chunkZ);
            cachedPrimers.put(key, primer);
            ChunkHeightmap heightmap = new ChunkHeightmap(cachedPrimers.get(key));
            cachedHeightmaps.put(key, heightmap);
            return heightmap;
        }
    }



    @Override
    public Chunk provideChunk(int chunkX, int chunkZ) {
        LostCitiesTerrainGenerator.setupChars();
        boolean isCity = BuildingInfo.isCity(chunkX, chunkZ, this);

        ChunkPrimer chunkprimer;
        if (isCity) {
            chunkprimer = new ChunkPrimer();
        } else {
            ChunkCoord key = new ChunkCoord(worldObj.provider.getDimension(), chunkX, chunkZ);
            if (cachedPrimers.containsKey(key)) {
                // We calculated a primer earlier. Reuse it
                chunkprimer = cachedPrimers.get(key);
                cachedPrimers.remove(key);
            } else {
                chunkprimer = generatePrimer(chunkX, chunkZ);
            }
            // Calculate the chunk heightmap in case we need it later
            if (!cachedHeightmaps.containsKey(key)) {
                // We might need this later
                cachedHeightmaps.put(key, new ChunkHeightmap(chunkprimer));
            }
        }

        terrainGenerator.generate(chunkX, chunkZ, chunkprimer);

        this.biomesForGeneration = this.worldObj.getBiomeProvider().getBiomes(this.biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);
        terrainGenerator.replaceBlocksForBiome(chunkX, chunkZ, chunkprimer, this.biomesForGeneration);

        if (profile.GENERATE_CAVES) {
            this.caveGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }
        if (profile.GENERATE_RAVINES) {
            if (!profile.PREVENT_LAKES_RAVINES_IN_CITIES || !isCity) {
                this.ravineGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
            }
        }

        if (profile.GENERATE_MINESHAFTS) {
            this.mineshaftGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        if (profile.GENERATE_VILLAGES) {
            if (profile.PREVENT_VILLAGES_IN_CITIES) {
                if (!isCity) {
                    this.villageGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
                }
            } else {
                this.villageGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
            }
        }

        if (profile.GENERATE_STRONGHOLDS) {
            this.strongholdGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        if (profile.GENERATE_SCATTERED) {
            this.scatteredFeatureGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        if (profile.GENERATE_OCEANMONUMENTS) {
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

    private void generateTrees(Random random, int chunkX, int chunkZ, World world, LostCityChunkGenerator provider) {
        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);
        for (BlockPos pos : info.getSaplingTodo()) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.SAPLING) {
                ((BlockSapling)Blocks.SAPLING).generateTree(world, pos, state, random);
            }
        }
        info.clearSaplingTodo();
    }

    private void generateVines(Random random, int chunkX, int chunkZ, World world, LostCityChunkGenerator provider) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;
        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);

        if (info.hasBuilding) {
            BuildingInfo adjacent = info.getXmax();
            int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
            for (int z = 0; z < 15; z++) {
                for (int y = bottom; y < (info.getMaxHeight()); y++) {
                    if (random.nextFloat() < provider.profile.VINE_CHANCE) {
                        createVineStrip(random, world, bottom, BlockVine.WEST, new BlockPos(cx + 16, y, cz + z), new BlockPos(cx + 15, y, cz + z));
                    }
                }
            }
        }
        if (info.getXmax().hasBuilding) {
            BuildingInfo adjacent = info.getXmax();
            int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? info.getMaxHeight() : (info.getCityGroundLevel() + 3));
            for (int z = 0; z < 15; z++) {
                for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
                    if (random.nextFloat() < provider.profile.VINE_CHANCE) {
                        createVineStrip(random, world, bottom, BlockVine.EAST, new BlockPos(cx + 15, y, cz + z), new BlockPos(cx + 16, y, cz + z));
                    }
                }
            }
        }

        if (info.hasBuilding) {
            BuildingInfo adjacent = info.getZmax();
            int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
            for (int x = 0; x < 15; x++) {
                for (int y = bottom; y < (info.getMaxHeight()); y++) {
                    if (random.nextFloat() < provider.profile.VINE_CHANCE) {
                        createVineStrip(random, world, bottom, BlockVine.NORTH, new BlockPos(cx + x, y, cz + 16), new BlockPos(cx + x, y, cz + 15));
                    }
                }
            }
        }
        if (info.getZmax().hasBuilding) {
            BuildingInfo adjacent = info.getZmax();
            int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? info.getMaxHeight() : (info.getCityGroundLevel() + 3));
            for (int x = 0; x < 15; x++) {
                for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
                    if (random.nextFloat() < provider.profile.VINE_CHANCE) {
                        createVineStrip(random, world, bottom, BlockVine.SOUTH, new BlockPos(cx + x, y, cz + 15), new BlockPos(cx + x, y, cz + 16));
                    }
                }
            }
        }
    }

    private void createVineStrip(Random random, World world, int bottom, PropertyBool direction, BlockPos pos, BlockPos vineHolderPos) {
        if (world.isAirBlock(vineHolderPos)) {
            return;
        }
        if (!world.isAirBlock(pos)) {
            return;
        }
        world.setBlockState(pos, Blocks.VINE.getDefaultState().withProperty(direction, true));
        pos = pos.down();
        while (pos.getY() >= bottom && random.nextFloat() < .8f) {
            if (!world.isAirBlock(pos)) {
                return;
            }
            world.setBlockState(pos, Blocks.VINE.getDefaultState().withProperty(direction, true));
            pos = pos.down();
        }
    }

    private void generateLootSpawners(Random random, int chunkX, int chunkZ, World world, LostCityChunkGenerator chunkGenerator) {
        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, chunkGenerator);

        for (Pair<BlockPos, String> pair : info.getMobSpawnerTodo()) {
            BlockPos pos = pair.getKey();
            // Double check that it is still a spawner (could be destroyed by explosion)
            if (world.getBlockState(pos).getBlock() == Blocks.MOB_SPAWNER) {
                TileEntity tileentity = world.getTileEntity(pos);
                if (tileentity instanceof TileEntityMobSpawner) {
                    TileEntityMobSpawner spawner = (TileEntityMobSpawner) tileentity;
                    String id = pair.getValue();
                    String fixedId = EntityTools.fixEntityId(id);
                    EntityTools.setSpawnerEntity(world, spawner, new ResourceLocation(fixedId), fixedId);
                }
            }
        }
        info.clearMobSpawnerTodo();


        for (Pair<BlockPos, BuildingInfo.ChestTodo> pair : info.getChestTodo()) {
            BlockPos pos = pair.getKey();
            // Double check that it is still a chest (could be destroyed by explosion)
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.CHEST) {
                if (chunkGenerator.profile.GENERATE_LOOT) {
                    createLootChest(info, random, world, pos, pair.getRight());
                }
            }
        }
        info.clearChestTodo();


        for (BlockPos pos : info.getGenericTodo()) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.GLOWSTONE) {
                world.setBlockState(pos, state, 3);
            }
        }
        info.clearGenericTodo();
    }


    private void createLootChest(BuildingInfo info, Random random, World world, BlockPos pos, BuildingInfo.ChestTodo todo) {
        world.setBlockState(pos, Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.SOUTH));
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof TileEntityChest) {
            if (todo != null) {
                String lootTable = todo.getLootCondition();
                int level = (pos.getY() - profile.GROUNDLEVEL) / 6;
                int floor = (pos.getY() - info.getCityGroundLevel()) / 6;
                ConditionContext conditionContext = new ConditionContext(level, floor, info.floorsBelowGround, info.getNumFloors(),
                        todo.getPart());
                String randomValue = AssetRegistries.CONDITIONS.get(lootTable).getRandomValue(random, conditionContext);
                if (randomValue.startsWith("lost")) {
                    System.out.println("pos: " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ": floor = " + floor + " -> " + randomValue);
                }
                ((TileEntityChest) tileentity).setLootTable(new ResourceLocation(randomValue), random.nextLong());
            }
        }
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

        if (profile.GENERATE_MINESHAFTS) {
            this.mineshaftGenerator.generateStructure(w, this.rand, cp);
        }
        if (profile.GENERATE_VILLAGES) {
            if (profile.PREVENT_VILLAGES_IN_CITIES) {
                if (!BuildingInfo.isCity(chunkX, chunkZ, this)) {
                    flag = this.villageGenerator.generateStructure(w, this.rand, cp);
                }
            } else {
                flag = this.villageGenerator.generateStructure(w, this.rand, cp);
            }
        }
        if (profile.GENERATE_STRONGHOLDS) {
            this.strongholdGenerator.generateStructure(w, this.rand, cp);
        }
        if (profile.GENERATE_SCATTERED) {
            this.scatteredFeatureGenerator.generateStructure(w, this.rand, cp);
        }
        if (profile.GENERATE_OCEANMONUMENTS) {
            this.oceanMonumentGenerator.generateStructure(w, this.rand, cp);
        }

        int k1;
        int l1;
        int i2;

        if (profile.GENERATE_LAKES) {
            boolean isCity = BuildingInfo.isCity(chunkX, chunkZ, this);
            if (!profile.PREVENT_LAKES_RAVINES_IN_CITIES || !isCity) {
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

                    if (l1 < (profile.GROUNDLEVEL - profile.WATERLEVEL_OFFSET) || this.rand.nextInt(10) == 0) {
                        (new WorldGenLakes(Blocks.LAVA)).generate(w, this.rand, new BlockPos(k1, l1, i2));
                    }
                }
            }
        }

        boolean doGen = false;
        if (profile.GENERATE_DUNGEONS) {
            doGen = TerrainGen.populate(this, w, rand, chunkX, chunkZ, flag, PopulateChunkEvent.Populate.EventType.DUNGEON);
            for (k1 = 0; doGen && k1 < 8; ++k1) {
                l1 = x + this.rand.nextInt(16) + 8;
                i2 = this.rand.nextInt(256);
                int j2 = z + this.rand.nextInt(16) + 8;
                (new WorldGenDungeons()).generate(w, this.rand, new BlockPos(l1, i2, j2));
            }
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

        generateTrees(rand, chunkX, chunkZ, w, this);
        generateVines(rand, chunkX, chunkZ, w, this);
        generateLootSpawners(rand, chunkX, chunkZ, w, this);

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(this, w, rand, chunkX, chunkZ, flag));

        BlockFalling.fallInstantly = false;
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        boolean flag = false;

        if (profile.GENERATE_OCEANMONUMENTS) {
            if (chunkIn.getInhabitedTime() < 3600L) {
                flag |= this.oceanMonumentGenerator.generateStructure(this.worldObj, this.rand, new ChunkPos(x, z));
            }
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
            if (profile.GENERATE_SCATTERED) {
                if (this.scatteredFeatureGenerator.isInsideStructure(pos)) {
                    return this.scatteredFeatureGenerator.getScatteredFeatureSpawnList();
                }
            }
            if (profile.GENERATE_OCEANMONUMENTS) {
                if (this.oceanMonumentGenerator.isPositionInStructure(this.worldObj, pos)) {
                    return this.oceanMonumentGenerator.getScatteredFeatureSpawnList();
                }
            }
        }

        return Biome.getSpawnableList(creatureType);
    }

    @Override
    public BlockPos clGetStrongholdGen(World worldIn, String structureName, BlockPos position) {
        return "Stronghold".equals(structureName) && this.strongholdGenerator != null
                ? CompatMapGenStructure.getClosestStrongholdPos(this.strongholdGenerator, worldIn, position)
                : null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        if (profile.GENERATE_MINESHAFTS) {
            this.mineshaftGenerator.generate(this.worldObj, x, z, null);
        }

        if (profile.GENERATE_VILLAGES) {
            if (profile.PREVENT_VILLAGES_IN_CITIES) {
                if (!BuildingInfo.isCity(x, z, this)) {
                    this.villageGenerator.generate(this.worldObj, x, z, null);
                }
            } else {
                this.villageGenerator.generate(this.worldObj, x, z, null);
            }
        }

        if (profile.GENERATE_STRONGHOLDS) {
            this.strongholdGenerator.generate(this.worldObj, x, z, null);
        }

        if (profile.GENERATE_SCATTERED) {
            this.scatteredFeatureGenerator.generate(this.worldObj, x, z, null);
        }

        if (profile.GENERATE_OCEANMONUMENTS) {
            this.oceanMonumentGenerator.generate(this.worldObj, x, z, null);
        }
    }

    @Override
    public ILostChunkInfo getChunkInfo(int chunkX, int chunkZ) {
        return BuildingInfo.getBuildingInfo(chunkX, chunkZ, this);
    }

    @Override
    public int getRealHeight(int level) {
        return profile.GROUNDLEVEL + level * 6;
    }
}
