package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.api.LostCityEvent;
import mcjty.lostcities.api.LostCityEvents;
import mcjty.lostcities.api.RailChunkType;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.editor.EditModeData;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.ModSetup;
import mcjty.lostcities.varia.*;
import mcjty.lostcities.worldgen.gen.*;
import mcjty.lostcities.worldgen.lost.*;
import mcjty.lostcities.worldgen.lost.cityassets.*;
import mcjty.lostcities.worldgen.lost.regassets.data.CitySphereSettings;
import mcjty.lostcities.worldgen.lost.regassets.data.ScatteredSettings;
import mcjty.lostcities.worldgen.lost.regassets.data.StreetParts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LostCityTerrainFeature {

    public static final int FLOORHEIGHT = 6;

    public final BlockState air;
    private final BlockState hardAir;

    private final BlockState base;
    public final BlockState liquid;

    private volatile Set<BlockState> railStates;
    private volatile Set<BlockState> statesNeedingTodo;
    private volatile Set<BlockState> statesNeedingLightingUpdate;
    private volatile Set<BlockState> statesNeedingPoiUpdate;

    private final NoiseGeneratorPerlin rubbleNoise;
    private final NoiseGeneratorPerlin leavesNoise;
    private final NoiseGeneratorPerlin ruinNoise;
    private final NoiseGeneratorPerlin bottomLayerNoise;    // Used in floating profile for the underside of buildings

    private volatile BlockState[] randomLeafs;
    private volatile BlockState[] randomDirt;
    private volatile Set<BlockState> randomDirtSet;

    public final IDimensionInfo provider;
    public final LostCityProfile profile;
    private final TimedCache<ChunkCoord, ChunkHeightmap> cachedHeightmaps = new TimedCache<>(Config.CACHE_CLEANUP_SECONDS::get);
    private final Statistics statistics = new Statistics();
    private final Map<Block, BlockEntityType> typeCache = new ConcurrentHashMap<>();

    public LostCityTerrainFeature(IDimensionInfo provider, LostCityProfile profile, RandomSource rand) {
        this.provider = provider;
        this.profile = profile;
//        int waterLevel = provider.getWorld() == null ? 65 : Tools.getSeaLevel(provider.getWorld());// profile.GROUNDLEVEL - profile.WATERLEVEL_OFFSET;
        this.rubbleNoise = new NoiseGeneratorPerlin(rand, 4);
        this.leavesNoise = new NoiseGeneratorPerlin(rand, 4);
        this.ruinNoise = new NoiseGeneratorPerlin(rand, 4);
        this.bottomLayerNoise = new NoiseGeneratorPerlin(rand, 4);

        air = Blocks.AIR.defaultBlockState();
        hardAir = Blocks.STRUCTURE_VOID.defaultBlockState();
        base = profile.getBaseBlock();
        liquid = profile.getLiquidBlock();

//        islandTerrainGenerator.setup(provider.getWorld().getWorld(), provider);
//        cavernTerrainGenerator.setup(provider.getWorld().getWorld(), provider);
//        spaceTerrainGenerator.setup(provider.getWorld().getWorld(), provider);
    }

    private BlockState getRandomLeaf(BuildingInfo info, CompiledPalette compiledPalette) {
        Character leavesBlock = info.getCityStyle().getLeavesBlock();
        if (leavesBlock != null) {
            return compiledPalette.get(leavesBlock);
        }
        BlockState[] leafs = randomLeafs;
        if (leafs == null) {
            synchronized (this) {
                leafs = randomLeafs;
                if (leafs == null) {
                    BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                    BlockState leaves2 = Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                    BlockState leaves3 = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

                    leafs = new BlockState[128];
                    int i = 0;
                    while (i < 20) {
                        leafs[i++] = leaves2;
                    }
                    while (i < 40) {
                        leafs[i++] = leaves3;
                    }
                    while (i < leafs.length) {
                        leafs[i++] = leaves;
                    }
                    randomLeafs = leafs;
                }
            }
        }
        return leafs[GenerationContext.nextPaletteIndex()];
    }

    private Set<BlockState> getPossibleRandomDirts(BuildingInfo info, CompiledPalette compiledPalette) {
        Character rubbleDirtBlock = info.getCityStyle().getRubbleDirtBlock();
        if (rubbleDirtBlock != null) {
            return compiledPalette.getAll(rubbleDirtBlock);
        } else {
            getRandomDirt(info, compiledPalette);
            return randomDirtSet;
        }
    }

    // Gets rubble block - regenerates list if empty
    private BlockState getRandomDirt(BuildingInfo info, CompiledPalette compiledPalette) {
        Character rubbleDirtBlock = info.getCityStyle().getRubbleDirtBlock();
        if (rubbleDirtBlock != null) {
            return compiledPalette.get(rubbleDirtBlock);
        }
        BlockState[] dirt = randomDirt;
        if (dirt == null) {
            synchronized (this) {
                dirt = randomDirt;
                if (dirt == null) {
                    BlockState mBricks = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
                    BlockState mCobble = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
                    BlockState moss = Blocks.MOSS_BLOCK.defaultBlockState();
                    randomDirtSet = Set.of(mBricks, mCobble, moss);

                    dirt = new BlockState[128];
                    int i = 0;
                    while (i < 20) {
                        dirt[i++] = mBricks;
                    }
                    while (i < 60) {
                        dirt[i++] = mCobble;
                    }
                    while (i < dirt.length) {
                        dirt[i++] = moss;
                    }
                    randomDirt = dirt;
                }
            }
        }
        return dirt[GenerationContext.nextPaletteIndex()];
    }

    public Set<BlockState> getRailStates() {
        Set<BlockState> states = railStates;
        if (states == null) {
            synchronized (this) {
                states = railStates;
                if (states == null) {
                    Set<BlockState> newStates = new HashSet<>();
                    addStates(Blocks.RAIL, newStates);
                    addStates(Blocks.POWERED_RAIL, newStates);
                    railStates = states = Set.copyOf(newStates);
                }
            }
        }
        return states;
    }

    private Set<BlockState> getStatesNeedingTodo() {
        Set<BlockState> states = statesNeedingTodo;
        if (states == null) {
            synchronized (this) {
                states = statesNeedingTodo;
                if (states == null) {
                    Set<BlockState> newStates = new HashSet<>();
                    for (Holder<Block> bh : Tools.getBlocksForTag(BlockItemTags.SAPLINGS.block())) {
                        addStates(bh.value(), newStates);
                    }
                    for (Holder<Block> bh : Tools.getBlocksForTag(BlockTags.SMALL_FLOWERS)) {
                        addStates(bh.value(), newStates);
                    }
                    statesNeedingTodo = states = Set.copyOf(newStates);
                }
            }
        }
        return states;
    }

    private Set<BlockState> getStatesNeedingLightingUpdate() {
        Set<BlockState> states = statesNeedingLightingUpdate;
        if (states == null) {
            synchronized (this) {
                states = statesNeedingLightingUpdate;
                if (states == null) {
                    Set<BlockState> newStates = new HashSet<>();
                    for (Holder<Block> bh : Tools.getBlocksForTag(LostTags.LIGHTS_TAG)) {
                        addStates(bh.value(), newStates);
                    }
                    statesNeedingLightingUpdate = states = Set.copyOf(newStates);
                }
            }
        }
        return states;
    }

    private Set<BlockState> getStatesNeedingPoiUpdate() {
        Set<BlockState> states = statesNeedingPoiUpdate;
        if (states == null) {
            synchronized (this) {
                states = statesNeedingPoiUpdate;
                if (states == null) {
                    Set<BlockState> newStates = new HashSet<>();
                    for (Holder<Block> bh : Tools.getBlocksForTag(LostTags.NEEDSPOI_TAG)) {
                        addStates(bh.value(), newStates);
                    }
                    statesNeedingPoiUpdate = states = Set.copyOf(newStates);
                }
            }
        }
        return states;
    }

    private static void addStates(Block block, Set<BlockState> set) {
        set.addAll(block.getStateDefinition().getPossibleStates());
    }

    public void setupStates(LostCityProfile profile) {
        // Kept for API compatibility. The immutable states are initialized by the constructor.
    }

    private boolean isVoid(int x, int z) {
        getDriver().current(x, 255, z);
        int minHeight = provider.getWorld().getMinY();
        while (getDriver().getBlock() == air && getDriver().getY() > minHeight) {
            getDriver().decY();
        }
        return getDriver().getY() == minHeight;
    }

    public void generate(WorldGenRegion region, ChunkAccess chunk) {
        long start = System.currentTimeMillis();
        int chunkX = chunk.getPos().x();
        int chunkZ = chunk.getPos().z();

        try (GenerationContext.Scope ignored = GenerationContext.open(region, provider.getSeed(), provider.getType(), chunkX, chunkZ)) {
            LevelAccessor oldRegion = getDriver().getRegion();
            ChunkAccess oldChunk = getDriver().getPrimer();
            ChunkCoord coord = new ChunkCoord(provider.getType(), chunkX, chunkZ);

            try {
                getDriver().setPrimer(region, chunk);

        ChunkHeightmap heightmap = getHeightmap(coord, provider.getWorld());
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, provider);

        // @todo this setup is not very clean
        CityStyle cityStyle = info.getCityStyle();
        GenerationContext.current().setStreet(cityStyle.getStreetBlock());//info.getCompiledPalette().get(cityStyle.getStreetBlock());

        boolean doCity = info.isCity || (info.outsideChunk && info.hasBuilding);

        // If this chunk has a building or street but we're in a floating profile and
        // we happen to have a void chunk we detect that here and go back to normal chunk generation
        // anyway
        if (doCity && provider.getProfile().CITY_AVOID_VOID && provider.getProfile().isFloating()) {
            boolean v = isVoid(2, 2) || isVoid(2, 14) || isVoid(14, 2) || isVoid(14, 14) || isVoid(8, 8);
            doCity = !v;
        }

        if (doCity) {
            doCityChunk(info, heightmap, chunk);
        } else {
            // We already have a prefilled core chunk (as generated from doCoreChunk)
            doNormalChunk(info, heightmap);
        }

        if (profile.isSpace() || profile.isSpheres()) {
            if (CitySphere.isCitySphereCenter(coord, provider)) {
                CitySphereSettings settings = provider.getWorldStyle().getCitysphereSettings();
                if (settings != null && settings.getCenterpart() != null) {
                    BuildingPart part = AssetRegistries.PARTS.getOrWarn(provider.getWorld(), settings.getCenterpart());
                    if (part != null) {
                        int offset = settings.getCenterPartOffset();
                        int partY = switch (settings.getCenterPartOrigin()) {
                            case FIXED -> 0;
                            case CENTER -> CitySphere.getCitySphere(coord, provider).getCenterPos().getY();
                            case FIRSTFLOOR -> info.getCityGroundLevel();
                            case GROUND -> info.groundLevel;
                            case TOP -> getTopLevel(info);
                        };
                        partY += offset;
                        generatePart(info, part, Transform.ROTATE_NONE, 0, partY, 0, HardAirSetting.WATERLEVEL);
                    }
                }
            }
        }

        Railway.RailChunkInfo railInfo = info.getRailInfo();
        if (railInfo.getType() != RailChunkType.NONE) {
            Railways.generateRailways(this, info, railInfo, heightmap);
        }
        Railways.generateRailwayDungeons(this, info);

//        if (profile.isSpace()) {
//            generateMonorails(info);
//        }
//
        fixTorches(info);

        // We make a new random here because the primer for a normal chunk may have
        // been cached and we want to be able to do the same when returning from a cached
        // primer vs generating it here
        GenerationContext.current().random().setSeed(chunkX * 257017164707L + chunkZ * 101754694003L);

        LostCityEvent.PreExplosionEvent event = new LostCityEvent.PreExplosionEvent(provider.getWorld(), LostCities.lostCitiesImp, chunkX, chunkZ, getDriver().getPrimer());
        LostCityEvent.PreExplosionEvent posted = LostCityEvents.post(LostCityEvents.PRE_EXPLOSION, event);
        if (!posted.isCanceled()) {
            if (info.getDamageArea().hasExplosions()) {
                breakBlocksForDamageNew(chunkX, chunkZ, info);
                fixAfterExplosion(info);
            }
            generateDebris(info);
        }

                getDriver().actuallyGenerate(chunk);
            } finally {
                getDriver().setPrimer(oldRegion, oldChunk);
            }
            ChunkFixer.fix(provider, coord);
        } finally {
            long time = System.currentTimeMillis() - start;
            statistics.addTime(time);
        }
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public RandomSource getRandom() {
        return GenerationContext.current().random();
    }

    public ChunkDriver getDriver() {
        return GenerationContext.current().driver();
    }

    private int getTopLevel(BuildingInfo info) {
        if (info.hasBuilding) {
            return info.getCityGroundLevel() + info.getNumFloors() * FLOORHEIGHT;
        } else {
            return info.getCityGroundLevel();
        }
    }

    private void fixTorches(BuildingInfo info) {
        List<BlockPos> torches = info.getTorchTodo();
        if (torches.isEmpty()) {
            return;
        }

        BlockState torchState = Blocks.WALL_TORCH.defaultBlockState();
        for (BlockPos pos : torches) {
            int x = pos.getX() & 0xf;
            int z = pos.getZ() & 0xf;
            getDriver().currentAbsolute(pos);
            if (getDriver().getBlockDown() != air) {
                getDriver().block(Blocks.TORCH.defaultBlockState());
            } else if (x > 0 && getDriver().getBlockWest() != air) {
                getDriver().block(torchState.setValue(WallTorchBlock.FACING, net.minecraft.core.Direction.EAST));
            } else if (x < 15 && getDriver().getBlockEast() != air) {
                getDriver().block(torchState.setValue(WallTorchBlock.FACING, net.minecraft.core.Direction.WEST));
            } else if (z > 0 && getDriver().getBlockNorth() != air) {
                getDriver().block(torchState.setValue(WallTorchBlock.FACING, net.minecraft.core.Direction.SOUTH));
            } else if (z < 15 && getDriver().getBlockSouth() != air) {
                getDriver().block(torchState.setValue(WallTorchBlock.FACING, net.minecraft.core.Direction.NORTH));
            }
            updateNeeded(info, pos, Block.UPDATE_CLIENTS);
        }
        info.clearTorchTodo();
    }

    private void doNormalChunk(BuildingInfo info, ChunkHeightmap heightmap) {
//        debugClearChunk(chunkX, chunkZ, primer);
        if ((!info.hasDirectStructureAvoidance() || !Config.AVOID_FLATTENING.get()) && (profile.isDefault() || profile.isVoidSpheres())) {
            correctTerrainShape(provider.getWorld(), info.coord, heightmap);
//            flattenChunkToCityBorder(chunkX, chunkZ);
        }

        int chunkX = info.coord.chunkX();
        int chunkZ = info.coord.chunkZ();
        LostCityEvent.PostGenOutsideChunkEvent postevent = new LostCityEvent.PostGenOutsideChunkEvent(provider.getWorld(), LostCities.lostCitiesImp, chunkX, chunkZ, getDriver().getPrimer());
        LostCityEvents.post(LostCityEvents.POST_GEN_OUTSIDE_CHUNK, postevent);

        Bridges.generateBridges(this, info);
        Highways.generateHighways(this, info);

        ScatteredSettings scatteredSettings = provider.getWorldStyle().getScatteredSettings();
        if (scatteredSettings != null) {
            if (!Scattered.avoidScattered(this, info)) {
                Scattered.generateScattered(this, info, scatteredSettings);
            }
        }
    }

    private void breakBlocksForDamageNew(int chunkX, int chunkZ, BuildingInfo info) {
        int cx = chunkX << 4;
        int cz = chunkZ << 4;

        DamageArea damageArea = info.getDamageArea();

        float damageFactor = 1.0f;

        boolean hasCollectedDamage = false;
        float[][] collectedDamage = new float[16][16];

        // @todo this only supports explosions between 0 and 256 for now
        for (int yy = 0; yy < 16; yy++) {
            boolean hasExplosions = damageArea.hasExplosions(yy);
            for (int y = 0; y < 16; y++) {
                if (hasExplosions) {
                    int cury = yy * 16 + y;
                    for (int x = 0; x < 16; x++) {
                        getDriver().current(x, cury, 0);
                        for (int z = 0; z < 16; z++) {
                            BlockState d = getDriver().getBlock();
                            if (d != air || cury <= info.waterLevel) {
                                float damage = damageArea.getDamage(cx + x, cury, cz + z) * damageFactor;
                                if (damage >= 0.001) {
                                    collectedDamage[x][z] += damage;
                                    hasCollectedDamage = true;
                                }
                            }
                            getDriver().incZ();
                        }
                    }
                }
                if (hasCollectedDamage) {
                    int cntDamaged = 0;
                    int cntAir = 0;
                    int cury = yy * 16 + y;
                    hasCollectedDamage = false;
                    for (int x = 0; x < 16; x++) {
                        getDriver().current(x, cury, 0);
                        for (int z = 0; z < 16; z++) {
                            BlockState d = getDriver().getBlock();
                            if (d != air || cury <= info.waterLevel) {
                                float damage = collectedDamage[x][z];
                                if (damage >= 0.001) {
                                    BlockState newd = damageArea.damageBlock(d, provider, getRandom(), cury, damage, info.getCompiledPalette(), liquid);
                                    if (newd != d) {
                                        getDriver().block(newd);
                                        cntDamaged++;
                                    }
                                }
                            } else {
                                cntAir++;
                            }
                            getDriver().incZ();
//                            collectedDamage[x][z] -= .75f;
                            collectedDamage[x][z] /= 1.4f;
//                            collectedDamage[x][z] = 0;
                            if (collectedDamage[x][z] <= 0) {
                                collectedDamage[x][z] = 0;
                            } else {
                                hasCollectedDamage = true;
                            }
                        }
                    }

                    int tot = cntDamaged + cntAir;
                    if (tot > 250) {
                        damageFactor = 200;
                    } else if (tot > 220) {
                        damageFactor = damageFactor * 1.4f;
                    } else if (tot > 180) {
                        damageFactor = damageFactor * 1.2f;
                    }

                }
            }
        }
    }

    public String getRandomPart(List<String> parts) {
        if (parts.size() == 1) {
            return parts.get(0);
        } else {
            return parts.get(GenerationContext.current().random().nextInt(parts.size()));
        }
    }

    public void clearRange(BuildingInfo info, int x, int z, int height1, int height2, boolean dowater) {
        if (dowater) {
            // Special case for drowned city
            getDriver().setBlockRange(x, height1, z, info.waterLevel, liquid);
            getDriver().setBlockRangeToAir(x, info.waterLevel + 1, z, height2);
        } else {
            getDriver().setBlockRangeToAir(x, height1, z, height2);
        }
    }

    public void clearRange(BuildingInfo info, int x, int z, int height1, int height2, boolean dowater, Predicate<BlockState> test) {
        if (dowater) {
            // Special case for drowned city
            getDriver().setBlockRange(x, height1, z, info.waterLevel, liquid, test);
            getDriver().setBlockRangeToAir(x, info.waterLevel + 1, z, height2, test);
        } else {
            getDriver().setBlockRangeToAir(x, height1, z, height2, test);
        }
    }

    private static final ThreadLocal<Random> COORDINATE_RANDOM = ThreadLocal.withInitial(Random::new);

    public static int getRandomizedOffset(int chunkX, int chunkZ, int min, int max) {
        Random random = COORDINATE_RANDOM.get();
        random.setSeed(chunkZ * 256203221L + chunkX * 899809363L);
        return random.nextInt(max - min + 1) + min;
    }

    public static int getHeightOffsetL1(int chunkX, int chunkZ) {
        Random random = COORDINATE_RANDOM.get();
        random.setSeed(chunkZ * 341873128712L + chunkX * 132897987541L);
        return random.nextInt(5);
    }

    public static int getHeightOffsetL2(int chunkX, int chunkZ) {
        Random random = COORDINATE_RANDOM.get();
        random.setSeed(chunkZ * 132897987541L + chunkX * 341873128712L);
        return random.nextInt(5);
    }

    /*
     * This routine is used on a normal (non-city) chunk to make sure the landscape nicely fits
     * with any possible adjacent city chunks. It works by creating two meshes that are overlayed
     * on the terrain. Meshes are defined at chunk corners. Every chunk corner has a corresponding
     * height on the two meshes.
     *
     * The upper mesh indicates the maximum height the terrain is allowed to go. If a certain chunk
     * corner is not adjacent to any city chunk or is not adjacent to any normal chunk then there is
     * no maximum height and in that case we set it to 100000. Otherwise (if the chunk corner
     * is adjacent to mixed chunks) the maximum allowed height of the terrain is equal to the minimum
     * height of all the city chunks (with minimum height we mean the lower city level or the height
     * of the first floor).
     *
     * The lower mesh indicates the minimum height the terrain is allowed to go. Same as with the upper
     * mesh there is no minimum in case the chunk corner is not a mixed type corner. Otherwise the
     * minimum height is going to be some (configurable) offset below the minimum lower city level.
     *
     * Every normal chunk is made to fit between the lower and the upper mesh by moving down
     * or up the top layer (6 thick) of the terrain. In a chunk these heights are interpolated
     * (bilinear interpolation).
     */
    private void correctTerrainShape(WorldGenLevel level, ChunkCoord coord, ChunkHeightmap heightmap) {
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, provider);
        BuildingInfo.MinMax mm00 = info.getDesiredMaxHeightL2();
        BuildingInfo.MinMax mm10 = info.getXmax().getDesiredMaxHeightL2();
        BuildingInfo.MinMax mm01 = info.getZmax().getDesiredMaxHeightL2();
        BuildingInfo.MinMax mm11 = info.getXmax().getZmax().getDesiredMaxHeightL2();

        int min = level.getMinY();
        int max = level.getMaxY() + 1;
        int heightmapH = Short.MIN_VALUE;

        float min00 = mm00.min;
        float min10 = mm10.min;
        float min01 = mm01.min;
        float min11 = mm11.min;
        float max00 = mm00.max;
        float max10 = mm10.max;
        float max01 = mm01.max;
        float max11 = mm11.max;
        if (max00 < max || max10 < max || max01 < max || max11 < max ||
                min00 < max || min10 < max || min01 < max || min11 < max) {
            // We need to fit the terrain between the upper and lower mesh here
            int maxHeightP = heightmap.getHeight() + 90;
            int minHeightP = heightmap.getHeight() - 90;
            if (max00 >= max) {
                max00 = maxHeightP;
            }
            if (max10 >= max) {
                max10 = maxHeightP;
            }
            if (max01 >= max) {
                max01 = maxHeightP;
            }
            if (max11 >= max) {
                max11 = maxHeightP;
            }
            if (min00 >= max) {
                min00 = minHeightP;
            }
            if (min10 >= max) {
                min10 = minHeightP;
            }
            if (min01 >= max) {
                min01 = minHeightP;
            }
            if (min11 >= max) {
                min11 = minHeightP;
            }

            for (int x = 0; x < 16; x++) {
                // Bilinear interpolation
                float factor = (15.0f - x) / 15.0f;
                float maxh0 = max11 + (max01 - max11) * factor;
                float maxh1 = max10 + (max00 - max10) * factor;
                float minh0 = min11 + (min01 - min11) * factor;
                float minh1 = min10 + (min00 - min10) * factor;
                for (int z = 0; z < 16; z++) {
                    float maxheight = maxh0 + (maxh1 - maxh0) * (15.0f - z) / 15.0f;
                    if (maxheight > max) {
                        maxheight = max;
                    }
                    int maxTouchedY = moveDown(x, z, (int) maxheight, max);

                    if (maxTouchedY == Short.MIN_VALUE) {
                        float minheight = minh0 + (minh1 - minh0) * (15.0f - z) / 15.0f;
                        if (minheight < min) {
                            minheight = min;
                        }
                        maxTouchedY = moveUp(x, z, (int) minheight, info.waterLevel > info.groundLevel);
                    }
                    if (maxTouchedY != Short.MIN_VALUE && x == 8 && z == 8) {
                        // Only adjust heightmap for center value
                        heightmapH = Math.max(heightmapH, maxTouchedY);
                    }
                }
            }
            if (heightmapH != Short.MIN_VALUE) {
                heightmap.setHeight(heightmapH);
            }
        }
    }

    // Return true if state is air or liquid
    public static boolean isEmpty(BlockState state) {
        if (state.isAir()) {
            return true;
        }
        if (state.is(Blocks.WATER)) {
            return true;
        }
        if (state.is(Blocks.LAVA)) {
            return true;
        }
        return false;
    }

    // Return true if state is Empty or Plant based - stops (most) funny tree/mushroom action on chunk borders
    private static boolean isFoliageOrEmpty(BlockState state) {
        if (isEmpty(state)) {
            return true;
        }
        return Tools.hasTag(state.getBlock(), LostTags.FOLIAGE_TAG);
    }

    // Return the new max height of the chunk in this column. Or Short.MIN_VALUE if nothing was done
    private int moveUp(int x, int z, int height, boolean dowater) {
        int maxYTouched = Short.MIN_VALUE;       // Max Y that we touched
        // Find the first non-empty block starting at the given height
        getDriver().current(x, height, z);
        int minHeight = provider.getWorld().getMinY();
        // We assume here we are not in a void chunk
        while (isFoliageOrEmpty(getDriver().getBlock()) && getDriver().getY() > minHeight) {
            getDriver().decY();
        }

        if (getDriver().getY() >= height) {
            return maxYTouched; // Nothing to do
        }

        int idx = getDriver().getY();    // Points to non-empty block below the empty block
        getDriver().current(x, height, z);
        while (idx > 0) {
            BlockState blockToMove = getDriver().getBlock(x, idx, z);
            if (blockToMove.isAir() || blockToMove.getBlock() == Blocks.BEDROCK) {
                break;
            }
            if (maxYTouched == Short.MIN_VALUE) {
                maxYTouched = idx;
            }
            getDriver().block(blockToMove);
            getDriver().decY();
            idx--;
        }
        return maxYTouched;
    }

    private final BlockState[] buffer = new BlockState[6];

    // Return the new max height of the chunk in this column. Or Short.MIN_VALUE if nothing was done
    private int moveDown(int x, int z, int height, int maxBuildLimit) {
        int maxYTouched = Short.MIN_VALUE;       // Max Y that we touched
        int y = maxBuildLimit-1;
        getDriver().current(x, y, z);
        // We assume here we are not in a void chunk
        while (isEmpty(getDriver().getBlock()) && getDriver().getY() > height) {
            getDriver().decY();
        }

        if (getDriver().getY() <= height) {
            return maxYTouched; // Nothing to do
        }

        // We arrived at our first non-air block
        int bufferIdx = 0;
        while (getDriver().getY() >= height) {
            if (bufferIdx < buffer.length) {
                buffer[bufferIdx++] = getDriver().getBlock();
            }
            getDriver().block(air);
            getDriver().decY();
        }

        maxYTouched = getDriver().getY();
        int idx = 0;
        while (idx < bufferIdx && getDriver().getY() > 0) {
            getDriver().block(buffer[idx++]);
            getDriver().decY();
        }

//
//        if (dowater) {
//            // Special case for drowned city
//            getDriver().setBlockRange(x, height1, z, info.waterLevel, liquid);
//            getDriver().setBlockRange(x, info.waterLevel+1, z, height2, air);
//        } else {
//            getDriver().setBlockRange(x, height1, z, height2, air);
//        }
        return maxYTouched;
    }


    public static boolean isWaterBiome(IDimensionInfo provider, ChunkCoord coord) {
        BiomeInfo biomeInfo = BiomeInfo.getBiomeInfo(provider, coord);
        Holder<Biome> mainBiome = biomeInfo.getMainBiome();
        return isWaterBiome(mainBiome);
    }

    private static boolean isWaterBiome(Holder<Biome> biome) {
        return biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_BEACH) || biome.is(BiomeTags.IS_RIVER);
    }

    /**
     * This function returns the height at a given point in this chunk
     * If the point is at a border and the adjacent chunk at that point happens to be lower
     * then this will return the minimum height
     */
    public int getMinHeightAt(BuildingInfo info, int x, int z, ChunkHeightmap heightmap) {
        int height = heightmap.getHeight();
        WorldGenLevel world = info.provider.getWorld();
        int adjacent;
        if (x == 0) {
            if (z == 0) {
                adjacent = getHeightmap(info.coord.northWest(), world).getHeight();
            } else if (z == 15) {
                adjacent = getHeightmap(info.coord.southWest(), world).getHeight();
            } else {
                adjacent = getHeightmap(info.coord.west(), world).getHeight();
            }
        } else if (x == 15) {
            if (z == 0) {
                adjacent = getHeightmap(info.coord.northEast(), world).getHeight();
            } else if (z == 15) {
                adjacent = getHeightmap(info.coord.southEast(), world).getHeight();
            } else {
                adjacent = getHeightmap(info.coord.east(), world).getHeight();
            }
        } else if (z == 0) {
            adjacent = getHeightmap(info.coord.north(), world).getHeight();
        } else if (z == 15) {
            adjacent = getHeightmap(info.coord.south(), world).getHeight();
        } else {
            return height;
        }
        return Math.min(height, adjacent);
    }

    public ChunkHeightmap getHeightmap(ChunkCoord chunk, @Nonnull WorldGenLevel world) {
        int heightSampleSize = Config.HEIGHT_SAMPLE_SIZE.get();
        int top, left;
        int constX = 1, constZ = 1; // We'll need to take care of the negative part of the chunks as well
        ChunkCoord sampler = chunk;
        if (heightSampleSize > 1) {
            // Recalculate chunk to be the center of the sample
            top = (chunk.chunkX() / heightSampleSize) * heightSampleSize;
            left = (chunk.chunkZ() / heightSampleSize) * heightSampleSize;
            constX = chunk.chunkX() < 0 ? -1 : 1;
            constZ = chunk.chunkZ() < 0 ? -1 : 1;
            if (heightSampleSize > 2) {
                int sampleOffset = heightSampleSize / 2;
                sampler = new ChunkCoord(chunk.dimension(), top + (sampleOffset * constX), left + (sampleOffset * constZ));
            }
        } else {
            top = chunk.chunkX();
            left = chunk.chunkZ();
        }
        synchronized (this) {
            ChunkHeightmap cached = cachedHeightmaps.get(chunk);
            if (cached != null) {
                return cached;
            }
            ChunkHeightmap heightmap = new ChunkHeightmap(profile.LANDSCAPE_TYPE, profile.GROUNDLEVEL);
            generateHeightmap(sampler.chunkX(), sampler.chunkZ(), world, heightmap);
            if (heightSampleSize > 1) {
                for (int i = 0; i < heightSampleSize; i++) {
                    for (int j = 0; j < heightSampleSize; j++) {
                        ChunkCoord sampleKey = new ChunkCoord(chunk.dimension(), top + (i * constX), left + (j * constZ));
                        cachedHeightmaps.put(sampleKey, new ChunkHeightmap(heightmap));
                    }
                }
            } else {
                cachedHeightmaps.put(chunk, heightmap);
            }
            return heightmap;
        }
    }

    private void generateHeightmap(int chunkX, int chunkZ, WorldGenLevel region, ChunkHeightmap heightmap) {
        ServerChunkCache chunkProvider = region.getLevel().getChunkSource();
        ChunkGenerator generator = chunkProvider.getGenerator();
        int cx = chunkX << 4;
        int cz = chunkZ << 4;
        RandomState randomState = chunkProvider.randomState();

        int height;
        if (Config.OPTIMIZED_HEIGHTMAP.get()) {
            height = HeightGenOpt.getBaseHeight((NoiseBasedChunkGenerator) generator, cx + 8, cz + 8, region, randomState);
        } else {
            height = generator.getBaseHeight(cx + 8, cz + 8, Heightmap.Types.OCEAN_FLOOR_WG, region, randomState);
        }
//        int height2 = HeightGenOpt.getBaseHeight((NoiseBasedChunkGenerator) generator, cx + 8, cz + 8, region, randomState);
//        if (Math.abs(height - height2) > 4) {
//            System.out.println("height1 / height2 = " + height + " / height2 = " + height2 + ", at position " + (cx + 8) + ", " + (cz + 8));
//            generator.getBaseHeight(cx + 8, cz + 8, Heightmap.Types.OCEAN_FLOOR_WG, region, randomState);
//            HeightGenOpt.getBaseHeight((NoiseBasedChunkGenerator) generator, cx + 8, cz + 8, region, randomState);
//        }
        heightmap.update(height);
    }

    private void doCityChunk(BuildingInfo info, ChunkHeightmap heightmap, ChunkAccess chunk) {
        boolean building = info.hasBuilding;

        if (info.profile.isDefault() || info.profile.isVoidSpheres()) {
            int minHeight = info.provider.getWorld().getMinY();
            BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    getDriver().setBlockRange(x, minHeight, z, minHeight + info.profile.BEDROCK_LAYER, bedrock);
                }
            }

            if (info.waterLevel > info.groundLevel) {
                // Special case for a high water level
                for (int x = 0; x < 16; ++x) {
                    for (int z = 0; z < 16; ++z) {
                        getDriver().setBlockRange(x, info.groundLevel, z, info.waterLevel, liquid);
                    }
                }
            }
        }

        // City surface leveling - for prettier cities
        // Note: Better results may be achieved with terrain noise adjustment (like how newer structures do it)
        if (profile.isDefault() || profile.isVoidSpheres()) {
            int ground = info.getCityGroundLevel();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int maxTouchedY = moveDown(x, z, ground + 1, provider.getWorld().getMaxY() + 1);
                    if (maxTouchedY == Short.MIN_VALUE) {
                        moveUp(x, z, ground, info.waterLevel > info.groundLevel);
                    }
                }
            }
        }

        int chunkX = info.coord.chunkX();
        int chunkZ = info.coord.chunkZ();
        LostCityEvent.PreGenCityChunkEvent event = new LostCityEvent.PreGenCityChunkEvent(provider.getWorld(), LostCities.lostCitiesImp, chunkX, chunkZ, getDriver().getPrimer());
        LostCityEvent.PreGenCityChunkEvent posted = LostCityEvents.post(LostCityEvents.PRE_GEN_CITY_CHUNK, event);
        if (!posted.isCanceled()) {
            if (building) {
                generateBuilding(info, heightmap, chunk);
            } else {
                generateStreet(info, heightmap);
            }
        }
        LostCityEvent.PostGenCityChunkEvent postevent = new LostCityEvent.PostGenCityChunkEvent(provider.getWorld(), LostCities.lostCitiesImp, chunkX, chunkZ, getDriver().getPrimer());
        LostCityEvents.post(LostCityEvents.POST_GEN_CITY_CHUNK, postevent);

        if (info.profile.RUIN_CHANCE > 0.0) {
            generateRuins(info);
        }

        int levelX = info.getHighwayXLevel();
        int levelZ = info.getHighwayZLevel();
        if (!building) {
            Railway.RailChunkInfo railInfo = info.getRailInfo();
            if (levelX < 0 && levelZ < 0 && !railInfo.getType().isSurface()
                    && info.getStreetSlopeDirection() == null) {
                generateStreetDecorations(info);
            }
        }
        if (levelX >= 0 || levelZ >= 0) {
            Highways.generateHighways(this, info);
        }

        if (info.profile.RUBBLELAYER) {
            if (!info.hasBuilding || info.ruinHeight >= 0) {
                generateRubble(info);
            }
        }

        Stuff.generateStuff(this, info);
    }

    private void generateStreetDecorations(BuildingInfo info) {
        Direction stairDirection = info.getActualStairDirection();
        if (stairDirection != null) {
            BuildingPart stairs = info.stairType;
            if (stairs != null) {
                Transform transform;
                int oy = info.getCityGroundLevel() + 1;
                transform = switch (stairDirection) {
                    case XMIN -> Transform.ROTATE_NONE;
                    case XMAX -> Transform.ROTATE_180;
                    case ZMIN -> Transform.ROTATE_90;
                    case ZMAX -> Transform.ROTATE_270;
                };

                generatePart(info, stairs, transform, 0, oy, 0, HardAirSetting.AIR);
            }
        }
    }

    private int countNotEmpty(int y, int max) {
        int cnt = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (getDriver().getBlock(x, y, z) != air) {
                    cnt++;
                    if (cnt >= max) {
                        return cnt;
                    }
                }
            }
        }
        return cnt;
    }

    /// Fix floating blocks after an explosion
    private void fixAfterExplosion(BuildingInfo info) {
        if (info.profile.isCavern() && !info.hasBuilding) {
            // In a cavern we only do this correction when there is a building
            return;
        }

        int start = info.getDamageArea().getLowestExplosionHeight();
        if (start == -1) {
            // Nothing is affected
            return;
        }
        int end = info.getDamageArea().getHighestExplosionHeight();

        for (int y = start; y <= end; y++) {
            int count = countNotEmpty(y, 20);
            if (count < 16) {   // @todo configurable?
                // (Almost) empty! That means everything above this can be deleted
                // Except in a cavern, there we only delete the building
                if (info.profile.isCavern()) {
                    // We know we have a building
                    int maxY = info.getCityGroundLevel() + info.getNumFloors() * FLOORHEIGHT;
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            getDriver().setBlockRangeToAir(x, y + 1, z, maxY);
                        }
                    }
                } else {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            getDriver().setBlockRangeToAir(x, y + 1, z, 256);  // @todo hardcoded height
                        }
                    }
                }
                break;
            }
        }
    }

    private void generateRubble(BuildingInfo info) {
        int chunkX = info.coord.chunkX();
        int chunkZ = info.coord.chunkZ();
        GenerationContext context = GenerationContext.current();
        double[] rubbleBuffer = this.rubbleNoise.getRegion(context.rubbleBuffer(), (chunkX << 4), (chunkZ << 4), 16, 16, 1.0 / 16.0, 1.0 / 16.0, 1.0D);
        double[] leavesBuffer = this.leavesNoise.getRegion(context.leavesBuffer(), (chunkX << 6), (chunkZ << 6), 16, 16, 1.0 / 64.0, 1.0 / 64.0, 4.0D);
        context.setRubbleBuffer(rubbleBuffer);
        context.setLeavesBuffer(leavesBuffer);

        Set<BlockState> possibleRandomDirts = getPossibleRandomDirts(info, info.getCompiledPalette());
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                double vr = info.profile.RUBBLE_DIRT_SCALE < 0.01f ? 0 : rubbleBuffer[x + z * 16] / info.profile.RUBBLE_DIRT_SCALE;
                double vl = info.profile.RUBBLE_LEAVE_SCALE < 0.01f ? 0 : leavesBuffer[x + z * 16] / info.profile.RUBBLE_LEAVE_SCALE;
                if (vr > .5 || vl > .5) {
                    int height = getInterpolatedHeight(info, x, z);
                    getDriver().current(x, height, z);
                    BlockState c = getDriver().getBlockDown();
                    if (c != air && c != liquid) {
                        for (int i = 0; i < vr; i++) {
                            if (isEmpty(getDriver().getBlock())) {
                                getDriver().add(getRandomDirt(info, info.getCompiledPalette()));
                            } else {
                                getDriver().incY();
                            }
                        }
                    }
                    //first round may not have generated this - stops crash on create world
                    BlockState leafBaseState = getDriver().getBlockDown();
                    if (leafBaseState == base || possibleRandomDirts.contains(leafBaseState)) {
                        for (int i = 0; i < vl; i++) {
                            if (isEmpty(getDriver().getBlock())) {
                                getDriver().add(getRandomLeaf(info, info.getCompiledPalette()));
                            } else {
                                getDriver().incY();
                            }
                        }
                    }
                }
            }
        }
    }

    private int getInterpolatedHeight(BuildingInfo info, int x, int z) {
        if (x < 8 && z < 8) {
            // First quadrant
            float h00 = info.getXmin().getZmin().getCityGroundLevelOutsideLower();
            float h10 = info.getZmin().getCityGroundLevelOutsideLower();
            float h01 = info.getXmin().getCityGroundLevelOutsideLower();
            float h11 = info.getCityGroundLevelOutsideLower();
            return bipolate(h00, h10, h01, h11, x + 8, z + 8);
        } else if (x >= 8 && z < 8) {
            // Second quadrant
            float h00 = info.getZmin().getCityGroundLevelOutsideLower();
            float h10 = info.getXmax().getZmin().getCityGroundLevelOutsideLower();
            float h01 = info.getCityGroundLevelOutsideLower();
            float h11 = info.getXmax().getCityGroundLevelOutsideLower();
            return bipolate(h00, h10, h01, h11, x - 8, z + 8);
        } else if (x < 8 && z >= 8) {
            // Third quadrant
            float h00 = info.getXmin().getCityGroundLevelOutsideLower();
            float h10 = info.getCityGroundLevelOutsideLower();
            float h01 = info.getXmin().getZmax().getCityGroundLevelOutsideLower();
            float h11 = info.getZmax().getCityGroundLevelOutsideLower();
            return bipolate(h00, h10, h01, h11, x + 8, z - 8);
        } else {
            // Fourth quadrant
            float h00 = info.getCityGroundLevelOutsideLower();
            float h10 = info.getXmax().getCityGroundLevelOutsideLower();
            float h01 = info.getZmax().getCityGroundLevelOutsideLower();
            float h11 = info.getXmax().getZmax().getCityGroundLevelOutsideLower();
            return bipolate(h00, h10, h01, h11, x - 8, z - 8);
        }
    }

    private int bipolate(float h00, float h10, float h01, float h11, int dx, int dz) {
        float factor = (15.0f - dx) / 15.0f;
        float h0 = h00 + (h10 - h00) * factor;
        float h1 = h01 + (h11 - h01) * factor;
        float h = h0 + (h1 - h0) * (15.0f - dz) / 15.0f;
        return (int) h;
    }


    private void generateRuins(BuildingInfo info) {
        if (info.ruinHeight < 0) {
            return;
        }

        int chunkX = info.coord.chunkX();
        int chunkZ = info.coord.chunkZ();
        double d0 = 0.03125D;
        GenerationContext context = GenerationContext.current();
        RandomSource random = context.random();
        double[] ruinBuffer = this.ruinNoise.getRegion(context.ruinBuffer(), (chunkX << 4), (chunkZ << 4), 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);
        context.setRuinBuffer(ruinBuffer);
        boolean doLeaves = info.profile.RUBBLELAYER;
        double[] leavesBuffer = context.leavesBuffer();
        if (doLeaves) {
            leavesBuffer = this.leavesNoise.getRegion(leavesBuffer, (chunkX << 6), (chunkZ << 6), 16, 16, 1.0 / 64.0, 1.0 / 64.0, 4.0D);
            context.setLeavesBuffer(leavesBuffer);
        }

        int baseheight = (int) (info.getCityGroundLevel() + 1 + (info.ruinHeight * info.getNumFloors() * FLOORHEIGHT));

        CompiledPalette palette = info.getCompiledPalette();

        BlockState ironbarsState = Blocks.IRON_BARS.defaultBlockState();
        Character infobarsChar = info.getCityStyle().getIronbarsBlock();
        Supplier<BlockState> ironbars = infobarsChar == null ? () -> ironbarsState : () -> palette.get(infobarsChar);
        Set<BlockState> infoBarSet = infobarsChar == null ? Collections.singleton(ironbarsState) : palette.getAll(infobarsChar);
        Predicate<BlockState> checkIronbars = infobarsChar == null ? s -> s == ironbarsState : infoBarSet::contains;
        Character rubbleBlock = info.getBuilding().getRubbleBlock();

        int maxBuildHeight = info.provider.getWorld().getMaxY() + 1;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                double v = ruinBuffer[x + z * 16];
//                double v = ruinNoise.getValue(x, z) / 16.0;
                int height = baseheight + (int) v;
                getDriver().current(x, height, z);
                height = info.getMaxHeight() + 10 - height;
                if (height > maxBuildHeight - 2) {
                    height = maxBuildHeight - 2;
                }
                int vl = 0;
                if (doLeaves) {
                    vl = (int) (info.profile.RUBBLE_LEAVE_SCALE < 0.01f ? 0 : leavesBuffer[x + z * 16] / info.profile.RUBBLE_LEAVE_SCALE);
//                    vl = (int) (info.profile.RUBBLE_LEAVE_SCALE < 0.01f ? 0 : leavesNoise.getValue(x / 64.0, z / 64.0) / 4.0 * info.profile.RUBBLE_LEAVE_SCALE);
                }
                boolean doRubble = palette.isDefined(rubbleBlock);
                while (height > 0) {
                    BlockState damage = palette.canBeDamagedToIronBars(getDriver().getBlock());
                    BlockState c = getDriver().getBlockDown();

                    if (doRubble && !checkIronbars.test(c) && c != air && c != liquid && random.nextFloat() < .2f) {      // @todo hardcoded random
                        doRubble = false;
                        getDriver().add(palette.get(rubbleBlock));
                    } else if ((damage != null || checkIronbars.test(c)) && c != air && c != liquid && random.nextFloat() < .2f) {    // @todo hardcoded random
                        getDriver().add(ironbars.get());
                    } else {
                        if (vl > 0) {
                            c = getDriver().getBlockDown();
                            while (isEmpty(c)) {
                                getDriver().decY();
                                height++;   // Make sure we keep on filling with air a bit longer because we are lowering here
                                c = getDriver().getBlockDown();
                            }
                            getDriver().add(getRandomLeaf(info, palette));
                            vl--;
                        } else {
                            getDriver().add(air);
                        }
                    }
                    height--;
                }
            }
        }
    }

    private void generateStreet(BuildingInfo info, ChunkHeightmap heightmap) {
        boolean xRail = info.hasXCorridor();
        boolean zRail = info.hasZCorridor();
        if (xRail || zRail) {
            Corridors.generateCorridors(this, info, xRail, zRail);
        }

        Railway.RailChunkInfo railInfo = info.getRailInfo();
        boolean canDoStreetOrPark = info.getHighwayXLevel() != info.cityLevel && info.getHighwayZLevel() != info.cityLevel
                && railInfo.getType() != RailChunkType.STATION_SURFACE
                && (railInfo.getType() != RailChunkType.STATION_EXTENSION_SURFACE || railInfo.getLevel() < info.cityLevel);

        if (canDoStreetOrPark) {
            int height = info.getCityGroundLevel();
            Direction streetSlopeDirection = info.getStreetSlopeDirection();
            // In default landscape type we clear the landscape on top of the building
//            if (profile.isDefault()) {
//                clearToMax(info, heightmap, height);
//            }

            BuildingInfo.StreetType streetType = info.streetType;
            boolean elevated = info.isElevatedParkSection();
            if (elevated) {
                Character elevationBlock = info.getCityStyle().getParkElevationBlock();
                BlockState elevation = info.getCompiledPalette().get(elevationBlock);
                for (int x = 0; x < 16; ++x) {
                    getDriver().current(x, height, 0);
                    for (int z = 0; z < 16; ++z) {
                        getDriver().block(elevation).incZ();
                    }
                }
                if (info.getEffectiveCitySettings().parkElevation()) {
                    height++;
                }
            } else if (!info.isHierarchicalOpen()) {
                Random rnd = new Random(info.coord.chunkZ() * 155557723L + info.coord.chunkX() * 45555558379L);
                info.streetType = BuildingInfo.StreetType.values()[rnd.nextInt(0, BuildingInfo.StreetType.values().length - 2)];
                streetType = info.streetType;
            }

            switch (streetType) {
                case NORMAL -> {
                    if (streetSlopeDirection == null) {
                        generateNormalStreetSection(info, height);
                    } else {
                        generateStreetSlopeSection(info, height, streetSlopeDirection);
                    }
                }
                case FULL -> generateFullStreetSection(info, height);
                case PARK -> generateParkSection(info, height, elevated);
            }
            height++;

            if (streetSlopeDirection == null && (streetType == BuildingInfo.StreetType.PARK || info.fountainType != null)) {
                BuildingPart part;
                if (streetType == BuildingInfo.StreetType.PARK) {
                    part = info.parkType;
                } else {
                    part = info.fountainType;
                }
                if (part != null) {
                    generatePart(info, part, Transform.ROTATE_NONE, 0, height, 0, HardAirSetting.AIR);
                }
            }

            if (streetSlopeDirection == null) {
                generateRandomVegetation(info, height);

                generateFrontPart(info, height, info.getXmin(), Transform.ROTATE_NONE);
                generateFrontPart(info, height, info.getZmin(), Transform.ROTATE_90);
                generateFrontPart(info, height, info.getXmax(), Transform.ROTATE_180);
                generateFrontPart(info, height, info.getZmax(), Transform.ROTATE_270);
            }
        }

        generateBorders(info, canDoStreetOrPark, heightmap);
    }

    private void generateBorders(BuildingInfo info, boolean canDoParks, ChunkHeightmap heightmap) {
        Character borderBlock = info.getCityStyle().getBorderBlock();

        switch (info.profile.LANDSCAPE_TYPE) {
            case DEFAULT -> fillToBedrockStreetBlock(info);
            case FLOATING -> fillMainStreetBlock(info, borderBlock, 3);
            case CAVERN -> fillMainStreetBlock(info, borderBlock, 2);
            case CAVERNSPHERES -> fillMainStreetBlock(info, borderBlock, 2);
            case SPACE -> fillToGroundStreetBlock(info, info.getCityGroundLevel());
            case SPHERES -> fillToBedrockStreetBlock(info);
        }

        if (doBorder(info, Direction.XMIN)) {
            int x = 0;
            for (int z = 0; z < 16; z++) {
                generateBorder(info, canDoParks, x, z, Direction.XMIN.get(info), heightmap);
            }
        }
        if (doBorder(info, Direction.XMAX)) {
            int x = 15;
            for (int z = 0; z < 16; z++) {
                generateBorder(info, canDoParks, x, z, Direction.XMAX.get(info), heightmap);
            }
        }
        if (doBorder(info, Direction.ZMIN)) {
            int z = 0;
            for (int x = 0; x < 16; x++) {
                generateBorder(info, canDoParks, x, z, Direction.ZMIN.get(info), heightmap);
            }
        }
        if (doBorder(info, Direction.ZMAX)) {
            int z = 15;
            for (int x = 0; x < 16; x++) {
                generateBorder(info, canDoParks, x, z, Direction.ZMAX.get(info), heightmap);
            }
        }
    }

    /**
     * Fill base blocks under streets to bedrock
     */
    private void fillToBedrockStreetBlock(BuildingInfo info) {
        // Base blocks below streets
        int minHeight = info.provider.getWorld().getMinY();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int y = info.getCityGroundLevel() - 1;
                getDriver().current(x, y, z);
                while (getDriver().getY() > (minHeight + info.profile.BEDROCK_LAYER) && isEmpty(getDriver().getBlock())) {
                    getDriver().block(base);
                    getDriver().decY();
                }
//                getDriver().setBlockRange(x, info.profile.BEDROCK_LAYER, z, info.getCityGroundLevel(), baseChar);
            }
        }
    }

    /**
     * Fill from a certain lowest level with base blocks until non air is hit
     */
    private void fillToGroundStreetBlock(BuildingInfo info, int lowestLevel) {
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int y = lowestLevel - 1;
                getDriver().current(x, y, z);
                while (y > 1 && getDriver().getBlock() == air) {
                    getDriver().block(base).decY();
                    y--;
                }
            }
        }
    }

    /**
     * Fill a main street block with base blocks and border blocks at the bottom
     */
    private void fillMainStreetBlock(BuildingInfo info, Character borderBlock, int offset) {
        BlockState border = info.getCompiledPalette().get(borderBlock);
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                getDriver().setBlockRange(x, info.getCityGroundLevel() - (offset - 1), z, info.getCityGroundLevel(), base);
                getDriver().current(x, info.getCityGroundLevel() - offset, z).block(border);
            }
        }
    }

    /**
     * Generate a single border column for one side of a street block
     */
    private void generateBorder(BuildingInfo info, boolean canDoParks, int x, int z, BuildingInfo adjacent, ChunkHeightmap heightmap) {
        Character borderBlock = info.getCityStyle().getBorderBlock();
        Character wallBlock = info.getCityStyle().getWallBlock();
        BlockState wall = info.getCompiledPalette().get(wallBlock);
        boolean connectsToAdjacentChunk = canDoParks && borderNeedsConnectionToAdjacentChunk(info, x, z);
        // setBlocksFromPalette() uses an exclusive upper bound. Keep the street
        // surface generated at ground level when this column is an opening.
        int borderTop = info.getCityGroundLevel() + (connectsToAdjacentChunk ? 0 : 1);

        switch (info.profile.LANDSCAPE_TYPE) {
            case DEFAULT, SPHERES -> {
                int y = getMinHeightAt(info, x, z, heightmap);
                if (y < info.getCityGroundLevel() + 1) {
                    // We are above heightmap level. Generated a border from that level to our ground level
                    setBlocksFromPalette(x, y - 1, z, borderTop, info.getCompiledPalette(), borderBlock);
                } else {
                    // We are below heightmap level. Generate a thin border anyway
                    setBlocksFromPalette(x, info.getCityGroundLevel() - 3, z, borderTop, info.getCompiledPalette(), borderBlock);
                }
            }
            case SPACE -> {
                int adjacentY = info.getCityGroundLevel() - 8;
                if (adjacent.isCity) {
                    adjacentY = Math.min(adjacentY, adjacent.getCityGroundLevel());
                } else {
                    ChunkHeightmap adjacentHeightmap = getHeightmap(adjacent.coord, provider.getWorld());
                    int minimumHeight = adjacentHeightmap.getHeight();
                    adjacentY = Math.min(adjacentY, minimumHeight - 2);
                }

                if (adjacentY > 5) {
                    setBlocksFromPalette(x, adjacentY, z, borderTop, info.getCompiledPalette(), borderBlock);
                }
            }
            case FLOATING -> {
                setBlocksFromPalette(x, info.getCityGroundLevel() - 3, z, borderTop, info.getCompiledPalette(), borderBlock);
                if (isCorner(x, z)) {
                    generateBorderSupport(info, wall, x, z, 3, heightmap);
                }
            }
            case CAVERN -> {
                setBlocksFromPalette(x, info.getCityGroundLevel() - 2, z, borderTop, info.getCompiledPalette(), borderBlock);
                if (isCorner(x, z)) {
                    generateBorderSupport(info, wall, x, z, 2, heightmap);
                }
            }
            case CAVERNSPHERES -> {
                setBlocksFromPalette(x, info.getCityGroundLevel() - 2, z, borderTop, info.getCompiledPalette(), borderBlock);
                if (isCorner(x, z)) {
                    generateBorderSupport(info, wall, x, z, 2, heightmap);
                }
            }
        }
        if (canDoParks) {
            if (!connectsToAdjacentChunk) {
                getDriver().current(x, info.getCityGroundLevel() + 1, z).block(wall);
            } else {
                getDriver().current(x, info.getCityGroundLevel() + 1, z).block(air);
            }
        }
    }

    /**
     * Generate a column of wall blocks (and stone below that in water)
     */
    private void generateBorderSupport(BuildingInfo info, BlockState wall, int x, int z, int offset, ChunkHeightmap heightmap) {
        int height = heightmap.getHeight();
        if (height > 1) {
            // None void
            int y = info.getCityGroundLevel() - offset - 1;
            getDriver().current(x, y, z);
            while (y > 1 && getDriver().getBlock() == air) {
                getDriver().block(wall).decY();
                y--;
            }
            while (y > 1 && getDriver().getBlock() == liquid) {
                getDriver().block(base).decY();
                y--;
            }
        }
    }

    private int generateFrontPart(BuildingInfo info, int height, BuildingInfo adj, Transform rot) {
        if (info.hasFrontPartFrom(adj)) {
            return generatePart(adj, adj.frontType, rot, 0, height, 0, HardAirSetting.AIR);
        }
        return height;
    }

    private static final ThreadLocal<Random> VEGETATION_RANDOM = ThreadLocal.withInitial(Random::new);

    private void generateRandomVegetation(BuildingInfo info, int height) {
        Random vegetationRandom = VEGETATION_RANDOM.get();
        vegetationRandom.setSeed(provider.getSeed() * 377 + info.coord.chunkZ() * 341873128712L + info.coord.chunkX() * 132897987541L);

        if (info.getXmin().hasBuilding) {
            for (int x = 0; x < info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS; x++) {
                for (int z = 0; z < 16; z++) {
                    getDriver().current(x, height, z);
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    // It's ok to only go down to 0 as we are not expecting to go lower then that
                    while (getDriver().getBlockDown() == air && getDriver().getY() > 0) {
                        getDriver().decY();
                    }
                    float v = Math.min(.8f, info.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS + 1 - x));
                    int cnt = 0;
                    while (vegetationRandom.nextFloat() < v && cnt < 30) {
                        getDriver().add(getRandomLeaf(info, info.getCompiledPalette()));
                        cnt++;
                    }
                }
            }
        }
        if (info.getXmax().hasBuilding) {
            for (int x = 15 - info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS; x < 15; x++) {
                for (int z = 0; z < 16; z++) {
                    getDriver().current(x, height, z);
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    // It's ok to only go down to 0 as we are not expecting to go lower then that
                    while (getDriver().getBlockDown() == air && getDriver().getY() > 0) {
                        getDriver().decY();
                    }
                    float v = Math.min(.8f, info.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (x - 14 + info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS));
                    int cnt = 0;
                    while (vegetationRandom.nextFloat() < v && cnt < 30) {
                        getDriver().add(getRandomLeaf(info, info.getCompiledPalette()));
                        cnt++;
                    }
                }
            }
        }
        if (info.getZmin().hasBuilding) {
            for (int z = 0; z < info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS; z++) {
                for (int x = 0; x < 16; x++) {
                    getDriver().current(x, height, z);
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    // It's ok to only go down to 0 as we are not expecting to go lower then that
                    while (getDriver().getBlockDown() == air && getDriver().getY() > 0) {
                        getDriver().decY();
                    }
                    float v = Math.min(.8f, info.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS + 1 - z));
                    int cnt = 0;
                    while (vegetationRandom.nextFloat() < v && cnt < 30) {
                        getDriver().add(getRandomLeaf(info, info.getCompiledPalette()));
                        cnt++;
                    }
                }
            }
        }
        if (info.getZmax().hasBuilding) {
            for (int z = 15 - info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS; z < 15; z++) {
                for (int x = 0; x < 16; x++) {
                    getDriver().current(x, height, z);
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    // It's ok to only go down to 0 as we are not expecting to go lower then that
                    while (getDriver().getBlockDown() == air && getDriver().getY() > 0) {
                        getDriver().decY();
                    }
                    float v = info.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (z - 14 + info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS);
                    int cnt = 0;
                    while (vegetationRandom.nextFloat() < v && cnt < 30) {
                        getDriver().add(getRandomLeaf(info, info.getCompiledPalette()));
                        cnt++;
                    }
                }
            }
        }
    }

    private void generateParkSection(BuildingInfo info, int height, boolean elevated) {
        BlockState b;
        boolean el00 = info.getXmin().getZmin().isElevatedParkSection();
        boolean el10 = info.getZmin().isElevatedParkSection();
        boolean el20 = info.getXmax().getZmin().isElevatedParkSection();
        boolean el01 = info.getXmin().isElevatedParkSection();
        boolean el21 = info.getXmax().isElevatedParkSection();
        boolean el02 = info.getXmin().getZmax().isElevatedParkSection();
        boolean el12 = info.getZmax().isElevatedParkSection();
        boolean el22 = info.getXmax().getZmax().isElevatedParkSection();
        CompiledPalette compiledPalette = info.getCompiledPalette();

        Character grassChar = info.getCityStyle().getGrassBlock();
        BlockState grassBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        Supplier<BlockState> grass = (grassChar == null) ? () -> grassBlock : () -> compiledPalette.get(grassChar);

        boolean parkBorder = info.getEffectiveCitySettings().parkBorder();
        char street = GenerationContext.current().street();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                if (x == 0 || x == 15 || z == 0 || z == 15) {
                    b = null;
                    if (elevated) {
                        if (x == 0 && z == 0) {
                            if (el01 && el00 && el10) {
                                b = grass.get();
                            }
                        } else if (x == 15 && z == 0) {
                            if (el21 && el20 && el10) {
                                b = grass.get();
                            }
                        } else if (x == 0 && z == 15) {
                            if (el01 && el02 && el12) {
                                b = grass.get();
                            }
                        } else if (x == 15 && z == 15) {
                            if (el12 && el22 && el21) {
                                b = grass.get();
                            }
                        } else if (x == 0) {
                            if (el01) {
                                b = grass.get();
                            }
                        } else if (x == 15) {
                            if (el21) {
                                b = grass.get();
                            }
                        } else if (z == 0) {
                            if (el10) {
                                b = grass.get();
                            }
                        } else if (z == 15) {
                            if (el12) {
                                b = grass.get();
                            }
                        }
                        if (b == null) {
                            b = parkBorder ? compiledPalette.get(street) : grass.get();
                        }
                    } else {
                        b = parkBorder ? compiledPalette.get(street) : grass.get();
                    }
                } else {
                    b = grass.get();
                }
                getDriver().current(x, height, z).block(b);
            }
        }
    }

    private void generateFullStreetSection(BuildingInfo info, int height) {
        StreetParts parts = getStreetParts(info);
        BuildingPart part = AssetRegistries.PARTS.getOrWarn(provider.getWorld(), getRandomPart(parts.full()));
        if (part != null) {
            generatePart(info, part, Transform.ROTATE_NONE, 0, height, 0, HardAirSetting.VOID);
        }
    }

    private void generateStreetSlopeSection(BuildingInfo info, int height, Direction slopeDirection) {
        StreetParts parts = getStreetParts(info);
        BuildingPart part = AssetRegistries.PARTS.getOrWarn(provider.getWorld(), getRandomPart(parts.stair()));
        if (part != null) {
            generatePart(info, part, slopeDirection.getRotation(), 0, height, 0, HardAirSetting.VOID);
        }
    }

    private void generateNormalStreetSection(BuildingInfo info, int height) {
        StreetParts parts = getStreetParts(info);
        boolean xmin = hasStreetPartConnection(info, info.getXmin(), info.getXmin().hasXBridge(provider) != null);
        boolean xmax = hasStreetPartConnection(info, info.getXmax(), info.getXmax().hasXBridge(provider) != null);
        boolean zmin = hasStreetPartConnection(info, info.getZmin(), info.getZmin().hasZBridge(provider) != null);
        boolean zmax = hasStreetPartConnection(info, info.getZmax(), info.getZmax().hasZBridge(provider) != null);
        int cnt = (xmin ? 1 : 0) + (xmax ? 1 : 0) + (zmin ? 1 : 0) + (zmax ? 1 : 0);
        Transform transform = Transform.ROTATE_NONE;
        BuildingPart part = switch (cnt) {
            case 0 -> AssetRegistries.PARTS.getOrWarn(provider.getWorld(), getRandomPart(parts.none()));
            case 1 -> {
                BuildingPart p = AssetRegistries.PARTS.getOrWarn(provider.getWorld(), getRandomPart(parts.end()));
                if (xmin) {
                } else if (xmax) {
                    transform = Transform.ROTATE_180;
                } else if (zmin) {
                    transform = Transform.ROTATE_90;
                } else {
                    transform = Transform.ROTATE_270;
                }
                yield p;
            }
            case 2 -> {
                if (xmin == xmax || zmin == zmax) {
                    BuildingPart p = AssetRegistries.PARTS.getOrWarn(provider.getWorld(), getRandomPart(parts.straight()));
                    if (xmin) {
                    } else if (xmax) {
                        transform = Transform.ROTATE_180;
                    } else if (zmin) {
                        transform = Transform.ROTATE_90;
                    } else {
                        transform = Transform.ROTATE_270;
                    }
                    yield p;
                } else {
                    BuildingPart p = AssetRegistries.PARTS.getOrWarn(provider.getWorld(), getRandomPart(parts.bend()));
                    if (xmin && zmin) {
                    } else if (xmin && zmax) {
                        transform = Transform.ROTATE_270;
                    } else if (xmax && zmin) {
                        transform = Transform.ROTATE_90;
                    } else {
                        transform = Transform.ROTATE_180;
                    }
                    yield p;
                }
            }
            case 3 -> {
                BuildingPart p = AssetRegistries.PARTS.getOrWarn(provider.getWorld(), getRandomPart(parts.t()));
                if (!xmin) {
                    transform = Transform.ROTATE_90;
                } else if (!xmax) {
                    transform = Transform.ROTATE_270;
                } else if (!zmin) {
                    transform = Transform.ROTATE_180;
                }
                yield p;
            }
            case 4 -> AssetRegistries.PARTS.getOrWarn(provider.getWorld(), getRandomPart(parts.all()));
            default -> throw new RuntimeException("Not possible!");
        };
        if (part != null) {
            generatePart(info, part, transform, 0, height, 0, HardAirSetting.VOID);
            generateMinorStreetConnectors(info, parts, height);
        }
    }

    private void generateMinorStreetConnectors(BuildingInfo info, StreetParts parts, int height) {
        if (!info.isPrimaryRoad() || parts.connector().isEmpty()) {
            return;
        }
        generateMinorStreetConnector(info, info.getXmin(), parts, height, Transform.ROTATE_NONE);
        generateMinorStreetConnector(info, info.getXmax(), parts, height, Transform.ROTATE_180);
        generateMinorStreetConnector(info, info.getZmin(), parts, height, Transform.ROTATE_90);
        generateMinorStreetConnector(info, info.getZmax(), parts, height, Transform.ROTATE_270);
    }

    private void generateMinorStreetConnector(BuildingInfo info, BuildingInfo adjacent, StreetParts parts,
                                               int height, Transform transform) {
        if (BuildingInfo.hasRoadConnection(info, adjacent) && !adjacent.isPrimaryRoad()) {
            BuildingPart connector = AssetRegistries.PARTS.getOrWarn(provider.getWorld(), getRandomPart(parts.connector()));
            if (connector != null) {
                generatePart(info, connector, transform, 0, height, 0, HardAirSetting.VOID);
            }
        }
    }

    private static StreetParts getStreetParts(BuildingInfo info) {
        return switch (info.plannedRoadType) {
            case PRIMARY -> info.getCityStyle().getLargeStreetParts();
            case TERTIARY -> info.getCityStyle().getTertiaryStreetParts();
            default -> info.getCityStyle().getStreetParts();
        };
    }

    private static boolean hasStreetPartConnection(BuildingInfo info, BuildingInfo adjacent, boolean bridgeConnection) {
        boolean roadConnection = BuildingInfo.hasRoadConnection(info, adjacent);
        if (info.isPrimaryRoad()) {
            // Minor streets still meet the primary road surface, but they must
            // not turn its quartz center line into a bend or junction.
            // A planned primary bridge is also a continuation of the primary
            // road, so its endpoint keeps the straight large-road part.
            return (roadConnection && adjacent.isPrimaryRoad()) || bridgeConnection;
        }
        return roadConnection || bridgeConnection;
    }

    private boolean borderNeedsConnectionToAdjacentChunk(BuildingInfo info, int x, int z) {
        for (Direction direction : Direction.VALUES) {
            if (direction.atSide(x, z)) {
                BuildingInfo adjacent = direction.get(info);
                if (adjacent.getStreetSlopeDirection() == direction.getOpposite()) {
                    StreetParts slopeParts = getStreetParts(adjacent);
                    if (!slopeParts.stair().isEmpty()) {
                        BuildingPart slope = AssetRegistries.PARTS.getOrWarn(provider.getWorld(), slopeParts.stair().get(0));
                        if (slope != null) {
                            Integer z1 = slope.getMetaInteger(ILostCities.META_Z_1);
                            Integer z2 = slope.getMetaInteger(ILostCities.META_Z_2);
                            if (z1 != null && z2 != null) {
                                Transform transform = direction.getOpposite().getRotation();
                                int xx1 = transform.rotateX(15, z1);
                                int zz1 = transform.rotateZ(15, z1);
                                int xx2 = transform.rotateX(15, z2);
                                int zz2 = transform.rotateZ(15, z2);
                                if (x >= Math.min(xx1, xx2) && x <= Math.max(xx1, xx2)
                                        && z >= Math.min(zz1, zz2) && z <= Math.max(zz1, zz2)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                if (adjacent.getActualStairDirection() == direction.getOpposite()) {
                    BuildingPart stairType = adjacent.stairType;
                    if (stairType != null) {
                        Integer z1 = stairType.getMetaInteger(ILostCities.META_Z_1);
                        Integer z2 = stairType.getMetaInteger(ILostCities.META_Z_2);
                        Transform transform = direction.getOpposite().getRotation();
                        int xx1 = transform.rotateX(15, z1);
                        int zz1 = transform.rotateZ(15, z1);
                        int xx2 = transform.rotateX(15, z2);
                        int zz2 = transform.rotateZ(15, z2);
                        if (x >= Math.min(xx1, xx2) && x <= Math.max(xx1, xx2) && z >= Math.min(zz1, zz2) && z <= Math.max(zz1, zz2)) {
                            return true;
                        }
                    }
                }
                if (adjacent.hasBridge(provider, direction.getOrientation()) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public enum HardAirSetting {
        AIR, WATERLEVEL, VOID
    }

    /**
     * Generate a part. If 'airWaterLevel' is true then 'hard air' blocks are replaced with water below the waterLevel.
     * Otherwise they are replaced with air.
     */
    public int generatePart(BuildingInfo info, IBuildingPart part,
                             Transform transform,
                             int ox, int oy, int oz, HardAirSetting airWaterLevel) {
        if (profile.EDITMODE) {
            EditModeData.getData().addPartData(info.coord, oy, part.getName());
        }
        CompiledPalette compiledPalette = computePalette(info, part);

        boolean nowater = part.getMetaBoolean(ILostCities.META_NOWATER);
        boolean forcedAir = part.getMetaBoolean(ILostCities.META_FORCEDAIR);

        for (int x = 0; x < part.getXSize(); x++) {
            for (int z = 0; z < part.getZSize(); z++) {
                char[] vs = part.getVSlice(x, z);
                if (vs != null) {
                    int rx = ox + transform.rotateX(x, z);
                    int rz = oz + transform.rotateZ(x, z);
                    getDriver().current(rx, oy, rz);
                    int len = vs.length;
                    for (int y = 0; y < len; y++) {
                        char c = vs[y];
                        BlockState b = compiledPalette.get(c);
                        if (b == null) {
                            throw new RuntimeException("Could not find entry '" + c + "' in the palette for part '" + part.getName() + "'!");
                        }

                        Palette.Info inf = compiledPalette.getInfo(c);

                        if (transform != Transform.ROTATE_NONE) {
                            b = transformBlockState(transform, b);
                        }

                        // We don't replace the world where the part is empty (air)
                        if (b != air) {
                            if (b == liquid) {
                                if (info.profile.AVOID_WATER) {
                                    b = air;
                                }
                            } else if (b == hardAir) {
                                if (forcedAir) {
                                    b = air;
                                } else {
                                    switch (airWaterLevel) {
                                        case AIR:
                                            b = air;
                                            break;
                                        case WATERLEVEL:
                                            if (!info.profile.AVOID_FOLIAGE && !nowater && oy + y < info.waterLevel) {
                                                b = liquid;
                                            } else {
                                                b = air;
                                            }
                                            break;
                                        case VOID:
                                            // hardAir (STRUCTURE_VOID) is replaced by whatever was already there
                                            break;
                                    }
                                }
                            } else if (inf != null) {
                                if (inf.isTorch()) {
                                    if (info.profile.GENERATE_LIGHTING) {
                                        info.addTorchTodo(getDriver().getCurrentCopy());
                                    } else {
                                        b = air;        // No torches
                                    }
                                } else if (inf.loot() != null && !inf.loot().isEmpty()) {
                                    handleLoot(info, part, provider.getWorld(), b, inf);
                                } else if (inf.mobId() != null && !inf.mobId().isEmpty()) {
                                    b = handleSpawner(info, part, oy, provider.getWorld(), rx, rz, y, b, inf);
                                } else if (inf.tag() != null) {
                                    b = handleBlockEntity(info, oy, provider.getWorld(), rx, rz, y, b, inf);
                                }
                            } else if (getStatesNeedingPoiUpdate().contains(b)) {
                                // If this block has POI data we need to delay setting it
                                BlockState finalB = b;
                                BlockPos p = getDriver().getCurrentCopy();
                                info.addPostTodo(p, () -> {
                                    if (provider.getWorld().getBlockState(p).getBlock() == Blocks.DIRT) {
                                        provider.getWorld().setBlock(p, finalB, Block.UPDATE_NONE);
                                    }
                                });
                                b = Blocks.DIRT.defaultBlockState();
                            } else if (getStatesNeedingLightingUpdate().contains(b)) {
                                updateNeeded(info, getDriver().getCurrentCopy(), Block.UPDATE_CLIENTS);
                            } else if (getStatesNeedingTodo().contains(b)) {
                                b = handleTodo(info, oy, provider.getWorld(), rx, rz, y, b);
                            }
                            getDriver().add(b);
                        } else {
                            getDriver().incY();
                        }
                    }
                }
            }
        }
        return oy + part.getSliceCount();
    }

    public CompiledPalette computePalette(BuildingInfo info, IBuildingPart part) {
        CompiledPalette compiledPalette = info.getCompiledPalette();
        // Cache the combined palette?
        Palette partPalette = part.getLocalPalette(provider.getWorld());
        if (partPalette != null) {
            compiledPalette = new CompiledPalette(compiledPalette, partPalette);
        }
        return compiledPalette;
    }

    private BlockEntityType getTypeForBlock(BlockState state) {
        return typeCache.computeIfAbsent(state.getBlock(), block -> {
            for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE.stream().toList()) {
                if (type.isValid(state)) {
                    return type;
                }
            }
            return null;
        });
    }

    private BlockState handleBlockEntity(BuildingInfo info, int oy, WorldGenLevel world, int rx, int rz, int y, BlockState b, Palette.Info inf) {
        BlockPos pos = info.getRelativePos(rx, oy + y, rz);
        BlockEntityType type = getTypeForBlock(b);
        if (type == null) {
            ModSetup.getLogger().warn("Error getting type for block: " + b.getBlock());
            return b;
        }
        CompoundTag tag = inf.tag().copy();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        tag.putString("id", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type).toString());
        world.getChunk(pos).setBlockEntityNbt(tag);
        if (b.getBlock() == Blocks.COMMAND_BLOCK) {
            info.addPostTodo(pos, () -> {
                WorldGenLevel inWorld = info.provider.getWorld();
                ((ServerChunkCache)inWorld.getChunkSource()).blockChanged(pos);
                inWorld.scheduleTick(pos, b.getBlock(), 1);
            });
        }
        return b;
    }

    private BlockState handleSpawner(BuildingInfo info, IBuildingPart part, int oy, WorldGenLevel world, int rx, int rz, int y, BlockState b, Palette.Info inf) {
        if (info.profile.GENERATE_SPAWNERS && !info.noLoot) {
            String mobid = inf.mobId();
            BlockPos pos = info.getRelativePos(rx, oy + y, rz);
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            tag.putString("id", "minecraft:mob_spawner");
            Identifier randomValue = getRandomSpawnerMob(world.getLevel(), GenerationContext.current().random(), provider, info,
                    new BuildingInfo.ConditionTodo(mobid, part.getName(), info), pos);
            CompoundTag sd = new CompoundTag();
            sd.putString("id", randomValue.toString());
            SpawnData data = new SpawnData(sd, Optional.empty(), Optional.empty());
            tag.put("SpawnData", SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, data).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData")));

            world.getChunk(pos).setBlockEntityNbt(tag);
        } else {
            b = air;
        }
        return b;
    }

    private void handleLoot(BuildingInfo info, IBuildingPart part, WorldGenLevel world, BlockState b, Palette.Info inf) {
        if (!info.noLoot) {
            BlockPos pos = getDriver().getCurrentCopy();
            info.addPostTodo(pos, () -> {
                WorldGenLevel inWorld = info.provider.getWorld();
                if (!inWorld.getBlockState(pos).isAir()) {
                    inWorld.setBlock(pos, b, Block.UPDATE_CLIENTS);
                    generateLoot(info, inWorld, pos, new BuildingInfo.ConditionTodo(inf.loot(), part.getName(), info));
                }
            });
        }
    }

    private BlockState handleTodo(BuildingInfo info, int oy, WorldGenLevel world, int rx, int rz, int y, BlockState b) {
        Block block = b.getBlock();
        boolean avoidFoliage = info.getEffectiveCitySettings().avoidFoliage();
        if (block instanceof SaplingBlock || block instanceof FlowerBlock) {
            if (avoidFoliage) {
                b = air;
            } else {
                BlockPos pos = info.getRelativePos(rx, oy + y, rz);
                if (block instanceof SaplingBlock saplingBlock) {
                    BlockState finalB = b;
                    if (Config.FORCE_SAPLING_GROWTH.get()) {
                        RandomSource forkedRand = GenerationContext.current().random().fork();
                        GlobalTodo.get(world.getLevel()).addTodo(pos, (level) -> {
                            if (level.hasChunksAt(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))
                                    && level.getBlockState(pos).getBlock() instanceof SaplingBlock) {
                                level.setBlock(pos, finalB, Block.UPDATE_CLIENTS);
                                // Use a fork so deferred growth never shares the generation context's random source
                                saplingBlock.advanceTree(level, pos, finalB, forkedRand);
                            }
                        });
                    } else {
                        info.addPostTodo(pos, () -> {
                            WorldGenLevel inWorld = info.provider.getWorld();
                            BlockState state = finalB.setValue(SaplingBlock.STAGE, 1);
                            inWorld.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);
                        });
                    }
                }
            }
        }
        return b;
    }

    private BlockState transformBlockState(Transform transform, BlockState b) {
        if (Tools.hasTag(b.getBlock(), LostTags.ROTATABLE_TAG)) {
            b = b.rotate(transform.getMcRotation());
        } else if (getRailStates().contains(b)) {
            EnumProperty<RailShape> shapeProperty;
            if (b.getBlock() == Blocks.RAIL) {
                shapeProperty = RailBlock.SHAPE;
            } else if (b.getBlock() == Blocks.POWERED_RAIL) {
                shapeProperty = PoweredRailBlock.SHAPE;
            } else {
                throw new RuntimeException("Error with rail!");
            }
            RailShape shape = b.getValue(shapeProperty);
            b = b.setValue(shapeProperty, transform.transform(shape));
        }
        return b;
    }


    public static Identifier getRandomSpawnerMob(Level world, RandomSource random, IDimensionInfo diminfo, BuildingInfo info, BuildingInfo.ConditionTodo todo, BlockPos pos) {
        String condition = todo.getCondition();
        Condition cnd = AssetRegistries.CONDITIONS.getOrThrow(world, condition);
        int level = (pos.getY() - diminfo.getProfile().GROUNDLEVEL) / FLOORHEIGHT;
        int floor = (pos.getY() - info.getCityGroundLevel()) / FLOORHEIGHT;
        String belowFloor = "<none>";
        ConditionContext conditionContext = new ConditionContext(level, floor, info.cellars, info.getNumFloors(),
                todo.getPart(), belowFloor, todo.getBuilding(), info.coord) {
            @Override
            public boolean isSphere() {
                return CitySphere.isInSphere(info.coord, pos, diminfo);
            }

            @Override
            public Identifier getBiome() {
                return world.getBiome(pos).unwrap().map(ResourceKey::identifier, biome -> world.registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome));
            }
        };
        String randomValue = cnd.getRandomValue(random, conditionContext);
        if (randomValue == null) {
            throw new RuntimeException("Condition '" + cnd.getName() + "' did not return a valid mob!");
        }
        return Identifier.parse(randomValue);
    }


    private void generateLoot(BuildingInfo info, LevelAccessor world, BlockPos pos, BuildingInfo.ConditionTodo condition) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof RandomizableContainerBlockEntity) {
            if (this.provider.getProfile().GENERATE_LOOT) {
                createLoot(info, GenerationContext.current().random(), world, pos, condition, this.provider);
            }
        } else if (te == null) {
            ModSetup.getLogger().error("Error setting loot at {},{},{}", pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public static void createLoot(BuildingInfo info, RandomSource random, LevelAccessor world, BlockPos pos, BuildingInfo.ConditionTodo todo, IDimensionInfo diminfo) {
        if (random.nextFloat() < diminfo.getProfile().CHEST_WITHOUT_LOOT_CHANCE) {
            return;
        }
        BlockEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof RandomizableContainerBlockEntity rcbe) {
            if (todo != null) {
                String lootTable = todo.getCondition();
                int level = (pos.getY() - diminfo.getProfile().GROUNDLEVEL) / FLOORHEIGHT;
                int floor = (pos.getY() - info.getCityGroundLevel()) / FLOORHEIGHT;
                ConditionContext conditionContext = new ConditionContext(level, floor, info.cellars, info.getNumFloors(),
                        todo.getPart(), "<none>", todo.getBuilding(), info.coord) {
                    @Override
                    public boolean isSphere() {
                        return CitySphere.isInSphere(info.coord, pos, diminfo);
                    }

                    @Override
                    public Identifier getBiome() {
                        return world.getBiome(pos).unwrap().map(ResourceKey::identifier, biome -> world.registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome));
                    }
                };
                String randomValue = AssetRegistries.CONDITIONS.getOrThrow(world, lootTable).getRandomValue(random, conditionContext);
//                ((LockableLootTileEntity) tileentity).setLootTable(Identifier.fromNamespaceAndPath(randomValue), random.nextLong());
//                tileentity.markDirty();
//                if (LostCityConfiguration.DEBUG) {
//                    LostCities.setup.getLogger().debug("createLootChest: loot=" + randomValue + " pos=" + pos.toString());
//                }
                rcbe.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(randomValue)));
            }
        }
    }


    private void generateDebris(BuildingInfo info) {
        generateDebrisFromChunk(info, info.getXmin(), (xx, zz) -> (15.0f - xx) / 16.0f);
        generateDebrisFromChunk(info, info.getXmax(), (xx, zz) -> xx / 16.0f);
        generateDebrisFromChunk(info, info.getZmin(), (xx, zz) -> (15.0f - zz) / 16.0f);
        generateDebrisFromChunk(info, info.getZmax(), (xx, zz) -> zz / 16.0f);
        generateDebrisFromChunk(info, info.getXmin().getZmin(), (xx, zz) -> ((15.0f - xx) * (15.0f - zz)) / 256.0f);
        generateDebrisFromChunk(info, info.getXmax().getZmax(), (xx, zz) -> (xx * zz) / 256.0f);
        generateDebrisFromChunk(info, info.getXmin().getZmax(), (xx, zz) -> ((15.0f - xx) * zz) / 256.0f);
        generateDebrisFromChunk(info, info.getXmax().getZmin(), (xx, zz) -> (xx * (15.0f - zz)) / 256.0f);
    }

    private void generateDebrisFromChunk(BuildingInfo info, BuildingInfo adjacentInfo, BiFunction<Integer, Integer, Float> locationFactor) {
        if (adjacentInfo.hasBuilding) {
            CompiledPalette adjacentPalette = adjacentInfo.getCompiledPalette();
            Character rubbleBlock = adjacentInfo.getBuilding().getRubbleBlock();
            if (!adjacentPalette.isDefined(rubbleBlock)) {
                rubbleBlock = adjacentInfo.getBuilding().getFillerBlock();
            }
            float damageFactor = adjacentInfo.getDamageArea().getDamageFactor();
            if (damageFactor > .5f) {
                // An estimate of the amount of blocks
                int blocks = (1 + adjacentInfo.getNumFloors()) * 1000;
                float damage = Math.max(1.0f, damageFactor * DamageArea.BLOCK_DAMAGE_CHANCE);
                int destroyedBlocks = (int) (blocks * damage);
                // How many go this direction (approx, based on cardinal directions from building as well as number that simply fall down)
                destroyedBlocks /= info.profile.DEBRIS_TO_NEARBYCHUNK_FACTOR;
                int h = adjacentInfo.getMaxHeight() + 10;
                int maxBuildHeight = info.provider.getWorld().getMaxY() + 1;
                if (h > maxBuildHeight - 1) {
                    int minBuildHeight = info.provider.getWorld().getMinY();
                    h = minBuildHeight - 1;
                }

                CompiledPalette palette = info.getCompiledPalette();
                BlockState ironbarsState = Blocks.IRON_BARS.defaultBlockState();
                Character infobarsChar = info.getCityStyle().getIronbarsBlock();
                Supplier<BlockState> ironbars = infobarsChar == null ? () -> ironbarsState : () -> palette.get(infobarsChar);
                RandomSource random = GenerationContext.current().random();

                for (int i = 0; i < destroyedBlocks; i++) {
                    int x = random.nextInt(16);
                    int z = random.nextInt(16);
                    if (random.nextFloat() < locationFactor.apply(x, z)) {
                        getDriver().current(x, h, z);
                        while (h > 0 && isEmpty(getDriver().getBlock())) {
                            h--;
                            getDriver().decY();
                        }
                        // Fix for FLOATING // @todo!
                        BlockState b;
                        if (random.nextInt(5) == 0) {
                            b = ironbars.get();
                        } else {
                            b = adjacentPalette.get(rubbleBlock);     // Filler from adjacent building
                        }
                        getDriver().current(x, h + 1, z).block(b);
                    }
                }
            }
        }
    }

    private boolean doBorder(BuildingInfo info, Direction direction) {
        BuildingInfo adjacent = direction.get(info);
        if (isHigherThenNearbyStreetChunk(info, adjacent)) {
            return true;
        } else if (!adjacent.isCity) {
            if (adjacent.cityLevel <= info.cityLevel) {
                return true;
            }
            if (info.profile.isSpace()) {
                // Base it on ground level
                ChunkHeightmap adjacentHeightmap = getHeightmap(adjacent.coord, provider.getWorld());
                int adjacentHeight = adjacentHeightmap.getHeight();
                if (adjacentHeight > 5) {
                    if ((adjacentHeight - 4) < info.getCityGroundLevel()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isHigherThenNearbyStreetChunk(BuildingInfo info, BuildingInfo adjacent) {
        if (!adjacent.isCity) {
            return false;
        }
        if (adjacent.hasBuilding) {
            return adjacent.cityLevel + adjacent.getNumFloors() < info.cityLevel;
        } else {
            return adjacent.cityLevel < info.cityLevel;
        }
    }

    private void setBlocksFromPalette(int x, int y, int z, int y2, CompiledPalette palette, char character) {
        if (palette.isSimple(character)) {
            BlockState b = palette.get(character);
            getDriver().setBlockRange(x, y, z, y2, b);
        } else {
            getDriver().current(x, y, z);
            while (y < y2) {
                getDriver().add(palette.get(character));
                y++;
            }
        }
    }

    private void generateBuilding(BuildingInfo info, ChunkHeightmap heightmap, ChunkAccess chunk) {
        int lowestLevel = info.getBuildingBottomHeight();
        int cellars = info.cellars;
        int floors = info.getNumFloors();
        int max = info.provider.getWorld().getMaxY() - 1 - FLOORHEIGHT;

        CompiledPalette palette = info.getCompiledPalette();
        makeRoomForBuilding(info, lowestLevel, heightmap, palette);

        char fillerBlock = info.getBuilding().getFillerBlock();
        List<Pair<Integer, BuildingPart>> part2Map = new ArrayList<>();

        int height = lowestLevel;
        for (int f = -cellars; f <= floors; f++) {
            // In default landscape type we clear the landscape on top of the building when we are at the top floor
            if (f == floors) {
                if (profile.isDefault() || profile.isVoidSpheres()) {
                    clearToMax(info, heightmap, height, max);
                }
            }

            BuildingPart part = info.getFloor(f);
            generatePart(info, part, Transform.ROTATE_NONE, 0, height, 0, HardAirSetting.AIR);
            part = info.getFloorPart2(f);
            if (part != null) {
                part2Map.add(Pair.of(height, part));
            }

            // Check for doors
            boolean isTop = f == floors;   // The top does not need generated doors
            if (!isTop && info.getAllowDoors()) {
                Doors.generateDoors(this, info, height + 1, f);
            }

            height += FLOORHEIGHT;    // We currently only support 6 here
        }

        if (cellars > 0 && info.getAllowFillers()) {
            // Underground we replace the glass with the filler
            for (int x = 0; x < 16; x++) {
                // Use safe version because this may end up being lower
                setBlocksFromPalette(x, lowestLevel, 0, Math.min(info.getCityGroundLevel(), info.getZmin().getCityGroundLevel()) + 1, palette, fillerBlock);
                setBlocksFromPalette(x, lowestLevel, 15, Math.min(info.getCityGroundLevel(), info.getZmax().getCityGroundLevel()) + 1, palette, fillerBlock);
            }
            for (int z = 1; z < 15; z++) {
                setBlocksFromPalette(0, lowestLevel, z, Math.min(info.getCityGroundLevel(), info.getXmin().getCityGroundLevel()) + 1, palette, fillerBlock);
                setBlocksFromPalette(15, lowestLevel, z, Math.min(info.getCityGroundLevel(), info.getXmax().getCityGroundLevel()) + 1, palette, fillerBlock);
            }
        }

        if (cellars >= 1) {
            // We have to potentially connect to corridors
            Corridors.generateCorridorConnections(getDriver(), info);
        }

        if (!part2Map.isEmpty()) {
            getDriver().actuallyGenerate(chunk);
            for (Pair<Integer, BuildingPart> entry : part2Map) {
                int h = entry.getKey();
                BuildingPart part = entry.getValue();
                generatePart(info, part, Transform.ROTATE_NONE, 0, h, 0, HardAirSetting.AIR);
            }
        }
    }

    /*
     * Make sure the space for the building is cleared and everything below the building is ok
     */
    private void makeRoomForBuilding(BuildingInfo info, int lowestLevel, ChunkHeightmap heightmap, CompiledPalette palette) {
        char borderBlock = info.getCityStyle().getBorderBlock();
        char fillerBlock = info.getBuilding().getFillerBlock();

        if (info.profile.isFloating()) {
            // For floating worldgen we try to fit the underside of the building better with the island
            // We also remove all blocks from the inside because we generate buildings on top of
            // generated chunks as opposed to blank chunks with non-floating worlds
            GenerationContext context = GenerationContext.current();
            double[] bottomLayerBuffer = this.bottomLayerNoise.getRegion(context.bottomLayerBuffer(), (info.coord.chunkX() << 4), (info.coord.chunkZ() << 4), 16, 16, 8.0 / 16.0, 8.0 / 16.0, 1.0D);
            context.setBottomLayerBuffer(bottomLayerBuffer);
            int minBuildHeight = info.provider.getWorld().getMinY();
            int maxBuildHeight = info.provider.getWorld().getMaxY() + 1;
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    double vr = bottomLayerBuffer[x + z * 16] / 4.0f;
                    getDriver().current(x, maxBuildHeight - 1, z);
                    int minHeight = minBuildHeight;
                    int lowestToFill = Math.max(minHeight, lowestLevel - 6 - (int) vr);
                    while (getDriver().getBlock() == air && getDriver().getY() > lowestToFill) {
                        getDriver().decY();
                    }

                    int height = getDriver().getY();//heightmap.getHeight(x, z);
                    if (height > minHeight + 1 && height < lowestLevel - 1) {
                        getDriver().setBlockRange(x, height + 1, z, lowestLevel, base);
                    }
                    // Also clear the inside of buildings to avoid geometry that doesn't really belong there
                    clearRange(info, x, z, lowestLevel, info.getCityGroundLevel() + info.getNumFloors() * FLOORHEIGHT, info.waterLevel > info.groundLevel);
                }
            }
        } else if (info.profile.isSpace()) {
            fillToGround(info, lowestLevel, borderBlock);
            // Also clear the inside of buildings to avoid geometry that doesn't really belong there
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    clearRange(info, x, z, lowestLevel, info.getCityGroundLevel() + info.getNumFloors() * FLOORHEIGHT, false);     // Never water in bubbles?
                }
            }
        } else if (info.profile.isCavern()) {
            // For normal cavern we have a thin layer of 'border' blocks because that looks nicer
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    if (isSide(x, z)) {
                        setBlocksFromPalette(x, lowestLevel - 10, z, lowestLevel, palette, borderBlock);
                    }
                    if (getDriver().getBlock(x, lowestLevel, z) == air) {
                        BlockState filler = palette.get(fillerBlock);
                        getDriver().current(x, lowestLevel, z).block(filler); // There is nothing below so we fill this with the filler
                    }

                    // Also clear the inside of buildings to avoid geometry that doesn't really belong there
                    clearRange(info, x, z, lowestLevel, info.getCityGroundLevel() + info.getNumFloors() * FLOORHEIGHT, info.waterLevel > info.groundLevel);
                }
            }
        } else { // (also for spheres)
            // For normal worldgen we have a thin layer of 'border' blocks because that looks nicer
            // We try to avoid this layer in big caves though
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    if (isSide(x, z)) {
                        int y = getMinHeightAt(info, x, z, heightmap);
                        if (y >= lowestLevel) {
                            // The building generates below heightmap height. So we generate a border of 3 only
                            y = lowestLevel - 3;
                        }
                        setBlocksFromPalette(x, y, z, lowestLevel, palette, borderBlock);
                    }
                    if (getDriver().getBlock(x, lowestLevel, z) == air) {
                        BlockState filler = palette.get(fillerBlock);
                        getDriver().current(x, lowestLevel, z).block(filler); // There is nothing below so we fill this with the filler
                    }

                    // Also clear the inside of buildings to avoid geometry that doesn't really belong there
                    clearRange(info, x, z, lowestLevel, info.getCityGroundLevel() + info.getNumFloors() * FLOORHEIGHT, info.waterLevel > info.groundLevel);
                }
            }
        }
    }

    private void clearToMax(BuildingInfo info, ChunkHeightmap heightmap, int height, int max) {
        int maximumHeight = Math.min(max, heightmap.getHeight() + 10);
        if (height < maximumHeight) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    clearRange(info, x, z, height, maximumHeight, false);
                }
            }
        }
    }

    // Used for space type worlds: fill underside the building/street until a block is encountered
    public void fillToGround(BuildingInfo info, int lowestLevel, Character borderBlock) {
        int deepestY = Math.max(1, lowestLevel - 10);
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int y = lowestLevel - 1;
                getDriver().current(x, y, z);
                if (isSide(x, z)) {
                    while (y > deepestY && getDriver().getBlock() == air) {
                        getDriver().block(info.getCompiledPalette().get(borderBlock)).decY();
                        y--;
                    }
                } else {
                    while (y > deepestY && getDriver().getBlock() == air) {
                        getDriver().block(base).decY();
                        y--;
                    }
                }
            }
        }
    }

    private static boolean isSide(int x, int z) {
        return x == 0 || x == 15 || z == 0 || z == 15;
    }

    private static boolean isCorner(int x, int z) {
        return (x == 0 || x == 15) && (z == 0 || z == 15);
    }

    public static void updateNeeded(BuildingInfo info, BlockPos pos, int flags) {
        info.addPostTodo(pos, () -> {
            WorldGenLevel world = info.provider.getWorld();
            BlockState state = world.getBlockState(pos);
            if (!state.isAir()) {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), flags);
                world.setBlock(pos, state, flags);
            }
        });
    }

}
