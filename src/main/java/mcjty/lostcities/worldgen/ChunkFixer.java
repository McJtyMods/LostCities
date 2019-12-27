package mcjty.lostcities.worldgen;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.worldgen.lost.BuildingInfo;
import mcjty.lostcities.worldgen.lost.CitySphere;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.Condition;
import mcjty.lostcities.worldgen.lost.cityassets.ConditionContext;
import net.minecraft.block.*;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Random;

public class ChunkFixer {


    private static void generateTrees(Random random, int chunkX, int chunkZ, IWorld world, IDimensionInfo provider) {
        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);
        for (BlockPos pos : info.getSaplingTodo()) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof SaplingBlock) {
                ((SaplingBlock) state.getBlock()).grow(world, pos, state, random);
            }
        }
        info.clearSaplingTodo();
    }

    private static void generateVines(Random random, int chunkX, int chunkZ, IWorld world, IDimensionInfo provider) {
        float vineChance = provider.getProfile().VINE_CHANCE;
        if (vineChance < 0.000001) {
            return;
        }
        int cx = chunkX * 16;
        int cz = chunkZ * 16;
        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, provider);

        int maxHeight = info.getMaxHeight();

        if (info.hasBuilding) {
            BuildingInfo adjacent = info.getXmax();
            int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
            AfterGenTodo.addTodo(provider.getType(), w -> {
                for (int z = 0; z < 15; z++) {
                    for (int y = bottom; y < maxHeight; y++) {
                        if (w.getRandom().nextFloat() < vineChance) {
                            createVineStrip(w, bottom, VineBlock.WEST, new BlockPos(cx + 16, y, cz + z), new BlockPos(cx + 15, y, cz + z));
                        }
                    }
                }
            });
        }
        if (info.getXmax().hasBuilding) {
            BuildingInfo adjacent = info.getXmax();
            int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? maxHeight : (info.getCityGroundLevel() + 3));
            AfterGenTodo.addTodo(provider.getType(), w -> {
                for (int z = 0; z < 15; z++) {
                    for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
                        if (w.getRandom().nextFloat() < vineChance) {
                            createVineStrip(w, bottom, VineBlock.EAST, new BlockPos(cx + 15, y, cz + z), new BlockPos(cx + 16, y, cz + z));
                        }
                    }
                }
            });
        }

        if (info.hasBuilding) {
            BuildingInfo adjacent = info.getZmax();
            int bottom = Math.max(adjacent.getCityGroundLevel() + 3, adjacent.hasBuilding ? adjacent.getMaxHeight() : (adjacent.getCityGroundLevel() + 3));
            AfterGenTodo.addTodo(provider.getType(), w -> {
                for (int x = 0; x < 15; x++) {
                    for (int y = bottom; y < maxHeight; y++) {
                        if (w.getRandom().nextFloat() < vineChance) {
                            createVineStrip(w, bottom, VineBlock.NORTH, new BlockPos(cx + x, y, cz + 16), new BlockPos(cx + x, y, cz + 15));
                        }
                    }
                }
            });
        }
        if (info.getZmax().hasBuilding) {
            BuildingInfo adjacent = info.getZmax();
            int bottom = Math.max(info.getCityGroundLevel() + 3, info.hasBuilding ? maxHeight : (info.getCityGroundLevel() + 3));
            AfterGenTodo.addTodo(provider.getType(), w -> {
                for (int x = 0; x < 15; x++) {
                    for (int y = bottom; y < (adjacent.getMaxHeight()); y++) {
                        if (w.getRandom().nextFloat() < vineChance) {
                            createVineStrip(w, bottom, VineBlock.SOUTH, new BlockPos(cx + x, y, cz + 15), new BlockPos(cx + x, y, cz + 16));
                        }
                    }
                }
            });
        }
    }

    private static void createVineStrip(IWorld world, int bottom, BooleanProperty direction, BlockPos pos, BlockPos vineHolderPos) {
        if (world.isAirBlock(vineHolderPos)) {
            return;
        }
        if (!world.isAirBlock(pos)) {
            return;
        }
        BlockState state = Blocks.VINE.getDefaultState().with(direction, true);
        world.setBlockState(pos, state, 0);
        pos = pos.down();
        while (pos.getY() >= bottom && world.getRandom().nextFloat() < .8f) {
            if (!world.isAirBlock(pos)) {
                return;
            }
            world.setBlockState(pos, state, 0);
            pos = pos.down();
        }
    }


    private static void generateLootSpawners(Random random, int chunkX, int chunkZ, IWorld world, IDimensionInfo diminfo) {
        BuildingInfo info = BuildingInfo.getBuildingInfo(chunkX, chunkZ, diminfo);

        for (Pair<BlockPos, BuildingInfo.ConditionTodo> pair : info.getMobSpawnerTodo()) {
            BlockPos pos = pair.getKey();
            BlockState state = world.getBlockState(pos);
            world.setBlockState(pos, state, 3); // Recreate the block on the world so that TE can be made
            // Double check that it is still a spawner (could be destroyed by explosion)
            if (state.getBlock() == Blocks.SPAWNER) {
                TileEntity tileentity = world.getTileEntity(pos);
                if (tileentity instanceof MobSpawnerTileEntity) {
                    MobSpawnerTileEntity spawner = (MobSpawnerTileEntity) tileentity;
                    BuildingInfo.ConditionTodo todo = pair.getValue();
                    String condition = todo.getCondition();
                    Condition cnd = AssetRegistries.CONDITIONS.get(condition);
                    if (cnd == null) {
                        throw new RuntimeException("Cannot find condition '" + condition + "'!");
                    }
                    int level = (pos.getY() - diminfo.getProfile().GROUNDLEVEL) / 6;
                    int floor = (pos.getY() - info.getCityGroundLevel()) / 6;
                    ConditionContext conditionContext = new ConditionContext(level, floor, info.floorsBelowGround, info.getNumFloors(),
                            todo.getPart(), todo.getBuilding(), info.chunkX, info.chunkZ) {
                        @Override
                        public boolean isSphere() {
                            return CitySphere.isInSphere(chunkX, chunkZ, pos, diminfo);
                        }

                        @Override
                        public ResourceLocation getBiome() {
                            return world.getBiome(pos).getRegistryName();
                        }
                    };
                    String randomValue = cnd.getRandomValue(random, conditionContext);
                    if (randomValue == null) {
                        throw new RuntimeException("Condition '" + cnd.getName() + "' did not return a valid mob!");
                    }
                    AbstractSpawner logic = spawner.getSpawnerBaseLogic();
                    logic.setEntityType(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(randomValue)));
                    spawner.markDirty();
                    if (LostCityConfiguration.DEBUG) {
                        LostCities.setup.getLogger().debug("generateLootSpawners: mob=" + randomValue + " pos=" + pos.toString());
                    }
                } else if (tileentity != null) {
                    LostCities.setup.getLogger().error("The mob spawner at (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") has a TileEntity of incorrect type " + tileentity.getClass().getName() + "!");
                } else {
                    LostCities.setup.getLogger().error("The mob spawner at (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") is missing its TileEntity!");
                }
            }
        }
        info.clearMobSpawnerTodo();


        for (Pair<BlockPos, BuildingInfo.ConditionTodo> pair : info.getLootTodo()) {
            BlockPos pos = pair.getKey();
            BlockState state = world.getBlockState(pos);
            world.setBlockState(pos, state, 3); // Recreate the block on the world so that TE can be made

            // Double check that it is still something that can hold loot (could be destroyed by explosion)
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof LockableLootTileEntity) {
                if (diminfo.getProfile().GENERATE_LOOT) {
                    createLoot(info, random, world, pos, pair.getRight(), diminfo);
                }
            } else if (te == null) {
                Block block = state.getBlock();
                if (block.hasTileEntity(state)) {
                    LostCities.setup.getLogger().error("The block " + block.getRegistryName() + " (" + block.getClass().getName() + ") at (" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ") is missing its TileEntity!");
                }
            }
        }
        info.clearLootTodo();

        // @todo 1.14 is this still needed?

//        for (BlockPos pos : info.getLightingUpdateTodo()) {
//            BlockState state = world.getBlockState(pos);
//            world.setBlockState(pos, state, 3);
//        }
//        info.clearLightingUpdateTodo();
    }


    private static void createLoot(BuildingInfo info, Random random, IWorld world, BlockPos pos, BuildingInfo.ConditionTodo todo, IDimensionInfo diminfo) {
        if (random.nextFloat() < diminfo.getProfile().CHEST_WITHOUT_LOOT_CHANCE) {
            return;
        }
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof LockableLootTileEntity) {
            if (todo != null) {
                String lootTable = todo.getCondition();
                int level = (pos.getY() - diminfo.getProfile().GROUNDLEVEL) / 6;
                int floor = (pos.getY() - info.getCityGroundLevel()) / 6;
                ConditionContext conditionContext = new ConditionContext(level, floor, info.floorsBelowGround, info.getNumFloors(),
                        todo.getPart(), todo.getBuilding(), info.chunkX, info.chunkZ) {
                    @Override
                    public boolean isSphere() {
                        return CitySphere.isInSphere(info.chunkX, info.chunkZ, pos, diminfo);
                    }

                    @Override
                    public ResourceLocation getBiome() {
                        return world.getBiome(pos).getRegistryName();
                    }
                };
                String randomValue = AssetRegistries.CONDITIONS.get(lootTable).getRandomValue(random, conditionContext);
                if (randomValue == null) {
                    throw new RuntimeException("Condition '" + lootTable + "' did not return a table under certain conditions!");
                }
//                ((LockableLootTileEntity) tileentity).setLootTable(new ResourceLocation(randomValue), random.nextLong());
//                tileentity.markDirty();
//                if (LostCityConfiguration.DEBUG) {
//                    LostCities.setup.getLogger().debug("createLootChest: loot=" + randomValue + " pos=" + pos.toString());
//                }
                LockableLootTileEntity.setLootTable(world, random, pos, new ResourceLocation(randomValue));
            }
        }
    }


    public static void fix(IDimensionInfo info, int chunkX, int chunkZ) {
        generateTrees(info.getRandom(), chunkX, chunkZ, info.getWorld(), info);
        generateVines(info.getRandom(), chunkX, chunkZ, info.getWorld(), info);
        generateLootSpawners(info.getRandom(), chunkX, chunkZ, info.getWorld(), info);
    }
}
