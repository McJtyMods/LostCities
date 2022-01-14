package mcjty.lostcities.setup;

import mcjty.lostcities.commands.ModCommands;
import mcjty.lostcities.config.LostCityConfiguration;
import mcjty.lostcities.varia.CustomTeleporter;
import mcjty.lostcities.varia.WorldTools;
import mcjty.lostcities.worldgen.LostCityFeature;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBiomeLoad(BiomeLoadingEvent event) {
        RegistryKey<Biome> biomeKey = RegistryKey.create(Registry.BIOME_REGISTRY, event.getName());
        if (!BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.VOID)) {
            event.getGeneration().getFeatures(GenerationStage.Decoration.RAW_GENERATION).add(() -> LostCityFeature.LOSTCITY_CONFIGURED_FEATURE);
        }
    }

//    @SubscribeEvent
//    public void onCreateSpawnPoint(WorldEvent.CreateSpawnPosition event) {
//        IWorld world = event.getWorld();
//        if (!world.isRemote()) {
//            if (!WorldTypeTools.isLostCities(world)) {
//                return;
//            }
//
//            LostCityProfile profile = WorldTypeTools.getProfile(world);
//            if (profile == null) {
//                return;
//            }
//
//            LostCityChunkGenerator provider = WorldTypeTools.getLostCityChunkGenerator(world.getWorld());
//
//            Predicate<BlockPos> isSuitable = pos -> true;
//            boolean needsCheck = false;
//
//            if (!profile.SPAWN_BIOME.isEmpty()) {
//                final Biome spawnBiome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(profile.SPAWN_BIOME));
//                if (spawnBiome == null) {
//                    LostCities.setup.getLogger().error("Cannot find biome '" + profile.SPAWN_BIOME + "' for the player to spawn in !");
//                } else {
//                    isSuitable = blockPos -> world.getBiome(blockPos) == spawnBiome;
//                    needsCheck = true;
//                }
//            } else if (!profile.SPAWN_CITY.isEmpty()) {
//                final PredefinedCity city = AssetRegistries.PREDEFINED_CITIES.get(profile.SPAWN_CITY);
//                if (city == null) {
//                    LostCities.setup.getLogger().error("Cannot find city '" + profile.SPAWN_CITY + "' for the player to spawn in !");
//                } else {
//                    float sqradius = getSqRadius(city.getRadius(), 0.8f);
//                    isSuitable = blockPos -> city.getDimension() == world.getWorld().getDimension().getType() &&
//                            CitySphere.squaredDistance(city.getChunkX()*16+8, city.getChunkZ()*16+8, blockPos.getX(), blockPos.getZ()) < sqradius;
//                    needsCheck = true;
//                }
//            } else if (!profile.SPAWN_SPHERE.isEmpty()) {
//                if ("<in>".equals(profile.SPAWN_SPHERE)) {
//                    isSuitable = blockPos -> {
//                        CitySphere sphere = CitySphere.getCitySphere(blockPos.getX() >> 4, blockPos.getZ() >> 4, provider);
//                        if (!sphere.isEnabled()) {
//                            return false;
//                        }
//                        float sqradius = getSqRadius((int) sphere.getRadius(), 0.8f);
//                        return sphere.getCenterPos().distanceSq(blockPos) < sqradius;
//                    };
//                    needsCheck = true;
//                } else if ("<out>".equals(profile.SPAWN_SPHERE)) {
//                    isSuitable = blockPos -> {
//                        CitySphere sphere = CitySphere.getCitySphere(blockPos.getX() >> 4, blockPos.getZ() >> 4, provider);
//                        if (!sphere.isEnabled()) {
//                            return true;
//                        }
//                        float sqradius = sphere.getRadius() * sphere.getRadius();
//                        return sphere.getCenterPos().distanceSq(new BlockPos(blockPos.getX(), sphere.getCenterPos().getY(), blockPos.getZ())) > sqradius;
//                    };
//                    needsCheck = true;
//                } else {
//                    final PredefinedSphere sphere = AssetRegistries.PREDEFINED_SPHERES.get(profile.SPAWN_SPHERE);
//                    if (sphere == null) {
//                        LostCities.setup.getLogger().error("Cannot find sphere '" + profile.SPAWN_SPHERE + "' for the player to spawn in !");
//                    } else {
//                        float sqradius = getSqRadius(sphere.getRadius(), 0.8f);
//                        isSuitable = blockPos -> sphere.getDimension() == world.getDimension().getType() &&
//                                CitySphere.squaredDistance(sphere.getChunkX() * 16 + 8, sphere.getChunkZ() * 16 + 8, blockPos.getX(), blockPos.getZ()) < sqradius;
//                        needsCheck = true;
//                    }
//                }
//            }
//
//            if (profile.SPAWN_NOT_IN_BUILDING) {
//                isSuitable = isSuitable.and(blockPos -> isOutsideBuilding(provider, blockPos));
//                needsCheck = true;
//            }
//
//            // Potentially set the spawn point
//            switch (profile.LANDSCAPE_TYPE) {
//                case DEFAULT:
//                case CAVERN:
//                    if (needsCheck) {
//                        findSafeSpawnPoint(world.getWorld(), provider, isSuitable);
//                        event.setCanceled(true);
//                    }
//                    break;
//                case FLOATING:
//                case SPACE:
//                    findSafeSpawnPoint(world.getWorld(), provider, isSuitable);
//                    event.setCanceled(true);
//                    break;
//            }
//        }
//    }
//
//    private boolean isOutsideBuilding(LostCityChunkGenerator provider, BlockPos pos) {
//        BuildingInfo info = BuildingInfo.getBuildingInfo(pos.getX() >> 4, pos.getZ() >> 4, provider);
//        return !(info.isCity() && info.hasBuilding);
//    }
//
//    private int getSqRadius(int radius, float pct) {
//        return (int) ((radius * pct) * (radius * pct));
//    }
//
//    private void findSafeSpawnPoint(World world, LostCityChunkGenerator provider, @Nonnull Predicate<BlockPos> isSuitable) {
//        Random rand = new Random(world.getSeed());
//        rand.nextFloat();
//        rand.nextFloat();
//        int radius = 200;
//        int attempts = 0;
////        int bottom = world.getWorldType().getMinimumSpawnHeight(world);
//        while (true) {
//            for (int i = 0 ; i < 200 ; i++) {
//                int x = rand.nextInt(radius * 2) - radius;
//                int z = rand.nextInt(radius * 2) - radius;
//                attempts++;
//
//                if (!isSuitable.test(new BlockPos(x, 128, z))) {
//                    continue;
//                }
//
//                LostCityProfile profile = BuildingInfo.getProfile(x >> 4, z >> 4, provider);
//
//                for (int y = profile.GROUNDLEVEL-5 ; y < 125 ; y++) {
//                    BlockPos pos = new BlockPos(x, y, z);
//                    if (isValidStandingPosition(world, pos)) {
//                        world.setSpawnPoint(pos.up());
//                        return;
//                    }
//                }
//            }
//            radius += 100;
//            if (attempts > 10000) {
//                LostCities.setup.getLogger().error("Can't find a valid spawn position!");
//                throw new RuntimeException("Can't find a valid spawn position!");
//            }
//        }
//    }

    private boolean isValidStandingPosition(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
//        return state.getBlock().isTopSolid(state) && state.getBlock().isFullCube(state) && state.getBlock().isOpaqueCube(state) && world.isAirBlock(pos.up()) && world.isAirBlock(pos.up(2));
        // @todo 1.14
        return state.canOcclude();
    }

    private boolean isValidSpawnBed(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock)) {
            return false;
        }
        Direction direction = Blocks.BLACK_BED.getBedDirection(state, world, pos);
        Block b1 = world.getBlockState(pos.below()).getBlock();
        Block b2 = world.getBlockState(pos.relative(direction.getOpposite()).below()).getBlock();
        Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(LostCityConfiguration.SPECIAL_BED_BLOCK.get()));
        if (b1 != b || b2 != b) {
            return false;
        }
        // Check if the bed is surrounded by 6 skulls
        if (!(world.getBlockState(pos.relative(direction)).getBlock() instanceof AbstractSkullBlock)) {   // @todo 1.14 other skulls!
            return false;
        }
        if (!(world.getBlockState(pos.relative(direction.getClockWise())).getBlock() instanceof AbstractSkullBlock)) {
            return false;
        }
        if (!(world.getBlockState(pos.relative(direction.getCounterClockWise())).getBlock() instanceof AbstractSkullBlock)) {
            return false;
        }
        if (!(world.getBlockState(pos.relative(direction.getOpposite(), 2)).getBlock() instanceof AbstractSkullBlock)) {
            return false;
        }
        if (!(world.getBlockState(pos.relative(direction.getOpposite()).relative(direction.getOpposite().getClockWise())).getBlock() instanceof AbstractSkullBlock)) {
            return false;
        }
        if (!(world.getBlockState(pos.relative(direction.getOpposite()).relative(direction.getOpposite().getCounterClockWise())).getBlock() instanceof AbstractSkullBlock)) {
            return false;
        }
        return true;
    }

    private BlockPos findValidTeleportLocation(World world, BlockPos start) {
        int chunkX = start.getX()>>4;
        int chunkZ = start.getZ()>>4;
        int y = start.getY();
        BlockPos pos = findValidTeleportLocation(world, chunkX, chunkZ, y);
        if (pos != null) {
            return pos;
        }
        for (int r = 1 ; r < 50 ; r++) {
            for (int i = -r ; i < r ; i++) {
                pos = findValidTeleportLocation(world, chunkX + i, chunkZ - r, y);
                if (pos != null) {
                    return pos;
                }
                pos = findValidTeleportLocation(world, chunkX + r, chunkZ + i, y);
                if (pos != null) {
                    return pos;
                }
                pos = findValidTeleportLocation(world, chunkX + r - i, chunkZ + r, y);
                if (pos != null) {
                    return pos;
                }
                pos = findValidTeleportLocation(world, chunkX - r, chunkZ + r - i, y);
                if (pos != null) {
                    return pos;
                }
            }
        }
        return null;
    }

    private BlockPos findValidTeleportLocation(World world, int chunkX, int chunkZ, int y) {
        BlockPos bestSpot = null;
        for (int dy = 0 ; dy < 255 ; dy++) {
            for (int x = 0 ; x < 16 ; x++) {
                for (int z = 0 ; z < 16 ; z++) {
                    if ((y + dy) < 250) {
                        BlockPos p = new BlockPos(chunkX * 16 + x, y + dy, chunkZ * 16 + z);
                        if (isValidSpawnBed(world, p)) {
                            return p.above();
                        }
                        if (bestSpot == null && isValidStandingPosition(world, p)) {
                            bestSpot = p.above();
                        }
                    }
                    if ((y - dy) > 1) {
                        BlockPos p = new BlockPos(chunkX * 16 + x, y - dy, chunkZ * 16 + z);
                        if (isValidSpawnBed(world, p)) {
                            return p.above();
                        }
                        if (bestSpot == null && isValidStandingPosition(world, p)) {
                            bestSpot = p.above();
                        }
                    }
                }
            }
        }
        return bestSpot;
    }

    @SubscribeEvent
    public void onPlayerSleepInBedEvent(PlayerSleepInBedEvent event) {
//        if (LostCityConfiguration.DIMENSION_ID == null) {
//            return;
//        }

        World world = event.getPlayer().getCommandSenderWorld();
        if (world.isClientSide) {
            return;
        }
        BlockPos bedLocation = event.getPos();
        if (!isValidSpawnBed(world, bedLocation)) {
            return;
        }

        if (world.dimension() == Registration.DIMENSION) {
            event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
            ServerWorld destWorld = WorldTools.getOverworld(world);
            BlockPos location = findLocation(bedLocation, destWorld);
            CustomTeleporter.teleportToDimension(event.getPlayer(), destWorld, location);
        } else {
            event.setResult(PlayerEntity.SleepResult.OTHER_PROBLEM);
            ServerWorld destWorld = event.getEntity().getCommandSenderWorld().getServer().getLevel(Registration.DIMENSION);
            BlockPos location = findLocation(bedLocation, destWorld);
            CustomTeleporter.teleportToDimension(event.getPlayer(), destWorld, location);
        }
    }

    private BlockPos findLocation(BlockPos bedLocation, ServerWorld destWorld) {
        BlockPos top = bedLocation.above(5);//destWorld.getHeight(Heightmap.Type.MOTION_BLOCKING, bedLocation).up(10);
        BlockPos location = top;
        while (top.getY() > 1 && destWorld.getBlockState(location).isAir(destWorld, location)) {
            location = location.below();
        }
//        BlockPos location = findValidTeleportLocation(destWorld, top);
        if (destWorld.isEmptyBlock(location.below())) {
            // No place to teleport
            destWorld.setBlockAndUpdate(bedLocation, Blocks.COBBLESTONE.defaultBlockState());
        }
        return location.above(1);
    }
}
