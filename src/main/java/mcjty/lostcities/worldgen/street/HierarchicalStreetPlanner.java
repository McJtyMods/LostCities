package mcjty.lostcities.worldgen.street;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pure, order-independent HIERARCHICAL_GRID_V1 road field.
 *
 * This class deliberately knows nothing about cities or buildings. Callers clip
 * the mathematical field to the city mask. A future bridge planner can therefore
 * inspect primary continuation through wilderness or water.
 */
public final class HierarchicalStreetPlanner {

    private static final long VERSION_SALT = 0x4847524944563101L; // "HGRIDV1" + version byte
    private static final long PRIMARY_X_SALT = 0x5f6c1d8a29e34b71L;
    private static final long PRIMARY_Z_SALT = 0x731ab9654ce20f8dL;
    private static final long PRIMARY_X_ACTIVATION_SALT = 0x2c64f8a10d735be9L;
    private static final long PRIMARY_Z_ACTIVATION_SALT = 0x58e307c4b92a6df1L;
    private static final long DENSITY_SALT = 0x26f40b9157acde31L;
    private static final long SECONDARY_X_COUNT_SALT = 0x18d2ca7645bf903eL;
    private static final long SECONDARY_Z_COUNT_SALT = 0x47a90edb1c6352f8L;
    private static final long SECONDARY_X_POSITION_SALT = 0x60b54df1298cae37L;
    private static final long SECONDARY_Z_POSITION_SALT = 0x39e71ac405d82bf6L;
    private static final long TERTIARY_CHANCE_SALT = 0x7b21c0e49fa3568dL;
    private static final long TERTIARY_SIDE_SALT = 0x14d8f72ca963be50L;
    private static final long TERTIARY_ORIGIN_SALT = 0x52ae901bd4763cf8L;
    private static final long TERTIARY_LENGTH_SALT = 0x6c03f1a984d27be5L;

    private final long seed;
    private final long dimensionSalt;
    private final StreetPlannerSettings settings;
    private final int primaryOffsetX;
    private final int primaryOffsetZ;

    public HierarchicalStreetPlanner(long seed, String dimensionId, StreetPlannerSettings settings) {
        this.seed = seed;
        this.dimensionSalt = stableStringHash(dimensionId);
        this.settings = settings;
        this.primaryOffsetX = floorModHash(hash(PRIMARY_X_SALT, 0, 0, 0), settings.primarySpacingX());
        this.primaryOffsetZ = floorModHash(hash(PRIMARY_Z_SALT, 0, 0, 0), settings.primarySpacingZ());
    }

    public int primaryOffsetX() {
        return primaryOffsetX;
    }

    public int primaryOffsetZ() {
        return primaryOffsetZ;
    }

    public StreetPlannerSettings settings() {
        return settings;
    }

    public PlannedRoadType getRoadType(int chunkX, int chunkZ) {
        return rawAt(chunkX, chunkZ).roadType;
    }

    public PlannedStreetInfo getStreetInfo(int chunkX, int chunkZ) {
        RawRoad raw = rawAt(chunkX, chunkZ);
        BlockLayout block = raw.block;
        boolean north = raw.roadType != PlannedRoadType.NONE && getRoadType(chunkX, chunkZ - 1) != PlannedRoadType.NONE;
        boolean south = raw.roadType != PlannedRoadType.NONE && getRoadType(chunkX, chunkZ + 1) != PlannedRoadType.NONE;
        boolean west = raw.roadType != PlannedRoadType.NONE && getRoadType(chunkX - 1, chunkZ) != PlannedRoadType.NONE;
        boolean east = raw.roadType != PlannedRoadType.NONE && getRoadType(chunkX + 1, chunkZ) != PlannedRoadType.NONE;
        return new PlannedStreetInfo(raw.roadType, north, south, west, east,
                block.blockX, block.blockZ, block.westX, block.northZ, block.eastX, block.southZ, block.density,
                block.secondaryX, block.secondaryZ, raw.tertiary);
    }

    private RawRoad rawAt(int chunkX, int chunkZ) {
        BlockLayout block = getBlockLayout(chunkX, chunkZ);
        boolean verticalPrimary = isVerticalPrimary(chunkX);
        boolean horizontalPrimary = isHorizontalPrimary(chunkZ);
        if (verticalPrimary || horizontalPrimary) {
            return new RawRoad(PlannedRoadType.PRIMARY, block, null);
        }
        if (block.secondaryX.contains(chunkX) || block.secondaryZ.contains(chunkZ)) {
            return new RawRoad(PlannedRoadType.SECONDARY, block, null);
        }
        TertiaryRoadSegment tertiary = getTertiarySegment(block, chunkX, chunkZ);
        if (tertiary != null && tertiary.contains(chunkX, chunkZ)) {
            return new RawRoad(PlannedRoadType.TERTIARY, block, tertiary);
        }
        return new RawRoad(PlannedRoadType.NONE, block, tertiary);
    }

    public BlockLayout getBlockLayout(int chunkX, int chunkZ) {
        // floorDiv is essential here: truncating division would create a seam at
        // negative coordinates. Candidate indices identify blocks even though
        // optional candidates may be absent. The active west/north line is the
        // inclusive bound; forced candidates cap each search at primaryForceEvery.
        int candidateX = Math.toIntExact(Math.floorDiv((long) chunkX - primaryOffsetX, settings.primarySpacingX()));
        int candidateZ = Math.toIntExact(Math.floorDiv((long) chunkZ - primaryOffsetZ, settings.primarySpacingZ()));
        int blockX = findActiveAtOrBefore(candidateX, true);
        int blockZ = findActiveAtOrBefore(candidateZ, false);
        int nextBlockX = findActiveAfter(blockX, true);
        int nextBlockZ = findActiveAfter(blockZ, false);
        int westX = candidateCoordinate(primaryOffsetX, blockX, settings.primarySpacingX());
        int northZ = candidateCoordinate(primaryOffsetZ, blockZ, settings.primarySpacingZ());
        int eastX = candidateCoordinate(primaryOffsetX, nextBlockX, settings.primarySpacingX());
        int southZ = candidateCoordinate(primaryOffsetZ, nextBlockZ, settings.primarySpacingZ());
        int spacingX = eastX - westX;
        int spacingZ = southZ - northZ;
        double density = unitDouble(hash(DENSITY_SALT, blockX, blockZ, 0));
        int countX = selectCount(settings.secondaryMinCountX(), settings.secondaryMaxCountX(), density,
                hash(SECONDARY_X_COUNT_SALT, blockX, blockZ, 0));
        int countZ = selectCount(settings.secondaryMinCountZ(), settings.secondaryMaxCountZ(), density,
                hash(SECONDARY_Z_COUNT_SALT, blockX, blockZ, 0));
        List<Integer> secondaryX = selectSecondaryPositions(blockX, blockZ, westX, spacingX, countX,
                SECONDARY_X_POSITION_SALT);
        List<Integer> secondaryZ = selectSecondaryPositions(blockX, blockZ, northZ, spacingZ, countZ,
                SECONDARY_Z_POSITION_SALT);
        return new BlockLayout(blockX, blockZ, westX, northZ, eastX, southZ, density, secondaryX, secondaryZ);
    }

    public boolean isVerticalPrimary(int chunkX) {
        long relative = (long) chunkX - primaryOffsetX;
        if (Math.floorMod(relative, settings.primarySpacingX()) != 0) {
            return false;
        }
        int candidate = Math.toIntExact(Math.floorDiv(relative, settings.primarySpacingX()));
        return isActivePrimaryCandidate(candidate, true);
    }

    public boolean isHorizontalPrimary(int chunkZ) {
        long relative = (long) chunkZ - primaryOffsetZ;
        if (Math.floorMod(relative, settings.primarySpacingZ()) != 0) {
            return false;
        }
        int candidate = Math.toIntExact(Math.floorDiv(relative, settings.primarySpacingZ()));
        return isActivePrimaryCandidate(candidate, false);
    }

    private boolean isActivePrimaryCandidate(int candidate, boolean xAxis) {
        if (Math.floorMod(candidate, settings.primaryForceEvery()) == 0) {
            return true;
        }
        long salt = xAxis ? PRIMARY_X_ACTIVATION_SALT : PRIMARY_Z_ACTIVATION_SALT;
        return unitDouble(hash(salt, xAxis ? candidate : 0, xAxis ? 0 : candidate, 0))
                < settings.primaryOptionalChance();
    }

    private int findActiveAtOrBefore(int candidate, boolean xAxis) {
        for (int distance = 0; distance < settings.primaryForceEvery(); distance++) {
            int current = Math.subtractExact(candidate, distance);
            if (isActivePrimaryCandidate(current, xAxis)) {
                return current;
            }
        }
        throw new IllegalStateException("No forced primary candidate found");
    }

    private int findActiveAfter(int candidate, boolean xAxis) {
        for (int distance = 1; distance <= settings.primaryForceEvery(); distance++) {
            int current = Math.addExact(candidate, distance);
            if (isActivePrimaryCandidate(current, xAxis)) {
                return current;
            }
        }
        throw new IllegalStateException("No forced primary candidate found");
    }

    private static int candidateCoordinate(int offset, int candidate, int spacing) {
        return Math.toIntExact((long) offset + (long) candidate * spacing);
    }

    private int selectCount(int minimum, int maximum, double density, long variationHash) {
        if (minimum == maximum) {
            return minimum;
        }
        // Density is shared by both axes; a smaller dedicated component prevents
        // every block from receiving identical X/Z counts.
        double variedDensity = density * .75 + unitDouble(variationHash) * .25;
        int count = minimum + (int) Math.floor(variedDensity * (maximum - minimum + 1));
        return Math.min(maximum, count);
    }

    private List<Integer> selectSecondaryPositions(int blockX, int blockZ, int start, int spacing, int desired, long salt) {
        if (desired == 0) {
            return List.of();
        }
        int first = settings.minimumEdgeDistance();
        int last = spacing - settings.minimumEdgeDistance();
        if (first > last) {
            return List.of();
        }
        List<Integer> candidates = new ArrayList<>();
        for (int local = first; local <= last; local++) {
            candidates.add(local);
        }
        candidates.sort((a, b) -> {
            int comparison = Long.compareUnsigned(hash(salt, blockX, blockZ, a), hash(salt, blockX, blockZ, b));
            return comparison != 0 ? comparison : Integer.compare(a, b);
        });
        List<Integer> selected = new ArrayList<>();
        for (int local : candidates) {
            boolean separated = true;
            for (int existing : selected) {
                if (Math.abs(existing - local) < settings.minimumRoadSeparation()) {
                    separated = false;
                    break;
                }
            }
            if (separated) {
                selected.add(local);
                if (selected.size() == desired) {
                    break;
                }
            }
        }
        selected.sort(Comparator.naturalOrder());
        return selected.stream().map(local -> start + local).toList();
    }

    private TertiaryRoadSegment getTertiarySegment(BlockLayout block, int chunkX, int chunkZ) {
        List<Integer> xRoads = new ArrayList<>(block.secondaryX.size() + 2);
        xRoads.add(block.westX);
        xRoads.addAll(block.secondaryX);
        xRoads.add(block.eastX);
        List<Integer> zRoads = new ArrayList<>(block.secondaryZ.size() + 2);
        zRoads.add(block.northZ);
        zRoads.addAll(block.secondaryZ);
        zRoads.add(block.southZ);

        int cellX = findCell(xRoads, chunkX);
        int cellZ = findCell(zRoads, chunkZ);
        if (cellX < 0 || cellZ < 0) {
            return null;
        }
        int x0 = xRoads.get(cellX);
        int x1 = xRoads.get(cellX + 1);
        int z0 = zRoads.get(cellZ);
        int z1 = zRoads.get(cellZ + 1);
        long identity = hash(TERTIARY_CHANCE_SALT, block.blockX, block.blockZ, cellX * 257 + cellZ);
        if (unitDouble(identity) >= settings.tertiaryChance()) {
            return null;
        }

        RoadDirection direction = selectTertiaryDirection(block, cellX, cellZ, x0, x1, z0, z1);
        if (direction == null) {
            return null;
        }
        boolean vertical = direction == RoadDirection.NORTH || direction == RoadDirection.SOUTH;
        int interiorLength = vertical ? z1 - z0 - 1 : x1 - x0 - 1;
        int maxLength = Math.min(settings.tertiaryMaxLength(), interiorLength - 1);
        if (maxLength < settings.tertiaryMinLength()) {
            return null;
        }
        int lengthRange = maxLength - settings.tertiaryMinLength() + 1;
        int length = settings.tertiaryMinLength() + floorModHash(hash(TERTIARY_LENGTH_SALT,
                block.blockX, block.blockZ, cellX * 257 + cellZ), lengthRange);

        int originX;
        int originZ;
        if (vertical) {
            int originMin = x0 + 2;
            int originMax = x1 - 2;
            if (originMin > originMax) {
                return null;
            }
            originX = originMin + floorModHash(hash(TERTIARY_ORIGIN_SALT,
                    block.blockX, block.blockZ, cellX * 257 + cellZ), originMax - originMin + 1);
            originZ = direction == RoadDirection.SOUTH ? z0 : z1;
        } else {
            int originMin = z0 + 2;
            int originMax = z1 - 2;
            if (originMin > originMax) {
                return null;
            }
            originX = direction == RoadDirection.EAST ? x0 : x1;
            originZ = originMin + floorModHash(hash(TERTIARY_ORIGIN_SALT,
                    block.blockX, block.blockZ, cellX * 257 + cellZ), originMax - originMin + 1);
        }
        return new TertiaryRoadSegment(identity, originX, originZ, direction, length);
    }

    private RoadDirection selectTertiaryDirection(BlockLayout block, int cellX, int cellZ,
                                                   int x0, int x1, int z0, int z1) {
        RoadDirection[] directions = RoadDirection.values();
        int first = floorModHash(hash(TERTIARY_SIDE_SALT,
                block.blockX, block.blockZ, cellX * 257 + cellZ), directions.length);
        // Keep the hashed side as the first choice, but do not discard the entire
        // cell merely because that side cannot fit an origin or the minimum
        // length. Walking the remaining sides is deterministic and is especially
        // useful for narrow rectangular city blocks.
        for (int offset = 0; offset < directions.length; offset++) {
            RoadDirection direction = directions[(first + offset) % directions.length];
            boolean vertical = direction == RoadDirection.NORTH || direction == RoadDirection.SOUTH;
            int transverseSpan = vertical ? x1 - x0 : z1 - z0;
            int inwardSpan = vertical ? z1 - z0 : x1 - x0;
            if (transverseSpan >= 4 && inwardSpan - 2 >= settings.tertiaryMinLength()) {
                return direction;
            }
        }
        return null;
    }

    private static int findCell(List<Integer> roads, int coordinate) {
        for (int i = 0; i < roads.size() - 1; i++) {
            if (coordinate > roads.get(i) && coordinate < roads.get(i + 1)) {
                return i;
            }
        }
        return -1;
    }

    private long hash(long streamSalt, int x, int z, int extra) {
        long value = seed ^ dimensionSalt ^ VERSION_SALT ^ streamSalt;
        value ^= mix64(Integer.toUnsignedLong(x) + 0x9e3779b97f4a7c15L);
        value ^= mix64(Integer.toUnsignedLong(z) + 0xc2b2ae3d27d4eb4fL);
        value ^= mix64(Integer.toUnsignedLong(extra) + 0x165667b19e3779f9L);
        return mix64(value);
    }

    private static int floorModHash(long hash, int modulus) {
        return (int) Math.floorMod(hash, (long) modulus);
    }

    private static double unitDouble(long hash) {
        return (hash >>> 11) * 0x1.0p-53;
    }

    private static long stableStringHash(String value) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < value.length(); i++) {
            hash ^= value.charAt(i);
            hash *= 0x100000001b3L;
        }
        return mix64(hash);
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    private record RawRoad(PlannedRoadType roadType, BlockLayout block, TertiaryRoadSegment tertiary) { }

    public record BlockLayout(
            int blockX,
            int blockZ,
            int westX,
            int northZ,
            int eastX,
            int southZ,
            double density,
            List<Integer> secondaryX,
            List<Integer> secondaryZ
    ) { }
}
