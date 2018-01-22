package mcjty.lostcities;

import mcjty.lostcities.config.LandscapeType;
import mcjty.lostcities.config.LostCityProfile;
import mcjty.lostcities.dimensions.world.LostBiomeDecorator;
import mcjty.lostcities.dimensions.world.LostCityChunkGenerator;
import mcjty.lostcities.dimensions.world.LostWorldType;
import mcjty.lostcities.dimensions.world.lost.CitySphere;
import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TerrainEventHandlers {

//    @SubscribeEvent
    public void onCreateDecorator(BiomeEvent.CreateDecorator event) {
        event.setNewBiomeDecorator(new LostBiomeDecorator(event.getOriginalBiomeDecorator()));
    }


    @SubscribeEvent
    public void onCreateDecorate(DecorateBiomeEvent.Decorate event) {
        World world = event.getWorld();
        if (!world.isRemote) {
            switch (event.getType()) {
                case BIG_SHROOM:
                case CACTUS:
                case CLAY:
                case DEAD_BUSH:
                case DESERT_WELL:
                case FOSSIL:
                case ICE:
                case LAKE_LAVA:
                case PUMPKIN:
                case ROCK:
                case SAND:
                case SAND_PASS2:
                case SHROOM:
                case CUSTOM:
                case GRASS:
                    break;
                case LAKE_WATER:
                case REED:
                case TREE:
                case LILYPAD:
                case FLOWERS:
                    LostCityProfile profile = LostWorldType.getProfile(world);
                    if (profile.LANDSCAPE_TYPE == LandscapeType.SPACE && profile.CITYSPHERE_LANDSCAPE_OUTSIDE) {
                        WorldServer worldServer = (WorldServer) world;
                        LostCityChunkGenerator provider = (LostCityChunkGenerator) worldServer.getChunkProvider().chunkGenerator;
                        int chunkX = (event.getPos().getX()) >> 4;
                        int chunkZ = (event.getPos().getZ()) >> 4;
                        ChunkCoord cityCenter = CitySphere.getCityCenterForSpace(chunkX, chunkZ, provider);
                        CitySphere sphere = CitySphere.getCitySphereAtCenter(cityCenter, provider);
                        if (!sphere.isEnabled()) {
                            return;
                        }
                        float radius = CitySphere.getSphereRadius(cityCenter.getChunkX(), cityCenter.getChunkZ(), provider);
                        double sqradiusOffset = (radius-2) * (radius-2);
                        int cx = cityCenter.getChunkX() * 16 + 8;
                        int cz = cityCenter.getChunkZ() * 16 + 8;
                        if (CitySphere.onCitySphereBorder(chunkX, chunkZ, provider)) {
//                        if (CitySphere.squaredDistance(cx, cz, event.getPos().getX(), event.getPos().getZ()) >= sqradiusOffset) {
                            Biome biome = world.getBiomeForCoordsBody(event.getPos());
                            BiomeDecorator decorator = biome.decorator;
                            int treesPerChunk = decorator.treesPerChunk;

                            if (world.rand.nextFloat() < decorator.extraTreeChance) {
                                ++treesPerChunk;
                            }
                            for (int i = 0; i < treesPerChunk; ++i) {
                                int treex = world.rand.nextInt(16) + 8;
                                int treez = world.rand.nextInt(16) + 8;
                                WorldGenAbstractTree worldgenabstracttree = biome.getRandomTreeFeature(world.rand);
                                worldgenabstracttree.setDecorationDefaults();
                                BlockPos blockpos = world.getHeight(event.getPos().add(treex, 0, treez));

                                if (CitySphere.squaredDistance(cx, cz, treex, treez) < sqradiusOffset) {
                                    if (worldgenabstracttree.generate(world, world.rand, blockpos)) {
                                        worldgenabstracttree.generateSaplings(world, world.rand, blockpos);
                                    }
                                }
                            }

                            event.setResult(Event.Result.DENY);
                        }
                    }
                    break;
            }
        }
    }
}
