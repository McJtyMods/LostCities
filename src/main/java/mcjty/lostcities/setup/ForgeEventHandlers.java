package mcjty.lostcities.setup;

import mcjty.lostcities.LostCities;
import mcjty.lostcities.commands.ModCommands;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.varia.ChunkCoord;
import mcjty.lostcities.varia.ComponentFactory;
import mcjty.lostcities.varia.CustomTeleporter;
import mcjty.lostcities.varia.WorldTools;
import mcjty.lostcities.worldgen.GlobalTodo;
import mcjty.lostcities.worldgen.IDimensionInfo;
import mcjty.lostcities.worldgen.lost.*;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import mcjty.lostcities.worldgen.lost.cityassets.PredefinedCity;
import mcjty.lostcities.worldgen.lost.cityassets.PredefinedSphere;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import static mcjty.lostcities.setup.Registration.LOSTCITY;

public class ForgeEventHandlers {

    private final Map<ResourceKey<Level>, BlockPos> spawnPositions = new HashMap<>();

    @SubscribeEvent
    public void commandRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }


    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.getData(Registration.ATTACHMENT_TYPE_SPAWNSET)) {
            player.setData(Registration.ATTACHMENT_TYPE_SPAWNSET, true);
            for (Map.Entry<ResourceKey<Level>, BlockPos> entry : spawnPositions.entrySet()) {
                if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                    serverPlayer.setRespawnPosition(entry.getKey(), entry.getValue(), 0.0f, true, true);
                    serverPlayer.teleportTo(entry.getValue().getX(), entry.getValue().getY(), entry.getValue().getZ());
                }
            }
        }
    }


    @SubscribeEvent
    public void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            GlobalTodo.getData(serverLevel).executeAndClearTodo(serverLevel);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        BuildingInfo.cleanCache();
        Highway.cleanCache();
        Railway.cleanCache();
        BiomeInfo.cleanCache();
        City.cleanCache();
        CitySphere.cleanCache();
    }

    @SubscribeEvent
    public void onCreateSpawnPoint(LevelEvent.CreateSpawnPosition event) {
        LevelAccessor world = event.getLevel();
        if (world instanceof ServerLevel serverLevel) {
            IDimensionInfo dimensionInfo = Registration.LOSTCITY_FEATURE.get().getDimensionInfo(serverLevel);
            if (dimensionInfo == null) {
                return;
            }
            LostCityProfile profile = dimensionInfo.getProfile();

            Predicate<BlockPos> isSuitable = pos -> true;
            boolean needsCheck = false;

            if (!profile.SPAWN_BIOME.isEmpty()) {
                final Biome spawnBiome = serverLevel.registryAccess().registryOrThrow(Registries.BIOME).get(ResourceLocation.parse(profile.SPAWN_BIOME));
                if (spawnBiome == null) {
                    ModSetup.getLogger().error("Cannot find biome '{}' for the player to spawn in !", profile.SPAWN_BIOME);
                } else {
                    isSuitable = blockPos -> world.getBiome(blockPos).value() == spawnBiome;
                    needsCheck = true;
                }
            } else if (!profile.SPAWN_CITY.isEmpty()) {
                final PredefinedCity city = AssetRegistries.PREDEFINED_CITIES.get(world, profile.SPAWN_CITY);
                if (city == null) {
                    ModSetup.getLogger().error("Cannot find city '{}' for the player to spawn in !", profile.SPAWN_CITY);
                } else {
                    float sqradius = getSqRadius(city.getRadius(), 0.8f);
                    isSuitable = blockPos -> city.getDimension() == serverLevel.dimension() &&
                            CitySphere.squaredDistance(city.getChunkX()*16+8, city.getChunkZ()*16+8, blockPos.getX(), blockPos.getZ()) < sqradius;
                    needsCheck = true;
                }
            } else if (!profile.SPAWN_SPHERE.isEmpty()) {
                if ("<in>".equals(profile.SPAWN_SPHERE)) {
                    isSuitable = blockPos -> {
                        ChunkCoord coord = new ChunkCoord(dimensionInfo.getType(), blockPos.getX() >> 4, blockPos.getZ() >> 4);
                        CitySphere sphere = CitySphere.getCitySphere(coord, dimensionInfo);
                        if (!sphere.isEnabled()) {
                            return false;
                        }
                        float sqradius = getSqRadius((int) sphere.getRadius(), 0.8f);
                        return sphere.getCenterPos().distSqr(blockPos.atY(sphere.getCenterPos().getY())) < sqradius;
                    };
                    needsCheck = true;
                } else if ("<out>".equals(profile.SPAWN_SPHERE)) {
                    isSuitable = blockPos -> {
                        ChunkCoord coord = new ChunkCoord(dimensionInfo.getType(), blockPos.getX() >> 4, blockPos.getZ() >> 4);
                        CitySphere sphere = CitySphere.getCitySphere(coord, dimensionInfo);
                        if (!sphere.isEnabled()) {
                            return true;
                        }
                        float sqradius = sphere.getRadius() * sphere.getRadius();
                        return sphere.getCenterPos().distSqr(blockPos.atY(sphere.getCenterPos().getY())) > sqradius;
                    };
                    needsCheck = true;
                } else {
                    final PredefinedSphere sphere = AssetRegistries.PREDEFINED_SPHERES.get(world, profile.SPAWN_SPHERE);
                    if (sphere == null) {
                        LostCities.setup.getLogger().error("Cannot find sphere '" + profile.SPAWN_SPHERE + "' for the player to spawn in !");
                    } else {
                        float sqradius = getSqRadius(sphere.getRadius(), 0.8f);
                        isSuitable = blockPos -> sphere.getDimension() == serverLevel.dimension() &&
                                CitySphere.squaredDistance(sphere.getChunkX() * 16 + 8, sphere.getChunkZ() * 16 + 8, blockPos.getX(), blockPos.getZ()) < sqradius;
                        needsCheck = true;
                    }
                }
            }

            if (profile.SPAWN_NOT_IN_BUILDING) {
                isSuitable = isSuitable.and(blockPos -> isOutsideBuilding(dimensionInfo, blockPos));
                needsCheck = true;
            } else if (profile.FORCE_SPAWN_IN_BUILDING) {
                isSuitable = isSuitable.and(blockPos -> !isOutsideBuilding(dimensionInfo, blockPos));
                needsCheck = true;
            }

            // Potentially set the spawn point
            switch (profile.LANDSCAPE_TYPE) {
                case DEFAULT, SPHERES -> {
                    if (needsCheck) {
                        BlockPos pos = findSafeSpawnPoint(serverLevel, dimensionInfo, isSuitable, event.getSettings());
                        event.getSettings().setSpawn(pos, 0.0f);
                        spawnPositions.put(serverLevel.dimension(), pos);
                        event.setCanceled(true);
                    }
                }
                case FLOATING, SPACE, CAVERN -> {
                    BlockPos pos = findSafeSpawnPoint(serverLevel, dimensionInfo, isSuitable, event.getSettings());
                    event.getSettings().setSpawn(pos, 0.0f);
                    spawnPositions.put(serverLevel.dimension(), pos);
                    event.setCanceled(true);
                }
            }
        }
    }

    private boolean isOutsideBuilding(IDimensionInfo provider, BlockPos pos) {
        ChunkCoord coord = new ChunkCoord(provider.getType(), pos.getX() >> 4, pos.getZ() >> 4);
        BuildingInfo info = BuildingInfo.getBuildingInfo(coord, provider);
        return !(info.isCity() && info.hasBuilding);
    }

    private int getSqRadius(int radius, float pct) {
        return (int) ((radius * pct) * (radius * pct));
    }

    private BlockPos findSafeSpawnPoint(Level world, IDimensionInfo provider, @Nonnull Predicate<BlockPos> isSuitable,
                                    @Nonnull ServerLevelData serverLevelData) {
        Random rand = new Random(provider.getSeed());
        int radius = 200;
        int attempts = 0;
//        int bottom = world.getWorldType().getMinimumSpawnHeight(world);
        while (true) {
            for (int i = 0 ; i < 200 ; i++) {
                int x = rand.nextInt(radius * 2) - radius;
                int z = rand.nextInt(radius * 2) - radius;
                attempts++;

                if (!isSuitable.test(new BlockPos(x, 128, z))) {
                    continue;
                }

                ChunkCoord coord = new ChunkCoord(provider.getType(), x >> 4, z >> 4);
                LostCityProfile profile = BuildingInfo.getProfile(coord, provider);

                for (int y = profile.GROUNDLEVEL-5 ; y < 125 ; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isValidStandingPosition(world, pos)) {
//                        serverLevelData.setSpawn(pos.above(), 0.0f);
                        return pos.above();
                    }
                }
            }
            radius += 100;
            if (attempts > 20000) {
                LostCities.setup.getLogger().error("Can't find a valid spawn position!");
                throw new RuntimeException("Can't find a valid spawn position!");
            }
        }
    }

    private boolean isValidStandingPosition(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!state.isFaceSturdy(world, pos, Direction.UP)) {
            return false;
        }
        if (state.is(Blocks.BEDROCK)) {
            return false;
        }
        if (!world.getBlockState(pos.above()).isAir() || !world.getBlockState(pos.above(2)).isAir()) {
            return false;
        }
        return true;
//        return state.getBlock().isTopSolid(state) && state.getBlock().isFullCube(state) && state.getBlock().isOpaqueCube(state) && world.isAirBlock(pos.up()) && world.isAirBlock(pos.up(2));
//        return state.canOcclude();
    }

    private boolean isValidSpawnBed(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock)) {
            return false;
        }
        Direction direction = Blocks.BLACK_BED.getBedDirection(state, world, pos);
        Block b1 = world.getBlockState(pos.below()).getBlock();
        Block b2 = world.getBlockState(pos.relative(direction.getOpposite()).below()).getBlock();
        Block b = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(Config.SPECIAL_BED_BLOCK.get()));
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

    private BlockPos findValidTeleportLocation(Level world, BlockPos start) {
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

    private BlockPos findValidTeleportLocation(Level world, int chunkX, int chunkZ, int y) {
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
    public void onPlayerSleepInBedEvent(CanPlayerSleepEvent event) {
//        if (LostCityConfiguration.DIMENSION_ID == null) {
//            return;
//        }

        Level world = event.getEntity().getCommandSenderWorld();
        if (world.isClientSide) {
            return;
        }
        BlockPos bedLocation = event.getPos();
        if (bedLocation == null || !isValidSpawnBed(world, bedLocation)) {
            return;
        }

        if (world.dimension() == Registration.DIMENSION) {
            event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
            ServerLevel destWorld = WorldTools.getOverworld(world);
            BlockPos location = findLocation(bedLocation, destWorld);
            CustomTeleporter.teleportToDimension(event.getEntity(), destWorld, location);
        } else {
            event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
            ServerLevel destWorld = event.getEntity().getCommandSenderWorld().getServer().getLevel(Registration.DIMENSION);
            if (destWorld == null) {
                event.getEntity().sendSystemMessage(ComponentFactory.literal("Error finding Lost City dimension: " + LOSTCITY + "!").withStyle(ChatFormatting.RED));
            } else {
                BlockPos location = findLocation(bedLocation, destWorld);
                CustomTeleporter.teleportToDimension(event.getEntity(), destWorld, location);
            }
        }
    }

    private BlockPos findLocation(BlockPos bedLocation, ServerLevel destWorld) {
        BlockPos top = bedLocation.above(5);//destWorld.getHeight(Heightmap.Type.MOTION_BLOCKING, bedLocation).up(10);
        BlockPos location = top;
        while (top.getY() > 1 && destWorld.getBlockState(location).isAir()) {
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
