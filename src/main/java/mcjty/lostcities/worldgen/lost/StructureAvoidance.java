package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.api.MultiPos;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Optional;

/**
 * Resolves the server-configured village and structure exclusions from the chunks already present
 * in the active world-generation region. No chunk loads or generation are initiated here.
 */
public final class StructureAvoidance {

    public enum Result {
        NONE,
        DIRECT,
        ADJACENT,
        UNKNOWN;

        public boolean avoidsCity() {
            return this == DIRECT || this == ADJACENT;
        }

        public boolean isKnown() {
            return this != UNKNOWN;
        }
    }

    private StructureAvoidance() {
    }

    public static Result check(ChunkCoord coord, WorldGenLevel level) {
        return check(coord, MultiPos.SINGLE, level);
    }

    /**
     * Check the complete footprint containing {@code coord}. A structure anywhere inside a
     * multibuilding footprint rejects the entire multibuilding, while DIRECT remains reserved for
     * a reference in {@code coord} itself so terrain flattening is only suppressed where needed.
     */
    public static Result check(ChunkCoord coord, MultiPos multiPos, WorldGenLevel level) {
        boolean checkAdjacent = Config.AVOID_VILLAGES_ADJACENT.get() || Config.AVOID_STRUCTURES_ADJACENT.get();
        if (!checkAdjacent && !Config.AVOID_VILLAGES.get() && !Config.hasAvoidedStructures()) {
            return Result.NONE;
        }
        if (!(level instanceof WorldGenRegion region)) {
            return Result.UNKNOWN;
        }

        int minX = coord.chunkX() - multiPos.x();
        int minZ = coord.chunkZ() - multiPos.z();
        int maxX = minX + multiPos.w() - 1;
        int maxZ = minZ + multiPos.h() - 1;
        int margin = checkAdjacent ? 1 : 0;
        boolean foundElsewhere = false;
        boolean missingReferences = false;
        for (int chunkX = minX - margin; chunkX <= maxX + margin; chunkX++) {
            for (int chunkZ = minZ - margin; chunkZ <= maxZ + margin; chunkZ++) {
                ChunkAccess chunk = region.getChunk(chunkX, chunkZ,
                        ChunkStatus.STRUCTURE_REFERENCES, false);
                if (chunk == null) {
                    missingReferences = true;
                    continue;
                }
                boolean insideFootprint = chunkX >= minX && chunkX <= maxX
                        && chunkZ >= minZ && chunkZ <= maxZ;
                if (hasAvoidedStructure(region, chunk, insideFootprint)) {
                    if (chunkX == coord.chunkX() && chunkZ == coord.chunkZ()) {
                        return Result.DIRECT;
                    }
                    foundElsewhere = true;
                }
            }
        }
        // An unavailable chunk could still contain a direct reference for this coordinate, which
        // matters for avoidFlattening. Only cache a classification when the requested area is known.
        if (missingReferences) {
            return Result.UNKNOWN;
        }
        return foundElsewhere ? Result.ADJACENT : Result.NONE;
    }

    private static boolean hasAvoidedStructure(WorldGenRegion region, ChunkAccess chunk, boolean insideFootprint) {
        if (!chunk.hasAnyStructureReferences()) {
            return false;
        }

        var structures = region.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (var entry : chunk.getAllReferences().entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            Optional<ResourceKey<Structure>> key = structures.getResourceKey(entry.getKey());
            if (Config.AVOID_VILLAGES.get() && (insideFootprint || Config.AVOID_VILLAGES_ADJACENT.get())
                    && key.map(k -> structures.getHolderOrThrow(k).is(StructureTags.VILLAGE)).orElse(false)) {
                return true;
            }
            if ((insideFootprint || Config.AVOID_STRUCTURES_ADJACENT.get())
                    && key.map(k -> Config.isAvoidedStructure(k.location())).orElse(false)) {
                return true;
            }
        }
        return false;
    }
}
