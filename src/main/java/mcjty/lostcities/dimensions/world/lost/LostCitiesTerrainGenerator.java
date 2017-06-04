package mcjty.lostcities.dimensions.world.lost;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.BaseTerrainGenerator;
import mcjty.lostcities.dimensions.world.NormalTerrainGenerator;
import mcjty.lostcities.dimensions.world.lost.data.*;
import mcjty.lostcities.varia.GeometryTools;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class LostCitiesTerrainGenerator extends NormalTerrainGenerator {

    private final byte groundLevel;
    private final byte waterLevel;
    private static IBlockState bedrock;
    private static IBlockState air;
    private IBlockState baseBlock;
    private IBlockState baseLiquid;

    public static final ResourceLocation LOOT = new ResourceLocation(LostCities.MODID, "chests/lostcitychest");
    private static final int STREETBORDER = 3;


    public LostCitiesTerrainGenerator() {
        super();
        this.groundLevel = (byte) LostCityConfiguration.GROUNDLEVEL;
        this.waterLevel = (byte) (LostCityConfiguration.GROUNDLEVEL - LostCityConfiguration.WATERLEVEL_OFFSET);
    }


    private static Map<Character, Function<BuildingInfo, IBlockState>> mapping = null;
    private static Map<Pair<Integer,Integer>,GenInfo> genInfos = null;  // Pair is: <buildingType,floorType>

    // Use this random when it doesn't really matter i fit is generated the same every time
    private static Random globalRandom = new Random();

    public static Map<Character, Function<BuildingInfo, IBlockState>> getMapping() {
        if (mapping == null) {
            mapping = new HashMap<>();
            mapping.put('#', info -> {
                        if (globalRandom.nextFloat() < LostCityConfiguration.STYLE_CHANCE_CRACKED) {
                            return info.getStyle().bricks_cracked;
                        } else if (globalRandom.nextFloat() < LostCityConfiguration.STYLE_CHANCE_MOSSY) {
                            return info.getStyle().bricks_mossy;
                        } else {
                            return info.getStyle().bricks;
                        }
                    }
            );
            mapping.put('x', info -> {
                        if (globalRandom.nextFloat() < LostCityConfiguration.STYLE_CHANCE_CRACKED) {
                            return info.getStyle().bricks_cracked;
                        } else if (globalRandom.nextFloat() < LostCityConfiguration.STYLE_CHANCE_MOSSY) {
                            return info.getStyle().bricks_mossy;
                        } else {
                            return info.getStyle().bricks;
                        }
                    }
            );
            mapping.put('$', info -> info.getStyle().bricks_variant);
            mapping.put('=', info -> info.getStyle().glass);
            mapping.put('+', info -> info.getStyle().glass_full);
            mapping.put('@', info -> {
                        switch (info.glassType) {
                            case 0:
                                return info.getStyle().glass;
                            case 1:
                                return info.getStyle().street;
                            case 2:
                                return info.getStyle().bricks;
                            case 3:
                                return info.getStyle().quartz;
                            default:
                                return info.getStyle().glass;
                        }
                    }
            );
            mapping.put(' ', info -> Blocks.AIR.getDefaultState());
            mapping.put('l', info -> Blocks.LADDER.getDefaultState());
            mapping.put('1', info -> Blocks.PLANKS.getDefaultState());      // Monster spawner 1
            mapping.put('2', info -> Blocks.PLANKS.getDefaultState());      // Monster spawner 2
            mapping.put('3', info -> Blocks.PLANKS.getDefaultState());      // Monster spawner 3
            mapping.put('4', info -> Blocks.PLANKS.getDefaultState());      // Monster spawner 4
            mapping.put('C', info -> Blocks.PLANKS.getDefaultState());      // Chest
            mapping.put('M', info -> Blocks.PLANKS.getDefaultState());      // Modular storage
            mapping.put('F', info -> Blocks.PLANKS.getDefaultState());      // Random feature
            mapping.put('R', info -> Blocks.PLANKS.getDefaultState());      // Random rftools machine
            mapping.put(':', info -> Blocks.IRON_BARS.getDefaultState());
            mapping.put('D', info -> Blocks.DIRT.getDefaultState());
            mapping.put('G', info -> Blocks.GRASS.getDefaultState());
            mapping.put('p', info -> {
                        switch (globalRandom.nextInt(11)) {
                            case 0:
                            case 1:
                            case 2:
                                return Blocks.RED_FLOWER.getDefaultState();
                            case 3:
                            case 4:
                            case 5:
                                return Blocks.YELLOW_FLOWER.getDefaultState();
                            case 6:
                                return Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.ACACIA);
                            case 7:
                                return Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.BIRCH);
                            case 8:
                                return Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.OAK);
                            case 9:
                                return Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.SPRUCE);
                            default:
                                return air;
                        }
                    }
            );
            mapping.put('*', info -> Blocks.FLOWER_POT.getDefaultState());
            mapping.put('X', info -> info.getStyle().bricks_monster);
            mapping.put('Q', info -> Blocks.QUARTZ_BLOCK.getDefaultState());
            mapping.put('L', info -> Blocks.BOOKSHELF.getDefaultState());
            mapping.put('W', info -> Blocks.WATER.getDefaultState());
            mapping.put('w', info -> Blocks.COBBLESTONE_WALL.getDefaultState());
            mapping.put('S', info -> Blocks.DOUBLE_STONE_SLAB.getDefaultState());
            mapping.put('<', info -> Blocks.QUARTZ_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
            mapping.put('>', info -> Blocks.QUARTZ_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH));
            mapping.put('_', info -> Blocks.STONE_SLAB.getDefaultState());
            mapping.put('.', info -> Blocks.OAK_FENCE.getDefaultState());
            mapping.put('-', info -> Blocks.WOODEN_PRESSURE_PLATE.getDefaultState());
            mapping.put('%', info -> {
                if (globalRandom.nextFloat() < .3f) {
                    return Blocks.WEB.getDefaultState();
                } else {
                    return air;
                }
            });
        }
        return mapping;
    }

    public static Map<Pair<Integer, Integer>, GenInfo> getGenInfos() {
        if (genInfos == null) {
            genInfos = new HashMap<>();
            getGenInfos(FloorsData.FLOORS, 0);
            getGenInfos(FloorsData.FLOORS2, 1);
            getGenInfos(FloorsData.FLOORS3, 2);
            getGenInfos(LibraryData.LIBRARY00, 10);
            getGenInfos(LibraryData.LIBRARY10, 11);
            getGenInfos(LibraryData.LIBRARY01, 12);
            getGenInfos(LibraryData.LIBRARY11, 13);
            getGenInfos(DataCenterData.CENTER00, 20);
            getGenInfos(DataCenterData.CENTER10, 21);
            getGenInfos(DataCenterData.CENTER01, 22);
            getGenInfos(DataCenterData.CENTER11, 23);
        }
        return genInfos;
    }

    private static void getGenInfos(Level[] floors, int floorIdx) {
        for (int i = 0; i < floors.length; i++) {
            GenInfo gi = new GenInfo();
            Level level = floors[i];
            for (int y = 0; y < 6; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        Character c = level.getC(x, y, z);
                        if (c == '1') {
                            gi.addSpawnerType(new BlockPos(x, y, z), 1);
                        } else if (c == '2') {
                            gi.addSpawnerType(new BlockPos(x, y, z), 2);
                        } else if (c == '3') {
                            gi.addSpawnerType(new BlockPos(x, y, z), 3);
                        } else if (c == '4') {
                            gi.addSpawnerType(new BlockPos(x, y, z), 4);
                        } else if (c == 'C') {
                            gi.addChest(new BlockPos(x, y, z));
                        } else if (c == 'M') {
                            gi.addModularStorage(new BlockPos(x, y, z));
                        } else if (c == 'F') {
                            gi.addRandomFeatures(new BlockPos(x, y, z));
                        } else if (c == 'R') {
                            gi.addRandomRFToolsMachine(new BlockPos(x, y, z));
                        }
                    }
                }
            }
            genInfos.put(Pair.of(floorIdx, i), gi);
        }
    }

    @Override
    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        baseBlock = Blocks.STONE.getDefaultState(); // @todo provider.dimensionInformation.getBaseBlockForTerrain();
        baseLiquid = Blocks.WATER.getDefaultState(); // @todo provider.dimensionInformation.getFluidForTerrain().getDefaultState();

        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider.seed, provider);
        air = Blocks.AIR.getDefaultState();
        bedrock = Blocks.BEDROCK.getDefaultState();

        if (info.isCity) {
            doCityChunk(chunkX, chunkZ, primer, info);
        } else {
            doNormalChunk(chunkX, chunkZ, primer, info);
        }
        generateDebris(primer, provider.rand, info);
    }

    private void doNormalChunk(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        generateHeightmap(chunkX * 4, 0, chunkZ * 4);
        for (int x4 = 0; x4 < 4; ++x4) {
            int l = x4 * 5;
            int i1 = (x4 + 1) * 5;

            for (int z4 = 0; z4 < 4; ++z4) {
                int k1 = (l + z4) * 33;
                int l1 = (l + z4 + 1) * 33;
                int i2 = (i1 + z4) * 33;
                int j2 = (i1 + z4 + 1) * 33;

                for (int height32 = 0; height32 < 32; ++height32) {
                    double d1 = heightMap[k1 + height32];
                    double d2 = heightMap[l1 + height32];
                    double d3 = heightMap[i2 + height32];
                    double d4 = heightMap[j2 + height32];
                    double d5 = (heightMap[k1 + height32 + 1] - d1) * 0.125D;
                    double d6 = (heightMap[l1 + height32 + 1] - d2) * 0.125D;
                    double d7 = (heightMap[i2 + height32 + 1] - d3) * 0.125D;
                    double d8 = (heightMap[j2 + height32 + 1] - d4) * 0.125D;

                    for (int h = 0; h < 8; ++h) {
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * 0.25D;
                        double d13 = (d4 - d2) * 0.25D;
                        int height = (height32 * 8) + h;

                        for (int x = 0; x < 4; ++x) {
                            int index = ((x + (x4 * 4)) << 12) | ((0 + (z4 * 4)) << 8) | height;
                            short maxheight = 256;
                            index -= maxheight;
                            double d16 = (d11 - d10) * 0.25D;
                            double d15 = d10 - d16;

                            for (int z = 0; z < 4; ++z) {
                                index += maxheight;
                                if ((d15 += d16) > 0.0D) {
                                    IBlockState b = info.getDamageArea().damageBlock(baseBlock, height < waterLevel ? baseLiquid : air, provider.rand, cx + (x4 * 4) + x, height, cz + (z4 * 4) + z, index, info.getStyle());
                                    BaseTerrainGenerator.setBlockState(primer, index, b);
                                    // @todo find a way to support this 127 feature
//                                    if (baseMeta == 127) {
//                                        realMeta = (byte)((height/2 + x/2 + z/2) & 0xf);
//                                    } else {
//                                        realMeta = baseMeta;
//                                    }
                                } else if (height < waterLevel) {
                                    BaseTerrainGenerator.setBlockState(primer, index, baseLiquid);
                                }
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }

        flattenChunkToCityBorder(chunkX, chunkZ, primer, info);
        generateBridges(chunkX, chunkZ, primer, info);
    }

    private void generateBridges(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info) {
        int bt = info.hasXBridge(provider);
        if (bt >= 0) {
            int cx = chunkX * 16;
            int cz = chunkZ * 16;
            DamageArea damageArea = info.getDamageArea();
            Style style = info.getStyle();
            Level level = BridgeData.BRIDGES[bt];
            for (int x = 0 ; x < 16 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    int index = (x << 12) | (z << 8) + groundLevel + 1;
                    int height = groundLevel + 1;
                    int l = 0;
                    while (l < level.getFloor().length) {
                        IBlockState b = level.get(info, x, l, z);
                        b = damageArea.damageBlock(b, air, provider.rand, cx + x, height, cz + z, index, style);
                        BaseTerrainGenerator.setBlockState(primer, index++, b);
                        height++;
                        l++;
                    }
                }
            }
            if (info.getXmin().hasXBridge(provider) >= 0 && info.getXmax().hasXBridge(provider) >= 0) {
                // Needs support
                for (int y = waterLevel-10 ; y <= groundLevel ; y++) {
                    setBridgeSupport(primer, cx, cz, damageArea, style, 7, y, 7);
                    setBridgeSupport(primer, cx, cz, damageArea, style, 7, y, 8);
                    setBridgeSupport(primer, cx, cz, damageArea, style, 8, y, 7);
                    setBridgeSupport(primer, cx, cz, damageArea, style, 8, y, 8);
                }
            }
            if (info.getXmin().hasXBridge(provider) < 0) {
                // Connection to the side section
                int x = 0;
                for (int z = 6 ; z <= 9 ; z++) {
                    int index = (x << 12) | (z << 8) + groundLevel;
                    IBlockState b = damageArea.damageBlock(Blocks.STONEBRICK.getDefaultState(), air, provider.rand, cx + x, groundLevel, cz + z, index, style);
                    BaseTerrainGenerator.setBlockState(primer, index, b);
                }
            }
            if (info.getXmax().hasXBridge(provider) < 0) {
                // Connection to the side section
                int x = 15;
                for (int z = 6 ; z <= 9 ; z++) {
                    int index = (x << 12) | (z << 8) + groundLevel;
                    IBlockState b = damageArea.damageBlock(Blocks.STONEBRICK.getDefaultState(), air, provider.rand, cx + x, groundLevel, cz + z, index, style);
                    BaseTerrainGenerator.setBlockState(primer, index, b);
                }
            }
        } else {
            bt = info.hasZBridge(provider);
            if (bt >= 0) {
                int cx = chunkX * 16;
                int cz = chunkZ * 16;
                DamageArea damageArea = info.getDamageArea();
                Style style = info.getStyle();
                Level level = BridgeData.BRIDGES[bt];
                for (int x = 0 ; x < 16 ; x++) {
                    for (int z = 0 ; z < 16 ; z++) {
                        int index = (x << 12) | (z << 8) + groundLevel + 1;
                        int height = groundLevel + 1;
                        int l = 0;
                        while (l < level.getFloor().length) {
                            IBlockState b = level.get(info, z, l, x);       // Swap x/z
                            b = damageArea.damageBlock(b, air, provider.rand, cx + x, height, cz + z, index, style);
                            BaseTerrainGenerator.setBlockState(primer, index++, b);
                            height++;
                            l++;
                        }
                    }
                }
                if (info.getZmin().hasZBridge(provider) >= 0 && info.getZmax().hasZBridge(provider) >= 0) {
                    // Needs support
                    for (int y = waterLevel-10 ; y <= groundLevel ; y++) {
                        setBridgeSupport(primer, cx, cz, damageArea, style, 7, y, 7);
                        setBridgeSupport(primer, cx, cz, damageArea, style, 7, y, 8);
                        setBridgeSupport(primer, cx, cz, damageArea, style, 8, y, 7);
                        setBridgeSupport(primer, cx, cz, damageArea, style, 8, y, 8);
                    }
                }
                if (info.getZmin().hasZBridge(provider) < 0) {
                    // Connection to the side section
                    int z = 0;
                    for (int x = 6 ; x <= 9 ; x++) {
                        int index = (x << 12) | (z << 8) + groundLevel;
                        IBlockState b = damageArea.damageBlock(Blocks.STONEBRICK.getDefaultState(), air, provider.rand, cx + x, groundLevel, cz + z, index, style);
                        BaseTerrainGenerator.setBlockState(primer, index, b);
                    }
                }
                if (info.getZmax().hasZBridge(provider) < 0) {
                    // Connection to the side section
                    int z = 15;
                    for (int x = 6 ; x <= 9 ; x++) {
                        int index = (x << 12) | (z << 8) + groundLevel;
                        IBlockState b = damageArea.damageBlock(Blocks.STONEBRICK.getDefaultState(), air, provider.rand, cx + x, groundLevel, cz + z, index, style);
                        BaseTerrainGenerator.setBlockState(primer, index, b);
                    }
                }
            }
        }
    }

    private void setBridgeSupport(ChunkPrimer primer, int cx, int cz, DamageArea damageArea, Style style, int x, int y, int z) {
        int index = (x << 12) | (z << 8) + y;
        IBlockState b = damageArea.damageBlock(Blocks.STONEBRICK.getDefaultState(), air, provider.rand, cx + x, y, cz + z, index, style);
        BaseTerrainGenerator.setBlockState(primer, index, b);
    }

    private void flattenChunkToCityBorder(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        int level = groundLevel;
        if (isOcean(provider.biomesForGeneration)) {
            // We have an ocean biome here. Flatten to a lower level
            level = waterLevel + 4;
        }

        List<GeometryTools.AxisAlignedBB2D> boxes = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) {
                    int ccx = chunkX + x;
                    int ccz = chunkZ + z;
                    BuildingInfo info2 = BuildingInfo.getBuildingInfo(ccx, ccz, provider.seed, provider);
                    if (info2.isCity) {
                        boxes.add(new GeometryTools.AxisAlignedBB2D(ccx * 16, ccz * 16, ccx * 16 + 15, ccz * 16 + 15));
                    }
                }
            }
        }
        if (!boxes.isEmpty()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    double mindist = 1000000000.0;
                    for (GeometryTools.AxisAlignedBB2D box : boxes) {
                        double dist = GeometryTools.squaredDistanceBoxPoint(box, cx + x, cz + z);
                        if (dist < mindist) {
                            mindist = dist;
                        }
                    }
                    int offset = (int) (Math.sqrt(mindist) * 2);
                    flattenChunkBorder(primer, x, offset, z, provider.rand, info, cx, cz, level);
                }
            }
        }
    }

    public static boolean isOcean(Biome[] biomes) {
        for (Biome biome : biomes) {
            if (biome != Biomes.OCEAN && biome != Biomes.DEEP_OCEAN && biome != Biomes.FROZEN_OCEAN) {
                return false;
            }
        }
        return true;
    }

    public static boolean isWaterBiome(LostCityChunkGenerator provider, int chunkX, int chunkZ) {
        Biome[] biomes = provider.worldObj.getBiomeProvider().getBiomesForGeneration(null, (chunkX - 1) * 4 - 2, chunkZ * 4 - 2, 10, 10);
        return isWaterBiome(biomes);
    }

    public static boolean isWaterBiome(Biome[] biomes) {
        for (Biome biome : biomes) {
            if (biome != Biomes.OCEAN && biome != Biomes.DEEP_OCEAN && biome != Biomes.FROZEN_OCEAN
                    && biome != Biomes.RIVER && biome != Biomes.FROZEN_RIVER && biome != Biomes.BEACH && biome != Biomes.COLD_BEACH) {
                return false;
            }
        }
        return true;
    }

    private void flattenChunkBorder(ChunkPrimer primer, int x, int offset, int z, Random rand, BuildingInfo info, int cx, int cz, int level) {
        int index = (x << 12) | (z << 8);
        for (int y = 0; y <= (level - offset - rand.nextInt(2)); y++) {
            IBlockState b = BaseTerrainGenerator.getBlockState(primer, index);
            if (b != bedrock) {
                if (b != baseBlock) {
                    b = info.getDamageArea().damageBlock(baseBlock, y < waterLevel ? baseLiquid : air, provider.rand, cx + x, y, cz + z, index, info.getStyle());
                    BaseTerrainGenerator.setBlockState(primer, index, b);
                }
            }
            index++;
        }
        int r = rand.nextInt(2);
        index = (x << 12) | (z << 8) + level + offset + r;
        for (int y = level + offset + 3; y < 256; y++) {
            IBlockState b = BaseTerrainGenerator.getBlockState(primer, index);
            if (b != air) {
                BaseTerrainGenerator.setBlockState(primer, index, air);
            }
            index++;
        }
    }

    private void doCityChunk(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info) {
        boolean building = info.hasBuilding;

        Random rand = new Random(provider.seed * 377 + chunkZ * 341873128712L + chunkX * 132897987541L);
        rand.nextFloat();
        rand.nextFloat();

        int index = 0;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {

                int height = 0;
                while (height < LostCityConfiguration.BEDROCK_LAYER) {
                    BaseTerrainGenerator.setBlockState(primer, index++, bedrock);
                    height++;
                }

                while (height < LostCityConfiguration.BEDROCK_LAYER + 30 + rand.nextInt(3)) {
                    BaseTerrainGenerator.setBlockState(primer, index++, baseBlock);
                    height++;
                }

                if (building) {
                    index = generateBuilding(primer, info, rand, chunkX, chunkZ, index, x, z, height);
                } else {
                    index = generateStreet(primer, info, rand, chunkX, chunkZ, index, x, z, height);
                }
            }
        }

        if (building) {
            if (info.getDamageArea().hasExplosions()) {
                fixAfterExplosion(primer, info, rand);
            }
        }
    }

    private static class Blob {
        private final int starty;
        private final int endy;
        private final Set<Integer> connectedBlocks = new HashSet<>();
        private int connections = 0;
        private int lowestY;

        public Blob(int starty, int endy) {
            this.starty = starty;
            this.endy = endy;
            lowestY = 256;
        }

        public boolean contains(int index) {
            return connectedBlocks.contains(index);
        }

        public boolean destroyOrMoveThis() {
            return ((float) connections  / connectedBlocks.size()) < LostCityConfiguration.DESTROY_LONE_BLOCKS_FACTOR;
        }

        private boolean isOutside(BuildingInfo info, int x, int y, int z) {
            if (x < 0) {
                if (y <= info.getXmin().getMaxHeight()) {
                    connections++;
                }
                return true;
            }
            if (x > 15) {
                if (y <= info.getXmax().getMaxHeight()) {
                    connections++;
                }
                return true;
            }
            if (z < 0) {
                if (y <= info.getZmin().getMaxHeight()) {
                    connections++;
                }
                return true;
            }
            if (z > 15) {
                if (y <= info.getZmax().getMaxHeight()) {
                    connections++;
                }
                return true;
            }
            if (y < starty) {
                connections++;
                return true;
            }
            return false;
        }

        public void scan(BuildingInfo info, ChunkPrimer primer, char a, BlockPos pos) {
            Queue<BlockPos> todo = new ArrayDeque<>();
            todo.add(pos);

            while (!todo.isEmpty()) {
                pos = todo.poll();
                int index = calcIndex(pos.getX(), pos.getY(), pos.getZ());
                if (connectedBlocks.contains(index)) {
                    continue;
                }
                if (isOutside(info, pos.getX(), pos.getY(), pos.getZ())) {
                    continue;
                }
                if (primer.data[index] == a) {
                    continue;
                }
                connectedBlocks.add(index);
                if (pos.getY() < lowestY) {
                    lowestY = pos.getY();
                }
                todo.add(pos.up());
                todo.add(pos.down());
                todo.add(pos.east());
                todo.add(pos.west());
                todo.add(pos.south());
                todo.add(pos.north());
            }
        }

        private int calcIndex(int x, int y, int z) {
            return (x << 12) | (z << 8) + y;
        }
    }

    private Blob findBlob(List<Blob> blobs, int index) {
        for (Blob blob : blobs) {
            if (blob.contains(index)) {
                return blob;
            }
        }
        return null;
    }

    /// Fix floating blocks after an explosion
    private void fixAfterExplosion(ChunkPrimer primer, BuildingInfo info, Random rand) {
        int start = groundLevel - info.floorsBelowGround * 6;
        int end = groundLevel + (info.floors+2) * 6;
        char air = (char) Block.BLOCK_STATE_IDS.get(LostCitiesTerrainGenerator.air);
        char liquid = (char) Block.BLOCK_STATE_IDS.get(baseLiquid);

        List<Blob> blobs = new ArrayList<>();

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int index = (x << 12) | (z << 8) + start;
                for (int y = start ; y < end ; y++) {
                    char p = primer.data[index];
                    if (p != air) {
                        Blob blob = findBlob(blobs, index);
                        if (blob == null) {
                            blob = new Blob(start, end + 6);
                            blob.scan(info, primer, air, new BlockPos(x, y, z));
                            blobs.add(blob);
                        }
                    }
                    index++;
                }
            }
        }

        // Sort all blobs we delete with lowest first
        blobs.sort((o1, o2) -> {
            int y1 = o1.destroyOrMoveThis() ? o1.lowestY : 1000;
            int y2 = o2.destroyOrMoveThis() ? o2.lowestY : 1000;
            return y1-y2;
        });

        Blob blocksToMove = new Blob(0, 256);
        for (Blob blob : blobs) {
            if (!blob.destroyOrMoveThis()) {
                // The rest of the blobs doesn't have to be destroyed anymore
                break;
            }
            if (rand.nextFloat() < LostCityConfiguration.DESTROY_OR_MOVE_CHANCE && blob.connectedBlocks.size() < LostCityConfiguration.DESTROY_SMALL_SECTIONS_SIZE) {
                for (Integer index : blob.connectedBlocks) {
                    primer.data[index] = ((index&0xff) < waterLevel) ? liquid : air;
                }
            } else {
                for (Integer index : blob.connectedBlocks) {
                    blocksToMove.connectedBlocks.add(index);
                }
            }
        }
        for (Integer index : blocksToMove.connectedBlocks) {
            char c = primer.data[index];
            primer.data[index] = ((index&0xff) < waterLevel) ? liquid : air;
            index--;
            while (blocksToMove.contains(index) || primer.data[index] == air || primer.data[index] == liquid) {
                index--;
            }
            index++;
            primer.data[index] = c;
        }
    }

    private int generateStreet(ChunkPrimer primer, BuildingInfo info, Random rand, int chunkX, int chunkZ, int index, int x, int z, int height) {
        DamageArea damageArea = info.getDamageArea();
        Style style = info.getStyle();
        int cx = chunkX * 16;
        int cz = chunkZ * 16;
        boolean xRail = info.hasXCorridor();
        boolean zRail = info.hasZCorridor();

        boolean doOceanBorder = isDoOceanBorder(info, chunkX, chunkZ, x, z);

        while (height < groundLevel) {
            IBlockState railx = Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
            IBlockState railz = Blocks.RAIL.getDefaultState();
            IBlockState b = baseBlock;
            if (doOceanBorder) {
                b = Blocks.STONEBRICK.getDefaultState();
            } else if (height >= groundLevel - 5 && height <= groundLevel - 1) {
                if (height <= groundLevel - 2 && ((xRail && z >= 7 && z <= 10) || (zRail && x >= 7 && x <= 10))) {
                    b = air;
                    if (height == groundLevel - 5 && xRail && z == 10) {
                        b = railx;
                    }
                    if (height == groundLevel - 5 && zRail && x == 10) {
                        b = railz;
                    }
                    if (height == groundLevel - 2) {
                        if ((xRail && x == 7 && (z == 8 || z == 9)) || (zRail && z == 7 && (x == 8 || x == 9))) {
                            b = Blocks.GLASS.getDefaultState();
                        } else {
                            b = Blocks.STONEBRICK.getDefaultState();
                        }
                    }
                } else if (height == groundLevel - 1 && ((xRail && x == 7 && (z == 8 || z == 9)) || (zRail && z == 7 && (x == 8 || x == 9)))) {
                    b = Blocks.GLOWSTONE.getDefaultState();
                }
            }
//            BaseTerrainGenerator.setBlockState(primer, index++, height < waterLevel ? baseLiquid : damageArea.damageBlock(b, height < waterLevel ? baseLiquid : air, rand, cx + x, height, cz + z, index, style));
            BaseTerrainGenerator.setBlockState(primer, index++, damageArea.damageBlock(b, height < waterLevel ? baseLiquid : air, rand, cx + x, height, cz + z, index, style));
            height++;
        }

        IBlockState b;

        BuildingInfo.StreetType streetType = info.streetType;
        boolean elevated = info.isElevatedParkSection();
        if (elevated) {
            streetType = BuildingInfo.StreetType.PARK;
            BaseTerrainGenerator.setBlockState(primer, index++, damageArea.damageBlock(Blocks.STONEBRICK.getDefaultState(), air, rand, cx + x, height, cz + z, index, style));
            height++;
        }

        b = baseBlock;
        switch (streetType) {
            case NORMAL:
                if (isStreetBorder(x, z)) {
                    if (x <= STREETBORDER && z > STREETBORDER && z < (15 - STREETBORDER)
                            && (info.getXmin().doesRoadExtendTo() || (info.getXmin().hasXBridge(provider) >= 0))) {
                        b = style.street;
                    } else if (x >= (15 - STREETBORDER) && z > STREETBORDER && z < (15 - STREETBORDER)
                            && (info.getXmax().doesRoadExtendTo() || (info.getXmax().hasXBridge(provider) >= 0))) {
                        b = style.street;
                    } else if (z <= STREETBORDER && x > STREETBORDER && x < (15 - STREETBORDER)
                            && (info.getZmin().doesRoadExtendTo() || (info.getZmin().hasZBridge(provider) >= 0))) {
                        b = style.street;
                    } else if (z >= (15 - STREETBORDER) && x > STREETBORDER && x < (15 - STREETBORDER)
                            && (info.getZmax().doesRoadExtendTo() || (info.getZmax().hasZBridge(provider) >= 0))) {
                        b = style.street;
                    }
                } else {
                    b = style.street;
                }
                break;
            case FULL:
                if (isSide(x, z)) {
                    b = style.street;
                } else {
                    b = style.street2;
                }
                break;
            case PARK:
                if (x == 0 || x == 15 || z == 0 || z == 15) {
                    b = style.street;
                    if (elevated) {
                        boolean el00 = info.getXmin().getZmin().isElevatedParkSection();
                        boolean el10 = info.getZmin().isElevatedParkSection();
                        boolean el20 = info.getXmax().getZmin().isElevatedParkSection();
                        boolean el01 = info.getXmin().isElevatedParkSection();
                        boolean el21 = info.getXmax().isElevatedParkSection();
                        boolean el02 = info.getXmin().getZmax().isElevatedParkSection();
                        boolean el12 = info.getZmax().isElevatedParkSection();
                        boolean el22 = info.getXmax().getZmax().isElevatedParkSection();
                        if (x == 0 && z == 0) {
                            if (el01 && el00 && el10) {
                                b = Blocks.GRASS.getDefaultState();
                            }
                        }  else if (x == 15 && z == 0) {
                            if (el21 && el20 && el10) {
                                b = Blocks.GRASS.getDefaultState();
                            }
                        }  else if (x == 0 && z == 15) {
                            if (el01 && el02 && el12) {
                                b = Blocks.GRASS.getDefaultState();
                            }
                        }  else if (x == 15 && z == 15) {
                            if (el12 && el22 && el21) {
                                b = Blocks.GRASS.getDefaultState();
                            }
                        } else if (x == 0) {
                            if (el01) {
                                b = Blocks.GRASS.getDefaultState();
                            }
                        } else if (x == 15) {
                            if (el21) {
                                b = Blocks.GRASS.getDefaultState();
                            }
                        } else if (z == 0) {
                            if (el10) {
                                b = Blocks.GRASS.getDefaultState();
                            }
                        } else if (z == 15) {
                            if (el12) {
                                b = Blocks.GRASS.getDefaultState();
                            }
                        }
                    }
                } else {
                    b = Blocks.GRASS.getDefaultState();
                }
                break;
        }
        if (doOceanBorder) {
            b = Blocks.STONEBRICK.getDefaultState();
        }
        BaseTerrainGenerator.setBlockState(primer, index++, damageArea.damageBlock(b, air, rand, cx + x, height, cz + z, index, style));
        height++;

        if (streetType == BuildingInfo.StreetType.PARK || info.fountainType >= 0) {
            int l = 0;
            Level level = streetType == BuildingInfo.StreetType.PARK ? ParkData.PARKS[info.parkType] : FountainData.FOUNTAINS[info.fountainType];
            while (l < level.getFloor().length) {
                if (l == 0 && doOceanBorder) {
                    b = Blocks.COBBLESTONE_WALL.getDefaultState();
                } else {
                    b = level.get(info, x, l, z);
                }
                b = damageArea.damageBlock(b, air, rand, cx + x, height, cz + z, index, style);
                BaseTerrainGenerator.setBlockState(primer, index++, b);
                height++;
                l++;
            }
        } else if (doOceanBorder) {
            b = Blocks.COBBLESTONE_WALL.getDefaultState();
            b = damageArea.damageBlock(b, air, rand, cx + x, height, cz + z, index, style);
            BaseTerrainGenerator.setBlockState(primer, index++, b);
            height++;
        }

        int blocks = 256 - height;
        BaseTerrainGenerator.setBlockStateRange(primer, index, index + blocks, air);
        index += blocks;
        return index;
    }

    private void generateDebris(ChunkPrimer primer, Random rand, BuildingInfo info) {
        generateDebrisFromChunk(primer, rand, info.getXmin(), (xx, zz) -> (15.0f-xx) / 16.0f);
        generateDebrisFromChunk(primer, rand, info.getXmax(), (xx, zz) -> xx / 16.0f);
        generateDebrisFromChunk(primer, rand, info.getZmin(), (xx, zz) -> (15.0f-zz) / 16.0f);
        generateDebrisFromChunk(primer, rand, info.getZmax(), (xx, zz) -> zz / 16.0f);
        generateDebrisFromChunk(primer, rand, info.getXmin().getZmin(), (xx, zz) -> ((15.0f-xx) * (15.0f-zz)) / 256.0f);
        generateDebrisFromChunk(primer, rand, info.getXmax().getZmax(), (xx, zz) -> (xx * zz) / 256.0f);
        generateDebrisFromChunk(primer, rand, info.getXmin().getZmax(), (xx, zz) -> ((15.0f-xx) * zz) / 256.0f);
        generateDebrisFromChunk(primer, rand, info.getXmax().getZmin(), (xx, zz) -> (xx * (15.0f-zz)) / 256.0f);
    }

    private void generateDebrisFromChunk(ChunkPrimer primer, Random rand, BuildingInfo adjacentInfo, BiFunction<Integer, Integer, Float> locationFactor) {
        if (adjacentInfo.hasBuilding) {
            char air = (char) Block.BLOCK_STATE_IDS.get(LostCitiesTerrainGenerator.air);
            char liquid = (char) Block.BLOCK_STATE_IDS.get(baseLiquid);
            float damageFactor = adjacentInfo.getDamageArea().getDamageFactor();
            if (damageFactor > .5f) {
                // An estimate of the amount of blocks
                int blocks = (1 + adjacentInfo.floors) * 1000;
                float damage = Math.max(1.0f, damageFactor * DamageArea.BLOCK_DAMAGE_CHANCE);
                int destroyedBlocks = (int) (blocks * damage);
                // How many go this direction (approx, based on cardinal directions from building as well as number that simply fall down)
                destroyedBlocks /= LostCityConfiguration.DEBRIS_TO_NEARBYCHUNK_FACTOR;
                for (int i = 0; i < destroyedBlocks; i++) {
                    int x = rand.nextInt(16);
                    int z = rand.nextInt(16);
                    if (rand.nextFloat() < locationFactor.apply(x, z)) {
                        int index = (x << 12) | (z << 8) + 255;
                        while (primer.data[index] == air || primer.data[index] == liquid) {
                            index--;
                        }
                        index++;
                        IBlockState b;
                        switch (rand.nextInt(5)) {
                            case 0:
                                b = Blocks.IRON_BARS.getDefaultState();
                                break;
                            case 1:
                                b = adjacentInfo.getStyle().bricks;
                                break;
                            default:
                                b = adjacentInfo.getStyle().bricks_cracked;
                                break;
                        }
                        BaseTerrainGenerator.setBlockState(primer, index, b);
                    }
                }
            }
        }
    }

    private boolean isDoOceanBorder(BuildingInfo info, int chunkX, int chunkZ, int x, int z) {
        if (x == 0 && !info.getXmin().isCity && info.getXmin().hasXBridge(provider) < 0) {
            Biome[] biomes = provider.worldObj.getBiomeProvider().getBiomesForGeneration(null, (chunkX - 1) * 4 - 2, chunkZ * 4 - 2, 10, 10);
            if (isOcean(biomes)) {
                return true;
            }
        } else if (x == 15 && !info.getXmax().isCity && info.getXmax().hasXBridge(provider) < 0) {
            Biome[] biomes = provider.worldObj.getBiomeProvider().getBiomesForGeneration(null, (chunkX + 1) * 4 - 2, chunkZ * 4 - 2, 10, 10);
            if (isOcean(biomes)) {
                return true;
            }
        }
        if (z == 0 && !info.getZmin().isCity && info.getZmin().hasZBridge(provider) < 0) {
            Biome[] biomes = provider.worldObj.getBiomeProvider().getBiomesForGeneration(null, chunkX * 4 - 2, (chunkZ - 1) * 4 - 2, 10, 10);
            if (isOcean(biomes)) {
                return true;
            }
        } else if (z == 15 && !info.getZmax().isCity && info.getZmax().hasZBridge(provider) < 0) {
            Biome[] biomes = provider.worldObj.getBiomeProvider().getBiomesForGeneration(null, chunkX * 4 - 2, (chunkZ + 1) * 4 - 2, 10, 10);
            if (isOcean(biomes)) {
                return true;
            }
        }
        return false;
    }

    private int generateBuilding(ChunkPrimer primer, BuildingInfo info, Random rand, int chunkX, int chunkZ, int index, int x, int z, int height) {
        DamageArea damageArea = info.getDamageArea();
        Style style = info.getStyle();
        int cx = chunkX * 16;
        int cz = chunkZ * 16;
        int lowestLevel = groundLevel - info.floorsBelowGround * 6;
        int buildingtop = LostCityConfiguration.GROUNDLEVEL + 6 + info.floors * 6;
        boolean corridor;
        if (isSide(x, z)) {
            BuildingInfo adjacent = info.getAdjacent(x, z);
            corridor = (adjacent.hasXCorridor() || adjacent.hasZCorridor()) && isRailDoorway(x, z);
        } else {
            corridor = false;
        }

        while (height < lowestLevel) {
//            BaseTerrainGenerator.setBlockState(primer, index++, height < waterLevel ? baseLiquid : damageArea.damageBlock(baseBlock, air, rand, cx + x, height, cz + z, index, style));
            BaseTerrainGenerator.setBlockState(primer, index++, damageArea.damageBlock(baseBlock, height < waterLevel ? baseLiquid : air, rand, cx + x, height, cz + z, index, style));
            height++;
        }
        while (height < buildingtop) {
            IBlockState b;

            // Make a connection to a corridor if needed
            if (corridor && height >= groundLevel - 5 && height <= groundLevel - 3) {
                b = air;
            } else {
                b = getBlockForLevel(info, x, z, height);
                b = damageArea.damageBlock(b, height < waterLevel ? baseLiquid : air, rand, cx + x, height, cz + z, index, style);
            }

            BaseTerrainGenerator.setBlockState(primer, index++, b);
            height++;
        }
        while (height < buildingtop + 6) {
            int f = getFloor(height);
            int floortype = info.topType;
            Level level = info.getTopData(floortype);
            if (f >= level.getFloor().length) {
                break;
            }
            IBlockState b = level.get(info, x, f, z);
            b = damageArea.damageBlock(b, air, rand, cx + x, height, cz + z, index, style);
            BaseTerrainGenerator.setBlockState(primer, index++, b);
            height++;
        }
        int blocks = 256 - height;
        BaseTerrainGenerator.setBlockStateRange(primer, index, index + blocks, air);
        index += blocks;
        return index;
    }

    private IBlockState getBlockForLevel(BuildingInfo info, int x, int z, int height) {
        int f = getFloor(height);
        int l = getLevel(height);
//        boolean isFull = l == -1;      // The level directly underground has no windows
        Level[] floors = info.getFloorData();
        Level level = floors[info.floorTypes[l + info.floorsBelowGround]];
        IBlockState b = level.get(info, x, f, z);
        Style style = info.getStyle();

        // If we are underground, the block is glass, we are on the side and the chunk next to
        // us doesn't have a building or floor there we replace the glass with a solid block
        // Gravel will later be replaced with either glass or solid block so we have to count that too
//        if (isFull && (b == Blocks.GLASS.getDefaultState() || style.isGlass(b)) && isSide(x, z) && (!info.getAdjacent(x, z).hasBuilding || info.getAdjacent(x, z).floorsBelowGround == 0)) {
        if (l < 0 && (b == Blocks.GLASS.getDefaultState() || b == style.glass) && isSide(x, z) && (!info.getAdjacent(x, z).hasBuilding || info.getAdjacent(x, z).floorsBelowGround < -l)) {
            b = style.bricks;
        }

        // For buildings that have a style which causes gaps at the side we fill in that gap if we are
        // at ground level
        if (b == air && height == groundLevel && isSide(x, z)) {
            b = baseBlock;
        }

        // for buildings that have a hole in the bottom floor we fill that hole if we are
        // at the bottom of the building
        if (b == air && f == 0 && (l+info.floorsBelowGround) == 0) {
            b = style.bricks;
        }

        if (x == 0 && (z >= 6 && z <= 9) && f >= 1 && f <= 3 && info.hasConnectionAtX(l + info.floorsBelowGround)) {
            BuildingInfo info2 = info.getXmin();
            if (info2.hasBuilding && ((l >= 0 && l <= info2.floors) || (l < 0 && (-l) <= info2.floorsBelowGround))) {
                if (f == 3 || z == 6 || z == 9) {
                    b = style.bricks;
                } else {
                    b = air;
                }
            } else if ((!info2.hasBuilding && l == 0) || (info2.hasBuilding && l == info2.floors+1)) {
                if (f == 3 || z == 6 || z == 9) {
                    b = style.bricks;
                } else {
                    b = info.doorBlock.getDefaultState()
                            .withProperty(BlockDoor.HALF, f == 1 ? BlockDoor.EnumDoorHalf.LOWER : BlockDoor.EnumDoorHalf.UPPER)
                            .withProperty(BlockDoor.HINGE, z == 7 ? BlockDoor.EnumHingePosition.LEFT : BlockDoor.EnumHingePosition.RIGHT)
                            .withProperty(BlockDoor.FACING, EnumFacing.EAST);
                }
            }
        } else if (x == 15 && (z >= 6 && z <= 9) && f >= 1 && f <= 3) {
            BuildingInfo info2 = info.getXmax();
            if (info2.hasBuilding && ((l >= 0 && l <= info2.floors) || (l < 0 && (-l) <= info2.floorsBelowGround)) && info2.hasConnectionAtX(l + info2.floorsBelowGround)) {
                if (f == 3 || z == 6 || z == 9) {
                    b = style.bricks;
                } else {
                    b = air;
                }
            } else if (((!info2.hasBuilding && l == 0) || (info2.hasBuilding && l == info2.floors+1)) && info2.hasConnectionAtX(l + info2.floorsBelowGround)) {
                if (f == 3 || z == 6 || z == 9) {
                    b = style.bricks;
                } else {
                    b = info.doorBlock.getDefaultState()
                            .withProperty(BlockDoor.HALF, f == 1 ? BlockDoor.EnumDoorHalf.LOWER : BlockDoor.EnumDoorHalf.UPPER)
                            .withProperty(BlockDoor.HINGE, z == 8 ? BlockDoor.EnumHingePosition.LEFT : BlockDoor.EnumHingePosition.RIGHT)
                            .withProperty(BlockDoor.FACING, EnumFacing.WEST);
                }
            }
        }
        if (z == 0 && (x >= 6 && x <= 9) && f >= 1 && f <= 3 && info.hasConnectionAtZ(l + info.floorsBelowGround)) {
            BuildingInfo info2 = info.getZmin();
            if (info2.hasBuilding && ((l >= 0 && l <= info2.floors) || (l < 0 && (-l) <= info2.floorsBelowGround))) {
                if (f == 3 || x == 6 || x == 9) {
                    b = style.bricks;
                } else {
                    b = air;
                }
            } else if ((!info2.hasBuilding && l == 0) || (info2.hasBuilding && l == info2.floors+1)) {
                if (f == 3 || x == 6 || x == 9) {
                    b = style.bricks;
                } else {
                    b = info.doorBlock.getDefaultState()
                            .withProperty(BlockDoor.HALF, f == 1 ? BlockDoor.EnumDoorHalf.LOWER : BlockDoor.EnumDoorHalf.UPPER)
                            .withProperty(BlockDoor.HINGE, x == 8 ? BlockDoor.EnumHingePosition.LEFT : BlockDoor.EnumHingePosition.RIGHT)
                            .withProperty(BlockDoor.FACING, EnumFacing.SOUTH);
                }
            }
        } else if (z == 15 && (x >= 6 && x <= 9) && f >= 1 && f <= 3) {
            BuildingInfo info2 = info.getZmax();
            if (info2.hasBuilding && ((l >= 0 && l <= info2.floors) || (l < 0 && (-l) <= info2.floorsBelowGround)) && info2.hasConnectionAtZ(l + info2.floorsBelowGround)) {
                if (f == 3 || x == 6 || x == 9) {
                    b = style.bricks;
                } else {
                    b = air;
                }
            } else if (((!info2.hasBuilding && l == 0) || (info2.hasBuilding && l == info2.floors+1)) && info2.hasConnectionAtZ(l + info2.floorsBelowGround)) {
                if (f == 3 || x == 6 || x == 9) {
                    b = style.bricks;
                } else {
                    b = info.doorBlock.getDefaultState()
                            .withProperty(BlockDoor.HALF, f == 1 ? BlockDoor.EnumDoorHalf.LOWER : BlockDoor.EnumDoorHalf.UPPER)
                            .withProperty(BlockDoor.HINGE, x == 7 ? BlockDoor.EnumHingePosition.LEFT : BlockDoor.EnumHingePosition.RIGHT)
                            .withProperty(BlockDoor.FACING, EnumFacing.NORTH);
                }
            }
        }
        boolean down = f == 0 && (l + info.floorsBelowGround) == 0;

        if (b.getBlock() == Blocks.LADDER && down) {
            b = style.bricks;
        }
        return b;
    }

    public static int getFloor(int height) {
        return (height - LostCityConfiguration.GROUNDLEVEL + 600) % 6;
    }

    public static int getLevel(int height) {
        return ((height - LostCityConfiguration.GROUNDLEVEL + 600) / 6) - 100;
    }

    private boolean isCorner(int x, int z) {
        return (x == 0 && z == 0) || (x == 0 && z == 15) || (x == 15 && z == 0) || (x == 15 && z == 15);
    }

    private boolean isSide(int x, int z) {
        return x == 0 || x == 15 || z == 0 || z == 15;
    }

    private boolean isStreetBorder(int x, int z) {
        return x <= STREETBORDER || x >= (15 - STREETBORDER) || z <= STREETBORDER || z >= (15 - STREETBORDER);
    }

    private boolean isRailDoorway(int x, int z) {
        if (x == 0 || x == 15) {
            return z >= 7 && z <= 10;
        }
        if (z == 0 || z == 15) {
            return x >= 7 && x <= 10;
        }
        return false;
    }
}
