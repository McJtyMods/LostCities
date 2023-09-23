package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.api.ILostCities;
import mcjty.lostcities.api.LostCityEvent;
import mcjty.lostcities.api.RailChunkType;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.editor.EditModeData;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.setup.ModSetup;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.NoiseGeneratorPerlin;
import mcjty.lostcities.varia.QualityRandom;
import mcjty.lostcities.varia.Tools;
import mcjty.lostcities.worldgen.lost.*;
import mcjty.lostcities.worldgen.lost.cityassets.*;
import mcjty.lostcities.worldgen.lost.regassets.data.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LostCityTerrainFeature {

    public static final int FLOORHEIGHT = 6;

    private static int gSeed = 123456789;
    private final int mainGroundLevel;
    private final BlockState air;
    private final BlockState hardAir;

    private BlockState base = null;
    private BlockState liquid;

    private Set<BlockState> railStates = null;
    private Set<BlockState> statesNeedingTodo = null;
    private Set<BlockState> statesNeedingLightingUpdate = null;

    private char street;
    private char streetBase;
    private char street2;
    private int streetBorder;

    private final NoiseGeneratorPerlin rubbleNoise;
    private final NoiseGeneratorPerlin leavesNoise;
    private final NoiseGeneratorPerlin ruinNoise;

    private BlockState[] randomLeafs = null;
    private BlockState[] randomDirt = null;
    private Set<BlockState> randomDirtSet = null;

    private final ChunkDriver driver;

    private final IDimensionInfo provider;
    private final LostCityProfile profile;
    private final RandomSource rand;

    private final Map<ChunkCoord, ChunkHeightmap> cachedHeightmaps = new HashMap<>();

    public LostCityTerrainFeature(IDimensionInfo provider, LostCityProfile profile, RandomSource rand) {
        this.provider = provider;
        this.profile = profile;
        this.rand = rand;
        driver = new ChunkDriver();
        this.mainGroundLevel = profile.GROUNDLEVEL;
        int waterLevel = provider.getWorld() == null ? 65 : Tools.getSeaLevel(provider.getWorld());// profile.GROUNDLEVEL - profile.WATERLEVEL_OFFSET;
        this.rubbleNoise = new NoiseGeneratorPerlin(rand, 4);
        this.leavesNoise = new NoiseGeneratorPerlin(rand, 4);
        this.ruinNoise = new NoiseGeneratorPerlin(rand, 4);

        air = Blocks.AIR.defaultBlockState();
        hardAir = Blocks.STRUCTURE_VOID.defaultBlockState();

//        islandTerrainGenerator.setup(provider.getWorld().getWorld(), provider);
//        cavernTerrainGenerator.setup(provider.getWorld().getWorld(), provider);
//        spaceTerrainGenerator.setup(provider.getWorld().getWorld(), provider);
    }

    private BlockState getRandomLeaf(BuildingInfo info, CompiledPalette compiledPalette) {
        Character leavesBlock = info.getCityStyle().getLeavesBlock();
        if (leavesBlock != null) {
            return compiledPalette.get(leavesBlock);
        }
        if (randomLeafs == null) {
            BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
            BlockState leaves2 = Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
            BlockState leaves3 = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

            randomLeafs = new BlockState[128];
            int i = 0;
            while (i < 20) {
                randomLeafs[i] = leaves2;
                i++;
            }
            while (i < 40) {
                randomLeafs[i] = leaves3;
                i++;
            }
            while (i < randomLeafs.length) {
                randomLeafs[i] = leaves;
                i++;
            }
        }
        return randomLeafs[fastrand128()];
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
        if (randomDirt == null) {
            randomDirtSet = new HashSet<>();
            BlockState mBricks = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            BlockState mCobble = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            BlockState moss = Blocks.MOSS_BLOCK.defaultBlockState();
            randomDirtSet.add(mBricks);
            randomDirtSet.add(mCobble);
            randomDirtSet.add(moss);

            randomDirt = new BlockState[128];
            int i = 0;
            while (i < 20) {
                randomDirt[i] = mBricks;
                i++;
            }
            while (i < 60) {
                randomDirt[i] = mCobble;
                i++;
            }
            while (i < randomDirt.length) {
                randomDirt[i] = moss;
                i++;
            }
        }
        return randomDirt[fastrand128()];
    }

    private Set<BlockState> getRailStates() {
        if (railStates == null) {
            railStates = new HashSet<>();
            addStates(Blocks.RAIL, railStates);
            addStates(Blocks.POWERED_RAIL, railStates);
        }
        return railStates;
    }

    private Set<BlockState> getStatesNeedingTodo() {
        if (statesNeedingTodo == null) {
            statesNeedingTodo = new HashSet<>();
            for (Holder<Block> bh : Tools.getBlocksForTag(BlockTags.SAPLINGS)) {
                addStates(bh.value(), statesNeedingTodo);
            }
            for (Holder<Block> bh : Tools.getBlocksForTag(BlockTags.SMALL_FLOWERS)) {
                addStates(bh.value(), statesNeedingTodo);
            }
        }
        return statesNeedingTodo;
    }

    private Set<BlockState> getStatesNeedingLightingUpdate() {
        if (statesNeedingLightingUpdate == null) {
            statesNeedingLightingUpdate = new HashSet<>();
            for (Holder<Block> bh : Tools.getBlocksForTag(LostTags.LIGHTS_TAG)) {
                addStates(bh.value(), statesNeedingLightingUpdate);
            }
        }
        return statesNeedingLightingUpdate;
    }

    private static void addStates(Block block, Set<BlockState> set) {
        set.addAll(block.getStateDefinition().getPossibleStates());
    }

    public void setupStates(LostCityProfile profile) {
        if (base == null) {
            base = profile.getBaseBlock();
            liquid = profile.getLiquidBlock();
        }
    }

    private static int fastrand() {
        gSeed = (214013 * gSeed + 2531011);
        return (gSeed >> 16) & 0x7FFF;
    }

    public static int fastrand128() {
        gSeed = (214013 * gSeed + 2531011);
        return (gSeed >> 16) & 0x7F;
    }

    private boolean isVoid(int x, int z) {
        driver.current(x, 255, z);
        int minHeight = provider.getWorld().getMinBuildHeight();
        while (driver.getBlock() == air && driver.getY() > minHeight) {
            driver.decY();
        }
        return driver.getY() == minHeight;
    }

//    public static long startTime = -1;
//    public static long chunksDone = 0;

    public void generate(WorldGenRegion region, ChunkAccess chunk) {
//        if (chunksDone == 0) {
//            startTime = System.currentTimeMillis();
//        } else if (chunksDone % 100 == 0) {
//            System.out.println("At " + chunksDone + " elapsed = " + (System.currentTimeMillis()-startTime));
//        }
//        chunksDone++;
        LevelAccessor oldRegion = driver.getRegion();
        ChunkAccess oldChunk = driver.getPrimer();
        driver.setPrimer(region, chunk);

        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);

        // @todo this setup is not very clean
        CityStyle cityStyle = info.getCityStyle();

        street = cityStyle.getStreetBlock();//info.getCompiledPalette().get(cityStyle.getStreetBlock());
        streetBase = cityStyle.getStreetBaseBlock();
        street2 = cityStyle.getStreetVariantBlock();
        streetBorder = (16 - cityStyle.getStreetWidth()) / 2;

        boolean doCity = info.isCity || (info.outsideChunk && info.hasBuilding);

        // Check if there is no village here
        ChunkAccess ch = region.getChunk(chunkX, chunkZ);
        doCity = doCity && !hasVillage(ch);

        // If this chunk has a building or street but we're in a floating profile and
        // we happen to have a void chunk we detect that here and go back to normal chunk generation
        // anyway
        if (doCity && provider.getProfile().CITY_AVOID_VOID && provider.getProfile().isFloating()) {
            boolean v = isVoid(2, 2) || isVoid(2, 14) || isVoid(14, 2) || isVoid(14, 14) || isVoid(8, 8);
            doCity = !v;
        }

        if (doCity) {
            doCityChunk(chunkX, chunkZ, info);
        } else {
            // We already have a prefilled core chunk (as generated from doCoreChunk)
            doNormalChunk(chunkX, chunkZ, info);
        }

        if (profile.isSpace() || profile.isSpheres()) {
            if (CitySphere.isCitySphereCenter(chunkX, chunkZ, provider)) {
                CitySphereSettings settings = provider.getWorldStyle().getCitysphereSettings();
                if (settings != null && settings.getCenterpart() != null) {
                    BuildingPart part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), settings.getCenterpart());
                    int offset = settings.getCenterPartOffset();
                    int partY = switch (settings.getCenterPartOrigin()) {
                        case FIXED -> 0;
                        case CENTER -> CitySphere.getCitySphere(chunkX, chunkZ, provider).getCenterPos().getY();
                        case FIRSTFLOOR -> info.getCityGroundLevel();
                        case GROUND -> info.groundLevel;
                        case TOP -> getTopLevel(info);
                    };
                    partY += offset;
                    generatePart(info, part, Transform.ROTATE_NONE, 0, partY, 0, true);
                }
            }
        }

        Railway.RailChunkInfo railInfo = info.getRailInfo();
        if (railInfo.getType() != RailChunkType.NONE) {
            generateRailways(info, railInfo);
        }
        generateRailwayDungeons(info);

        if (profile.isSpace()) {
            generateMonorails(info);
        }

        fixTorches(info);

        // We make a new random here because the primer for a normal chunk may have
        // been cached and we want to be able to do the same when returning from a cached
        // primer vs generating it here
        rand.setSeed(chunkX * 257017164707L + chunkZ * 101754694003L);

        LostCityEvent.PreExplosionEvent event = new LostCityEvent.PreExplosionEvent(provider.getWorld(), LostCities.lostCitiesImp, chunkX, chunkZ, driver.getPrimer());
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            if (info.getDamageArea().hasExplosions()) {
                breakBlocksForDamageNew(chunkX, chunkZ, info);
                fixAfterExplosion(info);
            }
            generateDebris(info);
        }

        driver.actuallyGenerate(chunk);
        driver.setPrimer(oldRegion, oldChunk);
        ChunkFixer.fix(provider, chunkX, chunkZ);
    }

    private int getTopLevel(BuildingInfo info) {
        if (info.hasBuilding) {
            return info.getCityGroundLevel() + info.getNumFloors() * FLOORHEIGHT;
        } else {
            return info.getCityGroundLevel();
        }
    }

    public void generateSpheres(WorldGenRegion region, ChunkAccess chunk) {
        // Do the city spheres
        if (profile.isSpace() || profile.isSpheres()) {
            LevelAccessor oldRegion = driver.getRegion();
            ChunkAccess oldChunk = driver.getPrimer();
            driver.setPrimer(region, chunk);

            int chunkX = chunk.getPos().x;
            int chunkZ = chunk.getPos().z;

            CitySphere sphere = CitySphere.getCitySphere(chunkX, chunkZ, provider);
            CitySphere.initSphere(sphere, provider);   // Make sure city sphere information is complete
            if (sphere.isEnabled()) {
                float radius = sphere.getRadius();
                BlockPos cc = sphere.getCenterPos();
                int cx = cc.getX() - chunkX * 16;
                int cz = cc.getZ() - chunkZ * 16;
                fillSphere(cx, profile.GROUNDLEVEL, cz, (int) radius, sphere.getGlassBlock(), sphere.getSideBlock());
            }

            driver.actuallyGenerate(chunk);
            driver.setPrimer(oldRegion, oldChunk);
            ChunkFixer.fix(provider, chunkX, chunkZ);
        }
    }

    private boolean hasVillage(ChunkAccess ch) {
        if (ch.hasAnyStructureReferences()) {
            var structures = provider.getWorld().registryAccess().registryOrThrow(Registries.STRUCTURE);
            var references = ch.getAllReferences();
            // @todo we can do this more optimally if we first find all configured structures for village
            for (var entry : references.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    Optional<ResourceKey<Structure>> key = structures.getResourceKey(entry.getKey());
                    if (key.map(k -> structures.getHolderOrThrow(k).is(StructureTags.VILLAGE)).orElse(false)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void fillSphere(int centerx, int centery, int centerz, int radius,
                            BlockState glass, BlockState sideBlock) {
        double sqradius = radius * radius;
        double sqradiusOffset = (radius-2) * (radius-2);

        int minY = Math.max(provider.getWorld().getMinBuildHeight(), centery-radius-1);
        int maxY = Math.min(provider.getWorld().getMaxBuildHeight(), centery+radius+1);

        for (int x = 0 ; x < 16 ; x++) {
            double dxdx = (x - centerx) * (x - centerx);
            for (int z = 0; z < 16; z++) {
                double dzdz = (z - centerz) * (z - centerz);
                if (dxdx + dzdz <= sqradius) {
                    driver.current(x, minY, z);
                    for (int y = minY; y <= centery; y++) {
                        double dydy = (y - centery) * (y - centery);
                        double sqdist = dxdx + dydy + dzdz;
                        if (sqdist <= sqradius && sqdist >= sqradiusOffset) {
                            driver.block(sideBlock);
                        }
                        driver.incY();
                    }
                    for (int y = centery + 1; y < maxY; y++) {
                        double dydy = (y - centery) * (y - centery);
                        double sqdist = dxdx + dydy + dzdz;
                        if (sqdist <= sqradius) {
                            if (sqdist >= sqradiusOffset) {
                                driver.block(glass);
                            }
                        } else {
                            // Optionally clear above the sphere
                            if (profile.CITYSPHERE_CLEARABOVE > 0) {
                                maxY = Math.min(provider.getWorld().getMaxBuildHeight(), y + profile.CITYSPHERE_CLEARABOVE);
                                int yy = y;
                                while (yy <= maxY) {
                                    driver.block(air);
                                    driver.incY();
                                    yy++;
                                }
                            }
                            break;
                        }
                        driver.incY();
                    }
                }
            }
        }
    }


    private void generateMonorails(BuildingInfo info) {
        MonorailParts monoRailParts = provider.getWorldStyle().getPartSelector().monoRailParts();
        Transform transform;
        boolean horiz = info.hasHorizontalMonorail();
        boolean vert = info.hasVerticalMonorail();
        if (horiz && vert) {
            if (!CitySphere.intersectsWithCitySphere(info.chunkX, info.chunkZ, provider)) {
                BuildingPart part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.both());
                generatePart(info, part, Transform.ROTATE_NONE, 0, mainGroundLevel + info.profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, 0, true);
            }
            return;
        } else if (horiz) {
            transform = Transform.ROTATE_90;
        } else if (vert) {
            transform = Transform.ROTATE_NONE;
        } else {
            return;
        }
        BuildingPart part;

        if (CitySphere.fullyInsideCitySpere(info.chunkX, info.chunkZ, provider)) {
            // If there is a non enclosed monorail nearby we generate a station
            if (hasNonStationMonoRail(info.getXmin())) {
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.station());
                Character borderBlock = info.getCityStyle().getBorderBlock();
                transform = Transform.MIRROR_90_X; // flip
                fillToGround(info, mainGroundLevel + info.profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, borderBlock);
            } else if (hasNonStationMonoRail(info.getXmax())) {
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.station());
                Character borderBlock = info.getCityStyle().getBorderBlock();
                transform = Transform.ROTATE_90;
                fillToGround(info, mainGroundLevel + info.profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, borderBlock);
            } else if (hasNonStationMonoRail(info.getZmin())) {
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.station());
                Character borderBlock = info.getCityStyle().getBorderBlock();
                transform = Transform.ROTATE_NONE;
                fillToGround(info, mainGroundLevel + info.profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, borderBlock);
            } else if (hasNonStationMonoRail(info.getZmax())) {
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.station());
                Character borderBlock = info.getCityStyle().getBorderBlock();
                transform = Transform.MIRROR_Z; // flip
                fillToGround(info, mainGroundLevel + info.profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, borderBlock);
            } else {
                return;
            }
        } else {
            part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), monoRailParts.vertical());
        }

        generatePart(info, part, transform, 0, mainGroundLevel + info.profile.CITYSPHERE_MONORAIL_HEIGHT_OFFSET, 0, true);
    }

    private boolean hasNonStationMonoRail(BuildingInfo info) {
        return info.hasMonorail() && !CitySphere.fullyInsideCitySpere(info.chunkX, info.chunkZ, provider);
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
            driver.currentAbsolute(pos);
            if (driver.getBlockDown() != air) {
                driver.block(Blocks.TORCH.defaultBlockState());
            } else if (x > 0 && driver.getBlockWest() != air) {
                driver.block(torchState.setValue(WallTorchBlock.FACING, net.minecraft.core.Direction.EAST));
            } else if (x < 15 && driver.getBlockEast() != air) {
                driver.block(torchState.setValue(WallTorchBlock.FACING, net.minecraft.core.Direction.WEST));
            } else if (z > 0 && driver.getBlockNorth() != air) {
                driver.block(torchState.setValue(WallTorchBlock.FACING, net.minecraft.core.Direction.SOUTH));
            } else if (z < 15 && driver.getBlockSouth() != air) {
                driver.block(torchState.setValue(WallTorchBlock.FACING, net.minecraft.core.Direction.NORTH));
            }
            updateNeeded(info, pos);
        }
        info.clearTorchTodo();
    }

    private void doNormalChunk(int chunkX, int chunkZ, BuildingInfo info) {
//        debugClearChunk(chunkX, chunkZ, primer);
        if (profile.isDefault() || profile.isSpheres()) {
            correctTerrainShape(chunkX, chunkZ);
//            flattenChunkToCityBorder(chunkX, chunkZ);
        }

        LostCityEvent.PostGenOutsideChunkEvent postevent = new LostCityEvent.PostGenOutsideChunkEvent(provider.getWorld(), LostCities.lostCitiesImp, chunkX, chunkZ, driver.getPrimer());
        MinecraftForge.EVENT_BUS.post(postevent);

        generateBridges(info);
        generateHighways(chunkX, chunkZ, info);

        ScatteredSettings scatteredSettings = provider.getWorldStyle().getScatteredSettings();
        if (scatteredSettings != null) {
            if (avoidScattered(info)) {
                generateScattered(info, scatteredSettings);
            }
        }
    }

    private boolean avoidScattered(BuildingInfo info) {
        return !(info.isCity || info.hasBridge(provider) || Highway.hasHighway(info.chunkX, info.chunkZ, provider, profile));
    }

    private void generateScattered(BuildingInfo info, ScatteredSettings scatteredSettings) {
        int chunkX = info.chunkX;
        int chunkZ = info.chunkZ;

        // First normalize the coordinates to scatter area sized coordinates. Add a large amount to make sure the coordinates are positive
        int ax = (chunkX + 2000000) / scatteredSettings.getAreasize();
        int az = (chunkZ + 2000000) / scatteredSettings.getAreasize();

        QualityRandom scatteredRandom = new QualityRandom(provider.getSeed() + ax * 5564338337L + az * 25564337621L);
        scatteredRandom.nextFloat();
        scatteredRandom.nextFloat();

        if (scatteredRandom.nextFloat() < scatteredSettings.getChance()) {
            // No scattered structure in this area
            return;
        }

        // Find the right type of scattered asset for this area
        ScatteredReference reference = selectRandomScattered(info, scatteredSettings, scatteredRandom);
        if (reference == null) {
            // Nothing matches
            return;
        }
        Scattered scattered = AssetRegistries.SCATTERED.getOrThrow(provider.getWorld(), reference.getName());

        // Find the size of the scattered building
        int w;
        int h;
        MultiBuilding multiBuilding;
        if (scattered.getMultibuilding() != null) {
            multiBuilding = AssetRegistries.MULTI_BUILDINGS.getOrThrow(provider.getWorld(), scattered.getMultibuilding());
            w = multiBuilding.getDimX();
            h = multiBuilding.getDimZ();
        } else {
            w = h = 1;
            multiBuilding = null;
        }

        // Find the position of the building in the world
        int tlChunkX = (ax * scatteredSettings.getAreasize() - 2000000) + scatteredRandom.nextInt(scatteredSettings.getAreasize() - w + 1);
        int tlChunkZ = (az * scatteredSettings.getAreasize() - 2000000) + scatteredRandom.nextInt(scatteredSettings.getAreasize() - h + 1);

        if (chunkX < tlChunkX || chunkZ < tlChunkZ || chunkX >= (tlChunkX+w) || chunkZ >= (tlChunkZ+h)) {
            return;
        }

        // First test the conditions for all the relevant chunks (does this need to be cached?)
        int minheight = Integer.MAX_VALUE;
        int maxheight = Integer.MIN_VALUE;
        int avgheight = 0;
        for (int x = tlChunkX ; x < tlChunkX + w ; x++) {
            for (int z = tlChunkZ; z < tlChunkZ + h; z++) {
                BuildingInfo tinfo = BuildingInfo.getBuildingInfo(x, z, provider);
                if (!isValidScatterBiome(reference, x, z)) {
                    return;
                }
                if (!avoidScattered(tinfo)) {
                    return;
                }
                ChunkHeightmap heightmap = getHeightmap(x, z, provider.getWorld());
                minheight = Math.min(minheight, heightmap.getMinimumHeight());
                maxheight = Math.max(maxheight, heightmap.getMaximumHeight());
                avgheight += heightmap.getAverageHeight();
            }
        }
        // Check the height difference
        if (reference.getMaxheightdiff() != null) {
            int diff = maxheight - minheight;
            if (diff > reference.getMaxheightdiff()) {
                return;
            }
        }

        avgheight /= w*h;


        // We need to generate a part of the building
        if (multiBuilding == null) {
            // A single building
            List<String> buildings = scattered.getBuildings();
            if (buildings == null) {
                throw new RuntimeException("Missing buildings for scattered '" + reference.getName() + "'!");
            }
            String buildingName;
            if (buildings.size() == 1) {
                buildingName = buildings.get(0);
            } else {
                buildingName = buildings.get(scatteredRandom.nextInt(buildings.size()));
            }
            Building building = AssetRegistries.BUILDINGS.getOrThrow(provider.getWorld(), buildingName);
            int lowestLevel = handleScatteredTerrain(info, scattered);
            generateScatteredBuilding(info, building, scatteredRandom, lowestLevel, scattered.getTerrainfix());
        } else {
            int lowestLevel = handleScatteredTerrainMulti(info, scattered, multiBuilding, minheight, maxheight, avgheight);
            int relx = chunkX - tlChunkX;
            int relz = chunkZ - tlChunkZ;
            String buildingName = multiBuilding.getBuilding(relx, relz);
            Building building = AssetRegistries.BUILDINGS.getOrThrow(provider.getWorld(), buildingName);
            generateScatteredBuilding(info, building, scatteredRandom, lowestLevel, scattered.getTerrainfix());
        }
    }

    @Nullable
    private ScatteredReference selectRandomScattered(BuildingInfo info, ScatteredSettings scatteredSettings, Random rand) {
        List<ScatteredReference> list = scatteredSettings.getList();
        if (list.isEmpty()) {
            return null;
        }

        int totalweight = 0;
        List<ScatteredReference> filteredList = new ArrayList<>();
        for (ScatteredReference reference : list) {
            if (isValidScatterBiome(reference, info.chunkX, info.chunkZ)) {
                totalweight += reference.getWeight();
                filteredList.add(reference);
            }
        }
        if (filteredList.isEmpty()) {
            return null;
        }

        int rndweight = rand.nextInt(totalweight + scatteredSettings.getWeightnone());
        ScatteredReference reference = null;
        for (ScatteredReference scatteredReference : filteredList) {
            int weight = scatteredReference.getWeight();
            if (rndweight <= weight) {
                reference = scatteredReference;
                break;
            }
            rndweight -= weight;
        }
        return reference;
    }

    private boolean isValidScatterBiome(ScatteredReference reference, int x, int z) {
        if (reference.getBiomeMatcher() != null) {
            BiomeInfo biome = BiomeInfo.getBiomeInfo(provider, x, z);
            return reference.getBiomeMatcher().test(biome.getMainBiome());
        }
        return true;
    }

    private void generateScatteredBuilding(BuildingInfo info, Building building, Random rand, int lowestLevel, Scattered.TerrainFix terrainFix) {
        int chunkX = info.chunkX;
        int chunkZ = info.chunkZ;

        int height = lowestLevel;
        int floors;
        int minfloors = building.getMinFloors();
        if (minfloors <= 0) {
            minfloors = 1;
        }
        int maxfloors = building.getMaxFloors();
        if (maxfloors <= 0) {
            maxfloors = 1;
        }
        if (minfloors >= maxfloors) {
            floors = minfloors;
        } else {
            floors = minfloors + rand.nextInt(maxfloors-minfloors+1);
        }
        for (int f = 0 ; f < floors ;  f++) {
            ConditionContext conditionContext = new ConditionContext(lowestLevel, f, 0, floors, "<none>", building.getName(),
                    chunkX, chunkZ) {
                @Override
                public boolean isBuilding() {
                    return true;
                }

                @Override
                public boolean isSphere() {
                    return CitySphere.isInSphere(chunkX, chunkZ, new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8), provider);
                }

                @Override
                public ResourceLocation getBiome() {
                    Holder<Biome> biome = provider.getWorld().getBiome(new BlockPos(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
                    return biome.unwrap().map(ResourceKey::location, b -> provider.getWorld().registryAccess().registryOrThrow(Registries.BIOME).getKey(b));
                }
            };
            String randomPart = building.getRandomPart(rand, conditionContext);
            BuildingPart part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), randomPart);
            randomPart = building.getRandomPart2(rand, conditionContext);
            BuildingPart part2 = AssetRegistries.PARTS.get(provider.getWorld(), randomPart);    // Null is legal

            if (f == 0) {
                switch (terrainFix) {
                    case NONE -> {
                    }
                    case CLEAR -> {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                clearRange(info, x, z, lowestLevel, lowestLevel + 50, false);
                            }
                        }
                    }
                    case REPEATSLICE -> {
                        CompiledPalette compiledPalette = computePalette(info, part);
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                char c = part.getPaletteChar(x, 0, z);
                                if (c != ' ') {
                                    int y = lowestLevel - 1;
                                    driver.current(x, y, z);
                                    BlockState b = driver.getBlock();
                                    while (b == air || b == liquid) {
                                        driver.block(compiledPalette.get(c));
                                        driver.decY();
                                        b = driver.getBlock();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            height += generatePart(info, part, Transform.ROTATE_NONE, 0, height, 0, false);
            if (part2 != null) {
                generatePart(info, part2, Transform.ROTATE_NONE, 0, height, 0, false);
            }
        }
    }

    private int handleScatteredTerrain(BuildingInfo info, Scattered scattered) {
        ChunkHeightmap heightmap = getHeightmap(info.coord, provider.getWorld());

        int lowestLevel = switch (scattered.getTerrainheight()) {
            case LOWEST -> heightmap.getMinimumHeight();
            case AVERAGE -> heightmap.getAverageHeight();
            case HIGHEST -> heightmap.getMaximumHeight();
            case OCEAN -> ((ServerChunkCache)provider.getWorld().getChunkSource()).getGenerator().getSeaLevel();
        };
        lowestLevel += scattered.getHeightoffset();
        return lowestLevel;
    }

    private int handleScatteredTerrainMulti(BuildingInfo info, Scattered scattered, MultiBuilding multiBuilding, int minimum, int maximum, int average) {
        int lowestLevel = switch (scattered.getTerrainheight()) {
            case LOWEST -> minimum;
            case AVERAGE -> maximum;
            case HIGHEST -> average;
            case OCEAN -> ((ServerChunkCache)provider.getWorld().getChunkSource()).getGenerator().getSeaLevel();
        };
        lowestLevel += scattered.getHeightoffset();
        return lowestLevel;
    }

    private void breakBlocksForDamageNew(int chunkX, int chunkZ, BuildingInfo info) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        DamageArea damageArea = info.getDamageArea();

        float damageFactor = 1.0f;

        boolean hasCollectedDamage = false;
        float[][] collectedDamage = new float[16][16];

        // @todo this only supports explosions between 0 and 256 for now
        for (int yy = 0; yy < 16; yy++) {
            boolean hasExplosions = damageArea.hasExplosions(yy);
            for (int y = 0; y < 16; y++) {
                if (hasExplosions) {
                    int cntDamaged = 0;
                    int cntAir = 0;
                    int cury = yy * 16 + y;
                    for (int x = 0; x < 16; x++) {
                        driver.current(x, cury, 0);
                        for (int z = 0; z < 16; z++) {
                            BlockState d = driver.getBlock();
                            if (d != air || cury <= info.waterLevel) {
                                float damage = damageArea.getDamage(cx + x, cury, cz + z) * damageFactor;
                                if (damage >= 0.001) {
                                    collectedDamage[x][z] += damage;
                                    hasCollectedDamage = true;
                                }
                            }
                            driver.incZ();
                        }
                    }
                }
                if (hasCollectedDamage) {
                    int cntDamaged = 0;
                    int cntAir = 0;
                    int cury = yy * 16 + y;
                    hasCollectedDamage = false;
                    for (int x = 0; x < 16; x++) {
                        driver.current(x, cury, 0);
                        for (int z = 0; z < 16; z++) {
                            BlockState d = driver.getBlock();
                            if (d != air || cury <= info.waterLevel) {
                                float damage = collectedDamage[x][z];
                                if (damage >= 0.001) {
                                    BlockState newd = damageArea.damageBlock(d, provider, cury, damage, info.getCompiledPalette(), liquid);
                                    if (newd != d) {
                                        driver.block(newd);
                                        cntDamaged++;
                                    }
                                }
                            } else {
                                cntAir++;
                            }
                            driver.incZ();
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

    private void generateHighways(int chunkX, int chunkZ, BuildingInfo info) {
        int levelX = Highway.getXHighwayLevel(chunkX, chunkZ, provider, info.profile);
        int levelZ = Highway.getZHighwayLevel(chunkX, chunkZ, provider, info.profile);
        if (levelX == levelZ && levelX >= 0) {
            // Crossing
            generateHighwayPart(info, levelX, Transform.ROTATE_NONE, info.getXmax(), info.getZmax(), true);
        } else if (levelX >= 0 && levelZ >= 0) {
            // There are two highways on different level. Make sure the lowest one is done first because it
            // will clear out what is above it
            if (levelX == 0) {
                generateHighwayPart(info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), false);
                generateHighwayPart(info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), false);
            } else {
                generateHighwayPart(info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), false);
                generateHighwayPart(info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), false);
            }
        } else {
            if (levelX >= 0) {
                generateHighwayPart(info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), false);
            } else if (levelZ >= 0) {
                generateHighwayPart(info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), false);
            }
        }
    }

    private static boolean isClearableAboveHighway(BlockState st) {
        return !st.is(BlockTags.LEAVES) && !st.is(BlockTags.LOGS);
    }

    private void generateHighwayPart(BuildingInfo info, int level, Transform transform, BuildingInfo adjacent1, BuildingInfo adjacent2, boolean bidirectional) {
        int highwayGroundLevel = info.groundLevel + level * FLOORHEIGHT;
        HighwayParts highwayParts = provider.getWorldStyle().getPartSelector().highwayParts();

        BuildingPart part;
        if (info.isTunnel(level)) {
            // We know we need a tunnel
            part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), highwayParts.tunnel(bidirectional));
            generatePart(info, part, transform, 0, highwayGroundLevel, 0, true);
        } else if (info.isCity && level <= adjacent1.cityLevel && level <= adjacent2.cityLevel && adjacent1.isCity && adjacent2.isCity) {
            // Simple highway in the city
            part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), highwayParts.open(bidirectional));
            int height = generatePart(info, part, transform, 0, highwayGroundLevel, 0, true);
            // Clear a bit more above the highway
            if (!info.profile.isCavern()) {
                int clearheight = 15;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        clearRange(info, x, z, height, height + clearheight, info.waterLevel > info.groundLevel,
                                LostCityTerrainFeature::isClearableAboveHighway);
                    }
                }
            }
        } else {
            part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), highwayParts.bridge(bidirectional));
            int height = generatePart(info, part, transform, 0, highwayGroundLevel, 0, true);
            // Clear a bit more above the highway
            if (!info.profile.isCavern()) {
                int clearheight = 15;
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        clearRange(info, x, z, height, height + clearheight, info.waterLevel > info.groundLevel,
                                LostCityTerrainFeature::isClearableAboveHighway);
                    }
                }
            }
        }

        Character support = part.getMetaChar(ILostCities.META_SUPPORT);
        if (info.profile.HIGHWAY_SUPPORTS && support != null) {
            BlockState sup = info.getCompiledPalette().get(support);
            int x1 = transform.rotateX(0, 15);
            int z1 = transform.rotateZ(0, 15);
            driver.current(x1, highwayGroundLevel - 1, z1);
            for (int y = 0; y < 40; y++) {
                if (isEmpty(driver.getBlock())) {
                    driver.block(sup);
                } else {
                    break;
                }
                driver.decY();
            }

            int x2 = transform.rotateX(0, 0);
            int z2 = transform.rotateZ(0, 0);
            driver.current(x2, highwayGroundLevel - 1, z2);
            for (int y = 0; y < 40; y++) {
                if (isEmpty(driver.getBlock())) {
                    driver.block(sup);
                } else {
                    break;
                }
                driver.decY();
            }
        }
    }

    private void clearRange(BuildingInfo info, int x, int z, int height1, int height2, boolean dowater) {
        if (dowater) {
            // Special case for drowned city
            driver.setBlockRange(x, height1, z, info.waterLevel, liquid);
            driver.setBlockRangeToAir(x, info.waterLevel + 1, z, height2);
        } else {
            driver.setBlockRangeToAir(x, height1, z, height2);
        }
    }

    private void clearRange(BuildingInfo info, int x, int z, int height1, int height2, boolean dowater, Predicate<BlockState> test) {
        if (dowater) {
            // Special case for drowned city
            driver.setBlockRange(x, height1, z, info.waterLevel, liquid, test);
            driver.setBlockRangeToAir(x, info.waterLevel + 1, z, height2, test);
        } else {
            driver.setBlockRangeToAir(x, height1, z, height2, test);
        }
    }

    private void generateBridges(BuildingInfo info) {
        if (info.getHighwayXLevel() == 0 || info.getHighwayZLevel() == 0) {
            // If there is a highway at level 0 we cannot generate bridge parts. If there
            // is no highway or a highway at level 1 then bridge sections can generate just fine
            return;
        }
        BuildingPart bt = info.hasXBridge(provider);
        if (bt != null) {
            generateBridge(info, bt, Orientation.X);
        } else {
            bt = info.hasZBridge(provider);
            if (bt != null) {
                generateBridge(info, bt, Orientation.Z);
            }
        }
    }

    private void generateBridge(BuildingInfo info, BuildingPart bt, Orientation orientation) {
        CompiledPalette compiledPalette = computePalette(info, bt);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                driver.current(x, mainGroundLevel + 1, z);
                int l = 0;
                while (l < bt.getSliceCount()) {
                    Character c = orientation == Orientation.X ? bt.getPaletteChar(x, l, z) : bt.getPaletteChar(z, l, x); // @todo general rotation system?
                    BlockState b = compiledPalette.get(c);
                    Palette.Info inf = compiledPalette.getInfo(c);
                    if (inf != null) {
                        if (inf.isTorch()) {
                            if (info.profile.GENERATE_LIGHTING) {
                                info.addTorchTodo(driver.getCurrentCopy());
                            } else {
                                b = air;        // No torch!
                            }
                        }
                    }
                    driver.add(b);
                    l++;
                }
            }
        }

        Character support = bt.getMetaChar(ILostCities.META_SUPPORT);
        if (info.profile.BRIDGE_SUPPORTS && support != null) {
            BlockState sup = compiledPalette.get(support);
            BuildingInfo minDir = orientation.getMinDir().get(info);
            BuildingInfo maxDir = orientation.getMaxDir().get(info);
            if (minDir.hasBridge(provider, orientation) != null && maxDir.hasBridge(provider, orientation) != null) {
                // Needs support
                for (int y = info.waterLevel - 10; y <= info.groundLevel; y++) {
                    driver.current(7, y, 7).block(sup);
                    driver.current(7, y, 8).block(sup);
                    driver.current(8, y, 7).block(sup);
                    driver.current(8, y, 8).block(sup);
                }
            }
            if (minDir.hasBridge(provider, orientation) == null) {
                // Connection to the side section
                if (orientation == Orientation.X) {
                    int x = 0;
                    driver.current(x, mainGroundLevel, 6);
                    for (int z = 6; z <= 9; z++) {
                        driver.block(sup).incZ();
                    }
                } else {
                    int z = 0;
                    driver.current(6, mainGroundLevel, z);
                    for (int x = 6; x <= 9; x++) {
                        driver.block(sup).incX();
                    }
                }
            }
            if (maxDir.hasBridge(provider, orientation) == null) {
                // Connection to the side section
                if (orientation == Orientation.X) {
                    int x = 15;
                    driver.current(x, mainGroundLevel, 6);
                    for (int z = 6; z <= 9; z++) {
                        driver.block(sup).incZ();
                    }
                } else {
                    int z = 15;
                    driver.current(6, mainGroundLevel, z);
                    for (int x = 6; x <= 9; x++) {
                        driver.block(sup).incX();
                    }
                }
            }
        }
    }

    private static final Random RANDOMIZED_OFFSET = new Random();
    public static int getRandomizedOffset(int chunkX, int chunkZ, int min, int max) {
        RANDOMIZED_OFFSET.setSeed(chunkZ * 256203221L + chunkX * 899809363L);
        RANDOMIZED_OFFSET.nextFloat();
        return RANDOMIZED_OFFSET.nextInt(max - min + 1) + min;
    }

    private static final Random RANDOMIZED_OFFSET_L1 = new Random();
    public static int getHeightOffsetL1(int chunkX, int chunkZ) {
        RANDOMIZED_OFFSET_L1.setSeed(chunkZ * 341873128712L + chunkX * 132897987541L);
        RANDOMIZED_OFFSET_L1.nextFloat();
        return RANDOMIZED_OFFSET_L1.nextInt(5);
    }

    private static final Random RANDOMIZED_OFFSET_L2 = new Random();
    public static int getHeightOffsetL2(int chunkX, int chunkZ) {
        RANDOMIZED_OFFSET_L2.setSeed(chunkZ * 132897987541L + chunkX * 341873128712L);
        RANDOMIZED_OFFSET_L2.nextFloat();
        return RANDOMIZED_OFFSET_L2.nextInt(5);
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
    private void correctTerrainShape(int chunkX, int chunkZ) {
        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);
        BuildingInfo.MinMax mm00 = info.getDesiredMaxHeightL2();
        BuildingInfo.MinMax mm10 = info.getXmax().getDesiredMaxHeightL2();
        BuildingInfo.MinMax mm01 = info.getZmax().getDesiredMaxHeightL2();
        BuildingInfo.MinMax mm11 = info.getXmax().getZmax().getDesiredMaxHeightL2();

        // @todo correct for build height change
        float min00 = mm00.min;
        float min10 = mm10.min;
        float min01 = mm01.min;
        float min11 = mm11.min;
        float max00 = mm00.max;
        float max10 = mm10.max;
        float max01 = mm01.max;
        float max11 = mm11.max;
        if (max00 < 256 || max10 < 256 || max01 < 256 || max11 < 256 ||
                min00 < 256 || min10 < 256 || min01 < 256 || min11 < 256) {
            // We need to fit the terrain between the upper and lower mesh here
            ChunkHeightmap heightmap = getHeightmap(info.coord, provider.getWorld());
            int maxHeightP = heightmap.getMaximumHeight() + 10;
            int minHeightP = heightmap.getMinimumHeight() - 10;
            if (max00 >= 256) {
                max00 = maxHeightP;
            }
            if (max10 >= 256) {
                max10 = maxHeightP;
            }
            if (max01 >= 256) {
                max01 = maxHeightP;
            }
            if (max11 >= 256) {
                max11 = maxHeightP;
            }
            if (min00 >= 256) {
                min00 = minHeightP;
            }
            if (min10 >= 256) {
                min10 = minHeightP;
            }
            if (min01 >= 256) {
                min01 = minHeightP;
            }
            if (min11 >= 256) {
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
                    boolean moved = moveDown(x, z, (int) maxheight, info.waterLevel > info.groundLevel);

                    if (!moved) {
                        float minheight = minh0 + (minh1 - minh0) * (15.0f - z) / 15.0f;
                        moveUp(x, z, (int) minheight, info.waterLevel > info.groundLevel);
                    }
                }
            }
        }
    }

    // Return true if state is air or liquid
    private static boolean isEmpty(BlockState state) {
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
        if (isEmpty(state)){
            return true;
        }
        return Tools.hasTag(state.getBlock(), LostTags.FOLIAGE_TAG);
    }

    private void moveUp(int x, int z, int height, boolean dowater) {
        // Find the first non-empty block starting at the given height
        driver.current(x, height, z);
        int minHeight = provider.getWorld().getMinBuildHeight();
        // We assume here we are not in a void chunk
        while (isFoliageOrEmpty(driver.getBlock()) && driver.getY() > minHeight) {
            driver.decY();
        }

        if (driver.getY() >= height) {
            return; // Nothing to do
        }

        int idx = driver.getY();    // Points to non-empty block below the empty block
        driver.current(x, height, z);
        while (idx > 0) {
            BlockState blockToMove = driver.getBlock(x, idx, z);
            if (blockToMove.isAir() || blockToMove.getBlock() == Blocks.BEDROCK) {
                break;
            }
            driver.block(blockToMove);
            driver.decY();
            idx--;
        }
    }

    private final BlockState[] buffer = new BlockState[6];

    private boolean moveDown(int x, int z, int height, boolean dowater) {
        int y = 255;
        driver.current(x, y, z);
        // We assume here we are not in a void chunk
        while (isEmpty(driver.getBlock()) && driver.getY() > height) {
            driver.decY();
        }

        if (driver.getY() <= height) {
            return false; // Nothing to do
        }

        // We arrived at our first non-air block
        int bufferIdx = 0;
        while (driver.getY() >= height) {
            if (bufferIdx < buffer.length) {
                buffer[bufferIdx++] = driver.getBlock();
            }
            driver.block(air);
            driver.decY();
        }

        int idx = 0;
        while (idx < bufferIdx && driver.getY() > 0) {
            driver.block(buffer[idx++]);
            driver.decY();
        }

//
//        if (dowater) {
//            // Special case for drowned city
//            driver.setBlockRange(x, height1, z, info.waterLevel, liquid);
//            driver.setBlockRange(x, info.waterLevel+1, z, height2, air);
//        } else {
//            driver.setBlockRange(x, height1, z, height2, air);
//        }
        return true;
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
     * @param info
     * @return
     */
    public int getMinHeightAt(BuildingInfo info, int x, int z) {
        ChunkHeightmap heightmap = getHeightmap(info.coord, info.provider.getWorld());
        int height = heightmap.getHeight(x, z);
        ChunkHeightmap adjacentHeightmap;
        WorldGenLevel world = info.provider.getWorld();
        int adjacent;
        if (x == 0) {
            if (z == 0) {
                adjacent = getHeightmap(info.chunkX - 1, info.chunkZ - 1, world).getHeight(15, 15);
            } else if (z == 15) {
                adjacent = getHeightmap(info.chunkX - 1, info.chunkZ + 1, world).getHeight(15, 0);
            } else {
                adjacent = getHeightmap(info.chunkX - 1, info.chunkZ, world).getHeight(15, z);
            }
        } else if (x == 15) {
            if (z == 0) {
                adjacent = getHeightmap(info.chunkX + 1, info.chunkZ - 1, world).getHeight(0, 15);
            } else if (z == 15) {
                adjacent = getHeightmap(info.chunkX + 1, info.chunkZ + 1, world).getHeight(0, 0);
            } else {
                adjacent = getHeightmap(info.chunkX + 1, info.chunkZ, world).getHeight(0, z);
            }
        } else if (z == 0) {
            adjacent = getHeightmap(info.chunkX, info.chunkZ - 1, world).getHeight(x, 15);
        } else if (z == 15) {
            adjacent = getHeightmap(info.chunkX, info.chunkZ + 1, world).getHeight(x, 0);
        } else {
            return height;
        }
        return Math.min(height, adjacent);
    }

    public ChunkHeightmap getHeightmap(int chunkX, int chunkZ, @Nonnull WorldGenLevel world) {
        ChunkCoord key = new ChunkCoord(world.getLevel().dimension(), chunkX, chunkZ);
        return getHeightmap(key, world);
    }

    public ChunkHeightmap getHeightmap(ChunkCoord key, @Nonnull WorldGenLevel world) {
        synchronized (this) {
            if (cachedHeightmaps.containsKey(key)) {
                return cachedHeightmaps.get(key);
            } else {
                ChunkHeightmap heightmap = new ChunkHeightmap(profile.LANDSCAPE_TYPE, profile.GROUNDLEVEL);
                generateHeightmap(key.chunkX(), key.chunkZ(), world, heightmap);
                cachedHeightmaps.put(key, heightmap);
                return heightmap;
            }
        }
    }

    private void generateHeightmap(int chunkX, int chunkZ, WorldGenLevel region, ChunkHeightmap heightmap) {
        ServerChunkCache chunkProvider = region.getLevel().getChunkSource();
        ChunkGenerator generator = chunkProvider.getGenerator();
        int cx = chunkX << 4;
        int cz = chunkZ << 4;
        RandomState randomState = chunkProvider.randomState();
        int height00 = generator.getBaseHeight(cx + 3, cz + 3, Heightmap.Types.OCEAN_FLOOR_WG, region, randomState);
        int height10 = generator.getBaseHeight(cx + 12, cz + 3, Heightmap.Types.OCEAN_FLOOR_WG, region, randomState);
        int height01 = generator.getBaseHeight(cx + 3, cz + 12, Heightmap.Types.OCEAN_FLOOR_WG, region, randomState);
        int height11 = generator.getBaseHeight(cx + 12, cz + 12, Heightmap.Types.OCEAN_FLOOR_WG, region, randomState);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y;
                if (x < 8 && z < 8) {
                    y = height00;
                } else if (x < 8) {
                    y = height01;
                } else if (z < 8) {
                    y = height10;
                } else {
                    y = height11;
                }
                heightmap.update(x, y, z);
            }
        }
    }

    private void doCityChunk(int chunkX, int chunkZ, BuildingInfo info) {
        boolean building = info.hasBuilding;

        ChunkHeightmap heightmap = getHeightmap(info.coord, provider.getWorld());

        if (info.profile.isDefault() || info.profile.isSpheres()) {
            int minHeight = info.minBuildHeight;
            BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    driver.setBlockRange(x, minHeight, z, minHeight + info.profile.BEDROCK_LAYER, bedrock);
                }
            }

            if (info.waterLevel > info.groundLevel) {
                // Special case for a high water level
                for (int x = 0; x < 16; ++x) {
                    for (int z = 0; z < 16; ++z) {
                        driver.setBlockRange(x, info.groundLevel, z, info.waterLevel, liquid);
                    }
                }
            }
        }

        // City surface leveling - for prettier cities
        // Note: Better results may be achieved with terrain noise adjustment (like how newer structures do it)
        if (profile.isDefault() || profile.isSpheres()) {
            int ground = info.getCityGroundLevel();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    boolean moved = moveDown(x, z, ground + 1, info.waterLevel > info.groundLevel);

                    if (!moved) {
                        moveUp(x, z, ground, info.waterLevel > info.groundLevel);
                    }
                }
            }
        }

        LostCityEvent.PreGenCityChunkEvent event = new LostCityEvent.PreGenCityChunkEvent(provider.getWorld(), LostCities.lostCitiesImp, chunkX, chunkZ, driver.getPrimer());
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            if (building) {
                generateBuilding(info, heightmap);
            } else {
                generateStreet(info, heightmap);
            }
        }
        LostCityEvent.PostGenCityChunkEvent postevent = new LostCityEvent.PostGenCityChunkEvent(provider.getWorld(), LostCities.lostCitiesImp, chunkX, chunkZ, driver.getPrimer());
        MinecraftForge.EVENT_BUS.post(postevent);

        if (info.profile.RUIN_CHANCE > 0.0) {
            generateRuins(info);
        }

        int levelX = info.getHighwayXLevel();
        int levelZ = info.getHighwayZLevel();
        if (!building) {
            Railway.RailChunkInfo railInfo = info.getRailInfo();
            if (levelX < 0 && levelZ < 0 && !railInfo.getType().isSurface()) {
                generateStreetDecorations(info);
            }
        }
        if (levelX >= 0 || levelZ >= 0) {
            generateHighways(chunkX, chunkZ, info);
        }

        if (info.profile.RUBBLELAYER) {
            if (!info.hasBuilding || info.ruinHeight >= 0) {
                generateRubble(chunkX, chunkZ, info);
            }
        }
    }

    private void generateRailwayDungeons(BuildingInfo info) {
        if (info.railDungeon == null) {
            return;
        }
        if (info.getZmin().getRailInfo().getType() == RailChunkType.HORIZONTAL ||
                info.getZmax().getRailInfo().getType() == RailChunkType.HORIZONTAL) {
            int height = info.groundLevel + Railway.RAILWAY_LEVEL_OFFSET * FLOORHEIGHT;
            generatePart(info, info.railDungeon, Transform.ROTATE_NONE, 0, height, 0, false);
        }
    }

    private void generateRailways(BuildingInfo info, Railway.RailChunkInfo railInfo) {
        RailwayParts railwayParts = provider.getWorldStyle().getPartSelector().railwayParts();
        int height = info.groundLevel + railInfo.getLevel() * FLOORHEIGHT;
        RailChunkType type = railInfo.getType();
        BuildingPart part;
        Transform transform = Transform.ROTATE_NONE;
        boolean needsStaircase = false;
        boolean clearUpper = false;
        switch (type) {
            case NONE:
                return;
            case STATION_SURFACE:
            case STATION_EXTENSION_SURFACE:
                if (railInfo.getLevel() < info.cityLevel) {
                    // Even for a surface station extension we switch to underground if we are an extension
                    // that is at a spot where the city is higher then where the station is
                    part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.stationUnderground());
                } else {
                    if (railInfo.getPart() != null) {
                        part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railInfo.getPart());
                    } else {
                        part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.stationOpen());
                    }
                }
                clearUpper = true;
                break;
            case STATION_UNDERGROUND:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.stationUndergroundStairs());
                needsStaircase = true;
                break;
            case STATION_EXTENSION_UNDERGROUND:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.stationUnderground());
                break;
            case RAILS_END_HERE:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.railsHorizontalEnd());
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case HORIZONTAL:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.railsHorizontal());

                // If the adjacent chunks are also horizontal we take a sample of the blocks around us to see if we are in water
                RailChunkType type1 = info.getXmin().getRailInfo().getType();
                RailChunkType type2 = info.getXmax().getRailInfo().getType();
                if (!type1.isStation() && !type2.isStation()) {
                    if (driver.getBlock(3, height + 2, 3) == liquid &&
                            driver.getBlock(12, height + 2, 3) == liquid &&
                            driver.getBlock(3, height + 2, 12) == liquid &&
                            driver.getBlock(12, height + 2, 12) == liquid &&
                            driver.getBlock(3, height + 4, 7) == liquid &&
                            driver.getBlock(12, height + 4, 8) == liquid) {
                        part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.railsHorizontalWater());
                    }
                }
                break;
            case VERTICAL:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.railsVertical());
                if (driver.getBlock(3, height + 2, 3) == liquid &&
                        driver.getBlock(12, height + 2, 3) == liquid &&
                        driver.getBlock(3, height + 2, 12) == liquid &&
                        driver.getBlock(12, height + 2, 12) == liquid &&
                        driver.getBlock(3, height + 4, 7) == liquid &&
                        driver.getBlock(12, height + 4, 8) == liquid) {
                    part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.railsVerticalWater());
                }
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case THREE_SPLIT:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.rails3Split());
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case GOING_DOWN_TWO_FROM_SURFACE:
            case GOING_DOWN_FURTHER:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.railsDown2());
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case GOING_DOWN_ONE_FROM_SURFACE:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.railsDown1());
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case DOUBLE_BEND:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.railsBend());
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            default:
                part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.railsFlat());
                break;
        }
        int h = generatePart(info, part, transform, 0, height, 0, false);
        if (clearUpper) {
            ChunkHeightmap heightmap = getHeightmap(info.coord, provider.getWorld());
            int maxh = heightmap.getMaximumHeight() + 4;
            if (h < maxh) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        clearRange(info, x, z, h, maxh, false);
                    }
                }
            }
        }

        Character railMainBlock = info.getCityStyle().getRailMainBlock();
        BlockState rail = info.getCompiledPalette().get(railMainBlock);

        if (type == RailChunkType.HORIZONTAL) {
            // If there is a rail dungeon north or south we must make a connection here
            if (info.getZmin().railDungeon != null) {
                for (int z = 0; z < 4; z++) {
                    driver.current(6, height + 1, z).add(rail).add(air).add(air);
                    driver.current(7, height + 1, z).add(rail).add(air).add(air);
                }
                for (int z = 0; z < 3; z++) {
                    driver.current(5, height + 2, z).add(rail).add(rail).add(rail);
                    driver.current(6, height + 4, z).block(rail);
                    driver.current(7, height + 4, z).block(rail);
                    driver.current(8, height + 2, z).add(rail).add(rail).add(rail);
                }
            }

            if (info.getZmax().railDungeon != null) {
                for (int z = 0; z < 5; z++) {
                    driver.current(6, height + 1, 15 - z).add(rail).add(air).add(air);
                    driver.current(7, height + 1, 15 - z).add(rail).add(air).add(air);
                }
                for (int z = 0; z < 4; z++) {
                    driver.current(5, height + 2, 15 - z).add(rail).add(rail).add(rail);
                    driver.current(6, height + 4, 15 - z).block(rail);
                    driver.current(7, height + 4, 15 - z).block(rail);
                    driver.current(8, height + 2, 15 - z).add(rail).add(rail).add(rail);
                }
            }
        }

        if (railInfo.getRails() < 3) {
            // We may have to reduce number of rails
            int index;
            switch (railInfo.getType()) {
                case NONE:
                    break;
                case STATION_SURFACE:
                case STATION_UNDERGROUND:
                case STATION_EXTENSION_SURFACE:
                case STATION_EXTENSION_UNDERGROUND:
                case HORIZONTAL: {
                    if (railInfo.getRails() == 1) {
                        driver.current(0, height + 1, 5);
                        for (int x = 0; x < 16; x++) {
                            driver.block(rail).incX();
                        }
                        driver.current(0, height + 1, 9);
                        for (int x = 0; x < 16; x++) {
                            driver.block(rail).incX();
                        }
                    } else {
                        driver.current(0, height + 1, 7);
                        for (int x = 0; x < 16; x++) {
                            driver.block(rail).incX();
                        }
                    }
                    break;
                }
                case GOING_DOWN_TWO_FROM_SURFACE:
                case GOING_DOWN_ONE_FROM_SURFACE:
                case GOING_DOWN_FURTHER:
                    if (railInfo.getRails() == 1) {
                        for (int x = 0; x < 16; x++) {
                            for (int y = height + 1; y < height + part.getSliceCount(); y++) {
                                driver.current(x, y, 5);
                                if (getRailStates().contains(driver.getBlock())) {
                                    driver.block(rail);
                                }
                                driver.current(x, y, 9);
                                if (getRailStates().contains(driver.getBlock())) {
                                    driver.block(rail);
                                }
                            }
                        }
                    } else {
                        for (int x = 0; x < 16; x++) {
                            for (int y = height + 1; y < height + part.getSliceCount(); y++) {
                                driver.current(x, y, 7);
                                if (getRailStates().contains(driver.getBlock())) {
                                    driver.block(rail);
                                }
                            }
                        }
                    }
                    break;
                case THREE_SPLIT:
                case VERTICAL:
                case DOUBLE_BEND:
                case RAILS_END_HERE:
                    break;
            }
        }

        if (needsStaircase) {
            part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.stationStaircase());
            for (int i = railInfo.getLevel() + 1; i < info.cityLevel; i++) {
                height = info.groundLevel + i * FLOORHEIGHT;
                generatePart(info, part, transform, 0, height, 0, false);
            }
            height = info.groundLevel + info.cityLevel * FLOORHEIGHT;
            part = AssetRegistries.PARTS.getOrThrow(provider.getWorld(), railwayParts.stationStaircaseSurface());
            generatePart(info, part, transform, 0, height, 0, false);
        }
    }

    private void generateStreetDecorations(BuildingInfo info) {
        Direction stairDirection = info.getActualStairDirection();
        if (stairDirection != null) {
            BuildingPart stairs = info.stairType;
            Transform transform;
            int oy = info.getCityGroundLevel() + 1;
            transform = switch (stairDirection) {
                case XMIN -> Transform.ROTATE_NONE;
                case XMAX -> Transform.ROTATE_180;
                case ZMIN -> Transform.ROTATE_90;
                case ZMAX -> Transform.ROTATE_270;
            };

            generatePart(info, stairs, transform, 0, oy, 0, false);
        }
    }

    private int countNotEmpty(int y, int max) {
        int cnt = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (driver.getBlock(x, y, z) != air) {
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
                            driver.setBlockRangeToAir(x, y + 1, z, maxY);
                        }
                    }
                } else {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            driver.setBlockRangeToAir(x, y + 1, z, 256);  // @todo hardcoded height
                        }
                    }
                }
                break;
            }
        }
    }

    private double[] rubbleBuffer = new double[256];
    private double[] leavesBuffer = new double[256];

    private void generateRubble(int chunkX, int chunkZ, BuildingInfo info) {
        this.rubbleBuffer = this.rubbleNoise.getRegion(this.rubbleBuffer, (chunkX * 16), (chunkZ * 16), 16, 16, 1.0 / 16.0, 1.0 / 16.0, 1.0D);
        this.leavesBuffer = this.leavesNoise.getRegion(this.leavesBuffer, (chunkX * 64), (chunkZ * 64), 16, 16, 1.0 / 64.0, 1.0 / 64.0, 4.0D);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                double vr = info.profile.RUBBLE_DIRT_SCALE < 0.01f ? 0 : rubbleBuffer[x + z * 16] / info.profile.RUBBLE_DIRT_SCALE;
                double vl = info.profile.RUBBLE_LEAVE_SCALE < 0.01f ? 0 : leavesBuffer[x + z * 16] / info.profile.RUBBLE_LEAVE_SCALE;
                if (vr > .5 || vl > .5) {
                    int height = getInterpolatedHeight(info, x, z);
                    driver.current(x, height, z);
                    BlockState c = driver.getBlockDown();
                    if (c != air && c != liquid) {
                        for (int i = 0; i < vr; i++) {
                            if (isEmpty(driver.getBlock())) {
                                driver.add(getRandomDirt(info, info.getCompiledPalette()));
                            } else {
                                driver.incY();
                            }
                        }
                    }
                    //first round may not have generated this - stops crash on create world
                    BlockState leafBaseState = driver.getBlockDown();
                    if (leafBaseState == base || getPossibleRandomDirts(info, info.getCompiledPalette()).contains(leafBaseState)) {
                        for (int i = 0; i < vl; i++) {
                            if (isEmpty(driver.getBlock())) {
                                driver.add(getRandomLeaf(info, info.getCompiledPalette()));
                            } else {
                                driver.incY();
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


    private double[] ruinBuffer = new double[256];

    private void generateRuins(BuildingInfo info) {
        if (info.ruinHeight < 0) {
            return;
        }

        int chunkX = info.chunkX;
        int chunkZ = info.chunkZ;
        double d0 = 0.03125D;
        this.ruinBuffer = this.ruinNoise.getRegion(this.ruinBuffer, (chunkX * 16), (chunkZ * 16), 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);
        boolean doLeaves = info.profile.RUBBLELAYER;
        if (doLeaves) {
            this.leavesBuffer = this.leavesNoise.getRegion(this.leavesBuffer, (chunkX * 64), (chunkZ * 64), 16, 16, 1.0 / 64.0, 1.0 / 64.0, 4.0D);
        }

        int baseheight = (int) (info.getCityGroundLevel() + 1 + (info.ruinHeight * info.getNumFloors() * FLOORHEIGHT));

        CompiledPalette palette = info.getCompiledPalette();

        BlockState ironbarsState = Blocks.IRON_BARS.defaultBlockState();
        Character infobarsChar = info.getCityStyle().getIronbarsBlock();
        Supplier<BlockState> ironbars = infobarsChar == null ? () -> ironbarsState : () -> palette.get(infobarsChar);
        Set<BlockState> infoBarSet = infobarsChar == null ? Collections.singleton(ironbarsState) : palette.getAll(infobarsChar);
        Predicate<BlockState> checkIronbars = infobarsChar == null ? s -> s == ironbarsState : infoBarSet::contains;
        Character rubbleBlock = info.getBuilding().getRubbleBlock();

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                double v = ruinBuffer[x + z * 16];
//                double v = ruinNoise.getValue(x, z) / 16.0;
                int height = baseheight + (int) v;
                driver.current(x, height, z);
                height = info.getMaxHeight() + 10 - height;
                if (height > info.maxBuildHeight - 2) {
                    height = info.maxBuildHeight - 2;
                }
                int vl = 0;
                if (doLeaves) {
                    vl = (int) (info.profile.RUBBLE_LEAVE_SCALE < 0.01f ? 0 : leavesBuffer[x + z * 16] / info.profile.RUBBLE_LEAVE_SCALE);
//                    vl = (int) (info.profile.RUBBLE_LEAVE_SCALE < 0.01f ? 0 : leavesNoise.getValue(x / 64.0, z / 64.0) / 4.0 * info.profile.RUBBLE_LEAVE_SCALE);
                }
                boolean doRubble = palette.isDefined(rubbleBlock);
                while (height > 0) {
                    BlockState damage = palette.canBeDamagedToIronBars(driver.getBlock());
                    BlockState c = driver.getBlockDown();

                    if (doRubble && !checkIronbars.test(c) && c != air && c != liquid && rand.nextFloat() < .2f) {      // @todo hardcoded random
                        doRubble = false;
                        driver.add(palette.get(rubbleBlock));
                    } else if ((damage != null || checkIronbars.test(c)) && c != air && c != liquid && rand.nextFloat() < .2f) {    // @todo hardcoded random
                        driver.add(ironbars.get());
                    } else {
                        if (vl > 0) {
                            c = driver.getBlockDown();
                            while (isEmpty(c)) {
                                driver.decY();
                                height++;   // Make sure we keep on filling with air a bit longer because we are lowering here
                                c = driver.getBlockDown();
                            }
                            driver.add(getRandomLeaf(info, palette));
                            vl--;
                        } else {
                            driver.add(air);
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
            generateCorridors(info, xRail, zRail);
        }

        Railway.RailChunkInfo railInfo = info.getRailInfo();
        boolean canDoParks = info.getHighwayXLevel() != info.cityLevel && info.getHighwayZLevel() != info.cityLevel
                && railInfo.getType() != RailChunkType.STATION_SURFACE
                && (railInfo.getType() != RailChunkType.STATION_EXTENSION_SURFACE || railInfo.getLevel() < info.cityLevel);

        if (canDoParks) {
            int height = info.getCityGroundLevel();
            // In default landscape type we clear the landscape on top of the building
//            if (profile.isDefault()) {
//                clearToMax(info, heightmap, height);
//            }

            BuildingInfo.StreetType streetType = info.streetType;
            boolean elevated = info.isElevatedParkSection();
            if (elevated) {
                Character elevationBlock = info.getCityStyle().getParkElevationBlock();
                BlockState elevation = info.getCompiledPalette().get(elevationBlock);
                streetType = BuildingInfo.StreetType.PARK;
                for (int x = 0; x < 16; ++x) {
                    driver.current(x, height, 0);
                    for (int z = 0; z < 16; ++z) {
                        driver.block(elevation).incZ();
                    }
                }
                height++;
            }

            switch (streetType) {
                case NORMAL -> generateNormalStreetSection(info, height);
                case FULL -> generateFullStreetSection(info, height);
                case PARK -> generateParkSection(info, height, elevated);
            }
            height++;

            if (streetType == BuildingInfo.StreetType.PARK || info.fountainType != null) {
                BuildingPart part;
                if (streetType == BuildingInfo.StreetType.PARK) {
                    part = info.parkType;
                } else {
                    part = info.fountainType;
                }
                generatePart(info, part, Transform.ROTATE_NONE, 0, height, 0, false);
            }

            generateRandomVegetation(info, height);

            generateFrontPart(info, height, info.getXmin(), Transform.ROTATE_NONE);
            generateFrontPart(info, height, info.getZmin(), Transform.ROTATE_90);
            generateFrontPart(info, height, info.getXmax(), Transform.ROTATE_180);
            generateFrontPart(info, height, info.getZmax(), Transform.ROTATE_270);
        }

        generateBorders(info, canDoParks);
    }

    private void generateBorders(BuildingInfo info, boolean canDoParks) {
        Character borderBlock = info.getCityStyle().getBorderBlock();

        switch (info.profile.LANDSCAPE_TYPE) {
            case DEFAULT -> fillToBedrockStreetBlock(info);
            case FLOATING -> fillMainStreetBlock(info, borderBlock, 3);
            case CAVERN -> fillMainStreetBlock(info, borderBlock, 2);
            case SPACE -> fillToGroundStreetBlock(info, info.getCityGroundLevel());
            case SPHERES -> fillToBedrockStreetBlock(info);
        }

        if (doBorder(info, Direction.XMIN)) {
            int x = 0;
            for (int z = 0; z < 16; z++) {
                generateBorder(info, canDoParks, x, z, Direction.XMIN.get(info));
            }
        }
        if (doBorder(info, Direction.XMAX)) {
            int x = 15;
            for (int z = 0; z < 16; z++) {
                generateBorder(info, canDoParks, x, z, Direction.XMAX.get(info));
            }
        }
        if (doBorder(info, Direction.ZMIN)) {
            int z = 0;
            for (int x = 0; x < 16; x++) {
                generateBorder(info, canDoParks, x, z, Direction.ZMIN.get(info));
            }
        }
        if (doBorder(info, Direction.ZMAX)) {
            int z = 15;
            for (int x = 0; x < 16; x++) {
                generateBorder(info, canDoParks, x, z, Direction.ZMAX.get(info));
            }
        }
    }

    /**
     * Fill base blocks under streets to bedrock
     */
    private void fillToBedrockStreetBlock(BuildingInfo info) {
        // Base blocks below streets
        int minHeight = info.minBuildHeight;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int y = info.getCityGroundLevel() - 1;
                driver.current(x, y, z);
                while (driver.getY() > (minHeight + info.profile.BEDROCK_LAYER) && isEmpty(driver.getBlock())) {
                    driver.block(base);
                    driver.decY();
                }
//                driver.setBlockRange(x, info.profile.BEDROCK_LAYER, z, info.getCityGroundLevel(), baseChar);
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
                driver.current(x, y, z);
                while (y > 1 && driver.getBlock() == air) {
                    driver.block(base).decY();
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
                driver.setBlockRange(x, info.getCityGroundLevel() - (offset - 1), z, info.getCityGroundLevel(), base);
                driver.current(x, info.getCityGroundLevel() - offset, z).block(border);
            }
        }
    }

    /**
     * Generate a single border column for one side of a street block
     */
    private void generateBorder(BuildingInfo info, boolean canDoParks, int x, int z, BuildingInfo adjacent) {
        Character borderBlock = info.getCityStyle().getBorderBlock();
        Character wallBlock = info.getCityStyle().getWallBlock();
        BlockState wall = info.getCompiledPalette().get(wallBlock);

        switch (info.profile.LANDSCAPE_TYPE) {
            case DEFAULT, SPHERES -> {
                ChunkHeightmap heightmap = getHeightmap(info.coord, info.provider.getWorld());
                int y = getMinHeightAt(info, x, z);
                if (y < info.getCityGroundLevel()+1) {
                    // We are above heightmap level. Generated a border from that level to our ground level
                    setBlocksFromPalette(x, y-1, z, info.getCityGroundLevel() + 1, info.getCompiledPalette(), borderBlock);
                } else {
                    // We are below heightmap level. Generate a thin border anyway
                    setBlocksFromPalette(x, info.getCityGroundLevel()-3, z, info.getCityGroundLevel() + 1, info.getCompiledPalette(), borderBlock);
                }
            }
            case SPACE -> {
                int adjacentY = info.getCityGroundLevel() - 8;
                if (adjacent.isCity) {
                    adjacentY = Math.min(adjacentY, adjacent.getCityGroundLevel());
                } else {
                    ChunkHeightmap adjacentHeightmap = getHeightmap(adjacent.coord, provider.getWorld());
                    int minimumHeight = adjacentHeightmap.getMinimumHeight();
                    adjacentY = Math.min(adjacentY, minimumHeight - 2);
                }

                if (adjacentY > 5) {
                    setBlocksFromPalette(x, adjacentY, z, info.getCityGroundLevel() + 1, info.getCompiledPalette(), borderBlock);
                }
            }
            case FLOATING -> {
                setBlocksFromPalette(x, info.getCityGroundLevel() - 3, z, info.getCityGroundLevel() + 1, info.getCompiledPalette(), borderBlock);
                if (isCorner(x, z)) {
                    generateBorderSupport(info, wall, x, z, 3);
                }
            }
            case CAVERN -> {
                setBlocksFromPalette(x, info.getCityGroundLevel() - 2, z, info.getCityGroundLevel() + 1, info.getCompiledPalette(), borderBlock);
                if (isCorner(x, z)) {
                    generateBorderSupport(info, wall, x, z, 2);
                }
            }
        }
        if (canDoParks) {
            if (!borderNeedsConnectionToAdjacentChunk(info, x, z)) {
                driver.current(x, info.getCityGroundLevel() + 1, z).block(wall);
            } else {
                driver.current(x, info.getCityGroundLevel() + 1, z).block(air);
            }
        }
    }

    /**
     * Generate a column of wall blocks (and stone below that in water)
     */
    private void generateBorderSupport(BuildingInfo info, BlockState wall, int x, int z, int offset) {
        ChunkHeightmap heightmap = getHeightmap(info.coord, provider.getWorld());
        int height = heightmap.getHeight(x, z);
        if (height > 1) {
            // None void
            int y = info.getCityGroundLevel() - offset - 1;
            driver.current(x, y, z);
            while (y > 1 && driver.getBlock() == air) {
                driver.block(wall).decY();
                y--;
            }
            while (y > 1 && driver.getBlock() == liquid) {
                driver.block(base).decY();
                y--;
            }
        }
    }

    private int generateFrontPart(BuildingInfo info, int height, BuildingInfo adj, Transform rot) {
        if (info.hasFrontPartFrom(adj)) {
            return generatePart(adj, adj.frontType, rot, 0, height, 0, false);
        }
        return height;
    }

    private void generateCorridors(BuildingInfo info, boolean xRail, boolean zRail) {
        BlockState railx = Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST);
        BlockState railz = Blocks.RAIL.defaultBlockState();

        Character corridorRoofBlock = info.getCityStyle().getCorridorRoofBlock();
        Character corridorGlassBlock = info.getCityStyle().getCorridorGlassBlock();
        CompiledPalette palette = info.getCompiledPalette();

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                BlockState b;
                if ((xRail && z >= 7 && z <= 10) || (zRail && x >= 7 && x <= 10)) {
                    int height = info.groundLevel - 6;
                    if (xRail && z == 10) {
                        b = railx;
                    } else if (zRail && x == 10) {
                        b = railz;
                    } else {
                        b = air;
                    }
                    driver.current(x, height, z).add(palette.get(corridorRoofBlock)).add(b).add(air).add(air);

                    if ((xRail && x == 7 && (z == 8 || z == 9)) || (zRail && z == 7 && (x == 8 || x == 9))) {
                        driver.add(palette.get(corridorGlassBlock));
                        BlockPos pos = driver.getCurrentCopy();
                        Character glowstoneChar = info.getCityStyle().getGlowstoneBlock();
                        BlockState glowstone = glowstoneChar == null ? Blocks.GLOWSTONE.defaultBlockState() : palette.get(glowstoneChar);
                        driver.add(glowstone);
                        updateNeeded(info, pos);
                    } else {
                        BlockState roof = palette.get(corridorRoofBlock);
                        driver.add(roof).add(roof);
                    }
                } else {
                    driver.setBlockRange(x, info.groundLevel - 5, z, info.getCityGroundLevel(), base);
                }
            }
        }
    }

    private static final Random VEGETATION_RAND = new Random();
    private void generateRandomVegetation(BuildingInfo info, int height) {
        VEGETATION_RAND.setSeed(provider.getSeed() * 377 + info.chunkZ * 341873128712L + info.chunkX * 132897987541L);
        VEGETATION_RAND.nextFloat();
        VEGETATION_RAND.nextFloat();

        if (info.getXmin().hasBuilding) {
            for (int x = 0; x < info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS; x++) {
                for (int z = 0; z < 16; z++) {
                    driver.current(x, height, z);
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    // It's ok to only go down to 0 as we are not expecting to go lower then that
                    while (driver.getBlockDown() == air && driver.getY() > 0) {
                        driver.decY();
                    }
                    float v = Math.min(.8f, info.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS + 1 - x));
                    int cnt = 0;
                    while (VEGETATION_RAND.nextFloat() < v && cnt < 30) {
                        driver.add(getRandomLeaf(info, info.getCompiledPalette()));
                        cnt++;
                    }
                }
            }
        }
        if (info.getXmax().hasBuilding) {
            for (int x = 15 - info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS; x < 15; x++) {
                for (int z = 0; z < 16; z++) {
                    driver.current(x, height, z);
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    // It's ok to only go down to 0 as we are not expecting to go lower then that
                    while (driver.getBlockDown() == air && driver.getY() > 0) {
                        driver.decY();
                    }
                    float v = Math.min(.8f, info.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (x - 14 + info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS));
                    int cnt = 0;
                    while (VEGETATION_RAND.nextFloat() < v && cnt < 30) {
                        driver.add(getRandomLeaf(info, info.getCompiledPalette()));
                        cnt++;
                    }
                }
            }
        }
        if (info.getZmin().hasBuilding) {
            for (int z = 0; z < info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS; z++) {
                for (int x = 0; x < 16; x++) {
                    driver.current(x, height, z);
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    // It's ok to only go down to 0 as we are not expecting to go lower then that
                    while (driver.getBlockDown() == air && driver.getY() > 0) {
                        driver.decY();
                    }
                    float v = Math.min(.8f, info.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS + 1 - z));
                    int cnt = 0;
                    while (VEGETATION_RAND.nextFloat() < v && cnt < 30) {
                        driver.add(getRandomLeaf(info, info.getCompiledPalette()));
                        cnt++;
                    }
                }
            }
        }
        if (info.getZmax().hasBuilding) {
            for (int z = 15 - info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS; z < 15; z++) {
                for (int x = 0; x < 16; x++) {
                    driver.current(x, height, z);
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    // It's ok to only go down to 0 as we are not expecting to go lower then that
                    while (driver.getBlockDown() == air && driver.getY() > 0) {
                        driver.decY();
                    }
                    float v = info.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (z - 14 + info.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS);
                    int cnt = 0;
                    while (VEGETATION_RAND.nextFloat() < v && cnt < 30) {
                        driver.add(getRandomLeaf(info, info.getCompiledPalette()));
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
                            b = compiledPalette.get(street);
                        }
                    } else {
                        b = compiledPalette.get(street);
                    }
                } else {
                    b = grass.get();
                }
                driver.current(x, height, z).block(b);
            }
        }
    }

    private void generateFullStreetSection(BuildingInfo info, int height) {
        CompiledPalette compiledPalette = info.getCompiledPalette();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                BlockState b = compiledPalette.get(isSide(x, z) ? street : street2);
                driver.current(x, height, z).block(b);
            }
        }
    }

    private void generateNormalStreetSection(BuildingInfo info, int height) {
//        char defaultStreet = info.profile.isFloating() ? street2 : streetBase;
        CompiledPalette compiledPalette = info.getCompiledPalette();
        BlockState b;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                if (isStreetBorder(x, z)) {
                    if (x <= streetBorder && z > streetBorder && z < (15 - streetBorder)
                            && (BuildingInfo.hasRoadConnection(info, info.getXmin()) || (info.getXmin().hasXBridge(provider) != null))) {
                        b = compiledPalette.get(street);
                    } else if (x >= (15 - streetBorder) && z > streetBorder && z < (15 - streetBorder)
                            && (BuildingInfo.hasRoadConnection(info, info.getXmax()) || (info.getXmax().hasXBridge(provider) != null))) {
                        b = compiledPalette.get(street);
                    } else if (z <= streetBorder && x > streetBorder && x < (15 - streetBorder)
                            && (BuildingInfo.hasRoadConnection(info, info.getZmin()) || (info.getZmin().hasZBridge(provider) != null))) {
                        b = compiledPalette.get(street);
                    } else if (z >= (15 - streetBorder) && x > streetBorder && x < (15 - streetBorder)
                            && (BuildingInfo.hasRoadConnection(info, info.getZmax()) || (info.getZmax().hasZBridge(provider) != null))) {
                        b = compiledPalette.get(street);
                    } else {
                        b = compiledPalette.get(streetBase);
                    }
                } else {
                    b = compiledPalette.get(street);
                }
                driver.current(x, height, z).block(b);
            }
        }
    }

    private boolean borderNeedsConnectionToAdjacentChunk(BuildingInfo info, int x, int z) {
        for (Direction direction : Direction.VALUES) {
            if (direction.atSide(x, z)) {
                BuildingInfo adjacent = direction.get(info);
                if (adjacent.getActualStairDirection() == direction.getOpposite()) {
                    BuildingPart stairType = adjacent.stairType;
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
                if (adjacent.hasBridge(provider, direction.getOrientation()) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generate a part. If 'airWaterLevel' is true then 'hard air' blocks are replaced with water below the waterLevel.
     * Otherwise they are replaced with air.
     */
    private int generatePart(BuildingInfo info, IBuildingPart part,
                             Transform transform,
                             int ox, int oy, int oz, boolean airWaterLevel) {
        if (profile.EDITMODE) {
            EditModeData.getData().addPartData(info.coord, oy, part.getName());
        }
        CompiledPalette compiledPalette = computePalette(info, part);

        boolean nowater = part.getMetaBoolean(ILostCities.META_NOWATER);

        for (int x = 0; x < part.getXSize(); x++) {
            for (int z = 0; z < part.getZSize(); z++) {
                char[] vs = part.getVSlice(x, z);
                if (vs != null) {
                    int rx = ox + transform.rotateX(x, z);
                    int rz = oz + transform.rotateZ(x, z);
                    driver.current(rx, oy, rz);
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
                                if (airWaterLevel && !info.profile.AVOID_WATER && !nowater) {
                                    b = (oy + y) < info.waterLevel ? liquid : air;
                                } else {
                                    b = air;
                                }
                            } else if (inf != null) {
                                if (inf.isTorch()) {
                                    if (info.profile.GENERATE_LIGHTING) {
                                        info.addTorchTodo(driver.getCurrentCopy());
                                    } else {
                                        b = air;        // No torches
                                    }
                                } else if (inf.loot() != null && !inf.loot().isEmpty()) {
                                    handleLoot(info, part, provider.getWorld(), b, inf);
                                } else if (inf.mobId() != null && !inf.mobId().isEmpty()) {
                                    b = handleSpawner(info, part, oy, provider.getWorld(), rx, rz, y, b, inf);
                                } else if (inf.tag() != null) {
                                    b = handleBlockEntity(info, part, oy, provider.getWorld(), rx, rz, y, b, inf);
                                }
                            } else if (getStatesNeedingLightingUpdate().contains(b)) {
                                BlockPos pos = driver.getCurrentCopy();
                                updateNeeded(info, pos);
                            } else if (getStatesNeedingTodo().contains(b)) {
                                b = handleTodo(info, oy, provider.getWorld(), rx, rz, y, b);
                            }
                            driver.add(b);
                        } else {
                            driver.incY();
                        }
                    }
                }
            }
        }
        return oy + part.getSliceCount();
    }

    private CompiledPalette computePalette(BuildingInfo info, IBuildingPart part) {
        CompiledPalette compiledPalette = info.getCompiledPalette();
        // Cache the combined palette?
        Palette partPalette = part.getLocalPalette(provider.getWorld());
        if (partPalette != null) {
            compiledPalette = new CompiledPalette(compiledPalette, partPalette);
        }
        return compiledPalette;
    }

    private BlockState handleBlockEntity(BuildingInfo info, IBuildingPart part, int oy, WorldGenLevel world, int rx, int rz, int y, BlockState b, Palette.Info inf) {
        BlockPos pos = new BlockPos(info.chunkX * 16 + rx, oy + y, info.chunkZ * 16 + rz);
        GlobalTodo.getData(world.getLevel()).addBlockEntityTodo(pos, b, inf.tag());
        return b;
    }

    private BlockState handleSpawner(BuildingInfo info, IBuildingPart part, int oy, WorldGenLevel world, int rx, int rz, int y, BlockState b, Palette.Info inf) {
        if (info.profile.GENERATE_SPAWNERS && !info.noLoot) {
            String mobid = inf.mobId();

            BlockPos pos = new BlockPos(info.chunkX * 16 + rx, oy + y, info.chunkZ * 16 + rz);
            ResourceLocation randomValue = getRandomSpawnerMob(world.getLevel(), rand, provider, info,
                    new BuildingInfo.ConditionTodo(mobid, part.getName(), info), pos);
            GlobalTodo.getData(world.getLevel()).addSpawnerTodo(pos, b, randomValue);
        } else {
            b = air;
        }
        return b;
    }

    private void handleLoot(BuildingInfo info, IBuildingPart part, WorldGenLevel world, BlockState b, Palette.Info inf) {
        if (!info.noLoot) {
            BlockPos pos = driver.getCurrentCopy();
            info.addPostTodo(pos, () -> {
                if (!world.getBlockState(pos).isAir()) {
                    world.setBlock(pos, b, Block.UPDATE_CLIENTS);
                    generateLoot(info, world, pos, new BuildingInfo.ConditionTodo(inf.loot(), part.getName(), info));
                }
            });
        }
    }

    private BlockState handleTodo(BuildingInfo info, int oy, WorldGenLevel world, int rx, int rz, int y, BlockState b) {
        Block block = b.getBlock();
        if (block instanceof SaplingBlock || block instanceof FlowerBlock) {
            if (info.profile.AVOID_FOLIAGE) {
                b = air;
            } else {
                BlockPos pos = new BlockPos(info.chunkX * 16 + rx, oy + y, info.chunkZ * 16 + rz);
                if (block instanceof SaplingBlock saplingBlock) {
                    BlockState finalB = b;
                    GlobalTodo.getData(world.getLevel()).addTodo(pos, (level) -> {
                        BlockState state = finalB.setValue(SaplingBlock.STAGE, 1);
                        if (level.isAreaLoaded(pos, 1)) {
                            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                            saplingBlock.advanceTree(level, pos, state, rand);
                        }
                    });
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

    public static void createSpawner(Level world, BlockPos pos, ResourceLocation randomEntity) {
        BlockEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof SpawnerBlockEntity spawner) {
            BaseSpawner logic = spawner.getSpawner();
            logic.setEntityId(ForgeRegistries.ENTITY_TYPES.getValue(randomEntity), world, world.random, pos);
            spawner.setChanged();
            if (Config.DEBUG) {
                ModSetup.getLogger().debug("generateLootSpawners: mob={} pos={}", randomEntity.toString(), pos);
            }
        } else if (tileentity != null) {
            ModSetup.getLogger().error("The mob spawner at ({}, {}, {}) has a TileEntity of incorrect type {}!", pos.getX(), pos.getY(), pos.getZ(), tileentity.getClass().getName());
        } else {
            ModSetup.getLogger().error("The mob spawner at ({}, {}, {}) is missing its TileEntity!", pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public static ResourceLocation getRandomSpawnerMob(Level world, RandomSource random, IDimensionInfo diminfo, BuildingInfo info, BuildingInfo.ConditionTodo todo, BlockPos pos) {
        String condition = todo.getCondition();
        Condition cnd = AssetRegistries.CONDITIONS.getOrThrow(world, condition);
        int level = (pos.getY() - diminfo.getProfile().GROUNDLEVEL) / FLOORHEIGHT;
        int floor = (pos.getY() - info.getCityGroundLevel()) / FLOORHEIGHT;
        ConditionContext conditionContext = new ConditionContext(level, floor, info.cellars, info.getNumFloors(),
                todo.getPart(), todo.getBuilding(), info.chunkX, info.chunkZ) {
            @Override
            public boolean isSphere() {
                return CitySphere.isInSphere(info.chunkX, info.chunkZ, pos, diminfo);
            }

            @Override
            public ResourceLocation getBiome() {
                return world.getBiome(pos).unwrap().map(ResourceKey::location, biome -> world.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome));
            }
        };
        String randomValue = cnd.getRandomValue(random, conditionContext);
        if (randomValue == null) {
            throw new RuntimeException("Condition '" + cnd.getName() + "' did not return a valid mob!");
        }
        return new ResourceLocation(randomValue);
    }


    private void generateLoot(BuildingInfo info, LevelAccessor world, BlockPos pos, BuildingInfo.ConditionTodo condition) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof RandomizableContainerBlockEntity) {
            if (this.provider.getProfile().GENERATE_LOOT) {
                createLoot(info, rand, world, pos, condition, this.provider);
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
        if (tileentity instanceof RandomizableContainerBlockEntity) {
            if (todo != null) {
                String lootTable = todo.getCondition();
                int level = (pos.getY() - diminfo.getProfile().GROUNDLEVEL) / FLOORHEIGHT;
                int floor = (pos.getY() - info.getCityGroundLevel()) / FLOORHEIGHT;
                ConditionContext conditionContext = new ConditionContext(level, floor, info.cellars, info.getNumFloors(),
                        todo.getPart(), todo.getBuilding(), info.chunkX, info.chunkZ) {
                    @Override
                    public boolean isSphere() {
                        return CitySphere.isInSphere(info.chunkX, info.chunkZ, pos, diminfo);
                    }

                    @Override
                    public ResourceLocation getBiome() {
                        return world.getBiome(pos).unwrap().map(ResourceKey::location, biome -> world.registryAccess().registryOrThrow(Registries.BIOME).getKey(biome));
                    }
                };
                String randomValue = AssetRegistries.CONDITIONS.getOrThrow(world, lootTable).getRandomValue(random, conditionContext);
//                ((LockableLootTileEntity) tileentity).setLootTable(new ResourceLocation(randomValue), random.nextLong());
//                tileentity.markDirty();
//                if (LostCityConfiguration.DEBUG) {
//                    LostCities.setup.getLogger().debug("createLootChest: loot=" + randomValue + " pos=" + pos.toString());
//                }
                RandomizableContainerBlockEntity.setLootTable(world, random, pos, new ResourceLocation(randomValue));
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
                if (h > info.maxBuildHeight-1) {
                    h = info.minBuildHeight-1;
                }

                CompiledPalette palette = info.getCompiledPalette();
                BlockState ironbarsState = Blocks.IRON_BARS.defaultBlockState();
                Character infobarsChar = info.getCityStyle().getIronbarsBlock();
                Supplier<BlockState> ironbars = infobarsChar == null ? () -> ironbarsState : () -> palette.get(infobarsChar);

                for (int i = 0; i < destroyedBlocks; i++) {
                    int x = rand.nextInt(16);
                    int z = rand.nextInt(16);
                    if (rand.nextFloat() < locationFactor.apply(x, z)) {
                        driver.current(x, h, z);
                        while (h > 0 && isEmpty(driver.getBlock())) {
                            h--;
                            driver.decY();
                        }
                        // Fix for FLOATING // @todo!
                        BlockState b;
                        if (rand.nextInt(5) == 0) {
                            b = ironbars.get();
                        } else {
                            b = adjacentPalette.get(rubbleBlock);     // Filler from adjacent building
                        }
                        driver.current(x, h + 1, z).block(b);
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
                int adjacentHeight = adjacentHeightmap.getAverageHeight();
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
            driver.setBlockRange(x, y, z, y2, b);
        } else {
            driver.current(x, y, z);
            while (y < y2) {
                driver.add(palette.get(character));
                y++;
            }
        }
    }

    private void generateBuilding(BuildingInfo info, ChunkHeightmap heightmap) {
        int min = info.minBuildHeight + 2;
        int max = info.maxBuildHeight - 2 - FLOORHEIGHT;

        int cellars = info.cellars;
        int floors = info.getNumFloors();
        int lowestLevel = info.getCityGroundLevel() - cellars * FLOORHEIGHT;

        // Fix lowest level so it goes above minimum build height
        while (lowestLevel <= min) {
            System.out.println("------------------------------ " + lowestLevel);
            lowestLevel += FLOORHEIGHT;
            cellars--;
            if (cellars < 0) {
                return;     // Bail out, this is a degenerate case
            }
        }

        while (info.getCityGroundLevel() + floors * FLOORHEIGHT >= max) {
            System.out.println("++++++++++++++++++++++++++++++ " + floors);
            floors--;
            if (floors < 0) {
                return;     // Bail out, this is a degenerate case
            }
        }

        CompiledPalette palette = info.getCompiledPalette();
        makeRoomForBuilding(info, lowestLevel, heightmap, palette);

        char fillerBlock = info.getBuilding().getFillerBlock();

        int height = lowestLevel;
        for (int f = -cellars; f <= floors; f++) {
            // In default landscape type we clear the landscape on top of the building when we are at the top floor
            if (f == floors) {
                if (profile.isDefault() || profile.isSpheres()) {
                    clearToMax(info, heightmap, height, max);
                }
            }

            BuildingPart part = info.getFloor(f);
            generatePart(info, part, Transform.ROTATE_NONE, 0, height, 0, false);
            part = info.getFloorPart2(f);
            if (part != null) {
                generatePart(info, part, Transform.ROTATE_NONE, 0, height, 0, false);
            }

            // Check for doors
            boolean isTop = f == floors;   // The top does not need generated doors
            if (!isTop && info.getAllowDoors()) {
                generateDoors(info, height + 1, f);
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
            generateCorridorConnections(info);
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
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    driver.current(x, info.maxBuildHeight - 1, z);
                    int minHeight = info.minBuildHeight;
                    while (driver.getBlock() == air && driver.getY() > minHeight) {
                        driver.decY();
                    }

                    int height = driver.getY();//heightmap.getHeight(x, z);
                    if (height > minHeight + 1 && height < lowestLevel - 1) {
                        driver.setBlockRange(x, height + 1, z, lowestLevel, base);
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
                        setBlocksFromPalette(x, lowestLevel-10, z, lowestLevel, palette, borderBlock);
                    }
                    if (driver.getBlock(x, lowestLevel, z) == air) {
                        BlockState filler = palette.get(fillerBlock);
                        driver.current(x, lowestLevel, z).block(filler); // There is nothing below so we fill this with the filler
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
                        int y = getMinHeightAt(info, x, z);
                        if (y >= lowestLevel) {
                            // The building generates below heightmap height. So we generate a border of 3 only
                            y = lowestLevel-3;
                        }
                        setBlocksFromPalette(x, y, z, lowestLevel, palette, borderBlock);
                    }
                    if (driver.getBlock(x, lowestLevel, z) == air) {
                        BlockState filler = palette.get(fillerBlock);
                        driver.current(x, lowestLevel, z).block(filler); // There is nothing below so we fill this with the filler
                    }

                    // Also clear the inside of buildings to avoid geometry that doesn't really belong there
                    clearRange(info, x, z, lowestLevel, info.getCityGroundLevel() + info.getNumFloors() * FLOORHEIGHT, info.waterLevel > info.groundLevel);
                }
            }
        }
    }

    private void clearToMax(BuildingInfo info, ChunkHeightmap heightmap, int height, int max) {
        int maximumHeight = Math.min(max, heightmap.getMaximumHeight() + 10);
        if (height < maximumHeight) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    clearRange(info, x, z, height, maximumHeight, false);
                }
            }
        }
    }

    // Used for space type worlds: fill underside the building/street until a block is encountered
    private void fillToGround(BuildingInfo info, int lowestLevel, Character borderBlock) {
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int y = lowestLevel - 1;
                driver.current(x, y, z);
                if (isSide(x, z)) {
                    while (y > 1 && driver.getBlock() == air) {
                        driver.block(info.getCompiledPalette().get(borderBlock)).decY();
                        y--;
                    }
                } else {
                    while (y > 1 && driver.getBlock() == air) {
                        driver.block(base).decY();
                        y--;
                    }
                }
            }
        }
    }

    private BlockState getDoor(Block door, boolean upper, boolean left, net.minecraft.core.Direction facing) {
        return door.defaultBlockState()
                .setValue(DoorBlock.HALF, upper ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.HINGE, left ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT)
                .setValue(DoorBlock.FACING, facing);
    }

    private void generateDoors(BuildingInfo info, int height, int f) {

        BlockState filler = info.getCompiledPalette().get(info.getBuilding().getFillerBlock());

        height--;       // Start generating doors one below for the filler

        if (info.hasConnectionAtX(f + info.cellars)) {
            int x = 0;
            if (hasConnectionWithBuilding(f, info, info.getXmin())) {
                driver.setBlockRange(x, height, 6, height + 4, filler);
                driver.setBlockRange(x, height, 9, height + 4, filler);

                driver.current(x, height, 7).add(filler).add(air).add(air).add(filler);
                driver.current(x, height, 8).add(filler).add(air).add(air).add(filler);

            } else if (hasConnectionToTopOrOutside(f, info, info.getXmin())) {
                driver.setBlockRange(x, height, 6, height + 4, filler);
                driver.setBlockRange(x, height, 9, height + 4, filler);

                driver.current(x, height, 7)
                        .add(filler)
                        .add(getDoor(info.doorBlock, false, true, net.minecraft.core.Direction.EAST))
                        .add(getDoor(info.doorBlock, true, true, net.minecraft.core.Direction.EAST))
                        .add(filler);
                driver.current(x, height, 8)
                        .add(filler)
                        .add(getDoor(info.doorBlock, false, false, net.minecraft.core.Direction.EAST))
                        .add(getDoor(info.doorBlock, true, false, net.minecraft.core.Direction.EAST))
                        .add(filler);
            }
        }
        if (hasConnectionWithBuildingMax(f, info, info.getXmax(), Orientation.X)) {
            int x = 15;
            driver.setBlockRange(x, height, 6, height + 4, filler);
            driver.setBlockRange(x, height, 9, height + 4, filler);
            driver.current(x, height, 7).add(filler).add(air).add(air).add(filler);
            driver.current(x, height, 8).add(filler).add(air).add(air).add(filler);
        } else if (hasConnectionToTopOrOutside(f, info, info.getXmax()) && (info.getXmax().hasConnectionAtXFromStreet(f + info.getXmax().cellars))) {
            int x = 15;
            driver.setBlockRange(x, height, 6, height + 4, filler);
            driver.setBlockRange(x, height, 9, height + 4, filler);
            driver.current(x, height, 7)
                    .add(filler)
                    .add(getDoor(info.doorBlock, false, false, net.minecraft.core.Direction.WEST))
                    .add(getDoor(info.doorBlock, true, false, net.minecraft.core.Direction.WEST))
                    .add(filler);
            driver.current(x, height, 8)
                    .add(filler)
                    .add(getDoor(info.doorBlock, false, true, net.minecraft.core.Direction.WEST))
                    .add(getDoor(info.doorBlock, true, true, net.minecraft.core.Direction.WEST))
                    .add(filler);
        }
        if (info.hasConnectionAtZ(f + info.cellars)) {
            int z = 0;
            if (hasConnectionWithBuilding(f, info, info.getZmin())) {
                driver.setBlockRange(6, height, z, height + 4, filler);
                driver.setBlockRange(9, height, z, height + 4, filler);
                driver.current(7, height, z).add(filler).add(air).add(air).add(filler);
                driver.current(8, height, z).add(filler).add(air).add(air).add(filler);
            } else if (hasConnectionToTopOrOutside(f, info, info.getZmin())) {
                driver.setBlockRange(6, height, z, height + 4, filler);
                driver.setBlockRange(9, height, z, height + 4, filler);
                driver.current(7, height, z)
                        .add(filler)
                        .add(getDoor(info.doorBlock, false, true, net.minecraft.core.Direction.NORTH))
                        .add(getDoor(info.doorBlock, true, true, net.minecraft.core.Direction.NORTH))
                        .add(filler);
                driver.current(8, height, z)
                        .add(filler)
                        .add(getDoor(info.doorBlock, false, false, net.minecraft.core.Direction.NORTH))
                        .add(getDoor(info.doorBlock, true, false, net.minecraft.core.Direction.NORTH))
                        .add(filler);
            }
        }
        if (hasConnectionWithBuildingMax(f, info, info.getZmax(), Orientation.Z)) {
            int z = 15;
            driver.setBlockRange(6, height, z, height + 4, filler);
            driver.setBlockRange(9, height, z, height + 4, filler);
            driver.current(7, height, z).add(filler).add(air).add(air).add(filler);
            driver.current(8, height, z).add(filler).add(air).add(air).add(filler);
        } else if (hasConnectionToTopOrOutside(f, info, info.getZmax()) && (info.getZmax().hasConnectionAtZFromStreet(f + info.getZmax().cellars))) {
            int z = 15;
            driver.setBlockRange(6, height, z, height + 4, filler);
            driver.setBlockRange(9, height, z, height + 4, filler);
            driver.current(7, height, z)
                    .add(filler)
                    .add(getDoor(info.doorBlock, false, false, net.minecraft.core.Direction.SOUTH))
                    .add(getDoor(info.doorBlock, true, false, net.minecraft.core.Direction.SOUTH))
                    .add(filler);
            driver.current(8, height, z)
                    .add(filler)
                    .add(getDoor(info.doorBlock, false, true, net.minecraft.core.Direction.SOUTH))
                    .add(getDoor(info.doorBlock, true, true, net.minecraft.core.Direction.SOUTH))
                    .add(filler);
        }
    }

    private void generateCorridorConnections(BuildingInfo info) {
        if (info.getXmin().hasXCorridor()) {
            int x = 0;
            for (int z = 7; z <= 10; z++) {
                driver.setBlockRangeToAir(x, info.groundLevel - 5, z, info.groundLevel - 2);
            }
        }
        if (info.getXmax().hasXCorridor()) {
            int x = 15;
            for (int z = 7; z <= 10; z++) {
                driver.setBlockRangeToAir(x, info.groundLevel - 5, z, info.groundLevel - 2);
            }
        }
        if (info.getZmin().hasZCorridor()) {
            int z = 0;
            for (int x = 7; x <= 10; x++) {
                driver.setBlockRangeToAir(x, info.groundLevel - 5, z, info.groundLevel - 2);
            }
        }
        if (info.getZmax().hasZCorridor()) {
            int z = 15;
            for (int x = 7; x <= 10; x++) {
                driver.setBlockRangeToAir(x, info.groundLevel - 5, z, info.groundLevel - 2);
            }
        }
    }

    private boolean hasConnectionWithBuildingMax(int localLevel, BuildingInfo info, BuildingInfo info2, Orientation x) {
        if (info.isValidFloor(localLevel) && info.getFloor(localLevel).getMetaBoolean(ILostCities.META_DONTCONNECT)) {
            return false;
        }
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        if (info2.isValidFloor(localAdjacent) && info2.getFloor(localAdjacent).getMetaBoolean(ILostCities.META_DONTCONNECT)) {
            return false;
        }
        int level = localAdjacent + info2.cellars;
        return info2.hasBuilding && ((localAdjacent >= 0 && localAdjacent < info2.getNumFloors()) || (localAdjacent < 0 && (-localAdjacent) <= info2.cellars)) && info2.hasConnectionAt(level, x);
    }

    private boolean hasConnectionToTopOrOutside(int localLevel, BuildingInfo info, BuildingInfo info2) {
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        if (info.getFloor(localLevel).getMetaBoolean(ILostCities.META_DONTCONNECT)) {
            return false;
        }
        return (info2.isCity && !info2.hasBuilding && localLevel == 0 && localAdjacent == 0) || (info2.hasBuilding && localAdjacent == info2.getNumFloors());
//        return (!info2.hasBuilding && localLevel == localAdjacent) || (info2.hasBuilding && localAdjacent == info2.getNumFloors());
    }

    private boolean hasConnectionWithBuilding(int localLevel, BuildingInfo info, BuildingInfo info2) {
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        return info2.hasBuilding && ((localAdjacent >= 0 && localAdjacent < info2.getNumFloors()) || (localAdjacent < 0 && (-localAdjacent) <= info2.cellars));
    }

    private boolean isSide(int x, int z) {
        return x == 0 || x == 15 || z == 0 || z == 15;
    }

    private boolean isCorner(int x, int z) {
        return (x == 0 || x == 15) && (z == 0 || z == 15);
    }

    private boolean isStreetBorder(int x, int z) {
        return x <= streetBorder || x >= (15 - streetBorder) || z <= streetBorder || z >= (15 - streetBorder);
    }

    private void updateNeeded(BuildingInfo info, BlockPos pos) {
        info.addPostTodo(pos, () -> {
            WorldGenLevel world = provider.getWorld();
            BlockState state = world.getBlockState(pos);
            if (!state.isAir()) {
                world.setBlock(pos, air, Block.UPDATE_CLIENTS);
                world.setBlock(pos, state, Block.UPDATE_CLIENTS);
            }
        });
    }

}
