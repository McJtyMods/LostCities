package mcjty.lostcities.varia;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.Set;

public class WorldTools {

    public static boolean chunkLoaded(Level world, BlockPos pos) {
        if (world == null || pos == null) {
            return false;
        }
        return world.hasChunkAt(pos);
//        return world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4) != null && world.getChunkFromBlockCoords(pos).isLoaded();
    }

    public static ServerLevel getOverworld() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getLevel(Level.OVERWORLD);
    }

    public static ServerLevel getOverworld(Level world) {
        MinecraftServer server = world.getServer();
        return server.getLevel(Level.OVERWORLD);
    }

    public static ServerLevel loadWorld(ResourceKey<Level> type) {
        ServerLevel world = getWorld(type);
        if (world == null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            return server.getLevel(type);
        }
        return world;
    }

    public static ServerLevel getWorld(ResourceKey<Level> type) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getLevel(type);
    }

    public static ServerLevel getWorld(Level world, ResourceKey<Level> type) {
        MinecraftServer server = world.getServer();
        return server.getLevel(type);
    }

    public static Map<Structure, LongSet> checkStructures(ServerLevel level, ChunkCoord coord) {
        BlockPos center = new BlockPos(coord.chunkX() << 4 + 8, 60, coord.chunkZ() << 4 + 8);
        StructureManager structuremanager = level.structureManager();
//        Map<StructurePlacement, Set<Holder<Structure>>> map = new Object2ObjectArrayMap<>();
        Map<Structure, LongSet> structures = structuremanager.getAllStructuresAt(center);
        return structures;
//        HolderSet<Structure> holderSet = HolderSet.direct(BuiltinStructures.ANCIENT_CITY);
//        ChunkGeneratorStructureState chunkgeneratorstructurestate = level.getChunkSource().getGeneratorState();
//
//        for(Holder<Structure> holder : holderSet) {
//            for(StructurePlacement structureplacement : chunkgeneratorstructurestate.getPlacementsForStructure(holder)) {
//                map.computeIfAbsent(structureplacement, (_v) -> new ObjectArraySet<>()).add(holder);
//            }
//        }

    }
}
