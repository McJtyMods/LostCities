package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.dimensions.world.lost.*;
import mcjty.lostcities.dimensions.world.lost.cityassets.AssetRegistries;
import mcjty.lostcities.dimensions.world.lost.cityassets.BuildingPart;
import mcjty.lostcities.dimensions.world.lost.cityassets.CompiledPalette;
import mcjty.lostcities.varia.GeometryTools;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyEnum;
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

    private final int groundLevel;
    private final int waterLevel;
    public static char airChar;
    public static char hardAirChar;
    public static char bricksChar;
    public static char glowstoneChar;
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
    public static char goldBlockChar;
    public static char diamondBlockChar;
    public static char spawnerChar;
    public static char chestChar;
    public static IBlockState air;
    public static IBlockState hardAir;  // Used in parts to carve out
    public static IBlockState water;

    private static Set<Character> rotatableChars = null;
    private static Set<Character> railChars = null;

    private IBlockState baseBlock;
    private Character street;
    private Character streetBase;
    private Character street2;
    private Character styledBricks;
    private int streetBorder;


    public LostCitiesTerrainGenerator(LostCityChunkGenerator provider) {
        super(provider);
        this.groundLevel = provider.profile.GROUNDLEVEL;
        this.waterLevel = provider.profile.WATERLEVEL;
    }


    // Use this random when it doesn't really matter i fit is generated the same every time
    public static Random globalRandom = new Random();

    private static Set<Character> getRailChars() {
        if (railChars == null) {
            railChars = new HashSet<>();
            addStates(Blocks.RAIL, railChars);
            addStates(Blocks.GOLDEN_RAIL, railChars);
        }
        return railChars;
    }

    private static Set<Character> getRotatableChars() {
        if (rotatableChars == null) {
            rotatableChars = new HashSet<>();
            addStates(Blocks.ACACIA_STAIRS, rotatableChars);
            addStates(Blocks.BIRCH_STAIRS, rotatableChars);
            addStates(Blocks.BRICK_STAIRS, rotatableChars);
            addStates(Blocks.QUARTZ_STAIRS, rotatableChars);
            addStates(Blocks.STONE_BRICK_STAIRS, rotatableChars);
            addStates(Blocks.DARK_OAK_STAIRS, rotatableChars);
            addStates(Blocks.JUNGLE_STAIRS, rotatableChars);
            addStates(Blocks.NETHER_BRICK_STAIRS, rotatableChars);
            addStates(Blocks.OAK_STAIRS, rotatableChars);
            addStates(Blocks.PURPUR_STAIRS, rotatableChars);
            addStates(Blocks.RED_SANDSTONE_STAIRS, rotatableChars);
            addStates(Blocks.SANDSTONE_STAIRS, rotatableChars);
            addStates(Blocks.SPRUCE_STAIRS, rotatableChars);
            addStates(Blocks.STONE_STAIRS, rotatableChars);
        }
        return rotatableChars;
    }

    private static void addStates(Block block, Set<Character> set) {
        for (int m = 0; m < 16; m++) {
            try {
                IBlockState state = block.getStateFromMeta(m);
                set.add((char) Block.BLOCK_STATE_IDS.get(state));
            } catch (Exception e) {
                // Ignore
            }
        }
    }


    // Note that for normal chunks this is called with a pre-filled in landscape primer
    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        baseBlock = Blocks.STONE.getDefaultState();

        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);

        // @todo this setup is not very clean
        air = Blocks.AIR.getDefaultState();
        hardAir = Blocks.COMMAND_BLOCK.getDefaultState();
        water = Blocks.WATER.getDefaultState();
        street = info.getCompiledPalette().get(info.getCityStyle().getStreetBlock());
        streetBase = info.getCompiledPalette().get(info.getCityStyle().getStreetBaseBlock());
        street2 = info.getCompiledPalette().get(info.getCityStyle().getStreetVariantBlock());
        streetBorder = (16 - info.getCityStyle().getStreetWidth()) / 2;

        airChar = (char) Block.BLOCK_STATE_IDS.get(air);
        hardAirChar = (char) Block.BLOCK_STATE_IDS.get(hardAir);
        bricksChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.STONEBRICK.getDefaultState());
        glowstoneChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GLOWSTONE.getDefaultState());
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
        goldBlockChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GOLD_BLOCK.getDefaultState());
        diamondBlockChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.DIAMOND_BLOCK.getDefaultState());
        spawnerChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.MOB_SPAWNER.getDefaultState());
        chestChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.CHEST.getDefaultState());

        // @todo This should not be hardcoded here
        styledBricks = info.getCompiledPalette().get('#');

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
                if (primer.data[idx - 1] != airChar) {
                    primer.data[idx] = tu;
                } else if (x > 0 && primer.data[idx - (1 << 12)] != airChar) {
                    primer.data[idx] = te;
                } else if (x < 15 && primer.data[idx + (1 << 12)] != airChar) {
                    primer.data[idx] = tw;
                } else if (z > 0 && primer.data[idx - (1 << 8)] != airChar) {
                    primer.data[idx] = ts;
                } else if (z < 15 && primer.data[idx + (1 << 8)] != airChar) {
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
                                    primer.data[index] = baseChar;
                                } else if (height < waterLevel) {
                                    primer.data[index] = liquidChar;
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
//        debugClearChunk(chunkX, chunkZ, primer);
        flattenChunkToCityBorder(chunkX, chunkZ, primer);
        generateBridges(primer, info, torches);
        generateHighways(chunkX, chunkZ, primer, info, torches);

        Railway.RailChunkInfo railInfo = info.getRailInfo();
        if (railInfo.getType() != Railway.RailChunkType.NONE) {
            generateRailways(primer, info, torches, railInfo);
        }
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
                            int height = yy * 16;
                            int index = (x << 12) | (z << 8) + height;
                            for (int y = 0; y < 16; y++) {
                                primer.data[index] = ((index & 0xff) < waterLevel) ? liquidChar : airChar;
                                index++;
                            }
                        }
                    }
                } else {
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            int index = (x << 12) | (z << 8) + yy * 16;
                            for (int y = 0; y < 16; y++) {
                                char d = primer.data[index];
                                if (d != airChar || (index & 0xff) < waterLevel) {
                                    Character newd = damageArea.damageBlock(d, provider, cx + x, yy * 16 + y, cz + z, info.getCompiledPalette());
                                    if (newd != d) {
                                        primer.data[index] = newd;
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
            generateHighwayPart(primer, info, levelX, Transform.ROTATE_NONE, info.getXmax(), info.getZmax(), "_bi", torches);
        } else if (levelX >= 0 && levelZ >= 0) {
            // There are two highways on different level. Make sure the lowest one is done first because it
            // will clear out what is above it
            if (levelX == 0) {
                generateHighwayPart(primer, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), "", torches);
                generateHighwayPart(primer, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), "", torches);
            } else {
                generateHighwayPart(primer, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), "", torches);
                generateHighwayPart(primer, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), "", torches);
            }
        } else {
            if (levelX >= 0) {
                generateHighwayPart(primer, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), "", torches);
            } else if (levelZ >= 0) {
                generateHighwayPart(primer, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), "", torches);
            }
        }
    }

    private void generateHighwayPart(ChunkPrimer primer, BuildingInfo info, int level, Transform transform, BuildingInfo adjacent1, BuildingInfo adjacent2, String suffix,
                                     List<Integer> torches) {
        int highwayGroundLevel = provider.profile.GROUNDLEVEL + level * 6;

        if (info.isTunnel(level)) {
            // We know we need a tunnel
            generatePart(primer, info, AssetRegistries.PARTS.get("highway_tunnel" + suffix), transform, 0, highwayGroundLevel, 0, torches);
        } else if (info.isCity && level <= adjacent1.cityLevel && level <= adjacent2.cityLevel && adjacent1.isCity && adjacent2.isCity) {
            // Simple highway in the city
            int height = generatePart(primer, info, AssetRegistries.PARTS.get("highway_open" + suffix), transform, 0, highwayGroundLevel, 0, torches);
            // Clear a bit more above the highway
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int index = (x << 12) | (z << 8) + height;
                    BaseTerrainGenerator.setBlockStateRange(primer, index, index + 15, airChar);
                }
            }
        } else {
            int height = generatePart(primer, info, AssetRegistries.PARTS.get("highway_bridge" + suffix), transform, 0, highwayGroundLevel, 0, torches);
            // Clear a bit more above the highway
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int index = (x << 12) | (z << 8) + height;
                    BaseTerrainGenerator.setBlockStateRange(primer, index, index + 15, airChar);
                }
            }
        }

        // Make sure the bridge is supported if needed
        if (transform == Transform.ROTATE_NONE) {
            int index = highwayGroundLevel - 1; // (coordinate 0,0)
            for (int y = 0; y < 40; y++) {
                boolean done = false;
                if (primer.data[index] == airChar || primer.data[index] == liquidChar) {
                    primer.data[index] = bricksChar;
                    done = true;
                }
                if (primer.data[index + (15 << 8)] == airChar || primer.data[index + (15 << 8)] == liquidChar) {
                    primer.data[index + (15 << 8)] = bricksChar;
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
                    primer.data[index] = bricksChar;
                    done = true;
                }
                if (primer.data[index + (15 << 12)] == airChar || primer.data[index + (15 << 12)] == liquidChar) {
                    primer.data[index + (15 << 12)] = bricksChar;
                    done = true;
                }
                index--;
                if (!done) {
                    break;
                }
            }
        }
    }

    private void generateBridges(ChunkPrimer primer, BuildingInfo info, List<Integer> torches) {
        if (info.getHighwayXLevel() == 0 || info.getHighwayZLevel() == 0) {
            // If there is a highway at level 0 we cannot generate bridge parts. If there
            // is no highway or a highway at level 1 then bridge sections can generate just fine
            return;
        }
        BuildingPart bt = info.hasXBridge(provider);
        if (bt != null) {
            generateBridge(primer, info, bt, Orientation.X, torches);
        } else {
            bt = info.hasZBridge(provider);
            if (bt != null) {
                generateBridge(primer, info, bt, Orientation.Z, torches);
            }
        }
    }

    private void generateBridge(ChunkPrimer primer, BuildingInfo info, BuildingPart bt, Orientation orientation, List<Integer> torches) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int index = (x << 12) | (z << 8) + groundLevel + 1;
                int l = 0;
                while (l < bt.getSliceCount()) {
                    Character b = orientation == Orientation.X ? bt.get(info, x, l, z) : bt.get(info, z, l, x); // @todo general rotation system?
                    if (b == torchChar) {
                        if (provider.profile.GENERATE_LIGHTING) {
                            torches.add(index);
                        } else {
                            b = airChar;        // No torch!
                        }
                    }
                    primer.data[index++] = b;
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
                    primer.data[index] = bricksChar;
                }
            } else {
                int z = 0;
                for (int x = 6; x <= 9; x++) {
                    int index = (x << 12) | (z << 8) + groundLevel;
                    primer.data[index] = bricksChar;
                }
            }
        }
        if (maxDir.hasBridge(provider, orientation) == null) {
            // Connection to the side section
            if (orientation == Orientation.X) {
                int x = 15;
                for (int z = 6; z <= 9; z++) {
                    int index = (x << 12) | (z << 8) + groundLevel;
                    primer.data[index] = bricksChar;
                }
            } else {
                int z = 15;
                for (int x = 6; x <= 9; x++) {
                    int index = (x << 12) | (z << 8) + groundLevel;
                    primer.data[index] = bricksChar;
                }
            }
        }
    }

    private void setBridgeSupport(ChunkPrimer primer, int x, int y, int z) {
        int index = (x << 12) | (z << 8) + y;
        primer.data[index] = bricksChar;
    }

    private void debugClearChunk(int chunkX, int chunkZ, ChunkPrimer primer) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int index = (x << 12) | (z << 8);
                for (int y = 255; y >= 0; y--) {
                    primer.data[index + y] = airChar;
                }
            }
        }
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
            char b = primer.data[index];
            if (b != bedrockChar) {
                primer.data[index] = baseChar;
            }
            index++;
        }
        int r = rand.nextInt(2);
        index = (x << 12) | (z << 8) + level + offset + r;
        for (int y = level + offset + 3; y < 256; y++) {
            primer.data[index++] = airChar;
        }
    }

    private void flattenChunkBorderDownwards(ChunkPrimer primer, int x, int offset, int z, Random rand, int level) {
        int r = rand.nextInt(2);
        int index = (x << 12) | (z << 8) + level + offset + r;
        for (int y = level + offset + 3; y < 256; y++) {
            primer.data[index++] = airChar;
        }
    }

    private void doCityChunk(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info, List<Integer> torches) {
        boolean building = info.hasBuilding;

        Random rand = new Random(provider.seed * 377 + chunkZ * 341873128712L + chunkX * 132897987541L);
        rand.nextFloat();
        rand.nextFloat();

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int index = (x << 12) | (z << 8);
                BaseTerrainGenerator.setBlockStateRange(primer, index, index + provider.profile.BEDROCK_LAYER, bedrockChar);
            }
        }

        if (building) {
            generateBuilding(primer, info, torches);
        } else {
            generateStreet(primer, info, rand, torches);
        }

        int levelX = info.getHighwayXLevel();
        int levelZ = info.getHighwayZLevel();
        if (building) {
            if (levelX >= 0 || levelZ >= 0) {
                generateHighways(chunkX, chunkZ, primer, info, torches);
            }
        } else {
            if (levelX < 0 && levelZ < 0) {
                generateStreetDecorations(primer, info, torches);
            } else {
                generateHighways(chunkX, chunkZ, primer, info, torches);
            }
        }

        Railway.RailChunkInfo railInfo = info.getRailInfo();
        if (railInfo.getType() != Railway.RailChunkType.NONE) {
            generateRailways(primer, info, torches, railInfo);
        }
    }

    private void generateRailways(ChunkPrimer primer, BuildingInfo info, List<Integer> torches, Railway.RailChunkInfo railInfo) {
        int height = provider.profile.GROUNDLEVEL + railInfo.getLevel() * 6;
        Railway.RailChunkType type = railInfo.getType();
        BuildingPart part;
        Transform transform = Transform.ROTATE_NONE;
        switch (type) {
            case NONE:
                return;
            case STATION_SURFACE:
            case STATION_EXTENSION_SURFACE:
                if (railInfo.getLevel() < info.cityLevel) {
                    // Even for a surface station extension we switch to underground if we are an extension
                    // that is at a spot where the city is higher then where the station is
                    part = AssetRegistries.PARTS.get("station_underground");
                } else {
                    part = AssetRegistries.PARTS.get("station_open");
                }
                break;
            case STATION_UNDERGROUND:
            case STATION_EXTENSION_UNDERGROUND:
                part = AssetRegistries.PARTS.get("station_underground");
                break;
            case HORIZONTAL:
                part = AssetRegistries.PARTS.get("rails_horizontal");
                break;
            case VERTICAL:
                part = AssetRegistries.PARTS.get("rails_vertical");
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case THREE_SPLIT:
                part = AssetRegistries.PARTS.get("rails_3split");
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case GOING_DOWN_TWO_FROM_SURFACE:
            case GOING_DOWN_FURTHER:
                part = AssetRegistries.PARTS.get("rails_down2");
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case GOING_DOWN_ONE_FROM_SURFACE:
                part = AssetRegistries.PARTS.get("rails_down1");
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case DOUBLE_BEND:
                part = AssetRegistries.PARTS.get("rails_bend");
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            default:
                part = AssetRegistries.PARTS.get("rails_flat");
                break;
        }
        generatePart(primer, info, part, transform, 0, height, 0, torches);
    }

    private void generateStreetDecorations(ChunkPrimer primer, BuildingInfo info, List<Integer> torches) {
        Direction stairDirection = info.getActualStairDirection();
        if (stairDirection != null) {
            BuildingPart stairs = info.stairType;
            Transform transform;
            int oy = info.getCityGroundLevel() + 1;
            switch (stairDirection) {
                case XMIN:
                    transform = Transform.ROTATE_NONE;
                    break;
                case XMAX:
                    transform = Transform.ROTATE_180;
                    break;
                case ZMIN:
                    transform = Transform.ROTATE_90;
                    break;
                case ZMAX:
                    transform = Transform.ROTATE_270;
                    break;
                default:
                    throw new RuntimeException("Cannot happen!");
            }

            generatePart(primer, info, stairs, transform, 0, oy, 0, torches);
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
                    if (p != airChar && p != liquidChar) {
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

    private void generateStreet(ChunkPrimer primer, BuildingInfo info, Random rand, List<Integer> torches) {
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int index = (x << 12) | (z << 8);
                BaseTerrainGenerator.setBlockStateRange(primer, index + provider.profile.BEDROCK_LAYER, index + groundLevel - 5, baseChar);
            }
        }

        boolean xRail = info.hasXCorridor();
        boolean zRail = info.hasZCorridor();
        if (xRail || zRail) {
            generateCorridors(primer, info, xRail, zRail);
        } else {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    int index = (x << 12) | (z << 8);
                    BaseTerrainGenerator.setBlockStateRange(primer, index + groundLevel - 5, index + info.getCityGroundLevel(), baseChar);
                }
            }
        }

        Railway.RailChunkInfo railInfo = info.getRailInfo();
        boolean canDoParks = info.getHighwayXLevel() != info.cityLevel && info.getHighwayZLevel() != info.cityLevel
                && railInfo.getType() != Railway.RailChunkType.STATION_SURFACE
                && (railInfo.getType() != Railway.RailChunkType.STATION_EXTENSION_SURFACE || railInfo.getLevel() < info.cityLevel);

        if (canDoParks) {
            int height = info.getCityGroundLevel();

            BuildingInfo.StreetType streetType = info.streetType;
            boolean elevated = info.isElevatedParkSection();
            if (elevated) {
                streetType = BuildingInfo.StreetType.PARK;
                for (int x = 0; x < 16; ++x) {
                    for (int z = 0; z < 16; ++z) {
                        primer.data[(x << 12) | (z << 8) + height] = bricksChar;
                    }
                }
                height++;
            }

            char b;
            switch (streetType) {
                case NORMAL:
                    generateNormalStreetSection(primer, info, height);
                    break;
                case FULL:
                    generateFullStreetSection(primer, height);
                    break;
                case PARK:
                    generateParkSection(primer, info, height, elevated);
            }
            height++;

            if (streetType == BuildingInfo.StreetType.PARK || info.fountainType != null) {
                BuildingPart part;
                if (streetType == BuildingInfo.StreetType.PARK) {
                    part = info.parkType;
                } else {
                    part = info.fountainType;
                }
                generatePart(primer, info, part, Transform.ROTATE_NONE, 0, height, 0, torches);
            }

            generateRandomVegetation(primer, info, rand, height);

        }

        if (doBorder(info, Direction.XMIN)) {
            int x = 0;
            for (int z = 0 ; z < 16 ; z++) {
                generateOceanBorder(primer, info, xRail, zRail, canDoParks, x, z);
            }
        }
        if (doBorder(info, Direction.XMAX)) {
            int x = 15;
            for (int z = 0 ; z < 16 ; z++) {
                generateOceanBorder(primer, info, xRail, zRail, canDoParks, x, z);
            }
        }
        if (doBorder(info, Direction.ZMIN)) {
            int z = 0;
            for (int x = 0 ; x < 16 ; x++) {
                generateOceanBorder(primer, info, xRail, zRail, canDoParks, x, z);
            }
        }
        if (doBorder(info, Direction.ZMAX)) {
            int z = 15;
            for (int x = 0 ; x < 16 ; x++) {
                generateOceanBorder(primer, info, xRail, zRail, canDoParks, x, z);
            }
        }


    }

    private void generateCorridors(ChunkPrimer primer, BuildingInfo info, boolean xRail, boolean zRail) {
        IBlockState railx = Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
        char railxC = (char) Block.BLOCK_STATE_IDS.get(railx);
        IBlockState railz = Blocks.RAIL.getDefaultState();
        char railzC = (char) Block.BLOCK_STATE_IDS.get(railz);
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int index = (x << 12) | (z << 8);
                char b;
                if ((xRail && z >= 7 && z <= 10) || (zRail && x >= 7 && x <= 10)) {
                    int height = groundLevel - 5;
                    if (xRail && z == 10) {
                        b = railxC;
                    } else if (zRail && x == 10) {
                        b = railzC;
                    } else {
                        b = airChar;
                    }
                    primer.data[index + (height++)] = b;

                    primer.data[index + (height++)] = airChar;
                    primer.data[index + (height++)] = airChar;

                    if ((xRail && x == 7 && (z == 8 || z == 9)) || (zRail && z == 7 && (x == 8 || x == 9))) {
                        primer.data[index + (height++)] = glassChar;
                        primer.data[index + (height++)] = glowstoneChar;
                    } else {
                        primer.data[index + (height++)] = bricksChar;
                        primer.data[index + (height++)] = bricksChar;
                    }
//                            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + info.getCityGroundLevel(), baseChar);
                } else {
                    BaseTerrainGenerator.setBlockStateRange(primer, index + groundLevel - 5, index + info.getCityGroundLevel(), baseChar);
                }
            }
        }
    }

    private void generateRandomVegetation(ChunkPrimer primer, BuildingInfo info, Random rand, int height) {
        if (info.getXmin().hasBuilding) {
            for (int x = 0 ; x < 2 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    int index = (x << 12) | (z << 8) + height;
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    while (primer.data[index - 1] == airChar) {
                        index--;
                        height--;
                    }
                    while (rand.nextFloat() < (provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (3 - x))) {
                        primer.data[index++] = leavesChar;
                        height++;
                    }
                }
            }
        }
        if (info.getXmax().hasBuilding) {
            for (int x = 13 ; x < 15 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    int index = (x << 12) | (z << 8) + height;
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    while (primer.data[index - 1] == airChar) {
                        index--;
                        height--;
                    }
                    while (rand.nextFloat() < (provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (x - 12))) {
                        primer.data[index++] = leavesChar;
                        height++;
                    }
                }
            }
        }
        if (info.getZmin().hasBuilding) {
            for (int z = 0 ; z < 2 ; z++) {
                for (int x = 0 ; x < 16 ; x++) {
                    int index = (x << 12) | (z << 8) + height;
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    while (primer.data[index - 1] == airChar) {
                        index--;
                        height--;
                    }
                    while (rand.nextFloat() < (provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (3 - z))) {
                        primer.data[index++] = leavesChar;
                        height++;
                    }
                }
            }
        }
        if (info.getZmax().hasBuilding) {
            for (int z = 13 ; z < 15 ; z++) {
                for (int x = 0 ; x < 16 ; x++) {
                    int index = (x << 12) | (z << 8) + height;
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    while (primer.data[index - 1] == airChar) {
                        index--;
                        height--;
                    }
                    while (rand.nextFloat() < (provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (z - 12))) {
                        primer.data[index++] = leavesChar;
                        height++;
                    }
                }
            }
        }
    }

    private void generateParkSection(ChunkPrimer primer, BuildingInfo info, int height, boolean elevated) {
        char b;
        boolean el00 = info.getXmin().getZmin().isElevatedParkSection();
        boolean el10 = info.getZmin().isElevatedParkSection();
        boolean el20 = info.getXmax().getZmin().isElevatedParkSection();
        boolean el01 = info.getXmin().isElevatedParkSection();
        boolean el21 = info.getXmax().isElevatedParkSection();
        boolean el02 = info.getXmin().getZmax().isElevatedParkSection();
        boolean el12 = info.getZmax().isElevatedParkSection();
        boolean el22 = info.getXmax().getZmax().isElevatedParkSection();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                if (x == 0 || x == 15 || z == 0 || z == 15) {
                    b = street;
                    if (elevated) {
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
                primer.data[(x << 12) | (z << 8) + height] = b;
            }
        }
    }

    private void generateFullStreetSection(ChunkPrimer primer, int height) {
        char b;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                if (isSide(x, z)) {
                    b = street;
                } else {
                    b = street2;
                }
                primer.data[(x << 12) | (z << 8) + height] = b;
            }
        }
    }

    private void generateNormalStreetSection(ChunkPrimer primer, BuildingInfo info, int height) {
        char b;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                b = streetBase;
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
                primer.data[(x << 12) | (z << 8) + height] = b;
            }
        }
    }

    private void generateOceanBorder(ChunkPrimer primer, BuildingInfo info, boolean xRail, boolean zRail, boolean canDoParks, int x, int z) {
        int index = (x << 12) | (z << 8);
        if (xRail || zRail) {
            if (groundLevel < info.getCityGroundLevel()) {
                BaseTerrainGenerator.setBlockStateRange(primer, index + groundLevel, index + info.getCityGroundLevel()+1, bricksChar);
            }
        } else {
            BaseTerrainGenerator.setBlockStateRange(primer, index + provider.profile.BEDROCK_LAYER + 30, index + info.getCityGroundLevel()+1, bricksChar);
        }
        if (canDoParks) {
            if (!borderNeedsConnectionToAdjacentChunk(info, x, z)) {
                primer.data[index + info.getCityGroundLevel()+1] = wallChar;
            } else {
                primer.data[index + info.getCityGroundLevel()+1] = airChar;
            }
        }
    }

    private boolean borderNeedsConnectionToAdjacentChunk(BuildingInfo info, int x, int z) {
        boolean needOpening = false;
        for (Direction direction : Direction.VALUES) {
            BuildingInfo adjacent = direction.get(info);
            if (direction.atSide(x, z) && adjacent.getActualStairDirection() == direction.getOpposite()) {
                BuildingPart stairType = adjacent.stairType;
                Integer z1 = stairType.getMetaInteger("z1");
                Integer z2 = stairType.getMetaInteger("z2");
                Transform transform = direction.getOpposite().getRotation();
                int xx1 = transform.rotateX(15, z1);
                int zz1 = transform.rotateZ(15, z1);
                int xx2 = transform.rotateX(15, z2);
                int zz2 = transform.rotateZ(15, z2);
                if (x >= Math.min(xx1, xx2) && x <= Math.max(xx1, xx2) && z >= Math.min(zz1, zz2) && z <= Math.max(zz1, zz2)) {
                    needOpening = true;
                    break;
                }
            }
        }
        return needOpening;
    }

    private int generatePart(ChunkPrimer primer, BuildingInfo info, BuildingPart part,
                             Transform transform,
                             int ox, int oy, int oz, List<Integer> torches) {
        CompiledPalette compiledPalette = info.getCompiledPalette();
        for (int x = 0; x < part.getXSize(); x++) {
            for (int z = 0; z < part.getZSize(); z++) {
                char[] vs = part.getVSlice(x, z);
                if (vs != null) {
                    int rx = ox + transform.rotateX(x, z);
                    int rz = oz + transform.rotateZ(x, z);
                    int index = (rx << 12) | (rz << 8) + oy;
                    int len = vs.length;
                    for (int y = 0 ; y < len ; y++) {
                        char c = vs[y];
                        Character b = compiledPalette.get(c);
                        if (b == null) {
                            throw new RuntimeException("Could not find entry '" + c + "' in the palette for part '" + part.getName() + "'!");
                        }
                        if (transform != Transform.ROTATE_NONE) {
                            if (getRotatableChars().contains(b)) {
                                IBlockState bs = Block.BLOCK_STATE_IDS.getByValue(b);
                                bs = bs.withRotation(transform.getMcRotation());
                                b = (char) Block.BLOCK_STATE_IDS.get(bs);
                            } else if (getRailChars().contains(b)) {
                                IBlockState bs = Block.BLOCK_STATE_IDS.getByValue(b);
                                PropertyEnum<BlockRailBase.EnumRailDirection> shapeProperty;
                                if (bs.getBlock() == Blocks.RAIL) {
                                    shapeProperty = BlockRail.SHAPE;
                                } else if (bs.getBlock() == Blocks.GOLDEN_RAIL) {
                                    shapeProperty = BlockRailPowered.SHAPE;
                                } else {
                                    throw new RuntimeException("Error with rail!");
                                }
                                BlockRailBase.EnumRailDirection shape = bs.getValue(shapeProperty);
                                bs = bs.withProperty(shapeProperty, transform.transform(shape));
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
                            } else if (b == spawnerChar) {
                                if (provider.profile.GENERATE_SPAWNERS) {
                                    String mobid = part.getMobID(info, x, y, z);
                                    info.addSpawnerTodo(new BlockPos(x, oy + y, z), mobid);
                                } else {
                                    b = airChar;
                                }
                            } else if (b == chestChar) {
                                info.addChestTodo(new BlockPos(x, oy+y, z));
                            }
                            primer.data[index] = b;
                        }
                        index++;
                    }
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
                        primer.data[index] = b;
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

    private void generateBuilding(ChunkPrimer primer, BuildingInfo info, List<Integer> torches) {
        int lowestLevel = info.getCityGroundLevel() - info.floorsBelowGround * 6;

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int index = (x << 12) | (z << 8);
                BaseTerrainGenerator.setBlockStateRange(primer, index + provider.profile.BEDROCK_LAYER, index + lowestLevel, baseChar);
                if (primer.data[index] == airChar) {
                    primer.data[index + lowestLevel] = styledBricks;      // There is nothing below so we fill this with bricks
                }
            }
        }

        int height = lowestLevel;
        for (int f = -info.floorsBelowGround; f <= info.getNumFloors(); f++) {
            BuildingPart part = info.getFloor(f);
            generatePart(primer, info, part, Transform.ROTATE_NONE, 0, height, 0, torches);

            // Check for doors
            boolean isTop = f == info.getNumFloors();   // The top does not need generated doors
            if (!isTop) {
                generateDoors(primer, info, height+1, f);
            }

            height += 6;    // We currently only support 6 here
        }

        if (info.floorsBelowGround > 0) {
            // Underground we replace the glass with solid bricks
            for (int x = 0 ; x < 16 ; x++) {
                int index = (x << 12) | (0 << 8);
                BaseTerrainGenerator.setBlockStateRange(primer, index + lowestLevel, index + info.getCityGroundLevel()+1, styledBricks);
                index = (x << 12) | (15 << 8);
                BaseTerrainGenerator.setBlockStateRange(primer, index + lowestLevel, index + info.getCityGroundLevel()+1, styledBricks);
            }
            for (int z = 1 ; z < 15 ; z++) {
                int index = (0 << 12) | (z << 8);
                BaseTerrainGenerator.setBlockStateRange(primer, index + lowestLevel, index + info.getCityGroundLevel()+1, styledBricks);
                index = (15 << 12) | (z << 8);
                BaseTerrainGenerator.setBlockStateRange(primer, index + lowestLevel, index + info.getCityGroundLevel()+1, styledBricks);
            }
        }

        if (info.floorsBelowGround >= 1) {
            // We have to potentially connect to corridors
            generateCorridorConnections(primer, info);
        }
    }

    private char getDoor(Block door, boolean upper, boolean left, EnumFacing facing) {
        IBlockState bs = door.getDefaultState()
                .withProperty(BlockDoor.HALF, upper ? BlockDoor.EnumDoorHalf.UPPER : BlockDoor.EnumDoorHalf.LOWER)
                .withProperty(BlockDoor.HINGE, left ? BlockDoor.EnumHingePosition.LEFT : BlockDoor.EnumHingePosition.RIGHT)
                .withProperty(BlockDoor.FACING, facing);
        return (char) Block.BLOCK_STATE_IDS.get(bs);
    }

    private void generateDoors(ChunkPrimer primer, BuildingInfo info, int height, int f) {
        if (info.hasConnectionAtX(f + info.floorsBelowGround)) {
            int x = 0;
            int index = (x << 12) | (6 << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            index = (x << 12) | (9 << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            if (hasConnectionWithBuilding(f, info, info.getXmin())) {
                index = (x << 12) | (7 << 8);
                primer.data[index + height] = airChar;
                primer.data[index + height + 1] = airChar;
                primer.data[index + height + 2] = styledBricks;
                index = (x << 12) | (8 << 8);
                primer.data[index + height] = airChar;
                primer.data[index + height + 1] = airChar;
                primer.data[index + height + 2] = styledBricks;
            } else if (hasConnectionToTopOrOutside(f, info, info.getXmin())) {
                index = (x << 12) | (7 << 8);
                primer.data[index + height] = getDoor(info.doorBlock, false, true, EnumFacing.EAST);
                primer.data[index + height + 1] = getDoor(info.doorBlock, true, true, EnumFacing.EAST);
                primer.data[index + height + 2] = styledBricks;
                index = (x << 12) | (8 << 8);
                primer.data[index + height] = getDoor(info.doorBlock, false, false, EnumFacing.EAST);
                primer.data[index + height + 1] = getDoor(info.doorBlock, true, false, EnumFacing.EAST);
                primer.data[index + height + 2] = styledBricks;
            }
        }
        if (hasConnectionWithBuildingMax(f, info, info.getXmax(), Orientation.X)) {
            int x = 15;
            int index = (x << 12) | (6 << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            index = (x << 12) | (9 << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            index = (x << 12) | (7 << 8);
            primer.data[index + height] = airChar;
            primer.data[index + height + 1] = airChar;
            primer.data[index + height + 2] = styledBricks;
            index = (x << 12) | (8 << 8);
            primer.data[index + height] = airChar;
            primer.data[index + height + 1] = airChar;
            primer.data[index + height + 2] = styledBricks;
        } else if (hasConnectionToTopOrOutside(f, info, info.getXmax()) && info.getXmax().hasConnectionAtX(f + info.getXmax().floorsBelowGround)) {
            int x = 15;
            int index = (x << 12) | (6 << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            index = (x << 12) | (9 << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            index = (x << 12) | (7 << 8);
            primer.data[index + height] = getDoor(info.doorBlock, false, false, EnumFacing.WEST);
            primer.data[index + height + 1] = getDoor(info.doorBlock, true, false, EnumFacing.WEST);
            primer.data[index + height + 2] = styledBricks;
            index = (x << 12) | (8 << 8);
            primer.data[index + height] = getDoor(info.doorBlock, false, true, EnumFacing.WEST);
            primer.data[index + height + 1] = getDoor(info.doorBlock, true, true, EnumFacing.WEST);
            primer.data[index + height + 2] = styledBricks;
        }
        if (info.hasConnectionAtZ(f + info.floorsBelowGround)) {
            int z = 0;
            int index = (6 << 12) | (z << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            index = (9 << 12) | (z << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            if (hasConnectionWithBuilding(f, info, info.getZmin())) {
                index = (7 << 12) | (z << 8);
                primer.data[index + height] = airChar;
                primer.data[index + height + 1] = airChar;
                primer.data[index + height + 2] = styledBricks;
                index = (8 << 12) | (z << 8);
                primer.data[index + height] = airChar;
                primer.data[index + height + 1] = airChar;
                primer.data[index + height + 2] = styledBricks;
            } else if (hasConnectionToTopOrOutside(f, info, info.getZmin())) {
                index = (7 << 12) | (z << 8);
                primer.data[index + height] = getDoor(info.doorBlock, false, true, EnumFacing.NORTH);
                primer.data[index + height + 1] = getDoor(info.doorBlock, true, true, EnumFacing.NORTH);
                primer.data[index + height + 2] = styledBricks;
                index = (8 << 12) | (z << 8);
                primer.data[index + height] = getDoor(info.doorBlock, false, false, EnumFacing.NORTH);
                primer.data[index + height + 1] = getDoor(info.doorBlock, true, false, EnumFacing.NORTH);
                primer.data[index + height + 2] = styledBricks;
            }
        }
        if (hasConnectionWithBuildingMax(f, info, info.getZmax(), Orientation.Z)) {
            int z = 15;
            int index = (6 << 12) | (z << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            index = (9 << 12) | (z << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            index = (7 << 12) | (z << 8);
            primer.data[index + height] = airChar;
            primer.data[index + height + 1] = airChar;
            primer.data[index + height + 2] = styledBricks;
            index = (8 << 12) | (z << 8);
            primer.data[index + height] = airChar;
            primer.data[index + height + 1] = airChar;
            primer.data[index + height + 2] = styledBricks;
        } else if (hasConnectionToTopOrOutside(f, info, info.getZmax()) && info.getZmax().hasConnectionAtZ(f + info.getZmax().floorsBelowGround)) {
            int z = 15;
            int index = (6 << 12) | (z << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            index = (9 << 12) | (z << 8);
            BaseTerrainGenerator.setBlockStateRange(primer, index + height, index + height + 3, styledBricks);
            index = (7 << 12) | (z << 8);
            primer.data[index + height] = getDoor(info.doorBlock, false, false, EnumFacing.SOUTH);
            primer.data[index + height + 1] = getDoor(info.doorBlock, true, false, EnumFacing.SOUTH);
            primer.data[index + height + 2] = styledBricks;
            index = (8 << 12) | (z << 8);
            primer.data[index + height] = getDoor(info.doorBlock, false, true, EnumFacing.SOUTH);
            primer.data[index + height + 1] = getDoor(info.doorBlock, true, true, EnumFacing.SOUTH);
            primer.data[index + height + 2] = styledBricks;
        }
    }

    private void generateCorridorConnections(ChunkPrimer primer, BuildingInfo info) {
        if (info.getXmin().hasXCorridor()) {
            int x = 0;
            for (int z = 7 ; z <= 10 ; z++) {
                int index = (x << 12) | (z << 8);
                BaseTerrainGenerator.setBlockStateRange(primer, index + groundLevel-5, index + groundLevel-2, airChar);
            }
        }
        if (info.getXmax().hasXCorridor()) {
            int x = 15;
            for (int z = 7 ; z <= 10 ; z++) {
                int index = (x << 12) | (z << 8);
                BaseTerrainGenerator.setBlockStateRange(primer, index + groundLevel-5, index + groundLevel-2, airChar);
            }
        }
        if (info.getZmin().hasXCorridor()) {
            int z = 0;
            for (int x = 7 ; x <= 10 ; x++) {
                int index = (x << 12) | (z << 8);
                BaseTerrainGenerator.setBlockStateRange(primer, index + groundLevel-5, index + groundLevel-2, airChar);
            }
        }
        if (info.getZmax().hasXCorridor()) {
            int z = 15;
            for (int x = 7 ; x <= 10 ; x++) {
                int index = (x << 12) | (z << 8);
                BaseTerrainGenerator.setBlockStateRange(primer, index + groundLevel-5, index + groundLevel-2, airChar);
            }
        }
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

    private boolean isSide(int x, int z) {
        return x == 0 || x == 15 || z == 0 || z == 15;
    }

    private boolean isStreetBorder(int x, int z) {
        return x <= streetBorder || x >= (15 - streetBorder) || z <= streetBorder || z >= (15 - streetBorder);
    }
}
