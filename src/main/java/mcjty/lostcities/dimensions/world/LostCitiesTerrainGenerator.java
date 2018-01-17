package mcjty.lostcities.dimensions.world;

import mcjty.lostcities.api.LostCityEvent;
import mcjty.lostcities.api.RailChunkType;
import mcjty.lostcities.dimensions.world.lost.*;
import mcjty.lostcities.dimensions.world.lost.cityassets.*;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.GeometryTools;
import mcjty.lostcities.varia.PrimerTools;
import mcjty.lostcities.varia.Tools;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;
import java.util.function.BiFunction;

import static mcjty.lostcities.dimensions.world.IslandTerrainGenerator.ISLANDS;

public class LostCitiesTerrainGenerator extends NormalTerrainGenerator {

    private static int g_seed = 123456789;
    private final int groundLevel;
    private final int waterLevel;
    private static boolean charsSetup = false;
    public static char airChar;
    public static char hardAirChar;
    public static char glowstoneChar;
    public static char baseChar;
    public static char glassChar;       // @todo: for space: depend on city style
    public static char liquidChar;
    public static char leavesChar;
    public static char leaves2Char;
    public static char leaves3Char;
    public static char ironbarsChar;
    public static char grassChar;
    public static char bedrockChar;
    public static char endportalChar;
    public static char endportalFrameChar;
    public static char torchChar;
    public static char goldBlockChar;
    public static char diamondBlockChar;
    public static char spawnerChar;
    public static char chestChar;

    private static Set<Character> rotatableChars = null;
    private static Set<Character> railChars = null;
    private static Set<Character> glassChars = null;
    private static Set<Character> charactersNeedingTodo = null;

    private Character street;
    private Character streetBase;
    private Character street2;
    private int streetBorder;

    private NoiseGeneratorPerlin rubbleNoise;
    private NoiseGeneratorPerlin leavesNoise;
    private NoiseGeneratorPerlin ruinNoise;

    private static char randomLeafs[] = null;


    private IslandTerrainGenerator islandTerrainGenerator = new IslandTerrainGenerator(ISLANDS);
    private SpaceTerrainGenerator spaceTerrainGenerator = new SpaceTerrainGenerator();


    public LostCitiesTerrainGenerator(LostCityChunkGenerator provider) {
        super(provider);
        this.groundLevel = provider.profile.GROUNDLEVEL;
        this.waterLevel = provider.profile.GROUNDLEVEL - provider.profile.WATERLEVEL_OFFSET;
        this.rubbleNoise = new NoiseGeneratorPerlin(provider.rand, 4);
        this.leavesNoise = new NoiseGeneratorPerlin(provider.rand, 4);
        this.ruinNoise = new NoiseGeneratorPerlin(provider.rand, 4);

        islandTerrainGenerator.setup(provider.worldObj, provider);
        spaceTerrainGenerator.setup(provider.worldObj, provider);
    }

    public static char getRandomLeaf() {
        if (randomLeafs == null) {
            randomLeafs = new char[128];
            int i = 0;
            for ( ; i < 20 ; i++) {
                randomLeafs[i] = leaves2Char;
            }
            for ( ; i < 40 ; i++) {
                randomLeafs[i] = leaves3Char;
            }
            for ( ; i < randomLeafs.length ; i++) {
                randomLeafs[i] = leavesChar;
            }
        }
        return randomLeafs[fastrand128()];
    }

    public static Set<Character> getRailChars() {
        if (railChars == null) {
            railChars = new HashSet<>();
            addStates(Blocks.RAIL, railChars);
            addStates(Blocks.GOLDEN_RAIL, railChars);
        }
        return railChars;
    }

    public static Set<Character> getGlassChars() {
        if (glassChars == null) {
            glassChars = new HashSet<>();
            addStates(Blocks.GLASS, glassChars);
            addStates(Blocks.STAINED_GLASS, glassChars);
            addStates(Blocks.GLASS_PANE, glassChars);
            addStates(Blocks.STAINED_GLASS_PANE, glassChars);
        }
        return glassChars;
    }

    public static Set<Character> getCharactersNeedingTodo() {
        if (charactersNeedingTodo == null) {
            charactersNeedingTodo = new HashSet<>();
            charactersNeedingTodo.add(torchChar);
            charactersNeedingTodo.add(spawnerChar);
            charactersNeedingTodo.add(chestChar);
            charactersNeedingTodo.add(glowstoneChar);
            charactersNeedingTodo.add((char) Block.BLOCK_STATE_IDS.get(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.ACACIA)));
            charactersNeedingTodo.add((char) Block.BLOCK_STATE_IDS.get(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.BIRCH)));
            charactersNeedingTodo.add((char) Block.BLOCK_STATE_IDS.get(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.OAK)));
            charactersNeedingTodo.add((char) Block.BLOCK_STATE_IDS.get(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.SPRUCE)));
            charactersNeedingTodo.add((char) Block.BLOCK_STATE_IDS.get(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.DARK_OAK)));
            charactersNeedingTodo.add((char) Block.BLOCK_STATE_IDS.get(Blocks.SAPLING.getDefaultState().withProperty(BlockSapling.TYPE, BlockPlanks.EnumType.JUNGLE)));
        }
        return charactersNeedingTodo;
    }

    public static Set<Character> getRotatableChars() {
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
            addStates(Blocks.LADDER, rotatableChars);
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

    public static void setupChars() {
        if (!charsSetup) {
            airChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.AIR.getDefaultState());
            hardAirChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.COMMAND_BLOCK.getDefaultState());
            glowstoneChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GLOWSTONE.getDefaultState());
            baseChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.STONE.getDefaultState());
            liquidChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.WATER.getDefaultState());

            // @todo
            glassChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GLASS.getDefaultState());

            leavesChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.LEAVES.getDefaultState()
                    .withProperty(BlockLeaves.DECAYABLE, false));
            leaves2Char = (char) Block.BLOCK_STATE_IDS.get(Blocks.LEAVES.getDefaultState()
                    .withProperty(BlockLeaves.DECAYABLE, false)
                    .withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE));
            leaves3Char = (char) Block.BLOCK_STATE_IDS.get(Blocks.LEAVES.getDefaultState()
                    .withProperty(BlockLeaves.DECAYABLE, false)
                    .withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE));

            ironbarsChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.IRON_BARS.getDefaultState());
            grassChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GRASS.getDefaultState());
            bedrockChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.BEDROCK.getDefaultState());
            endportalChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.END_PORTAL.getDefaultState());
            endportalFrameChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.END_PORTAL_FRAME.getDefaultState());
            torchChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.TORCH.getDefaultState());
            goldBlockChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.GOLD_BLOCK.getDefaultState());
            diamondBlockChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.DIAMOND_BLOCK.getDefaultState());
            spawnerChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.MOB_SPAWNER.getDefaultState());
            chestChar = (char) Block.BLOCK_STATE_IDS.get(Blocks.CHEST.getDefaultState());
            charsSetup = true;
        }
    }

    private static int fastrand() {
        g_seed = (214013*g_seed+2531011);
        return (g_seed>>16)&0x7FFF;
    }

    public static int fastrand128() {
        g_seed = (214013*g_seed+2531011);
        return (g_seed>>16)&0x7F;
    }

    // Note that for normal chunks this is called with a pre-filled in landscape primer
    public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);

        // @todo this setup is not very clean
        CityStyle cityStyle = info.getCityStyle();

        street = info.getCompiledPalette().get(cityStyle.getStreetBlock());
        streetBase = info.getCompiledPalette().get(cityStyle.getStreetBaseBlock());
        street2 = info.getCompiledPalette().get(cityStyle.getStreetVariantBlock());
        streetBorder = (16 - cityStyle.getStreetWidth()) / 2;

        if (info.isCity) {
            doCityChunk(chunkX, chunkZ, primer, info);
        } else {
            // We already have a prefilled core chunk (as generated from doCoreChunk)
            doNormalChunk(chunkX, chunkZ, primer, info);
        }

        Railway.RailChunkInfo railInfo = info.getRailInfo();
        if (railInfo.getType() != RailChunkType.NONE) {
            generateRailways(primer, info, railInfo);
        }
        generateRailwayDungeons(primer, info);

        fixTorches(primer, info);

        // We make a new random here because the primer for a normal chunk may have
        // been cached and we want to be able to do the same when returning from a cached
        // primer vs generating it here
        provider.rand.setSeed(chunkX * 257017164707L + chunkZ * 101754694003L);

        LostCityEvent.PreExplosionEvent event = new LostCityEvent.PreExplosionEvent(provider.worldObj, provider, chunkX, chunkZ, primer);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            if (info.getDamageArea().hasExplosions()) {
                breakBlocksForDamage(chunkX, chunkZ, primer, info);
                fixAfterExplosionNew(primer, info, provider.rand);
            }
            generateDebris(primer, provider.rand, info);
        }

//        if (provider.profile.isFloating()) {
//            // cityFactor -> 0: 128
//            // cityFactor -> .1: 10
//            // cityFactor -> .2: 20
//            // cityFactor -> .3: 30
//            float offset = characteristics.cityFactor * 200 - 30;
//            if (offset > 30) {
//                offset = 30;
//            }
//            int to = (int) (info.getCityGroundLevel() - offset);
//            if (to > 0 && to < 255) {
//                for (int x = 0; x < 16; x++) {
//                    for (int z = 0; z < 16; z++) {
//                        int index = (x << 12) | (z << 8);
//                        clearRange(primer, index, 0, to);
//                    }
//                }
//            }
//        }



    }

    private void fixTorches(ChunkPrimer primer, BuildingInfo info) {
        List<Integer> torches = info.getTorchTodo();
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
        info.clearTorchTodo();
    }

    @Override
    public void replaceBlocksForBiome(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] Biomes) {
        switch (provider.profile.LANDSCAPE_TYPE) {
            case DEFAULT:
                super.replaceBlocksForBiome(chunkX, chunkZ, primer, Biomes);
                break;
            case FLOATING:
                islandTerrainGenerator.replaceBlocksForBiome(chunkX, chunkZ, primer, Biomes);
                break;
            case SPACE:
                spaceTerrainGenerator.replaceBlocksForBiome(chunkX, chunkZ, primer, Biomes);
                break;
        }
    }

    public void doCoreChunk(int chunkX, int chunkZ, ChunkPrimer primer) {
        switch (provider.profile.LANDSCAPE_TYPE) {
            case DEFAULT:
                defaultGenerate(chunkX, chunkZ, primer);
                break;
            case FLOATING:
                islandTerrainGenerator.generate(chunkX, chunkZ, primer);
                break;
            case SPACE:
                spaceTerrainGenerator.generate(chunkX, chunkZ, primer);
                break;
        }
    }

    private void defaultGenerate(int chunkX, int chunkZ, ChunkPrimer primer) {
        generateHeightmap(chunkX, chunkZ);
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

    public void doNormalChunk(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info) {
//        debugClearChunk(chunkX, chunkZ, primer);
        if (provider.profile.isDefault()) {
            flattenChunkToCityBorder(chunkX, chunkZ, primer);
        }
        generateBridges(primer, info);
        generateHighways(chunkX, chunkZ, primer, info);
    }

    private void breakBlocksForDamage(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        DamageArea damageArea = info.getDamageArea();

        boolean clear = false;
        float damageFactor = 1.0f;

        for (int yy = 0; yy < 16; yy++) {
            if (clear || damageArea.hasExplosions(yy)) {
                if (clear || damageArea.isCompletelyDestroyed(yy)) {
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
                    // All further subchunks will also be totally cleared
                    clear = true;
                } else {
                    for (int y = 0 ; y < 16 ; y++) {
                        int cntDamaged = 0;
                        int cntAir = 0;
                        int index = yy*16 + y;
                        int cury = yy * 16 + y;
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                char d = primer.data[index];
                                if (d != airChar || (index & 0xff) < waterLevel) {
                                    float damage = damageArea.getDamage(cx + x, cury, cz + z) * damageFactor;
                                    if (damage >= 0.001) {
                                        Character newd = damageArea.damageBlock(d, provider, cury, damage, info.getCompiledPalette());
                                        if (newd != d) {
                                            primer.data[index] = newd;
                                            cntDamaged++;
                                        }
                                    }
                                } else {
                                    cntAir++;
                                }
                                index += 1<<8;
                            }
                        }

                        int tot = cntDamaged + cntAir;
                        if (tot > 250) {
                            damageFactor = 200;
                            clear = true;
                        } else if (tot > 220) {
                            damageFactor = damageFactor * 1.4f;
                        } else if (tot > 180) {
                            damageFactor = damageFactor * 1.2f;
                        }

                    }

//                    for (int x = 0; x < 16; x++) {
//                        for (int z = 0; z < 16; z++) {
//                            int index = (x << 12) | (z << 8) + yy * 16;
//                            for (int y = 0; y < 16; y++) {
//                                char d = primer.data[index];
//                                if (d != airChar || (index & 0xff) < waterLevel) {
//                                    int cury = yy * 16 + y;
//                                    float damage = damageArea.getDamage(cx + x, cury, cz + z);
//                                    if (damage >= 0.001) {
//                                        Character newd = damageArea.damageBlock(d, provider, cury, damage, info.getCompiledPalette());
//                                        if (newd != d) {
//                                            primer.data[index] = newd;
//                                        }
//                                    }
//                                }
//                                index++;
//                            }
//                        }
//                    }
                }
            }
        }
    }

    private void generateHighways(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info) {
        int levelX = Highway.getXHighwayLevel(chunkX, chunkZ, provider);
        int levelZ = Highway.getZHighwayLevel(chunkX, chunkZ, provider);
        if (levelX == levelZ && levelX >= 0) {
            // Crossing
            generateHighwayPart(primer, info, levelX, Transform.ROTATE_NONE, info.getXmax(), info.getZmax(), "_bi");
        } else if (levelX >= 0 && levelZ >= 0) {
            // There are two highways on different level. Make sure the lowest one is done first because it
            // will clear out what is above it
            if (levelX == 0) {
                generateHighwayPart(primer, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), "");
                generateHighwayPart(primer, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), "");
            } else {
                generateHighwayPart(primer, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), "");
                generateHighwayPart(primer, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), "");
            }
        } else {
            if (levelX >= 0) {
                generateHighwayPart(primer, info, levelX, Transform.ROTATE_NONE, info.getZmin(), info.getZmax(), "");
            } else if (levelZ >= 0) {
                generateHighwayPart(primer, info, levelZ, Transform.ROTATE_90, info.getXmax(), info.getXmax(), "");
            }
        }
    }

    private void generateHighwayPart(ChunkPrimer primer, BuildingInfo info, int level, Transform transform, BuildingInfo adjacent1, BuildingInfo adjacent2, String suffix) {
        int highwayGroundLevel = provider.profile.GROUNDLEVEL + level * 6;

        BuildingPart part;
        if (info.isTunnel(level)) {
            // We know we need a tunnel
            part = AssetRegistries.PARTS.get("highway_tunnel" + suffix);
            generatePart(primer, info, part, transform, 0, highwayGroundLevel, 0, true);
        } else if (info.isCity && level <= adjacent1.cityLevel && level <= adjacent2.cityLevel && adjacent1.isCity && adjacent2.isCity) {
            // Simple highway in the city
            part = AssetRegistries.PARTS.get("highway_open" + suffix);
            int height = generatePart(primer, info, part, transform, 0, highwayGroundLevel, 0, true);
            // Clear a bit more above the highway
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int index = (x << 12) | (z << 8);
                    clearRange(primer, index, height, height+15);
                }
            }
        } else {
            part = AssetRegistries.PARTS.get("highway_bridge" + suffix);
            int height = generatePart(primer, info, part, transform, 0, highwayGroundLevel, 0, true);
            // Clear a bit more above the highway
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int index = (x << 12) | (z << 8);
                    clearRange(primer, index, height, height+15);
                }
            }
        }

        Character support = part.getMetaChar("support");
        if (support != null) {
            char sup = info.getCompiledPalette().get(support);
            int x1 = transform.rotateX(0, 15);
            int z1 = transform.rotateZ(0, 15);
            int index1 = (x1 << 12) | (z1 << 8) + highwayGroundLevel - 1;
            int x2 = transform.rotateX(0, 0);
            int z2 = transform.rotateZ(0, 0);

            int index2 = (x2 << 12) | (z2 << 8) + highwayGroundLevel - 1;
            for (int y = 0; y < 40; y++) {
                boolean done = false;
                if (primer.data[index1] == airChar || primer.data[index1] == liquidChar) {
                    primer.data[index1] = sup;
                    done = true;
                }
                if (primer.data[index2] == airChar || primer.data[index2] == liquidChar) {
                    primer.data[index2] = sup;
                    done = true;
                }
                index1--;
                index2--;
                if (!done) {
                    break;
                }
            }
        }
    }

    private void clearRange(ChunkPrimer primer, int index, int height1, int height2) {
        if (waterLevel > groundLevel) {
            // Special case for drowned city
            PrimerTools.setBlockStateRangeSafe(primer, index + height1, index + waterLevel, liquidChar);
            PrimerTools.setBlockStateRangeSafe(primer, index + waterLevel, index + height2, airChar);
        } else {
            PrimerTools.setBlockStateRange(primer, index + height1, index + height2, airChar);
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
                    if (b == torchChar) {
                        if (provider.profile.GENERATE_LIGHTING) {
                            info.addTorchTodo(index);
                        } else {
                            b = airChar;        // No torch!
                        }
                    }
                    primer.data[index++] = b;
                    l++;
                }
            }
        }

        Character support = bt.getMetaChar("support");
        if (support != null) {
            char sup = info.getCompiledPalette().get(support);
            BuildingInfo minDir = orientation.getMinDir().get(info);
            BuildingInfo maxDir = orientation.getMaxDir().get(info);
            if (minDir.hasBridge(provider, orientation) != null && maxDir.hasBridge(provider, orientation) != null) {
                // Needs support
                for (int y = waterLevel - 10; y <= groundLevel; y++) {
                    setBridgeSupport(primer, 7, y, 7, sup);
                    setBridgeSupport(primer, 7, y, 8, sup);
                    setBridgeSupport(primer, 8, y, 7, sup);
                    setBridgeSupport(primer, 8, y, 8, sup);
                }
            }
            if (minDir.hasBridge(provider, orientation) == null) {
                // Connection to the side section
                if (orientation == Orientation.X) {
                    int x = 0;
                    for (int z = 6; z <= 9; z++) {
                        int index = (x << 12) | (z << 8) + groundLevel;
                        primer.data[index] = sup;
                    }
                } else {
                    int z = 0;
                    for (int x = 6; x <= 9; x++) {
                        int index = (x << 12) | (z << 8) + groundLevel;
                        primer.data[index] = sup;
                    }
                }
            }
            if (maxDir.hasBridge(provider, orientation) == null) {
                // Connection to the side section
                if (orientation == Orientation.X) {
                    int x = 15;
                    for (int z = 6; z <= 9; z++) {
                        int index = (x << 12) | (z << 8) + groundLevel;
                        primer.data[index] = sup;
                    }
                } else {
                    int z = 15;
                    for (int x = 6; x <= 9; x++) {
                        int index = (x << 12) | (z << 8) + groundLevel;
                        primer.data[index] = sup;
                    }
                }
            }
        }
    }

    private void setBridgeSupport(ChunkPrimer primer, int x, int y, int z, char sup) {
        int index = (x << 12) | (z << 8) + y;
        primer.data[index] = sup;
    }

    /**
     * Get the lowest height of a corner of four chunks
     * info: reference to the bottom-right chunk. The 0,0 position of this chunk is the reference
     */
    private int getHeightAt00Corner(BuildingInfo info) {
        int h = getHeightForChunk(info);
        h = Math.min(h, getHeightForChunk(info.getXmin()));
        h = Math.min(h, getHeightForChunk(info.getZmin()));
        h = Math.min(h, getHeightForChunk(info.getXmin().getZmin()));
        return h;
    }

    private int getHeightForChunk(BuildingInfo info) {
        if (info.isCity) {
            return info.getCityGroundLevel();
        } else {
            if (info.isOcean()) {
                return groundLevel-4;
            } else {
                return info.getCityGroundLevel();
            }
        }
    }

    private void flattenChunkToCityBorder(int chunkX, int chunkZ, ChunkPrimer primer) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;

        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);
        float h00 = getHeightAt00Corner(info);
        float h10 = getHeightAt00Corner(info.getXmax());
        float h01 = getHeightAt00Corner(info.getZmax());
        float h11 = getHeightAt00Corner(info.getXmax().getZmax());

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
                    int height = bipolate(h11, h01, h10, h00, x, z);
//                    int height = bipolate(h00, h10, h01, h11, x, z);
                    for (GeometryTools.AxisAlignedBB2D box : boxes) {
                        double dist = GeometryTools.squaredDistanceBoxPoint(box, cx + x, cz + z);
                        if (dist < mindist) {
                            mindist = dist;
                        }
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

                    int offset = (int) (Math.sqrt(mindist) * 2);
                    flattenChunkBorderDownwards(primer, x, offset, z, provider.rand, height);
                }
            }
        }
    }

    public static boolean isWaterBiome(LostCityChunkGenerator provider, ChunkCoord coord) {
        BiomeInfo biomeInfo = BiomeInfo.getBiomeInfo(provider, coord);
        Biome[] biomes = biomeInfo.getBiomes();
        return isWaterBiome(biomes[55]) || isWaterBiome(biomes[54]) || isWaterBiome(biomes[56]);
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
        index = (x << 12) | (z << 8);
        clearRange(primer, index, level + offset + r, 230);
    }

    private void flattenChunkBorderDownwards(ChunkPrimer primer, int x, int offset, int z, Random rand, int level) {
        int r = rand.nextInt(2);
        int index = (x << 12) | (z << 8);
        clearRange(primer, index, level + offset + r, 230);
    }

    private void doCityChunk(int chunkX, int chunkZ, ChunkPrimer primer, BuildingInfo info) {
        boolean building = info.hasBuilding;

        ChunkHeightmap heightmap = provider.getHeightmap(info.chunkX, info.chunkZ);

        Random rand = new Random(provider.seed * 377 + chunkZ * 341873128712L + chunkX * 132897987541L);
        rand.nextFloat();
        rand.nextFloat();

        if (provider.profile.isDefault()) {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    int index = (x << 12) | (z << 8);
                    PrimerTools.setBlockStateRange(primer, index, index + provider.profile.BEDROCK_LAYER, bedrockChar);
                }
            }

            if (waterLevel > groundLevel) {
                // Special case for a high water level
                for (int x = 0; x < 16; ++x) {
                    for (int z = 0; z < 16; ++z) {
                        int index = (x << 12) | (z << 8);
                        PrimerTools.setBlockStateRange(primer, index + groundLevel, index + waterLevel, liquidChar);
                    }
                }
            }
        }

        LostCityEvent.PreGenCityChunkEvent event = new LostCityEvent.PreGenCityChunkEvent(provider.worldObj, provider, chunkX, chunkZ, primer);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            if (building) {
                generateBuilding(primer, info, heightmap);
            } else {
                generateStreet(primer, info, heightmap, rand);
            }
        }
        LostCityEvent.PostGenCityChunkEvent postevent = new LostCityEvent.PostGenCityChunkEvent(provider.worldObj, provider, chunkX, chunkZ, primer);
        MinecraftForge.EVENT_BUS.post(postevent);

        if (provider.profile.RUINS) {
            generateRuins(primer, info);
        }

        int levelX = info.getHighwayXLevel();
        int levelZ = info.getHighwayZLevel();
        if (!building) {
            Railway.RailChunkInfo railInfo = info.getRailInfo();
            if (levelX < 0 && levelZ < 0 && !railInfo.getType().isSurface()) {
                generateStreetDecorations(primer, info);
            }
        }
        if (levelX >= 0 || levelZ >= 0) {
            generateHighways(chunkX, chunkZ, primer, info);
        }

        if (provider.profile.RUBBLELAYER) {
            if (!info.hasBuilding || info.ruinHeight >= 0) {
                generateRubble(primer, chunkX, chunkZ, info);
            }
        }
    }

    private void generateRailwayDungeons(ChunkPrimer primer, BuildingInfo info) {
        if (info.railDungeon == null) {
            return;
        }
        if (info.getZmin().getRailInfo().getType() == RailChunkType.HORIZONTAL ||
                info.getZmax().getRailInfo().getType() == RailChunkType.HORIZONTAL) {
            int height = provider.profile.GROUNDLEVEL + Railway.RAILWAY_LEVEL_OFFSET * 6;
            generatePart(primer, info, info.railDungeon, Transform.ROTATE_NONE, 0, height, 0, false);
        }
    }

    private void generateRailways(ChunkPrimer primer, BuildingInfo info, Railway.RailChunkInfo railInfo) {
        int height = provider.profile.GROUNDLEVEL + railInfo.getLevel() * 6;
        RailChunkType type = railInfo.getType();
        BuildingPart part;
        Transform transform = Transform.ROTATE_NONE;
        boolean needsStaircase = false;
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
                    if (railInfo.getPart() != null) {
                        part = AssetRegistries.PARTS.get(railInfo.getPart());
                    } else {
                        part = AssetRegistries.PARTS.get("station_open");
                    }
                }
                break;
            case STATION_UNDERGROUND:
                part = AssetRegistries.PARTS.get("station_underground_stairs");
                needsStaircase = true;
                break;
            case STATION_EXTENSION_UNDERGROUND:
                part = AssetRegistries.PARTS.get("station_underground");
                break;
            case RAILS_END_HERE:
                part = AssetRegistries.PARTS.get("rails_horizontal_end");
                if (railInfo.getDirection() == Railway.RailDirection.EAST) {
                    transform = Transform.MIRROR_X;
                }
                break;
            case HORIZONTAL:
                part = AssetRegistries.PARTS.get("rails_horizontal");

                // If the adjacent chunks are also horizontal we take a sample of the blocks around us to see if we are in water
                RailChunkType type1 = info.getXmin().getRailInfo().getType();
                RailChunkType type2 = info.getXmax().getRailInfo().getType();
                if (!type1.isStation() && !type2.isStation()) {
                    if (primer.data[Tools.calcIndex(3, height+2, 3)] == liquidChar &&
                            primer.data[Tools.calcIndex(12, height+2, 3)] == liquidChar &&
                            primer.data[Tools.calcIndex(3, height+2, 12)] == liquidChar &&
                            primer.data[Tools.calcIndex(12, height+2, 12)] == liquidChar &&
                            primer.data[Tools.calcIndex(3, height+4, 7)] == liquidChar &&
                            primer.data[Tools.calcIndex(12, height+4, 8)] == liquidChar) {
                        part = AssetRegistries.PARTS.get("rails_horizontal_water");
                    }
                }
                break;
            case VERTICAL:
                part = AssetRegistries.PARTS.get("rails_vertical");
                if (primer.data[Tools.calcIndex(3, height+2, 3)] == liquidChar &&
                        primer.data[Tools.calcIndex(12, height+2, 3)] == liquidChar &&
                        primer.data[Tools.calcIndex(3, height+2, 12)] == liquidChar &&
                        primer.data[Tools.calcIndex(12, height+2, 12)] == liquidChar &&
                        primer.data[Tools.calcIndex(3, height+4, 7)] == liquidChar &&
                        primer.data[Tools.calcIndex(12, height+4, 8)] == liquidChar) {
                    part = AssetRegistries.PARTS.get("rails_vertical_water");
                }
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
        generatePart(primer, info, part, transform, 0, height, 0, false);

        Character railMainBlock = info.getCityStyle().getRailMainBlock();
        char rail = info.getCompiledPalette().get(railMainBlock);

        if (type == RailChunkType.HORIZONTAL) {
            // If there is a rail dungeon north or south we must make a connection here
            if (info.getZmin().railDungeon != null) {
                for (int z = 0; z < 4; z++) {
                    primer.data[Tools.calcIndex(6, height + 1, z)] = rail;
                    primer.data[Tools.calcIndex(6, height + 2, z)] = airChar;
                    primer.data[Tools.calcIndex(6, height + 3, z)] = airChar;
                    primer.data[Tools.calcIndex(7, height + 1, z)] = rail;
                    primer.data[Tools.calcIndex(7, height + 2, z)] = airChar;
                    primer.data[Tools.calcIndex(7, height + 3, z)] = airChar;
                }
                for (int z = 0; z < 3; z++) {
                    primer.data[Tools.calcIndex(5, height + 2, z)] = rail;
                    primer.data[Tools.calcIndex(5, height + 3, z)] = rail;
                    primer.data[Tools.calcIndex(5, height + 4, z)] = rail;
                    primer.data[Tools.calcIndex(6, height + 4, z)] = rail;
                    primer.data[Tools.calcIndex(7, height + 4, z)] = rail;
                    primer.data[Tools.calcIndex(8, height + 2, z)] = rail;
                    primer.data[Tools.calcIndex(8, height + 3, z)] = rail;
                    primer.data[Tools.calcIndex(8, height + 4, z)] = rail;
                }
            }

            if (info.getZmax().railDungeon != null) {
                for (int z = 0; z < 5; z++) {
                    primer.data[Tools.calcIndex(6, height + 1, 15 - z)] = rail;
                    primer.data[Tools.calcIndex(6, height + 2, 15 - z)] = airChar;
                    primer.data[Tools.calcIndex(6, height + 3, 15 - z)] = airChar;
                    primer.data[Tools.calcIndex(7, height + 1, 15 - z)] = rail;
                    primer.data[Tools.calcIndex(7, height + 2, 15 - z)] = airChar;
                    primer.data[Tools.calcIndex(7, height + 3, 15 - z)] = airChar;
                }
                for (int z = 0; z < 4; z++) {
                    primer.data[Tools.calcIndex(5, height + 2, 15 - z)] = rail;
                    primer.data[Tools.calcIndex(5, height + 3, 15 - z)] = rail;
                    primer.data[Tools.calcIndex(5, height + 4, 15 - z)] = rail;
                    primer.data[Tools.calcIndex(6, height + 4, 15 - z)] = rail;
                    primer.data[Tools.calcIndex(7, height + 4, 15 - z)] = rail;
                    primer.data[Tools.calcIndex(8, height + 2, 15 - z)] = rail;
                    primer.data[Tools.calcIndex(8, height + 3, 15 - z)] = rail;
                    primer.data[Tools.calcIndex(8, height + 4, 15 - z)] = rail;
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
                case HORIZONTAL:
                    if (railInfo.getRails() == 1) {
                        for (int x = 0 ; x < 16 ; x++) {
                            index = (x << 12) | (5 << 8) + height + 1;
                            primer.data[index] = rail;
                            index = (x << 12) | (9 << 8) + height + 1;
                            primer.data[index] = rail;
                        }
                    } else {
                        for (int x = 0 ; x < 16 ; x++) {
                            index = (x << 12) | (7 << 8) + height + 1;
                            primer.data[index] = rail;
                        }
                    }
                    break;
                case GOING_DOWN_TWO_FROM_SURFACE:
                case GOING_DOWN_ONE_FROM_SURFACE:
                case GOING_DOWN_FURTHER:
                    if (railInfo.getRails() == 1) {
                        for (int x = 0 ; x < 16 ; x++) {
                            for (int y = height + 1 ; y < height + part.getSliceCount() ; y++) {
                                index = (x << 12) | (5 << 8) + y;
                                if (getRailChars().contains(primer.data[index])) {
                                    primer.data[index] = rail;
                                }
                                index = (x << 12) | (9 << 8) + y;
                                if (getRailChars().contains(primer.data[index])) {
                                    primer.data[index] = rail;
                                }
                            }
                        }
                    } else {
                        for (int x = 0 ; x < 16 ; x++) {
                            for (int y = height + 1 ; y < height + part.getSliceCount() ; y++) {
                                index = (x << 12) | (7 << 8) + y;
                                if (getRailChars().contains(primer.data[index])) {
                                    primer.data[index] = rail;
                                }
                            }
                        }
                    }
                    break;
                case THREE_SPLIT:
                    break;
                case VERTICAL:
                    break;
                case DOUBLE_BEND:
                    break;
            }
        }

        if (needsStaircase) {
            part = AssetRegistries.PARTS.get("station_staircase");
            for (int i = railInfo.getLevel() + 1 ; i < info.cityLevel ; i++) {
                height = provider.profile.GROUNDLEVEL + i * 6;
                generatePart(primer, info, part, transform, 0, height, 0, false);
            }
            height = provider.profile.GROUNDLEVEL + info.cityLevel * 6;
            part = AssetRegistries.PARTS.get("station_staircase_surface");
            generatePart(primer, info, part, transform, 0, height, 0, false);
        }
    }

    private void generateStreetDecorations(ChunkPrimer primer, BuildingInfo info) {
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

            generatePart(primer, info, stairs, transform, 0, oy, 0, false);
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
                int index = Tools.calcIndex(x, y, z);
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
    private void fixAfterExplosionNew(ChunkPrimer primer, BuildingInfo info, Random rand) {
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

//        // Split large blobs that have very thin connections in Y direction
//        for (Blob blob : blobs) {
//            if (blob.getAvgdamage() > .3f && blob.getCntMindamage() < 10) { // @todo configurable?
//                int y = blob.needsSplitting();
//                if (y != -1) {
//                    Set<Integer> toRemove = blob.cut(y);
//                    for (Integer index : toRemove) {
//                        primer.data[index] = ((index & 0xff) < waterLevel) ? liquidChar : airChar;
//                    }
//                }
//            }
//        }

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

    private double[] rubbleBuffer = new double[256];
    private double[] leavesBuffer = new double[256];

    private void generateRubble(ChunkPrimer primer, int chunkX, int chunkZ, BuildingInfo info) {
        this.rubbleBuffer = this.rubbleNoise.getRegion(this.rubbleBuffer, (chunkX * 16), (chunkZ * 16), 16, 16, 1.0 / 16.0, 1.0 / 16.0, 1.0D);
        this.leavesBuffer = this.leavesNoise.getRegion(this.leavesBuffer, (chunkX * 64), (chunkZ * 64), 16, 16, 1.0 / 64.0, 1.0 / 64.0, 4.0D);

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                double vr = provider.profile.RUBBLE_DIRT_SCALE < 0.01f ? 0 : rubbleBuffer[x + z * 16] / provider.profile.RUBBLE_DIRT_SCALE;
                double vl = provider.profile.RUBBLE_LEAVE_SCALE < 0.01f ? 0 : leavesBuffer[x + z * 16] / provider.profile.RUBBLE_LEAVE_SCALE;
                if (vr > .5 || vl > .5) {
                    int height = getInterpolatedHeight(info, x, z);
                    int index = (x << 12) | (z << 8) + height;
                    if (primer.data[index-1] != airChar && primer.data[index-1] != liquidChar) {
                        for (int i = 0; i < vr; i++) {
                            if (primer.data[index] == airChar || primer.data[index] == liquidChar) {
                                primer.data[index] = baseChar;
                            }
                            index++;
                        }
                    }
                    if (primer.data[index-1] == baseChar) {
                        for (int i = 0 ; i < vl ; i++) {
                            if (primer.data[index] == airChar || primer.data[index] == liquidChar) {
                                primer.data[index++] = getRandomLeaf();
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

    private void generateRuins(ChunkPrimer primer, BuildingInfo info) {
        if (info.ruinHeight < 0) {
            return;
        }

        int chunkX = info.chunkX;
        int chunkZ = info.chunkZ;
        double d0 = 0.03125D;
        this.ruinBuffer = this.ruinNoise.getRegion(this.ruinBuffer, (chunkX * 16), (chunkZ * 16), 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);
        boolean doLeaves = provider.profile.RUBBLELAYER;
        if (doLeaves) {
            this.leavesBuffer = this.leavesNoise.getRegion(this.leavesBuffer, (chunkX * 64), (chunkZ * 64), 16, 16, 1.0 / 64.0, 1.0 / 64.0, 4.0D);
        }

        int baseheight = (int) (info.getCityGroundLevel() + 1 + (info.ruinHeight * info.getNumFloors() * 6.0f));

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                double v = ruinBuffer[x + z * 16];
                int height = baseheight + (int) v;
                int index = (x << 12) | (z << 8) + height;
                height = info.getMaxHeight()+10 - height;
                int vl = 0;
                if (doLeaves) {
                    vl = (int) (provider.profile.RUBBLE_LEAVE_SCALE < 0.01f ? 0 : leavesBuffer[x + z * 16] / provider.profile.RUBBLE_LEAVE_SCALE);
                }
                while (height > 0) {
                    Character damage = info.getCompiledPalette().canBeDamagedToIronBars(primer.data[index]);
                    if ((damage != null || primer.data[index-1] == ironbarsChar) && primer.data[index-1] != airChar && primer.data[index-1] != liquidChar && provider.rand.nextFloat() < .2f) {
                        primer.data[index++] = ironbarsChar;
                    } else {
                        if (vl > 0) {
                            while (primer.data[index-1] == airChar || primer.data[index-1] == liquidChar) {
                                index--;
                                height++;   // Make sure we keep on filling with air a bit longer because we are lowering here
                            }
                            primer.data[index++] = getRandomLeaf();
                            vl--;
                        } else {
                            primer.data[index++] = airChar;
                        }
                    }
                    height--;
                }
            }
        }
    }

    private void generateStreet(ChunkPrimer primer, BuildingInfo info, ChunkHeightmap heightmap, Random rand) {
        if (provider.profile.isDefault()) {
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    int index = (x << 12) | (z << 8);
                    PrimerTools.setBlockStateRange(primer, index + provider.profile.BEDROCK_LAYER, index + info.getCityGroundLevel(), baseChar);
                }
            }
        } else {
            Character borderBlock = info.getCityStyle().getBorderBlock();
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    int index = (x << 12) | (z << 8);
                    PrimerTools.setBlockStateRange(primer, index + info.getCityGroundLevel() - 2, index + info.getCityGroundLevel(), baseChar);
                    setBlocksFromPalette(primer, index + info.getCityGroundLevel() - 3, index + info.getCityGroundLevel() - 2, info.getCompiledPalette(), borderBlock);
                }
            }
        }

        boolean xRail = info.hasXCorridor();
        boolean zRail = info.hasZCorridor();
        if (xRail || zRail) {
            generateCorridors(primer, info, xRail, zRail);
        }

        Railway.RailChunkInfo railInfo = info.getRailInfo();
        boolean canDoParks = info.getHighwayXLevel() != info.cityLevel && info.getHighwayZLevel() != info.cityLevel
                && railInfo.getType() != RailChunkType.STATION_SURFACE
                && (railInfo.getType() != RailChunkType.STATION_EXTENSION_SURFACE || railInfo.getLevel() < info.cityLevel);

        if (canDoParks) {
            int height = info.getCityGroundLevel();

            BuildingInfo.StreetType streetType = info.streetType;
            boolean elevated = info.isElevatedParkSection();
            if (elevated) {
                Character elevationBlock = info.getCityStyle().getParkElevationBlock();
                char elevation = info.getCompiledPalette().get(elevationBlock);
                streetType = BuildingInfo.StreetType.PARK;
                for (int x = 0; x < 16; ++x) {
                    for (int z = 0; z < 16; ++z) {
                        primer.data[(x << 12) | (z << 8) + height] = elevation;
                    }
                }
                height++;
            }

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
                generatePart(primer, info, part, Transform.ROTATE_NONE, 0, height, 0, false);
            }

            generateRandomVegetation(primer, info, rand, height);

            generateFrontPart(primer, info, height, info.getXmin(), Transform.ROTATE_NONE);
            generateFrontPart(primer, info, height, info.getZmin(), Transform.ROTATE_90);
            generateFrontPart(primer, info, height, info.getXmax(), Transform.ROTATE_180);
            generateFrontPart(primer, info, height, info.getZmax(), Transform.ROTATE_270);
        }

        if (doBorder(info, Direction.XMIN)) {
            int x = 0;
            for (int z = 0 ; z < 16 ; z++) {
                generateBorder(primer, info, canDoParks, x, z, heightmap);
            }
        }
        if (doBorder(info, Direction.XMAX)) {
            int x = 15;
            for (int z = 0 ; z < 16 ; z++) {
                generateBorder(primer, info, canDoParks, x, z, heightmap);
            }
        }
        if (doBorder(info, Direction.ZMIN)) {
            int z = 0;
            for (int x = 0 ; x < 16 ; x++) {
                generateBorder(primer, info, canDoParks, x, z, heightmap);
            }
        }
        if (doBorder(info, Direction.ZMAX)) {
            int z = 15;
            for (int x = 0 ; x < 16 ; x++) {
                generateBorder(primer, info, canDoParks, x, z, heightmap);
            }
        }

        if (provider.profile.isFloating()) {
            Character borderBlock = info.getCityStyle().getBorderBlock();
            Character wallBlock = info.getCityStyle().getWallBlock();
            char wall = info.getCompiledPalette().get(wallBlock);
            for (int i1 = 0 ; i1 < 16 ; i1++) {
                for (int i2 = 0 ; i2 < 16 ; i2 += 15) {
                    int x = i1;
                    int z = i2;
                    generateBorderFloating(primer, info, heightmap, borderBlock, wall, x, z);
                    if (!isCorner(i1, i2)) {
                        x = i2;
                        z = i1;
                        generateBorderFloating(primer, info, heightmap, borderBlock, wall, x, z);
                    }
                }
            }
        }
    }

    private void generateBorderFloating(ChunkPrimer primer, BuildingInfo info, ChunkHeightmap heightmap, Character borderBlock, char wall, int x, int z) {
        int index = (x << 12) | (z << 8);
        setBlocksFromPalette(primer, index + info.getCityGroundLevel() - 3, index + info.getCityGroundLevel() + 1, info.getCompiledPalette(), borderBlock);
        if (isCorner(x, z)) {
            int height = heightmap.getHeight(x, z);
            if (height > 1 && height < info.getCityGroundLevel() - 3) {
                PrimerTools.setBlockStateRangeSafe(primer, index + height + 1, index + info.getCityGroundLevel() - 3, wall);
            }
        }
    }

    private void generateFrontPart(ChunkPrimer primer, BuildingInfo info, int height, BuildingInfo adj, Transform rot) {
        if (info.hasFrontPartFrom(adj)) {
            generatePart(primer, adj, adj.frontType, rot, 0, height, 0, false);
        }
    }

    private void generateCorridors(ChunkPrimer primer, BuildingInfo info, boolean xRail, boolean zRail) {
        IBlockState railx = Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE, BlockRailBase.EnumRailDirection.EAST_WEST);
        char railxC = (char) Block.BLOCK_STATE_IDS.get(railx);
        IBlockState railz = Blocks.RAIL.getDefaultState();
        char railzC = (char) Block.BLOCK_STATE_IDS.get(railz);

        Character corridorRoofBlock = info.getCityStyle().getCorridorRoofBlock();
        Character corridorGlassBlock = info.getCityStyle().getCorridorGlassBlock();
        CompiledPalette palette = info.getCompiledPalette();

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
                        char glass = palette.get(corridorGlassBlock);
                        primer.data[index + (height++)] = glass;
                        info.addGenericTodo(new BlockPos(x , height, z));
                        primer.data[index + (height++)] = glowstoneChar;
                    } else {
                        char roof = palette.get(corridorRoofBlock);
                        primer.data[index + (height++)] = roof;
                        primer.data[index + (height++)] = roof;
                    }
                } else {
                    PrimerTools.setBlockStateRange(primer, index + groundLevel - 5, index + info.getCityGroundLevel(), baseChar);
                }
            }
        }
    }

    private void generateRandomVegetation(ChunkPrimer primer, BuildingInfo info, Random rand, int height) {
        if (info.getXmin().hasBuilding) {
            for (int x = 0 ; x < provider.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    int index = (x << 12) | (z << 8) + height;
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    while (primer.data[index - 1] == airChar) {
                        index--;
                    }
                    float v = Math.min(.8f, provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (provider.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS + 1 - x));
                    int cnt = 0;
                    while (rand.nextFloat() < v && cnt < 30) {
                        primer.data[index++] = getRandomLeaf();
                        cnt++;
                    }
                }
            }
        }
        if (info.getXmax().hasBuilding) {
            for (int x = 15-provider.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS ; x < 15 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    int index = (x << 12) | (z << 8) + height;
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    while (primer.data[index - 1] == airChar) {
                        index--;
                    }
                    float v = Math.min(.8f, provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (x - 14 + provider.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS));
                    int cnt = 0;
                    while (rand.nextFloat() < v && cnt < 30) {
                        primer.data[index++] = getRandomLeaf();
                        cnt++;
                    }
                }
            }
        }
        if (info.getZmin().hasBuilding) {
            for (int z = 0 ; z < provider.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS ; z++) {
                for (int x = 0 ; x < 16 ; x++) {
                    int index = (x << 12) | (z << 8) + height;
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    while (primer.data[index - 1] == airChar) {
                        index--;
                    }
                    float v = Math.min(.8f, provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (provider.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS + 1 - z));
                    int cnt = 0;
                    while (rand.nextFloat() < v && cnt < 30) {
                        primer.data[index++] = getRandomLeaf();
                        cnt++;
                    }
                }
            }
        }
        if (info.getZmax().hasBuilding) {
            for (int z = 15-provider.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS ; z < 15 ; z++) {
                for (int x = 0 ; x < 16 ; x++) {
                    int index = (x << 12) | (z << 8) + height;
                    // @todo can be more optimal? Only go down to non air in case random succeeds?
                    while (primer.data[index - 1] == airChar) {
                        index--;
                    }
                    float v = provider.profile.CHANCE_OF_RANDOM_LEAFBLOCKS * (z - 14 + provider.profile.THICKNESS_OF_RANDOM_LEAFBLOCKS);
                    int cnt = 0;
                    while (rand.nextFloat() < v && cnt < 30) {
                        primer.data[index++] = getRandomLeaf();
                        cnt++;
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
//        char defaultStreet = provider.profile.isFloating() ? street2 : streetBase;
        char defaultStreet = streetBase;
        char b;
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                b = defaultStreet;
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

    private void generateBorder(ChunkPrimer primer, BuildingInfo info, boolean canDoParks, int x, int z, ChunkHeightmap heightmap) {
        Character borderBlock = info.getCityStyle().getBorderBlock();
        Character wallBlock = info.getCityStyle().getWallBlock();
        char wall = info.getCompiledPalette().get(wallBlock);

        int index = (x << 12) | (z << 8);

        if (provider.profile.isDefault()) {
            int y = groundLevel - 6; // We do the ocean border 6 lower then groundlevel
            setBlocksFromPalette(primer, index + y, index + info.getCityGroundLevel() + 1, info.getCompiledPalette(), borderBlock);
        }
        if (canDoParks) {
            if (!borderNeedsConnectionToAdjacentChunk(info, x, z)) {
                primer.data[index + info.getCityGroundLevel() + 1] = wall;
            } else {
                primer.data[index + info.getCityGroundLevel() + 1] = airChar;
            }
        }
    }

    private boolean borderNeedsConnectionToAdjacentChunk(BuildingInfo info, int x, int z) {
        for (Direction direction : Direction.VALUES) {
            if (direction.atSide(x, z)) {
                BuildingInfo adjacent = direction.get(info);
                if (adjacent.getActualStairDirection() == direction.getOpposite()) {
                    BuildingPart stairType = adjacent.stairType;
                    Integer z1 = stairType.getMetaInteger("z1");
                    Integer z2 = stairType.getMetaInteger("z2");
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
     * Generate a port. If 'airWaterLevel' is true then 'hard air' blocks are replaced with water below the waterLevel.
     * Otherwise they are replaced with air.
     */
    private int generatePart(ChunkPrimer primer, BuildingInfo info, BuildingPart part,
                             Transform transform,
                             int ox, int oy, int oz, boolean airWaterLevel) {
        CompiledPalette compiledPalette = info.getCompiledPalette();
        boolean combinedWithPart = false;
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
                            if (!combinedWithPart) {
                                Palette localPalette = part.getLocalPalette();
                                combinedWithPart = true;
                                if (localPalette != null) {
                                    compiledPalette = new CompiledPalette(compiledPalette, localPalette);
                                    b = compiledPalette.get(c);
                                }
                            }
                            if (b == null) {
                                throw new RuntimeException("Could not find entry '" + c + "' in the palette for part '" + part.getName() + "'!");
                            }
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
                            if (b == liquidChar) {
                                if (provider.profile.AVOID_WATER) {
                                    b = airChar;
                                }
                            } else if (b == hardAirChar) {
                                if (airWaterLevel && !provider.profile.AVOID_WATER) {
                                    b = (oy + y) < waterLevel ? liquidChar : airChar;
                                } else {
                                    b = airChar;
                                }
                            } else if (getCharactersNeedingTodo().contains(b)) {
                                if (b == torchChar) {
                                    if (provider.profile.GENERATE_LIGHTING) {
                                        info.addTorchTodo(index);
                                    } else {
                                        b = airChar;        // No torches
                                    }
                                } else if (b == spawnerChar) {
                                    if (provider.profile.GENERATE_SPAWNERS && !info.noLoot) {
                                        String mobid = part.getMobID(info, x, y, z);
                                        info.getTodoChunk(rx, rz).addSpawnerTodo(new BlockPos(info.chunkX * 16 + rx, oy + y, info.chunkZ * 16 + rz),
                                                new BuildingInfo.ConditionTodo(mobid, part.getName(), info));
                                    } else {
                                        b = airChar;
                                    }
                                } else if (b == chestChar) {
                                    if (!info.noLoot) {
                                        String lootTable = part.getLootTable(info, x, y, z);
                                        info.getTodoChunk(rx, rz).addChestTodo(new BlockPos(info.chunkX * 16 + rx, oy + y, info.chunkZ * 16 + rz),
                                                new BuildingInfo.ConditionTodo(lootTable, part.getName(), info));
                                    }
                                } else if (b == glowstoneChar) {
                                    info.getTodoChunk(rx, rz).addGenericTodo(new BlockPos(info.chunkX * 16 + rx, oy + y, info.chunkZ * 16 + rz));
                                } else {
                                    IBlockState bs = Block.BLOCK_STATE_IDS.getByValue(b);
                                    if (bs.getBlock() == Blocks.SAPLING) {
                                        if (provider.profile.AVOID_FOLIAGE) {
                                            b = airChar;
                                        } else {
                                            info.getTodoChunk(rx, rz).addSaplingTodo(new BlockPos(info.chunkX * 16 + rx, oy + y, info.chunkZ * 16 + rz));
                                        }
                                    }
                                }
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
            char filler = adjacentInfo.getCompiledPalette().get(adjacentInfo.getBuilding().getFillerBlock());
            float damageFactor = adjacentInfo.getDamageArea().getDamageFactor();
            if (damageFactor > .5f) {
                // An estimate of the amount of blocks
                int blocks = (1 + adjacentInfo.getNumFloors()) * 1000;
                float damage = Math.max(1.0f, damageFactor * DamageArea.BLOCK_DAMAGE_CHANCE);
                int destroyedBlocks = (int) (blocks * damage);
                // How many go this direction (approx, based on cardinal directions from building as well as number that simply fall down)
                destroyedBlocks /= provider.profile.DEBRIS_TO_NEARBYCHUNK_FACTOR;
                int h = adjacentInfo.getMaxHeight() + 10;
                for (int i = 0; i < destroyedBlocks; i++) {
                    int x = rand.nextInt(16);
                    int z = rand.nextInt(16);
                    if (rand.nextFloat() < locationFactor.apply(x, z)) {
                        int index = (x << 12) | (z << 8);
                        while (h > 0 && (primer.data[index+h] == airChar || primer.data[index+h] == liquidChar)) {
                            h--;
                        }
                        // Fix for FLOATING // @todo!
                        index = index+h;
                        index++;
                        Character b;
                        switch (rand.nextInt(5)) {
                            case 0:
                                b = ironbarsChar;
                                break;
                            default:
                                b = filler;     // Filler from adjacent building
                                break;
                        }
                        primer.data[index] = b;
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

    private void setBlocksFromPalette(ChunkPrimer primer, int start, int end, CompiledPalette palette, char character) {
        if (palette.isSimple(character)) {
            char b = palette.get(character);
            PrimerTools.setBlockStateRangeSafe(primer, start, end, b);
        } else {
            while (start < end) {
                primer.data[start++] = palette.get(character);
            }
        }
    }

    private void generateBuilding(ChunkPrimer primer, BuildingInfo info, ChunkHeightmap heightmap) {
        int lowestLevel = info.getCityGroundLevel() - info.floorsBelowGround * 6;

        Character borderBlock = info.getCityStyle().getBorderBlock();
        CompiledPalette palette = info.getCompiledPalette();
        char fillerBlock = info.getBuilding().getFillerBlock();

        if (provider.profile.isFloating()) {
            // For floating worldgen we try to fit the underside of the building better with the island
            // We also remove all blocks from the inside because we generate buildings on top of
            // generated chunks as opposed to blank chunks with non-floating worlds
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    int index = (x << 12) | (z << 8);
                    int height = heightmap.getHeight(x, z);
                    if (height > 1 && height < lowestLevel - 1) {
                        PrimerTools.setBlockStateRange(primer, index + height + 1, index + lowestLevel, baseChar);
                    }
                    clearRange(primer, index, lowestLevel, info.getCityGroundLevel() + info.getNumFloors() * 6);
                }
            }
        } else if (provider.profile.isSpace()) {
            // @todo
        } else {
            // For normal worldgen (non floating) we have a thin layer of 'border' blocks because that looks nicer
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    int index = (x << 12) | (z << 8);
                    if (isSide(x, z)) {
                        PrimerTools.setBlockStateRange(primer, index + provider.profile.BEDROCK_LAYER, index + lowestLevel - 10, baseChar);
                        int y = lowestLevel - 10;
                        while (y < lowestLevel) {
                            primer.data[index + y] = palette.get(borderBlock);
                            y++;
                        }
                    } else if (provider.profile.isDefault()) {
                        PrimerTools.setBlockStateRange(primer, index + provider.profile.BEDROCK_LAYER, index + lowestLevel, baseChar);
                    }
                    if (primer.data[index + lowestLevel] == airChar) {
                        char filler = palette.get(fillerBlock);
                        primer.data[index + lowestLevel] = filler;      // There is nothing below so we fill this with the filler
                    }
                }
            }
        }

        int height = lowestLevel;
        for (int f = -info.floorsBelowGround; f <= info.getNumFloors(); f++) {
            BuildingPart part = info.getFloor(f);
            generatePart(primer, info, part, Transform.ROTATE_NONE, 0, height, 0, false);
            part = info.getFloorPart2(f);
            if (part != null) {
                generatePart(primer, info, part, Transform.ROTATE_NONE, 0, height, 0, false);
            }

            // Check for doors
            boolean isTop = f == info.getNumFloors();   // The top does not need generated doors
            if (!isTop) {
                generateDoors(primer, info, height+1, f);
            }

            height += 6;    // We currently only support 6 here
        }

        if (info.floorsBelowGround > 0) {
            // Underground we replace the glass with the filler
            for (int x = 0 ; x < 16 ; x++) {
                int index = (x << 12) | (0 << 8);
                // Use safe version because this may end up being lower
                setBlocksFromPalette(primer, index + lowestLevel, index + Math.min(info.getCityGroundLevel(), info.getZmin().getCityGroundLevel())+1, palette, fillerBlock);
                index = (x << 12) | (15 << 8);
                setBlocksFromPalette(primer, index + lowestLevel, index + Math.min(info.getCityGroundLevel(), info.getZmax().getCityGroundLevel())+1, palette, fillerBlock);
            }
            for (int z = 1 ; z < 15 ; z++) {
                int index = (0 << 12) | (z << 8);
                setBlocksFromPalette(primer, index + lowestLevel, index + Math.min(info.getCityGroundLevel(), info.getXmin().getCityGroundLevel())+1, palette, fillerBlock);
                index = (15 << 12) | (z << 8);
                setBlocksFromPalette(primer, index + lowestLevel, index + Math.min(info.getCityGroundLevel(), info.getXmax().getCityGroundLevel())+1, palette, fillerBlock);
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

        char filler = info.getCompiledPalette().get(info.getBuilding().getFillerBlock());

        height--;       // Start generating doors one below for the filler

        if (info.hasConnectionAtX(f + info.floorsBelowGround)) {
            int x = 0;
            if (hasConnectionWithBuilding(f, info, info.getXmin())) {
                int index = (x << 12) | (6 << 8);
                PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
                index = (x << 12) | (9 << 8);
                PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
                index = (x << 12) | (7 << 8);
                primer.data[index + height] = filler;
                primer.data[index + height + 1] = airChar;
                primer.data[index + height + 2] = airChar;
                primer.data[index + height + 3] = filler;
                index = (x << 12) | (8 << 8);
                primer.data[index + height] = filler;
                primer.data[index + height + 1] = airChar;
                primer.data[index + height + 2] = airChar;
                primer.data[index + height + 3] = filler;
            } else if (hasConnectionToTopOrOutside(f, info, info.getXmin())) {
                int index = (x << 12) | (6 << 8);
                PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
                index = (x << 12) | (9 << 8);
                PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
                index = (x << 12) | (7 << 8);
                primer.data[index + height] = filler;
                primer.data[index + height + 1] = getDoor(info.doorBlock, false, true, EnumFacing.EAST);
                primer.data[index + height + 2] = getDoor(info.doorBlock, true, true, EnumFacing.EAST);
                primer.data[index + height + 3] = filler;
                index = (x << 12) | (8 << 8);
                primer.data[index + height] = filler;
                primer.data[index + height + 1] = getDoor(info.doorBlock, false, false, EnumFacing.EAST);
                primer.data[index + height + 2] = getDoor(info.doorBlock, true, false, EnumFacing.EAST);
                primer.data[index + height + 3] = filler;
            }
        }
        if (hasConnectionWithBuildingMax(f, info, info.getXmax(), Orientation.X)) {
            int x = 15;
            int index = (x << 12) | (6 << 8);
            PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
            index = (x << 12) | (9 << 8);
            PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
            index = (x << 12) | (7 << 8);
            primer.data[index + height] = filler;
            primer.data[index + height + 1] = airChar;
            primer.data[index + height + 2] = airChar;
            primer.data[index + height + 3] = filler;
            index = (x << 12) | (8 << 8);
            primer.data[index + height] = filler;
            primer.data[index + height + 1] = airChar;
            primer.data[index + height + 2] = airChar;
            primer.data[index + height + 3] = filler;
        } else if (hasConnectionToTopOrOutside(f, info, info.getXmax()) && (info.getXmax().hasConnectionAtXFromStreet(f + info.getXmax().floorsBelowGround))) {
            int x = 15;
            int index = (x << 12) | (6 << 8);
            PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
            index = (x << 12) | (9 << 8);
            PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
            index = (x << 12) | (7 << 8);
            primer.data[index + height] = filler;
            primer.data[index + height + 1] = getDoor(info.doorBlock, false, false, EnumFacing.WEST);
            primer.data[index + height + 2] = getDoor(info.doorBlock, true, false, EnumFacing.WEST);
            primer.data[index + height + 3] = filler;
            index = (x << 12) | (8 << 8);
            primer.data[index + height] = filler;
            primer.data[index + height + 1] = getDoor(info.doorBlock, false, true, EnumFacing.WEST);
            primer.data[index + height + 2] = getDoor(info.doorBlock, true, true, EnumFacing.WEST);
            primer.data[index + height + 3] = filler;
        }
        if (info.hasConnectionAtZ(f + info.floorsBelowGround)) {
            int z = 0;
            if (hasConnectionWithBuilding(f, info, info.getZmin())) {
                int index = (6 << 12) | (z << 8);
                PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
                index = (9 << 12) | (z << 8);
                PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
                index = (7 << 12) | (z << 8);
                primer.data[index + height] = filler;
                primer.data[index + height + 1] = airChar;
                primer.data[index + height + 2] = airChar;
                primer.data[index + height + 3] = filler;
                index = (8 << 12) | (z << 8);
                primer.data[index + height] = filler;
                primer.data[index + height + 1] = airChar;
                primer.data[index + height + 2] = airChar;
                primer.data[index + height + 3] = filler;
            } else if (hasConnectionToTopOrOutside(f, info, info.getZmin())) {
                int index = (6 << 12) | (z << 8);
                PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
                index = (9 << 12) | (z << 8);
                PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
                index = (7 << 12) | (z << 8);
                primer.data[index + height] = filler;
                primer.data[index + height + 1] = getDoor(info.doorBlock, false, true, EnumFacing.NORTH);
                primer.data[index + height + 2] = getDoor(info.doorBlock, true, true, EnumFacing.NORTH);
                primer.data[index + height + 3] = filler;
                index = (8 << 12) | (z << 8);
                primer.data[index + height] = filler;
                primer.data[index + height + 1] = getDoor(info.doorBlock, false, false, EnumFacing.NORTH);
                primer.data[index + height + 2] = getDoor(info.doorBlock, true, false, EnumFacing.NORTH);
                primer.data[index + height + 3] = filler;
            }
        }
        if (hasConnectionWithBuildingMax(f, info, info.getZmax(), Orientation.Z)) {
            int z = 15;
            int index = (6 << 12) | (z << 8);
            PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
            index = (9 << 12) | (z << 8);
            PrimerTools.setBlockStateRange(primer, index + height, index + height + 4, filler);
            index = (7 << 12) | (z << 8);
            primer.data[index + height] = filler;
            primer.data[index + height + 1] = airChar;
            primer.data[index + height + 2] = airChar;
            primer.data[index + height + 3] = filler;
            index = (8 << 12) | (z << 8);
            primer.data[index + height] = filler;
            primer.data[index + height + 1] = airChar;
            primer.data[index + height + 2] = airChar;
            primer.data[index + height + 3] = filler;
        } else if (hasConnectionToTopOrOutside(f, info, info.getZmax()) && (info.getZmax().hasConnectionAtZFromStreet(f + info.getZmax().floorsBelowGround))) {
            int z = 15;
            int index = (6 << 12) | (z << 8);
            PrimerTools.setBlockStateRange(primer, index + height, index + height + 3, filler);
            index = (9 << 12) | (z << 8);
            PrimerTools.setBlockStateRange(primer, index + height, index + height + 3, filler);
            index = (7 << 12) | (z << 8);
            primer.data[index + height] = filler;
            primer.data[index + height + 1] = getDoor(info.doorBlock, false, false, EnumFacing.SOUTH);
            primer.data[index + height + 2] = getDoor(info.doorBlock, true, false, EnumFacing.SOUTH);
            primer.data[index + height + 3] = filler;
            index = (8 << 12) | (z << 8);
            primer.data[index + height] = filler;
            primer.data[index + height + 1] = getDoor(info.doorBlock, false, true, EnumFacing.SOUTH);
            primer.data[index + height + 2] = getDoor(info.doorBlock, true, true, EnumFacing.SOUTH);
            primer.data[index + height + 3] = filler;
        }
    }

    private void generateCorridorConnections(ChunkPrimer primer, BuildingInfo info) {
        if (info.getXmin().hasXCorridor()) {
            int x = 0;
            for (int z = 7 ; z <= 10 ; z++) {
                int index = (x << 12) | (z << 8);
                PrimerTools.setBlockStateRange(primer, index + groundLevel-5, index + groundLevel-2, airChar);
            }
        }
        if (info.getXmax().hasXCorridor()) {
            int x = 15;
            for (int z = 7 ; z <= 10 ; z++) {
                int index = (x << 12) | (z << 8);
                PrimerTools.setBlockStateRange(primer, index + groundLevel-5, index + groundLevel-2, airChar);
            }
        }
        if (info.getZmin().hasXCorridor()) {
            int z = 0;
            for (int x = 7 ; x <= 10 ; x++) {
                int index = (x << 12) | (z << 8);
                PrimerTools.setBlockStateRange(primer, index + groundLevel-5, index + groundLevel-2, airChar);
            }
        }
        if (info.getZmax().hasXCorridor()) {
            int z = 15;
            for (int x = 7 ; x <= 10 ; x++) {
                int index = (x << 12) | (z << 8);
                PrimerTools.setBlockStateRange(primer, index + groundLevel-5, index + groundLevel-2, airChar);
            }
        }
    }

    private boolean hasConnectionWithBuildingMax(int localLevel, BuildingInfo info, BuildingInfo info2, Orientation x) {
        if (info.isValidFloor(localLevel) && info.getFloor(localLevel).getMetaBoolean("dontconnect")) {
            return false;
        }
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        if (info2.isValidFloor(localAdjacent) && info2.getFloor(localAdjacent).getMetaBoolean("dontconnect")) {
            return false;
        }
        int level = localAdjacent + info2.floorsBelowGround;
        return info2.hasBuilding && ((localAdjacent >= 0 && localAdjacent < info2.getNumFloors()) || (localAdjacent < 0 && (-localAdjacent) <= info2.floorsBelowGround)) && info2.hasConnectionAt(level, x);
    }

    private boolean hasConnectionToTopOrOutside(int localLevel, BuildingInfo info, BuildingInfo info2) {
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        return (info2.isCity && !info2.hasBuilding && localLevel == 0 && localAdjacent == 0) || (info2.hasBuilding && localAdjacent == info2.getNumFloors());
//        return (!info2.hasBuilding && localLevel == localAdjacent) || (info2.hasBuilding && localAdjacent == info2.getNumFloors());
    }

    private boolean hasConnectionWithBuilding(int localLevel, BuildingInfo info, BuildingInfo info2) {
        int globalLevel = info.localToGlobal(localLevel);
        int localAdjacent = info2.globalToLocal(globalLevel);
        return info2.hasBuilding && ((localAdjacent >= 0 && localAdjacent < info2.getNumFloors()) || (localAdjacent < 0 && (-localAdjacent) <= info2.floorsBelowGround));
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
}
