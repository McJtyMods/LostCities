package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.dimensions.world.lost.*;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.dimensions.world.lost.cityassets.BuildingPart;
import mcjty.lostcities.dimensions.world.lost.cityassets.CompiledPalette;
import mcjty.lostcities.varia.GeometryTools;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.*;
import java.util.function.BiFunction;

public class LostCitiesTerrainGenerator extends NormalTerrainGenerator {

    private final byte groundLevel;
    private final byte waterLevel;
    private static IBlockState bedrock;
    public static char airChar;
    public static char hardAirChar;
    public static char bricksChar;
    public static char baseChar;
    public static char wallChar;
    public static char liquidChar;
    public static char leavesChar;
    public static char ironbarsChar;
    public static char ladderChar;
    public static char grassChar;
    public static char glassChar;
    public static char glassPaneChar;
    public static char stainedGlassChar;
    public static char stainedGlassPaneChar;
    public static char bedrockChar;
    public static char endportalChar;
    public static char endportalFrameChar;
    public static char torchChar;
    public static IBlockState air;
    public static IBlockState hardAir;  // Used in parts to carve out
    public static IBlockState water;

    private IBlockState baseBlock;
    private Character street;
    private Character streetBase;
    private Character street2;
    private Character bricks;
    private int streetBorder;


    public LostCitiesTerrainGenerator(LostCityChunkGenerator provider) {
        super(provider);
        this.groundLevel = (byte) provider.profile.GROUNDLEVEL;
        this.waterLevel = (byte) (provider.profile.WATERLEVEL);
    }


    // Use this random when it doesn't really matter i fit is generated the same every time
    public static Random globalRandom = new Random();

    // Note that for normal chunks this is called with a pre-filled in landscape primer
    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        baseBlock = Blocks.STONE.getDefaultState();

        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);

        // @todo this setup is not very clean
        air = Blocks.AIR.getDefaultState();
        hardAir = Blocks.COMMAND_BLOCK.getDefaultState();
        water = Blocks.WATER.getDefaultState();
        bedrock = Blocks.BEDROCK.getDefaultState();
        street = info.getCompiledPalette().get(info.getCityStyle().getStreetBlock());
        streetBase = info.getCompiledPalette().get(info.getCityStyle().getStreetBaseBlock());
        street2 = info.getCompiledPalette().get(info.getCityStyle().getStreetVariantBlock());
        streetBorder = (16 - info.getCityStyle().getStreetWidth()) / 2;

        airChar = (char) Block.BLOCK_STATE_IDS.get(air);
        hardAirChar = (char) Block.BLOCK_STATE_IDS.get(hardAir);
        bricksChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.STONEBRICK.getDefaultState());
        wallChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.COBBLESTONE_WALL.getDefaultState());
        baseChar = (char) Block.BLOCK_STATE_IDS.get(baseBlock);
        liquidChar = (char) Block.BLOCK_STATE_IDS.get(water);
        leavesChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.LEAVES.getDefaultState().withProperty(BlockLeaves.DECAYABLE, false));
        ironbarsChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.IRON_BARS.getDefaultState());
        ladderChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.LADDER.getDefaultState());
        grassChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GRASS.getDefaultState());
        glassChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GLASS.getDefaultState());
        glassPaneChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GLASS_PANE.getDefaultState());
        stainedGlassChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.STAINED_GLASS.getDefaultState());
        stainedGlassPaneChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.STAINED_GLASS_PANE.getDefaultState());
        bedrockChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.BEDROCK.getDefaultState());
        endportalChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.END_PORTAL.getDefaultState());
        endportalFrameChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.END_PORTAL_FRAME.getDefaultState());
        torchChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.TORCH.getDefaultState());

        // @todo This should not be hardcoded here
        bricks = info.getCompiledPalette().get('#');

        List<Integer> torches = new ArrayList<>();

        if (info.isCity) {
            doCityChunk(chunkX, chunkZ, primer, info, torches);
        } else {
            // We already have a prefilled core chunk (as generated from doCoreChunk)
            doNormalChunk(chunkX, chunkZ, primer, info, torches);
        }
        fixTorches(primer, torches);

        // We make a new random here because the primer for a normal chunk may have
        // been cached and we want to be able to do the same when returning from a cached
        // primer vs generating it here
        provider.rand.setSeed(chunkX * 257017164707L + chunkZ * 101754694003L);

        if (info.getDamageArea().hasExplosions()) {
            breakBlocksForDamage(chunkX, chunkZ, primer, info);
            fixAfterExplosion(primer, info, provider.rand);
        }
        generateDebris(primer, provider.rand, info);
    }

    private void fixTorches(ChunkPrimer primer, List<Integer> torches) {
        if (torches.isEmpty()) {
            return;
        }

        char tn = (char) Block.BLOCK_STATE_IDS.get(Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH));
        char ts = (char) Block.BLOCK_STATE_IDS.get(Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH));
        char tw = (char) Block.BLOCK_STATE_IDS.get(Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.WEST));
        char te = (char) Block.BLOCK_STATE_IDS.get(Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.EAST));
        char tu = (char) Block.BLOCK_STATE_IDS.get(Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.UP));

        for (Integer idx : torches) {
            if (primer.data[idx] == torchChar) {
                int x = (idx >> 12) & 0xf;
                int z = (idx >> 8) & 0xf;
                if (primer.data[idx-1] != airChar) {
                    primer.data[idx] = tu;
                } else if (x > 0 && primer.data[idx - (1<<12)] != airChar) {
                    primer.data[idx] = te;
                } else if (x < 15 && primer.data[idx + (1<<12)] != airChar) {
                    primer.data[idx] = tw;
                } else if (z > 0 && primer.data[idx - (1<<8)] != airChar) {
                    primer.data[idx] = ts;
                } else if (z < 15 && primer.data[idx + (1<<8)] != airChar) {
                    primer.data[idx] = tn;
                }
            }
        }
    }

    public void doCoreChunk(int chunkX, int chunkZ, ChunkPrimer primer) {
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
                                    BaseTerrainGenerator.setBlockState(primer, index, baseChar);
                                } else if (height < waterLevel) {
                                    BaseTerrainGenerator.setBlockState(primer, index, liquidChar);
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
    }

    public void doNormalChunk(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info, List<Integer> torches) {
        flattenChunkToCityBorder(chunkX, chunkZ, primer);
        generateBridges(primer, info);
        generateHighways(chunkX, chunkZ, primer, info, torches);
    }

    private void breakBlocksForDamage(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        DamageArea damageArea = info.getDamageArea();

        for (int yy = 0; yy < 16; yy++) {
            if (damageArea.hasExplosions(yy)) {
                if (damageArea.isCompletelyDestroyed(yy)) {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int index = (x << 12) | (z << 8) + yy * 16;
                            for (int y = 0; y < 16; y++) {
                                primer.data[index++] = airChar;
                            }
                        }
                    }
                } else {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int index = (x << 12) | (z << 8) + yy * 16;
                            for (int y = 0; y < 16; y++) {
                                char d = primer.data[index];
                                if (d != airChar) {
                                    Character newd = damageArea.damageBlock(d, provider, cx + x, yy * 16 + y, cz + z, info.getCompiledPalette());
                                    if (newd != d) {
                                        BaseTerrainGenerator.setBlockState(primer, index, newd);
                                    }
                                }
                                index++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateHighways(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info, List<Integer> torches) {
        int levelX = Highway.getXHighwayLevel(chunkX, chunkZ, provider);
        int levelZ = Highway.getZHighwayLevel(chunkX, chunkZ, provider);
        if (levelX == levelZ && levelX >= 0) {
            // Crossing
            generateHighwayPart(primer, info, levelX, Rotation.ROTATE_NONE, info.getXmax(), info.getZmax(), "_bi", torches);
        } else if (levelX >= 0 && levelZ >= 0) {
            // There are two highways on different level. Make sure the lowest one is done first because it
            // will clear out what is above it
            if (levelX == 0) {
                generateHighwayPart(primer, info, levelX, Rotation.ROTATE_NONE, info.getZmin(), info.getZmax(), "", torches);
                generateHighwayPart(primer, info, levelZ, Rotation.ROTATE_90, info.getXmax(), info.getXmax(), "", torches);
            } else {
                generateHighwayPart(primer, info, levelZ, Rotation.ROTATE_90, info.getXmax(), info.getXmax(), "", torches);
                generateHighwayPart(primer, info, levelX, Rotation.ROTATE_NONE, info.getZmin(), info.getZmax(), "", torches);
            }
        } else {
            if (levelX >= 0) {
                generateHighwayPart(primer, info, levelX, Rotation.ROTATE_NONE, info.getZmin(), info.getZmax(), "", torches);
            } else if (levelZ >= 0) {
                generateHighwayPart(primer, info, levelZ, Rotation.ROTATE_90, info.getXmax(), info.getXmax(), "", torches);
            }
        }
    }

    private void generateHighwayPart(ChunkPrimer primer, BuildingInfo info, int level, Rotation rotation, BuildingInfo adjacent1, BuildingInfo adjacent2, String suffix,
                                     List<Integer> torches) {
        int highwayGroundLevel = provider.profile.GROUNDLEVEL + level * 6;

        if (info.isTunnel(level)) {
            // We know we need a tunnel
            generatePart(primer, info, AssetRegistries.PARTS.get("highway_tunnel" + suffix), rotation, 0, highwayGroundLevel, 0, torches);
        } else if (info.isCity && level <= adjacent1.cityLevel && level <= adjacent2.cityLevel && adjacent1.isCity && adjacent2.isCity) {
            // Simple highway in the city
            int height = generatePart(primer, info, AssetRegistries.PARTS.get("highway_open" + suffix), rotation, 0, highwayGroundLevel, 0, torches);
            // Clear a bit more above the highway
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int index = (x << 12) | (z << 8) + height;
                    BaseTerrainGenerator.setBlockStateRange(primer, index, index + 15, air);
                }
            }
        } else {
            int height = generatePart(primer, info, AssetRegistries.PARTS.get("highway_bridge" + suffix), rotation, 0, highwayGroundLevel, 0, torches);
            // Clear a bit more above the highway
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int index = (x << 12) | (z << 8) + height;
                    BaseTerrainGenerator.setBlockStateRange(primer, index, index + 15, air);
                }
            }
        }

        // Make sure the bridge is supported if needed
        if (rotation == Rotation.ROTATE_NONE) {
            int index = highwayGroundLevel - 1; // (coordinate 0,0)
            for (int y = 0; y < 40; y++) {
                boolean done = false;
                if (primer.data[index] == airChar || primer.data[index] == liquidChar) {
                    BaseTerrainGenerator.setBlockState(primer, index, bricksChar);
                    done = true;
                }
                if (primer.data[index + (15 << 8)] == airChar || primer.data[index + (15 << 8)] == liquidChar) {
                    BaseTerrainGenerator.setBlockState(primer, index + (15 << 8), bricksChar);
                    done = true;
                }
                index--;
                if (!done) {
                    break;
                }
            }
        } else {
            int index = highwayGroundLevel - 1; // (coordinate 0,0)
            for (int y = 0; y < 40; y++) {
                boolean done = false;
                if (primer.data[index] == airChar || primer.data[index] == liquidChar) {
                    BaseTerrainGenerator.setBlockState(primer, index, bricksChar);
                    done = true;
                }
                if (primer.data[index + (15 << 12)] == airChar || primer.data[index + (15 << 12)] == liquidChar) {
                    BaseTerrainGenerator.setBlockState(primer, index + (15 << 12), bricksChar);
                    done = true;
                }
                index--;
                if (!done) {
                    break;
                }
            }
        }
    }

    private void generateBridges(ChunkPrimer primer, BuildingInfo info) {
        if (info.getHighwayXLevel() == 0 || info.getHighwayZLevel() == 0) {
            // If there is a highway at level 0 we cannot generate bridge parts. If there
            // is no highway or a highway at level 1 then bridge sections can generate just fine
            return;
        }
        BuildingPart bt = info.hasXBridge(provider);
        if (bt != null) {
            generateBridge(primer, info, bt, Orientation.X);
        } else {
            bt = info.hasZBridge(provider);
            if (bt != null) {
                generateBridge(primer, info, bt, Orientation.Z);
            }
        }
    }

    private void generateBridge(ChunkPrimer primer, BuildingInfo info, BuildingPart bt, Orientation orientation) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int index = (x << 12) | (z << 8) + groundLevel + 1;
                int l = 0;
                while (l < bt.getSliceCount()) {
                    Character b = orientation == Orientation.X ? bt.get(info, x, l, z) : bt.get(info, z, l, x); // @todo general rotation system?
                    BaseTerrainGenerator.setBlockState(primer, index++, b);
                    l++;
                }
            }
        }
        BuildingInfo minDir = orientation.getMinDir().get(info);
        BuildingInfo maxDir = orientation.getMaxDir().get(info);
        if (minDir.hasBridge(provider, orientation) != null && maxDir.hasBridge(provider, orientation) != null) {
            // Needs support
            for (int y = waterLevel - 10; y <= groundLevel; y++) {
                setBridgeSupport(primer, 7, y, 7);
                setBridgeSupport(primer, 7, y, 8);
                setBridgeSupport(primer, 8, y, 7);
                setBridgeSupport(primer, 8, y, 8);
            }
        }
        if (minDir.hasBridge(provider, orientation) == null) {
            // Connection to the side section
            if (orientation == Orientation.X) {
                int x = 0;
                for (int z = 6; z <= 9; z++) {
                    int index = (x << 12) | (z << 8) + groundLevel;
                    BaseTerrainGenerator.setBlockState(primer, index, bricksChar);
                }
            } else {
                int z = 0;
                for (int x = 6; x <= 9; x++) {
                    int index = (x << 12) | (z << 8) + groundLevel;
                    BaseTerrainGenerator.setBlockState(primer, index, bricksChar);
                }
            }
        }
        if (maxDir.hasBridge(provider, orientation) == null) {
            // Connection to the side section
            if (orientation == Orientation.X) {
                int x = 15;
                for (int z = 6; z <= 9; z++) {
                    int index = (x << 12) | (z << 8) + groundLevel;
                    BaseTerrainGenerator.setBlockState(primer, index, bricksChar);
                }
            } else {
                int z = 15;
                for (int x = 6; x <= 9; x++) {
                    int index = (x << 12) | (z << 8) + groundLevel;
                    BaseTerrainGenerator.setBlockState(primer, index, bricksChar);
                }
            }
        }
    }

    private void setBridgeSupport(ChunkPrimer primer, int x, int y, int z) {
        int index = (x << 12) | (z << 8) + y;
        BaseTerrainGenerator.setBlockState(primer, index, bricksChar);
    }

    private void flattenChunkToCityBorder(int chunkX, int chunkZ, ChunkPrimer primer) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        List<GeometryTools.AxisAlignedBB2D> boxes = new ArrayList<>();
        List<GeometryTools.AxisAlignedBB2D> boxesDownwards = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) {
                    int ccx = chunkX + x;
                    int ccz = chunkZ + z;
                    BuildingInfo info2 = BuildingInfo.getBuildingInfo(ccx, ccz, provider);
                    if (info2.isCity) {
                        GeometryTools.AxisAlignedBB2D box = new GeometryTools.AxisAlignedBB2D(ccx * 16, ccz * 16, ccx * 16 + 15, ccz * 16 + 15);
                        box.height = info2.getCityGroundLevel();
                        boxes.add(box);
                    } else if (info2.getMaxHighwayLevel() >= 0 && !info2.isTunnel(info2.getMaxHighwayLevel())) {
                        // There is a highway but no tunnel. So we need to smooth downwards
                        GeometryTools.AxisAlignedBB2D box = new GeometryTools.AxisAlignedBB2D(ccx * 16, ccz * 16, ccx * 16 + 15, ccz * 16 + 15);
                        box.height = provider.profile.GROUNDLEVEL + info2.getMaxHighwayLevel() * 6;
                        boxesDownwards.add(box);
                    }
                }
            }
        }
        if (!boxes.isEmpty()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    double mindist = 1000000000.0;
                    int minheight = 1000000000;
                    for (GeometryTools.AxisAlignedBB2D box : boxes) {
                        double dist = GeometryTools.squaredDistanceBoxPoint(box, cx + x, cz + z);
                        if (dist < mindist) {
                            mindist = dist;
                        }
                        if (box.height < minheight) {
                            minheight = box.height;
                        }
                    }
                    int height = minheight;//info.getCityGroundLevel();
                    if (isOcean(provider.biomesForGeneration)) {
                        // We have an ocean biome here. Flatten to a lower level
                        height = waterLevel + 4;
                    }

                    int offset = (int) (Math.sqrt(mindist) * 2);
                    flattenChunkBorder(primer, x, offset, z, provider.rand, height);
                }
            }
        }
        if (!boxesDownwards.isEmpty()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    double mindist = 1000000000.0;
                    int minheight = 1000000000;
                    for (GeometryTools.AxisAlignedBB2D box : boxesDownwards) {
                        double dist = GeometryTools.squaredDistanceBoxPoint(box, cx + x, cz + z);
                        if (dist < mindist) {
                            mindist = dist;
                        }
                        if (box.height < minheight) {
                            minheight = box.height;
                        }
                    }
                    int height = minheight;//info.getCityGroundLevel();
                    if (isOcean(provider.biomesForGeneration)) {
                        // We have an ocean biome here. Flatten to a lower level
                        height = waterLevel + 4;
                    }

                    int offset = (int) (Math.sqrt(mindist) * 2);
                    flattenChunkBorderDownwards(primer, x, offset, z, provider.rand, height);
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
        return isWaterBiome(biomes[55]) || isWaterBiome(biomes[54]) || isWaterBiome(biomes[56]);
//        return isWaterBiome(biomes);
    }

    public static boolean isWaterBiome(Biome[] biomes) {
        for (Biome biome : biomes) {
            if (!isWaterBiome(biome)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isWaterBiome(Biome biome) {
        return !(biome != Biomes.OCEAN && biome != Biomes.DEEP_OCEAN && biome != Biomes.FROZEN_OCEAN
                && biome != Biomes.RIVER && biome != Biomes.FROZEN_RIVER && biome != Biomes.BEACH && biome != Biomes.COLD_BEACH);
    }

    private void flattenChunkBorder(ChunkPrimer primer, int x, int offset, int z, Random rand, int level) {
        int index = (x << 12) | (z << 8);
        for (int y = 0; y <= (level - offset - rand.nextInt(2)); y++) {
            IBlockState b = BaseTerrainGenerator.getBlockState(primer, index);
            if (b != bedrock) {
                if (b != baseBlock) {
                    b = baseBlock;
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

    private void flattenChunkBorderDownwards(ChunkPrimer primer, int x, int offset, int z, Random rand, int level) {
        int r = rand.nextInt(2);
        int index = (x << 12) | (z << 8) + level + offset + r;
        for (int y = level + offset + 3; y < 256; y++) {
            IBlockState b = BaseTerrainGenerator.getBlockState(primer, index);
            if (b != air) {
                BaseTerrainGenerator.setBlockState(primer, index, air);
            }
            index++;
        }
    }

    private void doCityChunk(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info, List<Integer> torches) {
        boolean building = info.hasBuilding;

        Random rand = new Random(provider.seed * 377 + chunkZ * 341873128712L + chunkX * 132897987541L);
        rand.nextFloat();
        rand.nextFloat();

        int index = 0;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {

                int height = 0;
                while (height < provider.profile.BEDROCK_LAYER) {
                    BaseTerrainGenerator.setBlockState(primer, index++, bedrock);
                    height++;
                }

                while (height < provider.profile.BEDROCK_LAYER + 30) {
                    BaseTerrainGenerator.setBlockState(primer, index++, baseChar);
                    height++;
                }

                if (building) {
                    index = generateBuilding(primer, info, index, x, z, height, torches);
                } else {
                    index = generateStreet(primer, info, rand, index, x, z, height, torches);
                }
            }
        }

        if (!building) {
            int levelX = info.getHighwayXLevel();
            int levelZ = info.getHighwayZLevel();
            if (levelX < 0 && levelZ < 0) {
                generateStreetDecorations(primer, info, torches);
            } else {
                generateHighways(chunkX, chunkZ, primer, info, torches);
            }
        }
    }

    private void generateStreetDecorations(ChunkPrimer primer, BuildingInfo info, List<Integer> torches) {
        Direction stairDirection = info.getActualStairDirection();
        if (stairDirection != null) {
            BuildingPart stairs = info.stairType;
            Rotation rotation;
            int oy = info.getCityGroundLevel() + 1;
            switch (stairDirection) {
                case XMIN:
                    rotation = Rotation.ROTATE_NONE;
                    break;
                case XMAX:
                    rotation = Rotation.ROTATE_180;
                    break;
                case ZMIN:
                    rotation = Rotation.ROTATE_90;
                    break;
                case ZMAX:
                    rotation = Rotation.ROTATE_270;
                    break;
                default:
                    throw new RuntimeException("Cannot happen!");
            }

            generatePart(primer, info, stairs, rotation, 0, oy, 0, torches);
        }
    }

    private static class Blob {
        private final int starty;
        private final int endy;
        private final Set<Integer> connectedBlocks = new HashSet<>();
        private final Map<Integer, Integer> blocksPerY = new HashMap<>();
        private int connections = 0;
        private int lowestY;
        private int highestY;
        private float avgdamage;
        private int cntMindamage;  // Number of blocks that receive almost no damage

        public Blob(int starty, int endy) {
            this.starty = starty;
            this.endy = endy;
            lowestY = 256;
            highestY = 0;
        }

        public float getAvgdamage() {
            return avgdamage;
        }

        public int getCntMindamage() {
            return cntMindamage;
        }

        public boolean contains(int index) {
            return connectedBlocks.contains(index);
        }

        public int getLowestY() {
            return lowestY;
        }

        public int getHighestY() {
            return highestY;
        }

        public Set<Integer> cut(int y) {
            Set<Integer> toRemove = new HashSet<>();
            for (Integer block : connectedBlocks) {
                if ((block & 255) >= y) {
                    toRemove.add(block);
                }
            }
            connectedBlocks.removeAll(toRemove);
            return toRemove;
        }

        public int needsSplitting() {
            float averageBlocksPerLevel = (float) connectedBlocks.size() / (highestY - lowestY + 1);
            int connectionThresshold = (int) (averageBlocksPerLevel / 10);
            if (connectionThresshold <= 0) {
                // Too small to split
                return -1;
            }
            int cuttingY = -1;      // Where we will cut
            int cuttingCount = 1000000;
            for (int y = lowestY; y <= highestY; y++) {
                if (y >= 3 && blocksPerY.get(y) <= connectionThresshold) {
                    if (blocksPerY.get(y) < cuttingCount) {
                        cuttingCount = blocksPerY.get(y);
                        cuttingY = y;
                    } else if (blocksPerY.get(y) > cuttingCount * 4) {
                        return cuttingY;
                    }
                }
            }
            return -1;
        }

        public boolean destroyOrMoveThis(LostCityChunkGenerator provider) {
            return connections < 5 || (((float) connections / connectedBlocks.size()) < provider.profile.DESTROY_LONE_BLOCKS_FACTOR);
        }

        private boolean isOutside(BuildingInfo info, int x, int y, int z) {
            if (x < 0) {
                if (y <= info.getXmin().getMaxHeight() + 3) {
                    connections++;
                }
                return true;
            }
            if (x > 15) {
                if (y <= info.getXmax().getMaxHeight() + 3) {
                    connections++;
                }
                return true;
            }
            if (z < 0) {
                if (y <= info.getZmin().getMaxHeight() + 3) {
                    connections++;
                }
                return true;
            }
            if (z > 15) {
                if (y <= info.getZmax().getMaxHeight() + 3) {
                    connections++;
                }
                return true;
            }
            if (y < starty) {
                connections += 5;
                return true;
            }
            return false;
        }

        public void scan(BuildingInfo info, ChunkPrimer primer, char air, char liquid, BlockPos pos) {
            DamageArea damageArea = info.getDamageArea();
            avgdamage = 0;
            cntMindamage = 0;
            Queue<BlockPos> todo = new ArrayDeque<>();
            todo.add(pos);

            while (!todo.isEmpty()) {
                pos = todo.poll();
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();
                int index = calcIndex(x, y, z);
                if (connectedBlocks.contains(index)) {
                    continue;
                }
                if (isOutside(info, x, y, z)) {
                    continue;
                }
                if (primer.data[index] == air || primer.data[index] == liquid) {
                    continue;
                }
                connectedBlocks.add(index);
                float damage = damageArea.getDamage(x, y, z);
                if (damage < 0.01f) {
                    cntMindamage++;
                }
                avgdamage += damage;
                if (!blocksPerY.containsKey(y)) {
                    blocksPerY.put(y, 1);
                } else {
                    blocksPerY.put(y, blocksPerY.get(y) + 1);
                }
                if (y < lowestY) {
                    lowestY = y;
                }
                if (y > highestY) {
                    highestY = y;
                }
                todo.add(pos.up());
                todo.add(pos.down());
                todo.add(pos.east());
                todo.add(pos.west());
                todo.add(pos.south());
                todo.add(pos.north());
            }

            avgdamage /= (float) connectedBlocks.size();
        }

        public static int calcIndex(int x, int y, int z) {
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
        int start = info.getDamageArea().getLowestExplosionHeight();
        if (start == -1) {
            // Nothing is affected
            return;
        }
        int end = info.getDamageArea().getHighestExplosionHeight();

        List<Blob> blobs = new ArrayList<>();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int index = (x << 12) | (z << 8) + start;
                for (int y = start; y < end; y++) {
                    char p = primer.data[index];
                    if (p != airChar) {
                        Blob blob = findBlob(blobs, index);
                        if (blob == null) {
                            blob = new Blob(start, end + 6);
                            blob.scan(info, primer, airChar, liquidChar, new BlockPos(x, y, z));
                            blobs.add(blob);
                        }
                    }
                    index++;
                }
            }
        }

        // Split large blobs that have very thin connections in Y direction
        for (Blob blob : blobs) {
            if (blob.getAvgdamage() > .3f && blob.getCntMindamage() < 10) { // @todo configurable?
                int y = blob.needsSplitting();
                if (y != -1) {
                    Set<Integer> toRemove = blob.cut(y);
                    for (Integer index : toRemove) {
                        primer.data[index] = ((index & 0xff) < waterLevel) ? liquidChar : airChar;
                    }
                }
            }
        }

        // Sort all blobs we delete with lowest first
        blobs.sort((o1, o2) -> {
            int y1 = o1.destroyOrMoveThis(provider) ? o1.lowestY : 1000;
            int y2 = o2.destroyOrMoveThis(provider) ? o2.lowestY : 1000;
            return y1 - y2;
        });


        Blob blocksToMove = new Blob(0, 256);
        for (Blob blob : blobs) {
            if (!blob.destroyOrMoveThis(provider)) {
                // The rest of the blobs doesn't have to be destroyed anymore
                break;
            }
            if (rand.nextFloat() < provider.profile.DESTROY_OR_MOVE_CHANCE || blob.connectedBlocks.size() < provider.profile.DESTROY_SMALL_SECTIONS_SIZE
                    || blob.connections < 5) {
                for (Integer index : blob.connectedBlocks) {
                    primer.data[index] = ((index & 0xff) < waterLevel) ? liquidChar : airChar;
                }
            } else {
                blocksToMove.connectedBlocks.addAll(blob.connectedBlocks);
            }
        }
        for (Integer index : blocksToMove.connectedBlocks) {
            char c = primer.data[index];
            primer.data[index] = ((index & 0xff) < waterLevel) ? liquidChar : airChar;
            index--;
            int y = index & 255;
            while (y > 2 && (blocksToMove.contains(index) || primer.data[index] == airChar || primer.data[index] == liquidChar)) {
                index--;
                y--;
            }
            index++;
            primer.data[index] = c;
        }
    }

    private int generateStreet(ChunkPrimer primer, BuildingInfo info, Random rand, int index, int x, int z, int height, List<Integer> torches) {
        boolean xRail = info.hasXCorridor();
        boolean zRail = info.hasZCorridor();

        boolean doOceanBorder = isDoOceanBorder(info, x, z);

        IBlockState railx = Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
        IBlockState railz = Blocks.RAIL.getDefaultState();

        while (height < info.getCityGroundLevel()) {
            IBlockState b = baseBlock;
            if (doOceanBorder) {
                b = Blocks.STONEBRICK.getDefaultState();
            } else if (height >= groundLevel - 5 && height <= groundLevel - 1) {    // This uses actual ground level for now
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
            BaseTerrainGenerator.setBlockState(primer, index++, b);
            height++;
        }

        Character b;

        if (info.getHighwayXLevel() != info.cityLevel && info.getHighwayZLevel() != info.cityLevel) {
            BuildingInfo.StreetType streetType = info.streetType;
            boolean elevated = info.isElevatedParkSection();
            if (elevated) {
                streetType = BuildingInfo.StreetType.PARK;
                BaseTerrainGenerator.setBlockState(primer, index++, bricksChar);
                height++;
            }

            b = streetBase;
            switch (streetType) {
                case NORMAL:
                    if (isStreetBorder(x, z)) {
                        if (x <= streetBorder && z > streetBorder && z < (15 - streetBorder)
                                && (BuildingInfo.hasRoadConnection(info, info.getXmin()) || (info.getXmin().hasXBridge(provider) != null))) {
                            b = street;
                        } else if (x >= (15 - streetBorder) && z > streetBorder && z < (15 - streetBorder)
                                && (BuildingInfo.hasRoadConnection(info, info.getXmax()) || (info.getXmax().hasXBridge(provider) != null))) {
                            b = street;
                        } else if (z <= streetBorder && x > streetBorder && x < (15 - streetBorder)
                                && (BuildingInfo.hasRoadConnection(info, info.getZmin()) || (info.getZmin().hasZBridge(provider) != null))) {
                            b = street;
                        } else if (z >= (15 - streetBorder) && x > streetBorder && x < (15 - streetBorder)
                                && (BuildingInfo.hasRoadConnection(info, info.getZmax()) || (info.getZmax().hasZBridge(provider) != null))) {
                            b = street;
                        }
                    } else {
                        b = street;
                    }
                    break;
                case FULL:
                    if (isSide(x, z)) {
                        b = street;
                    } else {
                        b = street2;
                    }
                    break;
                case PARK:
                    if (x == 0 || x == 15 || z == 0 || z == 15) {
                        b = street;
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
                                    b = grassChar;
                                }
                            } else if (x == 15 && z == 0) {
                                if (el21 && el20 && el10) {
                                    b = grassChar;
                                }
                            } else if (x == 0 && z == 15) {
                                if (el01 && el02 && el12) {
                                    b = grassChar;
                                }
                            } else if (x == 15 && z == 15) {
                                if (el12 && el22 && el21) {
                                    b = grassChar;
                                }
                            } else if (x == 0) {
                                if (el01) {
                                    b = grassChar;
                                }
                            } else if (x == 15) {
                                if (el21) {
                                    b = grassChar;
                                }
                            } else if (z == 0) {
                                if (el10) {
                                    b = grassChar;
                                }
                            } else if (z == 15) {
                                if (el12) {
                                    b = grassChar;
                                }
                            }
                        }
                    } else {
                        b = grassChar;
                    }
                    break;
            }
            if (doOceanBorder) {
                b = bricksChar;
            }
            BaseTerrainGenerator.setBlockState(primer, index++, b);
            height++;

            if (streetType == BuildingInfo.StreetType.PARK || info.fountainType != null) {
                int l = 0;
                BuildingPart part;
                if (streetType == BuildingInfo.StreetType.PARK) {
                    part = info.parkType;
                } else {
                    part = info.fountainType;
                }
                while (l < part.getSliceCount()) {
                    if (l == 0 && doOceanBorder && !borderNeedsConnectionToAdjacentChunk(info, x, z)) {
                        b = wallChar;
                    } else {
                        b = part.get(info, x, l, z);
                        if (b == torchChar) {
                            if (provider.profile.GENERATE_LIGHTING) {
                                torches.add(index);
                            } else {
                                b = airChar;        // No torch!
                            }
                        }
                    }
                    BaseTerrainGenerator.setBlockState(primer, index++, b);
                    height++;
                    l++;
                }
            } else if (doOceanBorder) {
                if (!borderNeedsConnectionToAdjacentChunk(info, x, z)) {
                    b = wallChar;
                    BaseTerrainGenerator.setBlockState(primer, index++, b);
                    height++;
                }
            }

            // Go back to groundlevel
            while (primer.data[index - 1] == airChar) {
                index--;
                height--;
            }
            // Only generate random leaf blocks on top of normal stone
            if (primer.data[index - 1] == baseChar) {
                if (info.getXmin().hasBuilding && x <= 2) {
                    while (rand.nextFloat() < (provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (3 - x))) {
                        b = leavesChar;
                        BaseTerrainGenerator.setBlockState(primer, index++, b);
                        height++;
                    }
                }
                if (info.getXmax().hasBuilding && x >= 13) {
                    while (rand.nextFloat() < (provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (x - 12))) {
                        b = leavesChar;
                        BaseTerrainGenerator.setBlockState(primer, index++, b);
                        height++;
                    }
                }
                if (info.getZmin().hasBuilding && z <= 2) {
                    while (rand.nextFloat() < (provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (3 - z))) {
                        b = leavesChar;
                        BaseTerrainGenerator.setBlockState(primer, index++, b);
                        height++;
                    }
                }
                if (info.getZmax().hasBuilding && z <= 13) {
                    while (rand.nextFloat() < (provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (z - 12))) {
                        b = leavesChar;
                        BaseTerrainGenerator.setBlockState(primer, index++, b);
                        height++;
                    }
                }

                while (rand.nextFloat() < (provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS / 6)) {
                    b = leavesChar;
                    BaseTerrainGenerator.setBlockState(primer, index++, b);
                    height++;
                }
            }
        }

        int blocks = 256 - height;
        BaseTerrainGenerator.setBlockStateRange(primer, index, index + blocks, air);
        index += blocks;

        return index;
    }

    private boolean borderNeedsConnectionToAdjacentChunk(BuildingInfo info, int x, int z) {
        boolean needOpening = false;
        for (Direction direction : Direction.VALUES) {
            BuildingInfo adjacent = direction.get(info);
            if (direction.atSide(x, z) && adjacent.getActualStairDirection() == direction.getOpposite()) {
                BuildingPart stairType = adjacent.stairType;
                Integer z1 = stairType.getMetaInteger("z1");
                Integer z2 = stairType.getMetaInteger("z2");
                Rotation rotation = direction.getOpposite().getRotation();
                int xx1 = rotation.rotateX(15, z1);
                int zz1 = rotation.rotateZ(15, z1);
                int xx2 = rotation.rotateX(15, z2);
                int zz2 = rotation.rotateZ(15, z2);
                if (x >= Math.min(xx1, xx2) && x <= Math.max(xx1, xx2) && z >= Math.min(zz1, zz2) && z <= Math.max(zz1, zz2)) {
                    needOpening = true;
                    break;
                }
            }
        }
        return needOpening;
    }

    private int generatePart(ChunkPrimer primer, BuildingInfo info, BuildingPart part,
                             Rotation rotation,
                             int ox, int oy, int oz, List<Integer> torches) {
        for (int x = 0; x < part.getXSize(); x++) {
            for (int z = 0; z < part.getZSize(); z++) {
                int rx = ox + rotation.rotateX(x, z);
                int rz = oz + rotation.rotateZ(x, z);
                int index = (rx << 12) | (rz << 8) + oy;
                for (int y = 0; y < part.getSliceCount(); y++) {
                    Character b = part.get(info, x, y, z);
                    if (rotation != Rotation.ROTATE_NONE) {
                        IBlockState bs = Block.BLOCK_STATE_IDS.getByValue(b);
                        if (bs.getBlock() instanceof BlockStairs) {
                            bs = bs.withRotation(rotation.getMcRotation());
                            b = (char) Block.BLOCK_STATE_IDS.get(bs);
                        }
                    }
                    // We don't replace the world where the part is empty (air)
                    if (b != airChar) {
                        if (b == hardAirChar) {
                            b = airChar;
                        } else if (b == torchChar) {
                            if (provider.profile.GENERATE_LIGHTING) {
                                torches.add(index);
                            } else {
                                b = airChar;        // No torches
                            }
                        }
                        BaseTerrainGenerator.setBlockState(primer, index, b);
                    }
                    index++;
                }
            }
        }
        return oy + part.getSliceCount();
    }

    private void generateDebris(ChunkPrimer primer, Random rand, BuildingInfo info) {
        generateDebrisFromChunk(primer, rand, info.getXmin(), (xx, zz) -> (15.0f - xx) / 16.0f);
        generateDebrisFromChunk(primer, rand, info.getXmax(), (xx, zz) -> xx / 16.0f);
        generateDebrisFromChunk(primer, rand, info.getZmin(), (xx, zz) -> (15.0f - zz) / 16.0f);
        generateDebrisFromChunk(primer, rand, info.getZmax(), (xx, zz) -> zz / 16.0f);
        generateDebrisFromChunk(primer, rand, info.getXmin().getZmin(), (xx, zz) -> ((15.0f - xx) * (15.0f - zz)) / 256.0f);
        generateDebrisFromChunk(primer, rand, info.getXmax().getZmax(), (xx, zz) -> (xx * zz) / 256.0f);
        generateDebrisFromChunk(primer, rand, info.getXmin().getZmax(), (xx, zz) -> ((15.0f - xx) * zz) / 256.0f);
        generateDebrisFromChunk(primer, rand, info.getXmax().getZmin(), (xx, zz) -> (xx * (15.0f - zz)) / 256.0f);
    }

    private void generateDebrisFromChunk(ChunkPrimer primer, Random rand, BuildingInfo adjacentInfo, BiFunction<Integer, Integer, Float> locationFactor) {
        if (adjacentInfo.hasBuilding) {
            float damageFactor = adjacentInfo.getDamageArea().getDamageFactor();
            if (damageFactor > .5f) {
                // An estimate of the amount of blocks
                int blocks = (1 + adjacentInfo.getNumFloors()) * 1000;
                float damage = Math.max(1.0f, damageFactor * DamageArea.BLOCK_DAMAGE_CHANCE);
                int destroyedBlocks = (int) (blocks * damage);
                // How many go this direction (approx, based on cardinal directions from building as well as number that simply fall down)
                destroyedBlocks /= provider.profile.DEBRIS_TO_NEARBYCHUNK_FACTOR;
                for (int i = 0; i < destroyedBlocks; i++) {
                    int x = rand.nextInt(16);
                    int z = rand.nextInt(16);
                    if (rand.nextFloat() < locationFactor.apply(x, z)) {
                        int index = (x << 12) | (z << 8) + 254;     // Start one lower for safety
                        while (primer.data[index] == airChar || primer.data[index] == liquidChar) {
                            index--;
                        }
                        index++;
                        Character b;
                        switch (rand.nextInt(5)) {
                            case 0:
                                b = ironbarsChar;
                                break;
                            default:
                                b = adjacentInfo.getCompiledPalette().get('#');   // @todo hardcoded!
                                break;
                        }
                        BaseTerrainGenerator.setBlockState(primer, index, b);
                    }
                }
            }
        }
    }

    private boolean isDoOceanBorder(BuildingInfo info, int x, int z) {
        if (x == 0 && doBorder(info, Direction.XMIN)) {
            return true;
        } else if (x == 15 && doBorder(info, Direction.XMAX)) {
            return true;
        }
        if (z == 0 && doBorder(info, Direction.ZMIN)) {
            return true;
        } else if (z == 15 && doBorder(info, Direction.ZMAX)) {
            return true;
        }
        return false;
    }

    private boolean doBorder(BuildingInfo info, Direction direction) {
        BuildingInfo adjacent = direction.get(info);
        if (isHigherThenNearbyStreetChunk(info, adjacent)) {
            return true;
        } else if (!adjacent.isCity && adjacent.hasBridge(provider, direction.getOrientation()) == null) {
            if (adjacent.cityLevel <= info.cityLevel) {
                return true;
            }
        }
        return false;
    }

    private boolean isHigherThenNearbyStreetChunk(BuildingInfo info, BuildingInfo adjacent) {
        return adjacent.isCity && !adjacent.hasBuilding && adjacent.cityLevel < info.cityLevel;
    }

    private int generateBuilding(ChunkPrimer primer, BuildingInfo info, int index, int x, int z, int height, List<Integer> torches) {
        int lowestLevel = info.getCityGroundLevel() - info.floorsBelowGround * 6;
        int buildingtop = info.getMaxHeight();
        boolean corridor;
        if (isSide(x, z)) {
            BuildingInfo adjacent = info.getAdjacent(x, z);
            corridor = (adjacent.hasXCorridor() || adjacent.hasZCorridor()) && isRailDoorway(x, z);
        } else {
            corridor = false;
        }

        while (height < lowestLevel) {
            BaseTerrainGenerator.setBlockState(primer, index++, baseChar);
            height++;
        }
        while (height < buildingtop + 6) {
            Character b;

            // Make a connection to a corridor if needed
            if (corridor && height >= groundLevel - 5 && height <= groundLevel - 3) {   // This uses actual groundLevel
                b = airChar;
            } else {
                b = getBlockForLevel(info, x, z, height);
                if (b == torchChar) {
                    // Remember this torch so we can fix orientation later
                    if (provider.profile.GENERATE_LIGHTING) {
                        torches.add(index);
                    } else {
                        b = airChar;   // No torches
                    }
                }
            }

            BaseTerrainGenerator.setBlockState(primer, index++, b);
            height++;
        }
        int blocks = 256 - height;
        BaseTerrainGenerator.setBlockStateRange(primer, index, index + blocks, air);
        index += blocks;
        return index;
    }

    private Character getBlockForLevel(BuildingInfo info, int x, int z, int height) {
        int f = getFloor(height);
        int localLevel = getLevel(info, height);
        boolean isTop = localLevel == info.getNumFloors();   // The top does not need generated doors

        BuildingPart part = info.getFloor(localLevel);
        if (f >= part.getSliceCount()) { // @todo avoid this?
            return airChar;
        }
        Character b = part.get(info, x, f, z);

        // If we are underground, the block is glass, we are on the side and the chunk next to
        // us doesn't have a building or floor there we replace the glass with a solid block
        BuildingInfo adjacent = info.getAdjacent(x, z);

        if (localLevel < 0 && CompiledPalette.isGlass(b) && isSide(x, z) && (!adjacent.hasBuilding || adjacent.floorsBelowGround < -localLevel)) {
            // However, if there is a street next to us and the city level is lower then we generate windows like normal anyway
            if (!(adjacent.isCity && (!adjacent.hasBuilding) && (info.cityLevel + localLevel) >= adjacent.cityLevel)) {
                b = bricks;
            }
        }

        // For buildings that have a style which causes gaps at the side we fill in that gap if we are
        // at ground level
        if (b == airChar && isSide(x, z) && adjacent.isCity && height == adjacent.getCityGroundLevel()) {
            b = baseChar;
        }

        // for buildings that have a hole in the bottom floor we fill that hole if we are
        // at the bottom of the building
        if (b == airChar && f == 0 && (localLevel + info.floorsBelowGround) == 0) {
            b = bricks;
        }

        if (!isTop) {
            if (x == 0 && (z >= 6 && z <= 9) && f >= 1 && f <= 3 && info.hasConnectionAtX(localLevel + info.floorsBelowGround)) {
                if (hasConnectionWithBuilding(localLevel, info, adjacent)) {
                    if (f == 3 || z == 6 || z == 9) {
                        return bricks;
                    } else {
                        return airChar;
                    }
                } else if (hasConnectionToTopOrOutside(localLevel, info, adjacent)) {
                    if (f == 3 || z == 6 || z == 9) {
                        return bricks;
                    } else {
                        return (char) Block.BLOCK_STATE_IDS.get(info.doorBlock.getDefaultState()
                                .withProperty(BlockDoor.HALF, f == 1 ? BlockDoor.EnumDoorHalf.LOWER : BlockDoor.EnumDoorHalf.UPPER)
                                .withProperty(BlockDoor.HINGE, z == 7 ? BlockDoor.EnumHingePosition.LEFT : BlockDoor.EnumHingePosition.RIGHT)
                                .withProperty(BlockDoor.FACING, EnumFacing.EAST));
                    }
                }
            } else if (x == 15 && (z >= 6 && z <= 9) && f >= 1 && f <= 3) {
                if (hasConnectionWithBuildingMax(localLevel, info, adjacent, Orientation.X)) {
                    if (f == 3 || z == 6 || z == 9) {
                        return bricks;
                    } else {
                        return airChar;
                    }
                } else if ((hasConnectionToTopOrOutside(localLevel, info, adjacent)) && adjacent.hasConnectionAtX(localLevel + adjacent.floorsBelowGround)) {
                    if (f == 3 || z == 6 || z == 9) {
                        return bricks;
                    } else {
                        return (char) Block.BLOCK_STATE_IDS.get(info.doorBlock.getDefaultState()
                                .withProperty(BlockDoor.HALF, f == 1 ? BlockDoor.EnumDoorHalf.LOWER : BlockDoor.EnumDoorHalf.UPPER)
                                .withProperty(BlockDoor.HINGE, z == 8 ? BlockDoor.EnumHingePosition.LEFT : BlockDoor.EnumHingePosition.RIGHT)
                                .withProperty(BlockDoor.FACING, EnumFacing.WEST));
                    }
                }
            }
            if (z == 0 && (x >= 6 && x <= 9) && f >= 1 && f <= 3 && info.hasConnectionAtZ(localLevel + info.floorsBelowGround)) {
                if (hasConnectionWithBuilding(localLevel, info, adjacent)) {
                    if (f == 3 || x == 6 || x == 9) {
                        return bricks;
                    } else {
                        return airChar;
                    }
                } else if (hasConnectionToTopOrOutside(localLevel, info, adjacent)) {
                    if (f == 3 || x == 6 || x == 9) {
                        return bricks;
                    } else {
                        return (char) Block.BLOCK_STATE_IDS.get(info.doorBlock.getDefaultState()
                                .withProperty(BlockDoor.HALF, f == 1 ? BlockDoor.EnumDoorHalf.LOWER : BlockDoor.EnumDoorHalf.UPPER)
                                .withProperty(BlockDoor.HINGE, x == 8 ? BlockDoor.EnumHingePosition.LEFT : BlockDoor.EnumHingePosition.RIGHT)
                                .withProperty(BlockDoor.FACING, EnumFacing.SOUTH));
                    }
                }
            } else if (z == 15 && (x >= 6 && x <= 9) && f >= 1 && f <= 3) {
                if (hasConnectionWithBuildingMax(localLevel, info, adjacent, Orientation.Z)) {
                    if (f == 3 || x == 6 || x == 9) {
                        return bricks;
                    } else {
                        return airChar;
                    }
                } else if ((hasConnectionToTopOrOutside(localLevel, info, adjacent)) && adjacent.hasConnectionAtZ(localLevel + adjacent.floorsBelowGround)) {
                    if (f == 3 || x == 6 || x == 9) {
                        return bricks;
                    } else {
                        return (char) Block.BLOCK_STATE_IDS.get(info.doorBlock.getDefaultState()
                                .withProperty(BlockDoor.HALF, f == 1 ? BlockDoor.EnumDoorHalf.LOWER : BlockDoor.EnumDoorHalf.UPPER)
                                .withProperty(BlockDoor.HINGE, x == 7 ? BlockDoor.EnumHingePosition.LEFT : BlockDoor.EnumHingePosition.RIGHT)
                                .withProperty(BlockDoor.FACING, EnumFacing.NORTH));
                    }
                }
            }
        }

        if (b == ladderChar && f == 0 && (localLevel + info.floorsBelowGround) == 0) {
            return bricks;
        }

        // If this is a spawner we put it on a todo so that the world generator can put the correct mob in it
        // @todo support this system in other places too
        Block block = Block.BLOCK_STATE_IDS.getByValue(b).getBlock();
        if (block == Blocks.MOB_SPAWNER) {
            if (provider.profile.GENERATE_SPAWNERS) {
                String mobid = part.getMobID(info, x, f, z);
                info.addSpawnerTodo(new BlockPos(x, height, z), mobid);
            } else {
                b = airChar;
            }
        } else if (block == Blocks.CHEST) {
            info.addChestTodo(new BlockPos(x, height, z));
        }

        return b;
    }

    private boolean hasConnectionWithBuildingMax(int localLevel, BuildingInfo info, BuildingInfo info2, Orientation x) {
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        int level = localAdjacent + info2.floorsBelowGround;
        return info2.hasBuilding && ((localAdjacent >= 0 && localAdjacent < info2.getNumFloors()) || (localAdjacent < 0 && (-localAdjacent) <= info2.floorsBelowGround)) && info2.hasConnectionAt(level, x);
    }

    private boolean hasConnectionToTopOrOutside(int localLevel, BuildingInfo info, BuildingInfo info2) {
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        return (!info2.hasBuilding && localLevel == 0 && localAdjacent == 0) || (info2.hasBuilding && localAdjacent == info2.getNumFloors());
    }

    private boolean hasConnectionWithBuilding(int localLevel, BuildingInfo info, BuildingInfo info2) {
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        return info2.hasBuilding && ((localAdjacent >= 0 && localAdjacent < info2.getNumFloors()) || (localAdjacent < 0 && (-localAdjacent) <= info2.floorsBelowGround));
    }

    public int getFloor(int height) {
        return (height - provider.profile.GROUNDLEVEL + 600) % 6;
    }

    public static int getLevel(BuildingInfo info, int height) {
        return ((height - info.getCityGroundLevel() + 600) / 6) - 100;
    }

    private boolean isCorner(int x, int z) {
        return (x == 0 && z == 0) || (x == 0 && z == 15) || (x == 15 && z == 0) || (x == 15 && z == 15);
    }

    private boolean isSide(int x, int z) {
        return x == 0 || x == 15 || z == 0 || z == 15;
    }

    private boolean isStreetBorder(int x, int z) {
        return x <= streetBorder || x >= (15 - streetBorder) || z <= streetBorder || z >= (15 - streetBorder);
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
