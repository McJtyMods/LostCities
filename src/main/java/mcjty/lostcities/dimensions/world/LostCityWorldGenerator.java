package mcjty.lostcities.dimensions.world;

import mcjty.lib.tools.EntityTools;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.dimensions.world.terrain.lost.BuildingInfo;
import mcjty.lostcities.dimensions.world.terrain.lost.GenInfo;
import mcjty.lostcities.dimensions.world.terrain.lost.LostCitiesTerrainGenerator;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockVine;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Random;

public class LostCityWorldGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
//        if (information.getTerrainType() == TerrainType.TERRAIN_LOSTCITIES) {
            generateLootSpawners(random, chunkX, chunkZ, world);
            generateVines(random, chunkX, chunkZ, world);
//        }
    }

    private void generateVines(Random random, int chunkX, int chunkZ, World world) {
        int cx = chunkX * 16;
        int cz = chunkZ * 16;
        BuildingInfo info = new BuildingInfo(chunkX, chunkZ, world.getSeed());

        int bottom = Math.max(LostCityConfiguration.GROUNDLEVEL + 3, info.hasBuilding ? (LostCityConfiguration.GROUNDLEVEL + 6 + info.floors * 6) : (LostCityConfiguration.GROUNDLEVEL + 3));

        if (info.getXmin().hasBuilding) {
            if (info.getXmin().getDamageArea().getDamageFactor() < .4f) {
                for (int z = 0; z < 15; z++) {
                    for (int y = bottom; y < (LostCityConfiguration.GROUNDLEVEL + info.getXmin().floors * 6); y++) {
                        if (random.nextFloat() < LostCityConfiguration.VINE_CHANCE) {
                            createVineStrip(random, world, bottom, y, BlockVine.WEST, cx + 0, cz + z);
                        }
                    }
                }
            }
        }
        if (info.getXmax().hasBuilding) {
            if (info.getXmax().getDamageArea().getDamageFactor() < .4f) {
                for (int z = 0; z < 15; z++) {
                    for (int y = bottom; y < (LostCityConfiguration.GROUNDLEVEL + info.getXmax().floors * 6); y++) {
                        if (random.nextFloat() < LostCityConfiguration.VINE_CHANCE) {
                            createVineStrip(random, world, bottom, y, BlockVine.EAST, cx + 15, cz + z);
                        }
                    }
                }
            }
        }
        if (info.getZmin().hasBuilding) {
            if (info.getZmin().getDamageArea().getDamageFactor() < .4f) {
                for (int x = 0; x < 15; x++) {
                    for (int y = bottom; y < (LostCityConfiguration.GROUNDLEVEL + info.getZmin().floors * 6); y++) {
                        if (random.nextFloat() < LostCityConfiguration.VINE_CHANCE) {
                            createVineStrip(random, world, bottom, y, BlockVine.NORTH, cx + x, cz + 0);
                        }
                    }
                }
            }
        }
        if (info.getZmax().hasBuilding) {
            if (info.getZmax().getDamageArea().getDamageFactor() < .4f) {
                for (int x = 0; x < 15; x++) {
                    for (int y = bottom; y < (LostCityConfiguration.GROUNDLEVEL + info.getZmax().floors * 6); y++) {
                        if (random.nextFloat() < LostCityConfiguration.VINE_CHANCE) {
                            createVineStrip(random, world, bottom, y, BlockVine.SOUTH, cx + x, cz + 15);
                        }
                    }
                }
            }
        }
    }

    private void createVineStrip(Random random, World world, int bottom, int y, PropertyBool direction, int vinex, int vinez) {
        world.setBlockState(new BlockPos(vinex, y, vinez), Blocks.VINE.getDefaultState().withProperty(direction, true));
        int yy = y-1;
        while (yy >= bottom && random.nextFloat() < .8f) {
            world.setBlockState(new BlockPos(vinex, yy, vinez), Blocks.VINE.getDefaultState().withProperty(direction, true));
            yy--;
        }
    }

    private void generateLootSpawners(Random random, int chunkX, int chunkZ, World world) {
        BuildingInfo info = new BuildingInfo(chunkX, chunkZ, world.getSeed());

        int buildingtop = 0;
        boolean building = info.hasBuilding;
        if (building) {
            buildingtop = LostCityConfiguration.GROUNDLEVEL + 6 + info.floors * 6;
        }

        int height = LostCityConfiguration.GROUNDLEVEL - info.floorsBelowGround * 6;


        while (height < buildingtop) {
            int f = LostCitiesTerrainGenerator.getFloor(height);
            if (f == 0) {
                BlockPos floorpos = new BlockPos(chunkX * 16, height, chunkZ * 16);
                int floortype = info.floorTypes[LostCitiesTerrainGenerator.getLevel(height) + info.floorsBelowGround];
                GenInfo getInfo = LostCitiesTerrainGenerator.getGenInfos().get(Pair.of(info.getGenInfoIndex(), floortype));
                for (BlockPos p : getInfo.getChest()) {
                    BlockPos pos = floorpos.add(p);
                    if (!world.isAirBlock(pos)) {
                        createLootChest(random, world, pos);
                    }
                }
                for (BlockPos p : getInfo.getRandomFeatures()) {
                    BlockPos pos = floorpos.add(p);
                    if (!world.isAirBlock(pos)) {
                        createRandomFeature(random, world, pos);
                    }
                }
//                for (BlockPos p : getInfo.getModularStorages()) {
//                    BlockPos pos = floorpos.add(p);
//                    if (!world.isAirBlock(pos)) {
//                        createModularStorage(random, world, pos);
//                    }
//                }
//                for (BlockPos p : getInfo.getRandomRFToolsMachines()) {
//                    BlockPos pos = floorpos.add(p);
//                    if (!world.isAirBlock(pos)) {
//                        createRFToolsMachine(random, world, pos);
//                    }
//                }
                for (Map.Entry<BlockPos, Integer> entry : getInfo.getSpawnerType().entrySet()) {
                    BlockPos pos = floorpos.add(entry.getKey());
                    if (!world.isAirBlock(pos)) {
                        world.setBlockState(pos, Blocks.MOB_SPAWNER.getDefaultState());
                        TileEntity tileentity = world.getTileEntity(pos);
                        if (tileentity instanceof TileEntityMobSpawner) {
                            TileEntityMobSpawner spawner = (TileEntityMobSpawner) tileentity;
                            switch (entry.getValue()) {
                                case 1:
                                    EntityTools.setSpawnerEntity(world, spawner, new ResourceLocation("minecraft:zombie"), "Zombie");
                                    break;
                                case 2:
                                    EntityTools.setSpawnerEntity(world, spawner, new ResourceLocation("minecraft:skeleton"), "Skeleton");
                                    break;
                                case 3:
                                    EntityTools.setSpawnerEntity(world, spawner, new ResourceLocation("minecraft:spider"), "Spider");
                                    break;
                                case 4:
                                    EntityTools.setSpawnerEntity(world, spawner, new ResourceLocation("minecraft:blaze"), "Blaze");
                                    break;
                            }
                        }
                    }
                }
            }
            height++;
        }

    }

    private void createRandomFeature(Random random, World world, BlockPos pos) {
        switch (random.nextInt(60)) {
            case 0:
            case 1:
                world.setBlockState(pos, Blocks.BREWING_STAND.getDefaultState());
                break;
            case 2:
            case 3:
                world.setBlockState(pos, Blocks.ANVIL.getDefaultState());
                break;
            case 4:
            case 5:
                world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
                break;
            case 6:
                world.setBlockState(pos, Blocks.ENCHANTING_TABLE.getDefaultState());
                break;
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
                world.setBlockState(pos, Blocks.CRAFTING_TABLE.getDefaultState());
                break;
            case 20:
//                createModularStorage(random, world, pos);
//                break;
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
                world.setBlockState(pos, Blocks.WEB.getDefaultState());
                break;
            default:
                world.setBlockState(pos, Blocks.FURNACE.getDefaultState());
                break;
        }
    }

    private void createLootChest(Random random, World world, BlockPos pos) {
        world.setBlockState(pos, Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.SOUTH));
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof TileEntityChest) {
            switch (random.nextInt(30)) {
                case 0:
                    ((TileEntityChest) tileentity).setLootTable(LootTableList.CHESTS_DESERT_PYRAMID, random.nextLong());
                    break;
                case 1:
                    ((TileEntityChest) tileentity).setLootTable(LootTableList.CHESTS_JUNGLE_TEMPLE, random.nextLong());
                    break;
                case 2:
                    ((TileEntityChest) tileentity).setLootTable(LootTableList.CHESTS_VILLAGE_BLACKSMITH, random.nextLong());
                    break;
                case 3:
                    ((TileEntityChest) tileentity).setLootTable(LootTableList.CHESTS_ABANDONED_MINESHAFT, random.nextLong());
                    break;
                case 4:
                    ((TileEntityChest) tileentity).setLootTable(LootTableList.CHESTS_NETHER_BRIDGE, random.nextLong());
                    break;
                default:
                    ((TileEntityChest) tileentity).setLootTable(LootTableList.CHESTS_SIMPLE_DUNGEON, random.nextLong());
                    break;
            }
        }
    }

    private void setStainedGlassIfAir(World world, int x, int y, int z, int i) {
        if (world.isAirBlock(new BlockPos(x, y, z))) {
            world.setBlockState(new BlockPos(x, y, z), Blocks.STAINED_GLASS.getStateFromMeta(i), 2);
        }
    }
}
