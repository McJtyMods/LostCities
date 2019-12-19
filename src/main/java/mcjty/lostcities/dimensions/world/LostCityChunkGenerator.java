package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.*;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.ICityCarver;
import mcjty.lostcities.dimensions.IDimensionInfo;
import mcjty.lostcities.dimensions.world.driver.IPrimerDriver;
import mcjty.lostcities.dimensions.world.driver.SafeDriver;
import mcjty.lostcities.dimensions.world.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.dimensions.world.lost.cityassets.WorldStyle;
import mcjty.lostcities.dimensions.world.terraingen.LostCitiesTerrainGenerator;
import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.IWorld;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.OctavesNoiseGenerator;
import net.minecraft.world.gen.OverworldGenSettings;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.CatSpawner;
import net.minecraft.world.spawner.PatrolSpawner;
import net.minecraft.world.spawner.PhantomSpawner;
import net.minecraft.world.spawner.WorldEntitySpawner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LostCityChunkGenerator extends NoiseChunkGenerator<OverworldGenSettings> implements ILostChunkGenerator, IDimensionInfo {

    public Random rand;
    private LostCityProfile profile; // Current profile
    private LostCityProfile outsideProfile; // Outside profile: only for citySphere worlds
    public WorldStyle worldStyle;

    private static final float[] field_222576_h = Util.make(new float[25], (p_222575_0_) -> {
        for(int i = -2; i <= 2; ++i) {
            for(int j = -2; j <= 2; ++j) {
                float f = 10.0F / MathHelper.sqrt((i * i + j * j) + 0.2F);
                p_222575_0_[i + 2 + (j + 2) * 5] = f;
            }
        }

    });
    private final OctavesNoiseGenerator depthNoise;
    private final boolean isAmplified;
    private final PhantomSpawner phantomSpawner = new PhantomSpawner();
    private final PatrolSpawner patrolSpawner = new PatrolSpawner();
    private final CatSpawner catSpawner = new CatSpawner();
    private final VillageSiege villageSiege = new VillageSiege();

    public final LostCitiesTerrainGenerator terrainGenerator;



    public LostCityChunkGenerator(IWorld world, BiomeProvider provider, OverworldGenSettings settings) {
        super(world, provider, 4, 8, 256, settings, true);
        this.randomSeed.skip(2620);
        this.depthNoise = new OctavesNoiseGenerator(this.randomSeed, 16);
        this.isAmplified = world.getWorldInfo().getGenerator() == WorldType.AMPLIFIED;

        this.rand = new Random((seed + 516) * 314);

        terrainGenerator = new LostCitiesTerrainGenerator(this);
        terrainGenerator.setup(world);
    }

    @Override
    public IWorld getWorld() {
        return world;
    }

    @Override
    public WorldStyle getWorldStyle() {
        return worldStyle;
    }

    @Override
    public ICityCarver getCarver() {
        return null;
    }

    @Override
    public Random getRandom() {
        return rand;
    }

    @Override
    public void spawnMobs(WorldGenRegion region) {
        int i = region.getMainChunkX();
        int j = region.getMainChunkZ();
        Biome biome = region.getChunk(i, j).getBiomes()[0];
        SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
        sharedseedrandom.setDecorationSeed(region.getSeed(), i << 4, j << 4);
        WorldEntitySpawner.performWorldGenSpawning(region, biome, i, j, sharedseedrandom);
    }

    @Override
    protected void fillNoiseColumn(double[] noiseColumn, int noiseX, int noiseZ) {
        double d0 = 684.412F;
        double d1 = 684.412F;
        double d2 = 8.555149841308594D;
        double d3 = 4.277574920654297D;
        int i = -10;
        int j = 3;
        this.func_222546_a(noiseColumn, noiseX, noiseZ, 684.412F, 684.412F, 8.555149841308594D, 4.277574920654297D, 3, -10);
    }

    @Override
    protected double func_222545_a(double p_222545_1_, double p_222545_3_, int p_222545_5_) {
        double d0 = 8.5D;
        double d1 = (p_222545_5_ - (8.5D + p_222545_1_ * 8.5D / 8.0D * 4.0D)) * 12.0D * 128.0D / 256.0D / p_222545_3_;
        if (d1 < 0.0D) {
            d1 *= 4.0D;
        }

        return d1;
    }

    @Override
    protected double[] getBiomeNoiseColumn(int noiseX, int noiseZ) {
        double[] adouble = new double[2];
        float f = 0.0F;
        float f1 = 0.0F;
        float f2 = 0.0F;
        int i = 2;
        float f3 = this.biomeProvider.getBiomeAtFactorFour(noiseX, noiseZ).getDepth();

        for(int j = -2; j <= 2; ++j) {
            for(int k = -2; k <= 2; ++k) {
                Biome biome = this.biomeProvider.getBiomeAtFactorFour(noiseX + j, noiseZ + k);
                float f4 = biome.getDepth();
                float f5 = biome.getScale();
                if (this.isAmplified && f4 > 0.0F) {
                    f4 = 1.0F + f4 * 2.0F;
                    f5 = 1.0F + f5 * 4.0F;
                }

                float f6 = field_222576_h[j + 2 + (k + 2) * 5] / (f4 + 2.0F);
                if (biome.getDepth() > f3) {
                    f6 /= 2.0F;
                }

                f += f5 * f6;
                f1 += f4 * f6;
                f2 += f6;
            }
        }

        f = f / f2;
        f1 = f1 / f2;
        f = f * 0.9F + 0.1F;
        f1 = (f1 * 4.0F - 1.0F) / 8.0F;
        adouble[0] = f1 + this.getNoiseDepthAt(noiseX, noiseZ);
        adouble[1] = f;
        return adouble;
    }

    private double getNoiseDepthAt(int noiseX, int noiseZ) {
        double d0 = this.depthNoise.getValue((noiseX * 200), 10.0D, (noiseZ * 200), 1.0D, 0.0D, true) / 8000.0D;
        if (d0 < 0.0D) {
            d0 = -d0 * 0.3D;
        }

        d0 = d0 * 3.0D - 2.0D;
        if (d0 < 0.0D) {
            d0 = d0 / 28.0D;
        } else {
            if (d0 > 1.0D) {
                d0 = 1.0D;
            }

            d0 = d0 / 40.0D;
        }

        return d0;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EntityClassification creatureType, BlockPos pos) {
        if (Feature.SWAMP_HUT.func_202383_b(this.world, pos)) {
            if (creatureType == EntityClassification.MONSTER) {
                return Feature.SWAMP_HUT.getSpawnList();
            }

            if (creatureType == EntityClassification.CREATURE) {
                return Feature.SWAMP_HUT.getCreatureSpawnList();
            }
        } else if (creatureType == EntityClassification.MONSTER) {
            if (Feature.PILLAGER_OUTPOST.isPositionInStructure(this.world, pos)) {
                return Feature.PILLAGER_OUTPOST.getSpawnList();
            }

            if (Feature.OCEAN_MONUMENT.isPositionInStructure(this.world, pos)) {
                return Feature.OCEAN_MONUMENT.getSpawnList();
            }
        }

        return super.getPossibleCreatures(creatureType, pos);
    }

    @Override
    public void spawnMobs(ServerWorld worldIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs) {
        this.phantomSpawner.tick(worldIn, spawnHostileMobs, spawnPeacefulMobs);
        this.patrolSpawner.tick(worldIn, spawnHostileMobs, spawnPeacefulMobs);
        this.catSpawner.tick(worldIn, spawnHostileMobs, spawnPeacefulMobs);
        this.villageSiege.func_225477_a(worldIn, spawnHostileMobs, spawnPeacefulMobs);
    }

    @Override
    public int getGroundHeight() {
        return this.world.getSeaLevel() + 1;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public LostCityProfile getProfile() {
        return profile;
    }

    @Override
    public LostCityProfile getOutsideProfile() {
        return outsideProfile;
    }


    @Override
    public ILostChunkInfo getChunkInfo(int chunkX, int chunkZ) {
        return BuildingInfo.getBuildingInfo(chunkX, chunkZ, this);
    }

    @Override
    public int getRealHeight(int level) {
        return profile.GROUNDLEVEL + level * 6;
    }

    @Override
    public ILostCityAssetRegistry<ILostCityBuilding> getBuildings() {
        return AssetRegistries.BUILDINGS.cast();
    }

    @Override
    public ILostCityAssetRegistry<ILostCityMultiBuilding> getMultiBuildings() {
        return AssetRegistries.MULTI_BUILDINGS.cast();
    }

    @Override
    public ILostCityAssetRegistry<ILostCityCityStyle> getCityStyles() {
        return AssetRegistries.CITYSTYLES.cast();
    }

    // Sometimes we have to precalculate primers for a chunk before the
    // chunk is generated. In that case we cache them here so that when the
    // chunk is really generated it will find it and use that instead of
    // making that primer again
    // @todo, make this cache timed so that primers expire if they are not used quickly enough?
    // @todo 1.14
    private Map<ChunkCoord, ChunkPrimer> cachedPrimers = new HashMap<>();
    private Map<ChunkCoord, ChunkHeightmap> cachedHeightmaps = new HashMap<>();

//    private MapGenStronghold strongholdGenerator = new MapGenStronghold();
//    private StructureOceanMonument oceanMonumentGenerator = new LostStructureOceanMonument();
//    private MapGenVillage villageGenerator = new MapGenVillage();
//    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
//    private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
//    private LostWoodlandMansion woodlandMansionGenerator = new LostWoodlandMansion(this);
//
//    public ChunkGeneratorSettings getSettings() {
//        if (settings == null) {
//            ChunkGeneratorSettings.Factory factory = new ChunkGeneratorSettings.Factory();
//            settings = factory.build();
//        }
//        return settings;
//    }
//
//    // Holds ravine generator
//    private MapGenBase ravineGenerator = new MapGenRavine();

    public IChunkPrimerFactory otherGenerator = null;
//
//    public LostCityChunkGenerator(World world, IChunkPrimerFactory otherGenerator) {
//        this(world, world.getSeed());
//        this.otherGenerator = otherGenerator;
//    }

//    public LostCityChunkGenerator(World world, long seed) {
//
//        WorldTypeTools.registerChunkGenerator(world.provider.getDimension(), this);
//
//        caveGenerator = new LostGenCaves(this);
//        caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
//        strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, STRONGHOLD);
//
//        villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, VILLAGE);
//        mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
//        scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(scatteredFeatureGenerator, SCATTERED_FEATURE);
//        ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
//        oceanMonumentGenerator = (StructureOceanMonument) TerrainGen.getModdedMapGen(oceanMonumentGenerator, OCEAN_MONUMENT);
//
//        dimensionId = world.provider.getDimension();
//        profile = WorldTypeTools.getProfile(world);
//        if (profile.isSpace() && !profile.CITYSPHERE_OUTSIDE_PROFILE.isEmpty()) {
//            outsideProfile = LostCityConfiguration.profiles.get(profile.CITYSPHERE_OUTSIDE_PROFILE);
//        } else {
//            outsideProfile = profile;
//        }
//
//        worldStyle = AssetRegistries.WORLDSTYLES.get(profile.getWorldStyle());
//        if (worldStyle == null) {
//            throw new RuntimeException("Unknown worldstyle '" + profile.getWorldStyle() + "'!");
//        }
//
//        String generatorOptions = profile.GENERATOR_OPTIONS;
//        if (generatorOptions != null && !generatorOptions.isEmpty()) {
//            this.settings = ChunkGeneratorSettings.Factory.jsonToFactory(generatorOptions).build();
//        }
//
//        this.worldObj = world;
//
//        this.worldType = world.getWorldInfo().getTerrainType();
//
//        this.seed = seed;
//        this.rand = new Random((seed + 516) * 314);
//
//        int waterLevel = (byte) (profile.GROUNDLEVEL - profile.WATERLEVEL_OFFSET);
//        if (waterLevel <= 0) {
//            waterLevel = 1;
//        }
//        world.setSeaLevel(waterLevel);
//
//        terrainGenerator = new LostCitiesTerrainGenerator(this);
//        terrainGenerator.setup(world);
//    }
//
    public ChunkPrimer generatePrimer(int chunkX, int chunkZ) {
        this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
        ChunkPrimer chunkprimer = new ChunkPrimer(new ChunkPos(chunkX, chunkZ), UpgradeData.EMPTY);

        if (otherGenerator != null) {
            // For ATG, experimental
            otherGenerator.fillChunk(chunkX, chunkZ, chunkprimer);
        } else {
            terrainGenerator.doCoreChunk(chunkX, chunkZ, chunkprimer);
        }
        return chunkprimer;
    }
//
//    // Get a heightmap for a chunk. If needed calculate (and cache) a primer
    @Override
    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ) {
        ChunkCoord key = new ChunkCoord(getWorld().getDimension().getType(), chunkX, chunkZ);
        if (cachedHeightmaps.containsKey(key)) {
            return cachedHeightmaps.get(key);
        } else if (cachedPrimers.containsKey(key)) {
            BlockState baseChar = profile.getBaseBlock();
            ChunkPrimer primer = cachedPrimers.get(key);
            SafeDriver driver = new SafeDriver();
            driver.setPrimer(primer);
            ChunkHeightmap heightmap = new ChunkHeightmap(driver, profile.LANDSCAPE_TYPE, profile.GROUNDLEVEL, baseChar);
            cachedHeightmaps.put(key, heightmap);
            return heightmap;
        } else {
            ChunkPrimer primer = generatePrimer(chunkX, chunkZ);
            cachedPrimers.put(key, primer);
            BlockState baseChar = profile.getBaseBlock();
            SafeDriver driver = new SafeDriver();
            driver.setPrimer(primer);
            ChunkHeightmap heightmap = new ChunkHeightmap(driver, profile.LANDSCAPE_TYPE, profile.GROUNDLEVEL, baseChar);
            cachedHeightmaps.put(key, heightmap);
            return heightmap;
        }
    }

    @Override
    public void generateSurface(IChunk chunkIn) {
        // @todo 1.14 is generateSurface what we want here?
        super.generateSurface(chunkIn);
        int chunkX = chunkIn.getPos().x;
        int chunkZ = chunkIn.getPos().z;
        LostCityProfile profile = BuildingInfo.getProfile(chunkX, chunkZ, this);

        terrainGenerator.setupChars(profile);
        boolean isCity = BuildingInfo.isCity(chunkX, chunkZ, this);

        ChunkPrimer chunkprimer = getChunkPrimer(chunkX, chunkZ, isCity);

        try {
            terrainGenerator.generate(chunkX, chunkZ, chunkprimer);
        } catch (Exception e) {
            LostCities.setup.getLogger().error("An exception occured while generating chunk: " + chunkX + "," + chunkZ, e);
            BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, this);
            LostCities.setup.getLogger().error("    Chunk profile: " + info.profile.getName());
            LostCities.setup.getLogger().error("    Is City: " + info.isCity());
            LostCities.setup.getLogger().error("    Building type: " + info.getBuildingType());
            LostCities.setup.getLogger().error("    City level: " + info.getCityLevel());
            LostCities.setup.getLogger().error("    City ground level: " + info.getCityGroundLevel());
            LostCities.setup.getLogger().error("    Num floors: " + info.getNumFloors());
            LostCities.setup.getLogger().error("    Num cellars: " + info.getNumCellars());
            throw (e);
        }

        BiomeProvider biomeProvider = this.getWorld().getChunkProvider().getChunkGenerator().getBiomeProvider();
        // @todo 1.14
//        this.biomesForGeneration = biomeProvider.getBiomes(this.biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);
//        terrainGenerator.replaceBlocksForBiome(chunkX, chunkZ, chunkprimer, this.biomesForGeneration);

        if (profile.GENERATE_CAVES) {
//            this.caveGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }
        if (profile.GENERATE_RAVINES) {
            if (!profile.PREVENT_LAKES_RAVINES_IN_CITIES || !isCity) {
//                this.ravineGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
            }
        }

        if (profile.GENERATE_MINESHAFTS) {
//            this.mineshaftGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        if (profile.GENERATE_VILLAGES) {
            if (profile.PREVENT_VILLAGES_IN_CITIES) {
                if (!isCity) {
//                    this.villageGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
                }
            } else {
//                this.villageGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
            }
        }

        if (profile.GENERATE_STRONGHOLDS) {
//            this.strongholdGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        if (profile.GENERATE_SCATTERED) {
//            this.scatteredFeatureGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        if (profile.GENERATE_OCEANMONUMENTS) {
//            this.oceanMonumentGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }

        if (profile.GENERATE_MANSIONS) {
//            this.woodlandMansionGenerator.generate(this.worldObj, chunkX, chunkZ, chunkprimer);
        }


        // @todo 1.14 (Don't make a new chunk but populate the given one)
        Chunk chunk = new Chunk(this.getWorld().getWorld(), chunkprimer);
        // @todo 1.14
//        byte[] abyte = chunk.getBiomeArray();
//
//        for (int i = 0; i < abyte.length; ++i) {
//            abyte[i] = (byte) Biome.getIdForBiome(this.biomesForGeneration[i]);
//        }

//        chunk.generateSkylightMap();
    }
//
    public ChunkPrimer getChunkPrimer(int chunkX, int chunkZ, boolean isCity) {
        ChunkPrimer chunkprimer;
        if (isCity && profile.isDefault()) {  // Generate a normal chunk if we have a floating city
            chunkprimer = new ChunkPrimer(new ChunkPos(chunkX, chunkZ), UpgradeData.EMPTY);
        } else {
            ChunkCoord key = new ChunkCoord(getWorld().getDimension().getType(), chunkX, chunkZ);
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
                BlockState baseChar = profile.getBaseBlock();
                IPrimerDriver driver = new SafeDriver();
                driver.setPrimer(chunkprimer);
                cachedHeightmaps.put(key, new ChunkHeightmap(driver, profile.LANDSCAPE_TYPE, profile.GROUNDLEVEL, baseChar));
            }
        }
        return chunkprimer;
    }
//
//    private void generateTrees(Random random, int chunkX, int chunkZ, World world, LostCityChunkGenerator provider) {
//        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);
//        for (BlockPos pos : info.getSaplingTodo()) {
//            BlockState state = world.getBlockState(pos);
//            if (state.getBlock() == Blocks.SAPLING) {
//                ((BlockSapling)Blocks.SAPLING).generateTree(world, pos, state, random);
//            }
//        }
//        info.clearSaplingTodo();
//    }
//
//    private void generateVines(Random random, int chunkX, int chunkZ, World world, LostCityChunkGenerator provider) {
//        int cx = chunkX * 16;
//        int cz = chunkZ * 16;
//        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);
//
//        if (info.hasBuilding) {
//            BuildingInfo adjacent = info.getXmax();
//            int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
//            for (int z = 0; z < 15; z++) {
//                for (int y = bottom; y < (info.getMaxHeight()); y++) {
//                    if (random.nextFloat() < provider.profile.VINE_CHANCE) {
//                        createVineStrip(random, world, bottom, BlockVine.WEST, new BlockPos(cx + 16, y, cz + z), new BlockPos(cx + 15, y, cz + z));
//                    }
//                }
//            }
//        }
//        if (info.getXmax().hasBuilding) {
//            BuildingInfo adjacent = info.getXmax();
//            int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? info.getMaxHeight() : (info.getCityGroundLevel() + 3));
//            for (int z = 0; z < 15; z++) {
//                for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
//                    if (random.nextFloat() < provider.profile.VINE_CHANCE) {
//                        createVineStrip(random, world, bottom, BlockVine.EAST, new BlockPos(cx + 15, y, cz + z), new BlockPos(cx + 16, y, cz + z));
//                    }
//                }
//            }
//        }
//
//        if (info.hasBuilding) {
//            BuildingInfo adjacent = info.getZmax();
//            int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
//            for (int x = 0; x < 15; x++) {
//                for (int y = bottom; y < (info.getMaxHeight()); y++) {
//                    if (random.nextFloat() < provider.profile.VINE_CHANCE) {
//                        createVineStrip(random, world, bottom, BlockVine.NORTH, new BlockPos(cx + x, y, cz + 16), new BlockPos(cx + x, y, cz + 15));
//                    }
//                }
//            }
//        }
//        if (info.getZmax().hasBuilding) {
//            BuildingInfo adjacent = info.getZmax();
//            int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? info.getMaxHeight() : (info.getCityGroundLevel() + 3));
//            for (int x = 0; x < 15; x++) {
//                for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
//                    if (random.nextFloat() < provider.profile.VINE_CHANCE) {
//                        createVineStrip(random, world, bottom, BlockVine.SOUTH, new BlockPos(cx + x, y, cz + 15), new BlockPos(cx + x, y, cz + 16));
//                    }
//                }
//            }
//        }
//    }
//
//    private void createVineStrip(Random random, World world, int bottom, PropertyBool direction, BlockPos pos, BlockPos vineHolderPos) {
//        if (world.isAirBlock(vineHolderPos)) {
//            return;
//        }
//        if (!world.isAirBlock(pos)) {
//            return;
//        }
//        world.setBlockState(pos, Blocks.VINE.getDefaultState().withProperty(direction, true));
//        pos = pos.down();
//        while (pos.getY() >= bottom && random.nextFloat() < .8f) {
//            if (!world.isAirBlock(pos)) {
//                return;
//            }
//            world.setBlockState(pos, Blocks.VINE.getDefaultState().withProperty(direction, true));
//            pos = pos.down();
//        }
//    }
//
//    private static final EntityId FIXER = new EntityId();
//
//    public static String fixEntityId(String id) {
//        NBTTagCompound nbt = new NBTTagCompound();
//        nbt.setString("id", id);
//        nbt = FIXER.fixTagCompound(nbt);
//        return nbt.getString("id");
//    }
//
//
//    private void generateLootSpawners(Random random, int chunkX, int chunkZ, World world, LostCityChunkGenerator chunkGenerator) {
//        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, chunkGenerator);
//
//        for (Pair<BlockPos, BuildingInfo.ConditionTodo> pair : info.getMobSpawnerTodo()) {
//            BlockPos pos = pair.getKey();
//            // Double check that it is still a spawner (could be destroyed by explosion)
//            if (world.getBlockState(pos).getBlock() == Blocks.MOB_SPAWNER) {
//                TileEntity tileentity = world.getTileEntity(pos);
//                if (tileentity instanceof TileEntityMobSpawner) {
//                    TileEntityMobSpawner spawner = (TileEntityMobSpawner) tileentity;
//                    BuildingInfo.ConditionTodo todo = pair.getValue();
//                    String condition = todo.getCondition();
//                    Condition cnd = AssetRegistries.CONDITIONS.get(condition);
//                    if (cnd == null) {
//                        throw new RuntimeException("Cannot find condition '" + condition + "'!");
//                    }
//                    int level = (pos.getY() - profile.GROUNDLEVEL) / 6;
//                    int floor = (pos.getY() - info.getCityGroundLevel()) / 6;
//                    ConditionContext conditionContext = new ConditionContext(level, floor, info.floorsBelowGround, info.getNumFloors(),
//                            todo.getPart(), todo.getBuilding(), info.chunkX, info.chunkZ) {
//                        @Override
//                        public boolean isSphere() {
//                            return CitySphere.isInSphere(chunkX, chunkZ, pos, LostCityChunkGenerator.this);
//                        }
//
//                        @Override
//                        public String getBiome() {
//                            return world.getBiome(pos).getBiomeName();
//                        }
//                    };
//                    String randomValue = cnd.getRandomValue(random, conditionContext);
//                    if (randomValue == null) {
//                        throw new RuntimeException("Condition '" + cnd.getName() + "' did not return a valid mob!");
//                    }
//                    String fixedId = fixEntityId(randomValue);
//                    MobSpawnerBaseLogic mobspawnerbaselogic = spawner.getSpawnerBaseLogic();
//                    mobspawnerbaselogic.setEntityId(new ResourceLocation(fixedId));
//                    spawner.markDirty();
//                    if (LostCityConfiguration.DEBUG) {
//                        LostCities.setup.getLogger().debug("generateLootSpawners: mob=" + randomValue + " pos=" + pos.toString());
//                    }
//                } else if(tileentity != null) {
//                    LostCities.setup.getLogger().error("The mob spawner at (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") has a TileEntity of incorrect type " + tileentity.getClass().getName() + "!");
//                } else {
//                    LostCities.setup.getLogger().error("The mob spawner at (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") is missing its TileEntity!");
//                }
//            }
//        }
//        info.clearMobSpawnerTodo();
//
//
//        for (Pair<BlockPos, BuildingInfo.ConditionTodo> pair : info.getLootTodo()) {
//            BlockPos pos = pair.getKey();
//            // Double check that it is still something that can hold loot (could be destroyed by explosion)
//            TileEntity te = world.getTileEntity(pos);
//            if (te instanceof TileEntityLockableLoot) {
//                if (chunkGenerator.profile.GENERATE_LOOT) {
//                    createLoot(info, random, world, pos, pair.getRight());
//                }
//            } else if (te == null) {
//                BlockState state = world.getBlockState(pos);
//                Block block = state.getBlock();
//                if (block.hasTileEntity(state)) {
//                    LostCities.setup.getLogger().error("The block " + block.getRegistryName() + " (" + block.getClass().getName() + ") at (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") is missing its TileEntity!");
//                }
//            }
//        }
//        info.clearLootTodo();
//
//
//        for (BlockPos pos : info.getLightingUpdateTodo()) {
//            BlockState state = world.getBlockState(pos);
//            world.setBlockState(pos, state, 3);
//        }
//        info.clearLightingUpdateTodo();
//    }
//
//
//    private void createLoot(BuildingInfo info, Random random, World world, BlockPos pos, BuildingInfo.ConditionTodo todo) {
//        if (random.nextFloat() < profile.CHEST_WITHOUT_LOOT_CHANCE) {
//            return;
//        }
//        TileEntity tileentity = world.getTileEntity(pos);
//        if (tileentity instanceof TileEntityLockableLoot) {
//            if (todo != null) {
//                String lootTable = todo.getCondition();
//                int level = (pos.getY() - profile.GROUNDLEVEL) / 6;
//                int floor = (pos.getY() - info.getCityGroundLevel()) / 6;
//                ConditionContext conditionContext = new ConditionContext(level, floor, info.floorsBelowGround, info.getNumFloors(),
//                        todo.getPart(), todo.getBuilding(), info.chunkX, info.chunkZ) {
//                    @Override
//                    public boolean isSphere() {
//                        return CitySphere.isInSphere(info.chunkX, info.chunkZ, pos, LostCityChunkGenerator.this);
//                    }
//
//                    @Override
//                    public String getBiome() {
//                        return world.getBiome(pos).getBiomeName();
//                    }
//                };
//                String randomValue = AssetRegistries.CONDITIONS.get(lootTable).getRandomValue(random, conditionContext);
//                if (randomValue == null) {
//                    throw new RuntimeException("Condition '" + lootTable + "' did not return a table under certain conditions!");
//                }
//                ((TileEntityLockableLoot) tileentity).setLootTable(new ResourceLocation(randomValue), random.nextLong());
//                tileentity.markDirty();
//                if (LostCityConfiguration.DEBUG) {
//                    LostCities.setup.getLogger().debug("createLootChest: loot=" + randomValue + " pos=" + pos.toString());
//                }
//            }
//        }
//    }
//
//
//    @Override
//    public void populate(int chunkX, int chunkZ) {
//        BlockFalling.fallInstantly = true;
//        int x = chunkX * 16;
//        int z = chunkZ * 16;
//        World w = this.worldObj;
//        Biome biome = w.getBiomeForCoordsBody(new BlockPos(x + 16, 0, z + 16));
//        this.rand.setSeed(w.getSeed());
//        long i1 = this.rand.nextLong() / 2L * 2L + 1L;
//        long j1 = this.rand.nextLong() / 2L * 2L + 1L;
//        this.rand.setSeed(chunkX * i1 + chunkZ * j1 ^ w.getSeed());
//        boolean flag = false;
//
//        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(this, w, rand, chunkX, chunkZ, flag));
//
//        ChunkPos cp = new ChunkPos(chunkX, chunkZ);
//        LostCityProfile profile = BuildingInfo.getProfile(chunkX, chunkZ, this);
//
//        if (profile.GENERATE_MINESHAFTS) {
//            this.mineshaftGenerator.generateStructure(w, this.rand, cp);
//        }
//        if (profile.GENERATE_VILLAGES) {
//            if (profile.PREVENT_VILLAGES_IN_CITIES) {
//                if (!BuildingInfo.isCity(chunkX, chunkZ, this)) {
//                    flag = this.villageGenerator.generateStructure(w, this.rand, cp);
//                }
//            } else {
//                flag = this.villageGenerator.generateStructure(w, this.rand, cp);
//            }
//        }
//        if (profile.GENERATE_STRONGHOLDS) {
//            this.strongholdGenerator.generateStructure(w, this.rand, cp);
//        }
//        if (profile.GENERATE_SCATTERED) {
//            this.scatteredFeatureGenerator.generateStructure(w, this.rand, cp);
//        }
//        if (profile.GENERATE_OCEANMONUMENTS) {
//            this.oceanMonumentGenerator.generateStructure(w, this.rand, cp);
//        }
//        if (profile.GENERATE_MANSIONS) {
//            this.woodlandMansionGenerator.generateStructure(w, this.rand, cp);
//        }
//
//        int k1;
//        int l1;
//        int i2;
//
//        if (profile.GENERATE_LAKES) {
//            boolean isCity = BuildingInfo.isCity(chunkX, chunkZ, this);
//            if (!profile.PREVENT_LAKES_RAVINES_IN_CITIES || !isCity) {
//                if (biome != Biomes.DESERT && biome != Biomes.DESERT_HILLS && !flag && this.rand.nextInt(4) == 0
//                        && TerrainGen.populate(this, w, rand, chunkX, chunkZ, flag, PopulateChunkEvent.Populate.EventType.LAKE)) {
//                    k1 = x + this.rand.nextInt(16) + 8;
//                    l1 = this.rand.nextInt(256);
//                    i2 = z + this.rand.nextInt(16) + 8;
//                    (new WorldGenLakes(profile.getLiquidBlock().getBlock())).generate(w, this.rand, new BlockPos(k1, l1, i2));
//                }
//
//                if (TerrainGen.populate(this, w, rand, chunkX, chunkZ, flag, PopulateChunkEvent.Populate.EventType.LAVA) && !flag && this.rand.nextInt(8) == 0) {
//                    k1 = x + this.rand.nextInt(16) + 8;
//                    l1 = this.rand.nextInt(this.rand.nextInt(248) + 8);
//                    i2 = z + this.rand.nextInt(16) + 8;
//
//                    if (l1 < (profile.GROUNDLEVEL - profile.WATERLEVEL_OFFSET) || this.rand.nextInt(10) == 0) {
//                        (new WorldGenLakes(Blocks.LAVA)).generate(w, this.rand, new BlockPos(k1, l1, i2));
//                    }
//                }
//            }
//        }
//
//        boolean doGen = false;
//        if (profile.GENERATE_DUNGEONS) {
//            doGen = TerrainGen.populate(this, w, rand, chunkX, chunkZ, flag, PopulateChunkEvent.Populate.EventType.DUNGEON);
//            for (k1 = 0; doGen && k1 < 8; ++k1) {
//                l1 = x + this.rand.nextInt(16) + 8;
//                i2 = this.rand.nextInt(256);
//                int j2 = z + this.rand.nextInt(16) + 8;
//                (new WorldGenDungeons()).generate(w, this.rand, new BlockPos(l1, i2, j2));
//            }
//        }
//
//        BlockPos pos = new BlockPos(x, 0, z);
//        biome.decorate(w, this.rand, pos);
//
//        if (TerrainGen.populate(this, w, rand, chunkX, chunkZ, flag, PopulateChunkEvent.Populate.EventType.ANIMALS)) {
//            WorldEntitySpawner.performWorldGenSpawning(w, biome, x + 8, z + 8, 16, 16, this.rand);
//        }
//        x += 8;
//        z += 8;
//
//        doGen = TerrainGen.populate(this, w, rand, chunkX, chunkZ, flag, PopulateChunkEvent.Populate.EventType.ICE);
//        for (k1 = 0; doGen && k1 < 16; ++k1) {
//            for (l1 = 0; l1 < 16; ++l1) {
//                i2 = w.getPrecipitationHeight(new BlockPos(x + k1, 0, z + l1)).getY();
//
//                if (w.canBlockFreeze(new BlockPos(k1 + x, i2 - 1, l1 + z), false)) {
//                    w.setBlockState(new BlockPos(k1 + x, i2 - 1, l1 + z), Blocks.ICE.getDefaultState(), 2);
//                }
//
//                if (w.canSnowAt(new BlockPos(k1 + x, i2, l1 + z), true)) {
//                    w.setBlockState(new BlockPos(k1 + x, i2, l1 + z), Blocks.SNOW_LAYER.getDefaultState(), 2);
//                }
//            }
//        }
//
//        generateTrees(rand, chunkX, chunkZ, w, this);
//        generateVines(rand, chunkX, chunkZ, w, this);
//        generateLootSpawners(rand, chunkX, chunkZ, w, this);
//
//        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(this, w, rand, chunkX, chunkZ, flag));
//
//        BlockFalling.fallInstantly = false;
//    }
//
//    @Override
//    public boolean generateStructures(Chunk chunkIn, int x, int z) {
//        boolean flag = false;
//
//        if (profile.GENERATE_OCEANMONUMENTS) {
//            if (chunkIn.getInhabitedTime() < 3600L) {
//                flag |= this.oceanMonumentGenerator.generateStructure(this.worldObj, this.rand, new ChunkPos(x, z));
//            }
//        }
//
//        return flag;
//    }
//
//    @Override
//    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
//        return getDefaultCreatures(creatureType, pos);
//    }
//
//    private List<Biome.SpawnListEntry> getDefaultCreatures(EnumCreatureType creatureType, BlockPos pos) {
//        Biome Biome = this.worldObj.getBiomeForCoordsBody(pos);
//        if (creatureType == EnumCreatureType.MONSTER) {
//            if (profile.GENERATE_SCATTERED) {
//                if (this.scatteredFeatureGenerator.isInsideStructure(pos)) {
//                    return this.scatteredFeatureGenerator.getMonsters();
//                }
//            }
//            if (profile.GENERATE_OCEANMONUMENTS) {
//                if (this.oceanMonumentGenerator.isPositionInStructure(this.worldObj, pos)) {
//                    return this.oceanMonumentGenerator.getMonsters();
//                }
//            }
//        }
//
//        return Biome.getSpawnableList(creatureType);
//    }
//
//    @Nullable
//    @Override
//    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
//        if ("Stronghold".equals(structureName) && this.strongholdGenerator != null) {
//            return this.strongholdGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
//        } else if ("Monument".equals(structureName) && this.oceanMonumentGenerator != null) {
//            return this.oceanMonumentGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
//        } else if ("LostMansion".equals(structureName) && this.woodlandMansionGenerator != null) {
//            return this.woodlandMansionGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
//        } else if ("Village".equals(structureName) && this.villageGenerator != null) {
//            return this.villageGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
//        } else if ("Mineshaft".equals(structureName) && this.mineshaftGenerator != null) {
//            return this.mineshaftGenerator.getNearestStructurePos(worldIn, position, findUnexplored);
//        } else {
//            return "Temple".equals(structureName) && this.scatteredFeatureGenerator != null ? this.scatteredFeatureGenerator.getNearestStructurePos(worldIn, position, findUnexplored) : null;
//        }
//    }
//
//    @Override
//    public void recreateStructures(Chunk chunkIn, int x, int z) {
//        if (profile.GENERATE_MINESHAFTS) {
//            this.mineshaftGenerator.generate(this.worldObj, x, z, null);
//        }
//
//        if (profile.GENERATE_VILLAGES) {
//            if (profile.PREVENT_VILLAGES_IN_CITIES) {
//                if (!BuildingInfo.isCity(x, z, this)) {
//                    this.villageGenerator.generate(this.worldObj, x, z, null);
//                }
//            } else {
//                this.villageGenerator.generate(this.worldObj, x, z, null);
//            }
//        }
//
//        if (profile.GENERATE_STRONGHOLDS) {
//            this.strongholdGenerator.generate(this.worldObj, x, z, null);
//        }
//
//        if (profile.GENERATE_SCATTERED) {
//            this.scatteredFeatureGenerator.generate(this.worldObj, x, z, null);
//        }
//
//        if (profile.GENERATE_OCEANMONUMENTS) {
//            this.oceanMonumentGenerator.generate(this.worldObj, x, z, null);
//        }
//
//        if (profile.GENERATE_MANSIONS) {
//            this.woodlandMansionGenerator.generate(this.worldObj, x, z, null);
//        }
//    }
//
//    public boolean hasMansion(int chunkX, int chunkZ) {
//        return woodlandMansionGenerator != null && woodlandMansionGenerator.hasStructure(worldObj, chunkX, chunkZ);
//    }
//
//    @Override
//    public boolean isInsideStructure(World world, String structureName, BlockPos blockPos) {
//        if ("Stronghold".equals(structureName) && this.strongholdGenerator != null) {
//            return this.strongholdGenerator.isInsideStructure(blockPos);
//        } else if ("LostMansion".equals(structureName) && this.woodlandMansionGenerator != null) {
//            return this.woodlandMansionGenerator.isInsideStructure(blockPos);
//        } else if ("Monument".equals(structureName) && this.oceanMonumentGenerator != null) {
//            return this.oceanMonumentGenerator.isInsideStructure(blockPos);
//        } else if ("Village".equals(structureName) && this.villageGenerator != null) {
//            return this.villageGenerator.isInsideStructure(blockPos);
//        } else if ("Mineshaft".equals(structureName) && this.mineshaftGenerator != null) {
//            return this.mineshaftGenerator.isInsideStructure(blockPos);
//        } else {
//            return "Temple".equals(structureName) && this.scatteredFeatureGenerator != null ? this.scatteredFeatureGenerator.isInsideStructure(blockPos) : false;
//        }
//    }
//
//
//    public boolean hasOceanMonument(int chunkX, int chunkZ) {
//        return oceanMonumentGenerator instanceof LostStructureOceanMonument && ((LostStructureOceanMonument) oceanMonumentGenerator).hasStructure(worldObj, chunkX, chunkZ);
//    }
}
