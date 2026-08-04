package mcjty.lostcities.worldgen.lost;

import mcjty.lostcities.api.MultiPos;
import mcjty.lostcities.setup.Config;
import mcjty.lostcities.varia.ChunkCoord;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Resolves the server-configured village and structure exclusions from the chunks already present
 * in the active world-generation region. No chunk loads or generation are initiated here.
 */
public final class StructureAvoidance {

    private record FootprintKey(ResourceKey<Level> dimension,
                                int minX, int minZ, int maxX, int maxZ, int margin) {
    }

    private record FootprintDecision(boolean avoidsCity) {

        private static final FootprintDecision ALLOW = new FootprintDecision(false);
        private static final FootprintDecision AVOID = new FootprintDecision(true);
    }

    private static final ConcurrentMap<FootprintKey, FootprintDecision> MULTIBUILDING_DECISIONS = new ConcurrentHashMap<>();

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

    public static void cleanCache() {
        MULTIBUILDING_DECISIONS.clear();
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

        if (multiPos.isMulti()) {
            FootprintKey key = new FootprintKey(coord.dimension(), minX, minZ, maxX, maxZ, margin);
            FootprintDecision decision = MULTIBUILDING_DECISIONS.computeIfAbsent(key,
                    ignored -> resolveFootprint(region, minX, minZ, maxX, maxZ, margin));
            if (!decision.avoidsCity()) {
                return Result.NONE;
            }
            // The shared decision controls city avoidance. Resolve DIRECT locally so terrain
            // flattening is still suppressed if this chunk's references became available later.
            ChunkAccess current = region.getChunk(coord.chunkX(), coord.chunkZ(),
                    ChunkStatus.STRUCTURE_REFERENCES, false);
            return current != null && hasAvoidedStructure(region, current, true)
                    ? Result.DIRECT
                    : Result.ADJACENT;
        }

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

    /**
     * Resolve one immutable decision for a complete multibuilding footprint. If references for
     * part of the footprint are unavailable, preserve the old fail-open behaviour and commit an
     * allow decision for every chunk in that footprint. A structure that is already visible is
     * sufficient to reject the complete footprint.
     */
    private static FootprintDecision resolveFootprint(WorldGenRegion region, int minX, int minZ,
                                                       int maxX, int maxZ, int margin) {
        for (int chunkX = minX - margin; chunkX <= maxX + margin; chunkX++) {
            for (int chunkZ = minZ - margin; chunkZ <= maxZ + margin; chunkZ++) {
                ChunkAccess chunk = region.getChunk(chunkX, chunkZ,
                        ChunkStatus.STRUCTURE_REFERENCES, false);
                if (chunk == null) {
                    continue;
                }
                boolean insideFootprint = chunkX >= minX && chunkX <= maxX
                        && chunkZ >= minZ && chunkZ <= maxZ;
                if (hasAvoidedStructure(region, chunk, insideFootprint)) {
                    return FootprintDecision.AVOID;
                }
            }
        }
        return FootprintDecision.ALLOW;
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
