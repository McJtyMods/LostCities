package mcjty.lostcities.worldgen.highway;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Pure, order-independent INTERCITY_NETWORK_V1 planner.
 *
 * Planning cells own at most one approximate city hub. Each hub independently
 * selects bounded, sector-diverse neighbours; an undirected edge exists only
 * when both endpoints select each other. The lexicographically smaller hub key
 * owns the route. Bounded LRU caches affect performance only.
 */
public final class IntercityHighwayPlanner {

    private static final long VERSION_SALT = 0x494e544552563101L; // "INTERV1" + version byte
    private static final long HUB_SAMPLE_SALT = 0x5d39a74e10c26bf1L;
    private static final long HUB_POSITION_SALT = 0x27f4b8916d3a50ceL;
    private static final long HUB_STRENGTH_SALT = 0x618ad2e5073fb94cL;
    private static final long CONNECTION_RANK_SALT = 0x34ce791b582da6f0L;
    private static final long CONNECTION_ACCEPT_SALT = 0x72b514e90c63dfa8L;
    private static final long ROUTE_SHAPE_SALT = 0x19e8c347a65d2bf0L;
    private static final int POTENTIAL_SCALE = 1_000_000;
    private static final int ROUTE_PENALTY_SAMPLE_SPACING = 8;
    private static final int ENDPOINT_PENALTY_DISCOUNT = 16;

    private final long seed;
    private final long dimensionSalt;
    private final HighwayPlannerSettings settings;
    private final CityPotential cityPotential;
    private final HighwayLevelSource highwayLevelSource;
    private final HighwayHubPersistence hubPersistence;
    private final BoundedCache<HubKey, Optional<HighwayHub>> hubCache = new BoundedCache<>(4096);
    private final BoundedCache<HubKey, List<ConnectionCandidate>> candidateCache = new BoundedCache<>(2048);
    private final BoundedCache<HubKey, List<HubKey>> selectionCache = new BoundedCache<>(2048);
    private final BoundedCache<HubKey, List<HighwayRoute>> routeCache = new BoundedCache<>(2048);
    private final BoundedCache<ChunkKey, HighwayInfo> chunkCache = new BoundedCache<>(8192);
    private final AtomicLong uncachedChunkQueries = new AtomicLong();

    public IntercityHighwayPlanner(long seed, String dimensionId, HighwayPlannerSettings settings,
                                   CityPotential cityPotential) {
        this(seed, dimensionId, settings, cityPotential,
                (chunkX, chunkZ) -> settings.networkLevel(), HighwayHubPersistence.NONE);
    }

    public IntercityHighwayPlanner(long seed, String dimensionId, HighwayPlannerSettings settings,
                                   CityPotential cityPotential, HighwayHubPersistence hubPersistence) {
        this(seed, dimensionId, settings, cityPotential,
                (chunkX, chunkZ) -> settings.networkLevel(), hubPersistence);
    }

    public IntercityHighwayPlanner(long seed, String dimensionId, HighwayPlannerSettings settings,
                                   CityPotential cityPotential, HighwayLevelSource highwayLevelSource,
                                   HighwayHubPersistence hubPersistence) {
        this.seed = seed;
        this.dimensionSalt = stableStringHash(dimensionId);
        this.settings = settings;
        this.cityPotential = cityPotential;
        this.highwayLevelSource = highwayLevelSource;
        this.hubPersistence = hubPersistence;
    }

    public HighwayPlannerSettings settings() {
        return settings;
    }

    public HubKey getPlanningCell(int chunkX, int chunkZ) {
        return new HubKey(Math.floorDiv(chunkX, settings.planningCellSize()),
                Math.floorDiv(chunkZ, settings.planningCellSize()));
    }

    public Optional<HighwayHub> getHub(int planningCellX, int planningCellZ) {
        HubKey key = new HubKey(planningCellX, planningCellZ);
        return getHub(key);
    }

    public Optional<HighwayHub> getHub(HubKey key) {
        return hubCache.computeIfAbsent(key, this::loadOrCalculateHub);
    }

    private Optional<HighwayHub> loadOrCalculateHub(HubKey key) {
        HighwayHubPersistence.Lookup persisted = hubPersistence.get(key);
        if (persisted.known()) {
            return persisted.hub();
        }
        Optional<HighwayHub> calculated = calculateHub(key);
        hubPersistence.put(key, calculated);
        return calculated;
    }

    public List<HighwayHub> getNearbyHubs(int chunkX, int chunkZ) {
        HubKey center = getPlanningCell(chunkX, chunkZ);
        int radius = settings.hubSearchRadiusCells();
        List<HighwayHub> result = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                getHub(offset(center, dx, dz)).ifPresent(result::add);
            }
        }
        result.sort(Comparator.comparing(HighwayHub::key));
        return List.copyOf(result);
    }

    public List<ConnectionCandidate> getConnectionCandidates(HubKey source) {
        return candidateCache.computeIfAbsent(source, this::calculateCandidates);
    }

    public List<HubKey> getSelectedNeighbours(HubKey source) {
        return selectionCache.computeIfAbsent(source, this::calculateSelectedNeighbours);
    }

    public List<HighwayConnectionKey> getAcceptedConnections(HubKey hubKey) {
        Optional<HighwayHub> hub = getHub(hubKey);
        if (hub.isEmpty()) {
            return List.of();
        }
        List<HighwayConnectionKey> connections = new ArrayList<>();
        for (HubKey neighbour : getSelectedNeighbours(hubKey)) {
            if (getSelectedNeighbours(neighbour).contains(hubKey)) {
                connections.add(HighwayConnectionKey.of(hubKey, neighbour));
            }
        }
        connections.sort(Comparator.naturalOrder());
        return List.copyOf(connections);
    }

    public List<HighwayRoute> getOwnedRoutes(HubKey owner) {
        return routeCache.computeIfAbsent(owner, this::calculateOwnedRoutes);
    }

    public Optional<HighwayRoute> getRoute(HighwayConnectionKey key) {
        return getOwnedRoutes(key.owner()).stream().filter(route -> route.key().equals(key)).findFirst();
    }

    public HighwayInfo getHighwayInfo(int chunkX, int chunkZ) {
        ChunkKey key = new ChunkKey(chunkX, chunkZ);
        HighwayInfo cached = chunkCache.get(key);
        if (cached != null) {
            return cached;
        }
        HighwayInfo calculated = calculateHighwayInfo(chunkX, chunkZ);
        chunkCache.put(key, calculated);
        uncachedChunkQueries.incrementAndGet();
        return calculated;
    }

    public CacheStats getCacheStats() {
        return new CacheStats(hubCache.hits(), hubCache.misses(), chunkCache.hits(), chunkCache.misses(),
                uncachedChunkQueries.get());
    }

    public void clearCaches() {
        hubCache.clear();
        candidateCache.clear();
        selectionCache.clear();
        routeCache.clear();
        chunkCache.clear();
    }

    private Optional<HighwayHub> calculateHub(HubKey cell) {
        int size = settings.planningCellSize();
        int spacing = settings.hubSampleSpacing();
        int startX = Math.toIntExact((long) cell.planningCellX() * size);
        int startZ = Math.toIntExact((long) cell.planningCellZ() * size);
        int offsetX = floorModHash(hash(HUB_POSITION_SALT, cell.planningCellX(), cell.planningCellZ(), 0), spacing);
        int offsetZ = floorModHash(hash(HUB_POSITION_SALT, cell.planningCellX(), cell.planningCellZ(), 1), spacing);
        HighwayHub best = null;
        long bestTie = 0L;
        for (int localX = offsetX; localX < size; localX += spacing) {
            for (int localZ = offsetZ; localZ < size; localZ += spacing) {
                int chunkX = moveHubXOffRailwayCorridor(Math.addExact(startX, localX), localX, size);
                int chunkZ = moveHubZOffRailwayCorridor(Math.addExact(startZ, localZ), localZ, size);
                int potentialScore = potentialScore(chunkX, chunkZ);
                long tie = hash(HUB_SAMPLE_SALT, chunkX, chunkZ,
                        floorModHash(hash(HUB_STRENGTH_SALT, cell.planningCellX(), cell.planningCellZ(), 0), Integer.MAX_VALUE));
                if (best == null || potentialScore > best.potentialScore()
                        || potentialScore == best.potentialScore() && Long.compareUnsigned(tie, bestTie) < 0) {
                    best = new HighwayHub(cell, chunkX, chunkZ, potentialScore, 0);
                    bestTie = tie;
                }
            }
        }
        int minimum = Math.round(settings.hubMinimumPotential() * POTENTIAL_SCALE);
        if (best == null || best.potentialScore() < minimum) {
            return Optional.empty();
        }
        return Optional.of(new HighwayHub(best.key(), best.chunkX(), best.chunkZ(), best.potentialScore(),
                highwayLevelSource.getCityLevel(best.chunkX(), best.chunkZ())));
    }

    private List<ConnectionCandidate> calculateCandidates(HubKey sourceKey) {
        Optional<HighwayHub> sourceOptional = getHub(sourceKey);
        if (sourceOptional.isEmpty()) {
            return List.of();
        }
        HighwayHub source = sourceOptional.get();
        int radius = settings.hubSearchRadiusCells();
        long minimumSquared = square(settings.minimumHubDistance());
        long maximumSquared = square(settings.maximumHubDistance());
        List<ConnectionCandidate> candidates = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                HubKey targetKey = offset(sourceKey, dx, dz);
                if (targetKey.equals(sourceKey)) {
                    continue;
                }
                Optional<HighwayHub> targetOptional = getHub(targetKey);
                if (targetOptional.isEmpty()) {
                    continue;
                }
                HighwayHub target = targetOptional.get();
                long deltaX = (long) target.chunkX() - source.chunkX();
                long deltaZ = (long) target.chunkZ() - source.chunkZ();
                long distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
                int routeLength = Math.toIntExact(Math.abs(deltaX) + Math.abs(deltaZ));
                if (distanceSquared < minimumSquared || distanceSquared > maximumSquared
                        || routeLength < settings.minimumRouteLength()
                        || !hasRailwayClearRoute(source, target)) {
                    continue;
                }
                HighwayConnectionKey key = HighwayConnectionKey.of(sourceKey, targetKey);
                long rank = hash(CONNECTION_RANK_SALT, key.first().planningCellX(), key.first().planningCellZ(),
                        pairExtra(key.second()));
                long acceptanceTie = hash(CONNECTION_ACCEPT_SALT, key.first().planningCellX(), key.first().planningCellZ(),
                        pairExtra(key.second()));
                candidates.add(new ConnectionCandidate(source, target, key, sector(source, target),
                        distanceSquared, routeLength, rank, acceptanceTie));
            }
        }
        candidates.sort(candidateComparator());
        return List.copyOf(candidates);
    }

    private List<HubKey> calculateSelectedNeighbours(HubKey source) {
        List<ConnectionCandidate> candidates = getConnectionCandidates(source);
        if (candidates.isEmpty()) {
            return List.of();
        }
        int maximum = settings.maximumConnectionsPerHub();
        List<HubKey> selected = new ArrayList<>(maximum);
        Set<HighwaySector> usedSectors = EnumSet.noneOf(HighwaySector.class);
        for (ConnectionCandidate candidate : candidates) {
            if (usedSectors.add(candidate.sector())) {
                selected.add(candidate.target().key());
                if (selected.size() == maximum) {
                    return List.copyOf(selected);
                }
            }
        }
        for (ConnectionCandidate candidate : candidates) {
            HubKey target = candidate.target().key();
            if (!selected.contains(target)) {
                selected.add(target);
                if (selected.size() == maximum) {
                    break;
                }
            }
        }
        return List.copyOf(selected);
    }

    private List<HighwayRoute> calculateOwnedRoutes(HubKey owner) {
        Optional<HighwayHub> ownerHub = getHub(owner);
        if (ownerHub.isEmpty()) {
            return List.of();
        }
        List<HighwayRoute> routes = new ArrayList<>();
        for (HubKey neighbour : getSelectedNeighbours(owner)) {
            HighwayConnectionKey key = HighwayConnectionKey.of(owner, neighbour);
            if (!key.owner().equals(owner) || !getSelectedNeighbours(neighbour).contains(owner)) {
                continue;
            }
            HighwayHub target = getHub(neighbour).orElseThrow();
            routes.add(createRoute(key, ownerHub.get(), target));
        }
        routes.sort(Comparator.comparing(HighwayRoute::key));
        return List.copyOf(routes);
    }

    private HighwayRoute createRoute(HighwayConnectionKey key, HighwayHub a, HighwayHub b) {
        HighwayHub first = a.key().equals(key.first()) ? a : b;
        HighwayHub second = a.key().equals(key.second()) ? a : b;
        int length = Math.abs(second.chunkX() - first.chunkX()) + Math.abs(second.chunkZ() - first.chunkZ());
        int level = selectHighwayLevel(first, second);
        if (first.chunkZ() == second.chunkZ()) {
            HighwaySegment segment = new HighwaySegment(first.chunkX(), first.chunkZ(), second.chunkX(), second.chunkZ(), HighwayAxis.X);
            return new HighwayRoute(key, first, second, List.of(segment), level, HighwayRoute.RouteShape.STRAIGHT, length, 0);
        }
        if (first.chunkX() == second.chunkX()) {
            HighwaySegment segment = new HighwaySegment(first.chunkX(), first.chunkZ(), second.chunkX(), second.chunkZ(), HighwayAxis.Z);
            return new HighwayRoute(key, first, second, List.of(segment), level, HighwayRoute.RouteShape.STRAIGHT, length, 0);
        }

        List<HighwaySegment> horizontalFirst = List.of(
                new HighwaySegment(first.chunkX(), first.chunkZ(), second.chunkX(), first.chunkZ(), HighwayAxis.X),
                new HighwaySegment(second.chunkX(), first.chunkZ(), second.chunkX(), second.chunkZ(), HighwayAxis.Z));
        List<HighwaySegment> verticalFirst = List.of(
                new HighwaySegment(first.chunkX(), first.chunkZ(), first.chunkX(), second.chunkZ(), HighwayAxis.Z),
                new HighwaySegment(first.chunkX(), second.chunkZ(), second.chunkX(), second.chunkZ(), HighwayAxis.X));
        boolean horizontalClear = isRailwayClear(horizontalFirst);
        boolean verticalClear = isRailwayClear(verticalFirst);
        long horizontalPenalty = routePenalty(horizontalFirst, first, second);
        long verticalPenalty = routePenalty(verticalFirst, first, second);
        boolean chooseHorizontal;
        if (horizontalClear != verticalClear) {
            chooseHorizontal = horizontalClear;
        } else if (horizontalPenalty != verticalPenalty) {
            chooseHorizontal = horizontalPenalty < verticalPenalty;
        } else {
            chooseHorizontal = (hash(ROUTE_SHAPE_SALT, key.first().planningCellX(), key.first().planningCellZ(),
                    pairExtra(key.second())) & 1L) == 0L;
        }
        return new HighwayRoute(key, first, second,
                chooseHorizontal ? horizontalFirst : verticalFirst,
                level,
                chooseHorizontal ? HighwayRoute.RouteShape.HORIZONTAL_THEN_VERTICAL : HighwayRoute.RouteShape.VERTICAL_THEN_HORIZONTAL,
                length,
                chooseHorizontal ? horizontalPenalty : verticalPenalty);
    }

    private int selectHighwayLevel(HighwayHub first, HighwayHub second) {
        return switch (settings.levelFromCitiesMode()) {
            case 0 -> first.cityLevel();
            case 1 -> Math.min(first.cityLevel(), second.cityLevel());
            case 2 -> Math.max(first.cityLevel(), second.cityLevel());
            case 3 -> (first.cityLevel() + second.cityLevel()) / 2;
            case 4 -> settings.networkLevel();
            default -> throw new IllegalStateException("Unsupported highway level mode "
                    + settings.levelFromCitiesMode());
        };
    }

    private boolean hasRailwayClearRoute(HighwayHub first, HighwayHub second) {
        if (first.chunkZ() == second.chunkZ()) {
            return isRailwayClear(List.of(new HighwaySegment(first.chunkX(), first.chunkZ(),
                    second.chunkX(), second.chunkZ(), HighwayAxis.X)));
        }
        if (first.chunkX() == second.chunkX()) {
            return isRailwayClear(List.of(new HighwaySegment(first.chunkX(), first.chunkZ(),
                    second.chunkX(), second.chunkZ(), HighwayAxis.Z)));
        }
        List<HighwaySegment> horizontalFirst = List.of(
                new HighwaySegment(first.chunkX(), first.chunkZ(), second.chunkX(), first.chunkZ(), HighwayAxis.X),
                new HighwaySegment(second.chunkX(), first.chunkZ(), second.chunkX(), second.chunkZ(), HighwayAxis.Z));
        List<HighwaySegment> verticalFirst = List.of(
                new HighwaySegment(first.chunkX(), first.chunkZ(), first.chunkX(), second.chunkZ(), HighwayAxis.Z),
                new HighwaySegment(first.chunkX(), second.chunkZ(), second.chunkX(), second.chunkZ(), HighwayAxis.X));
        return isRailwayClear(horizontalFirst) || isRailwayClear(verticalFirst);
    }

    /**
     * Subways occupy a fixed ten-chunk grid. Crossing one of those lines is safe because the
     * railway is underground, but sharing its chunk line or crossing a surface-access station
     * can make railway parts replace the highway. Keep parallel highway segments off railway
     * corridors and vertical highways off the columns containing stations.
     */
    private static boolean isRailwayClear(List<HighwaySegment> segments) {
        for (HighwaySegment segment : segments) {
            if (segment.axis() == HighwayAxis.X && isHorizontalRailwayCorridor(segment.startZ())) {
                return false;
            }
            if (segment.axis() == HighwayAxis.Z && isVerticalRailwayOrStationCorridor(segment.startX())) {
                return false;
            }
        }
        return true;
    }

    private static int moveHubXOffRailwayCorridor(int chunkX, int localX, int cellSize) {
        if (!isVerticalRailwayOrStationCorridor(chunkX)) {
            return chunkX;
        }
        return localX + 1 < cellSize ? Math.addExact(chunkX, 1) : Math.subtractExact(chunkX, 1);
    }

    private static int moveHubZOffRailwayCorridor(int chunkZ, int localZ, int cellSize) {
        if (!isHorizontalRailwayCorridor(chunkZ)) {
            return chunkZ;
        }
        return localZ + 1 < cellSize ? Math.addExact(chunkZ, 1) : Math.subtractExact(chunkZ, 1);
    }

    private static boolean isHorizontalRailwayCorridor(int chunkZ) {
        return Math.floorMod(chunkZ + 1, 10) == 0;
    }

    private static boolean isVerticalRailwayOrStationCorridor(int chunkX) {
        int gridX = Math.floorMod(chunkX + 1, 10);
        return gridX == 5 || gridX == 0;
    }

    /** Builds canonical V1 geometry for an already accepted hub pair. */
    public HighwayRoute createRoute(HighwayHub first, HighwayHub second) {
        return createRoute(HighwayConnectionKey.of(first.key(), second.key()), first, second);
    }

    private long routePenalty(List<HighwaySegment> segments, HighwayHub first, HighwayHub second) {
        if (settings.routeCityPenalty() == 0.0f) {
            return 0L;
        }
        long sum = 0L;
        for (HighwaySegment segment : segments) {
            int length = segment.length();
            for (int distance = 0; distance <= length; distance += ROUTE_PENALTY_SAMPLE_SPACING) {
                int chunkX = interpolate(segment.startX(), segment.endX(), distance, length);
                int chunkZ = interpolate(segment.startZ(), segment.endZ(), distance, length);
                if (manhattan(chunkX, chunkZ, first.chunkX(), first.chunkZ()) <= ENDPOINT_PENALTY_DISCOUNT
                        || manhattan(chunkX, chunkZ, second.chunkX(), second.chunkZ()) <= ENDPOINT_PENALTY_DISCOUNT) {
                    continue;
                }
                sum += potentialScore(chunkX, chunkZ);
            }
        }
        return Math.round(sum * (double) settings.routeCityPenalty());
    }

    private HighwayInfo calculateHighwayInfo(int chunkX, int chunkZ) {
        HubKey queryCell = getPlanningCell(chunkX, chunkZ);
        int radius = settings.hubSearchRadiusCells();
        Map<HighwayConnectionKey, HighwayRoute> routes = new TreeMap<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (HighwayRoute route : getOwnedRoutes(offset(queryCell, dx, dz))) {
                    if (route.contains(chunkX, chunkZ)) {
                        routes.put(route.key(), route);
                    }
                }
            }
        }
        if (routes.isEmpty()) {
            return HighwayInfo.NONE;
        }
        int xLevel = -1;
        int zLevel = -1;
        List<HighwayInfo.RouteHit> hits = new ArrayList<>();
        for (HighwayRoute route : routes.values()) {
            boolean xAxis = false;
            boolean zAxis = false;
            for (HighwaySegment segment : route.segments()) {
                if (segment.contains(chunkX, chunkZ)) {
                    xAxis |= segment.axis() == HighwayAxis.X;
                    zAxis |= segment.axis() == HighwayAxis.Z;
                }
            }
            if (xAxis) {
                xLevel = xLevel < 0 ? route.highwayLevel() : Math.min(xLevel, route.highwayLevel());
            }
            if (zAxis) {
                zLevel = zLevel < 0 ? route.highwayLevel() : Math.min(zLevel, route.highwayLevel());
            }
            hits.add(new HighwayInfo.RouteHit(route, xAxis, zAxis, xAxis && zAxis));
        }
        HighwayInfo.Classification classification;
        if (xLevel >= 0 && zLevel >= 0) {
            classification = xLevel == zLevel ? HighwayInfo.Classification.SAME_LEVEL_INTERSECTION
                    : HighwayInfo.Classification.MULTI_LEVEL_INTERSECTION;
        } else {
            classification = xLevel >= 0 ? HighwayInfo.Classification.X_HIGHWAY : HighwayInfo.Classification.Z_HIGHWAY;
        }
        return new HighwayInfo(xLevel, zLevel, classification, hits);
    }

    private int potentialScore(int chunkX, int chunkZ) {
        float potential = Math.min(Math.max(cityPotential.getPotential(chunkX, chunkZ), 0.0f), 1.0f);
        return Math.round(potential * POTENTIAL_SCALE);
    }

    private Comparator<ConnectionCandidate> candidateComparator() {
        return (first, second) -> {
            int comparison = Long.compare(first.distanceSquared(), second.distanceSquared());
            if (comparison != 0) {
                return comparison;
            }
            comparison = Integer.compare(second.target().potentialScore(), first.target().potentialScore());
            if (comparison != 0) {
                return comparison;
            }
            comparison = Long.compareUnsigned(first.rankTieBreaker(), second.rankTieBreaker());
            if (comparison != 0) {
                return comparison;
            }
            comparison = Long.compareUnsigned(first.acceptanceTieBreaker(), second.acceptanceTieBreaker());
            return comparison != 0 ? comparison : first.target().key().compareTo(second.target().key());
        };
    }

    private static HighwaySector sector(HighwayHub source, HighwayHub target) {
        long dx = (long) target.chunkX() - source.chunkX();
        long dz = (long) target.chunkZ() - source.chunkZ();
        if (Math.abs(dx) >= Math.abs(dz)) {
            return dx >= 0 ? HighwaySector.EAST : HighwaySector.WEST;
        }
        return dz >= 0 ? HighwaySector.SOUTH : HighwaySector.NORTH;
    }

    private long hash(long streamSalt, int x, int z, int extra) {
        long value = seed ^ dimensionSalt ^ VERSION_SALT ^ streamSalt;
        value ^= mix64(Integer.toUnsignedLong(x) + 0x9e3779b97f4a7c15L);
        value ^= mix64(Integer.toUnsignedLong(z) + 0xc2b2ae3d27d4eb4fL);
        value ^= mix64(Integer.toUnsignedLong(extra) + 0x165667b19e3779f9L);
        return mix64(value);
    }

    private static int pairExtra(HubKey key) {
        long mixed = mix64(Integer.toUnsignedLong(key.planningCellX())
                ^ Long.rotateLeft(Integer.toUnsignedLong(key.planningCellZ()), 32));
        return (int) (mixed ^ mixed >>> 32);
    }

    private static HubKey offset(HubKey key, int dx, int dz) {
        return new HubKey(Math.addExact(key.planningCellX(), dx), Math.addExact(key.planningCellZ(), dz));
    }

    private static long square(int value) {
        return (long) value * value;
    }

    private static int interpolate(int start, int end, int distance, int length) {
        if (length == 0) {
            return start;
        }
        return start + Integer.signum(end - start) * distance;
    }

    private static int manhattan(int x1, int z1, int x2, int z2) {
        return Math.toIntExact(Math.abs((long) x2 - x1) + Math.abs((long) z2 - z1));
    }

    private static int floorModHash(long hash, int modulus) {
        return (int) Math.floorMod(hash, (long) modulus);
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

    public record ConnectionCandidate(
            HighwayHub source,
            HighwayHub target,
            HighwayConnectionKey key,
            HighwaySector sector,
            long distanceSquared,
            int routeLength,
            long rankTieBreaker,
            long acceptanceTieBreaker
    ) { }

    public record CacheStats(long hubHits, long hubMisses, long chunkHits, long chunkMisses,
                             long uncachedChunkQueries) { }

    private record ChunkKey(int chunkX, int chunkZ) { }

    private static final class BoundedCache<K, V> {
        private final int maximumSize;
        private final LinkedHashMap<K, V> values;
        private long hits;
        private long misses;

        private BoundedCache(int maximumSize) {
            this.maximumSize = maximumSize;
            values = new LinkedHashMap<>(16, .75f, true);
        }

        synchronized V get(K key) {
            V value = values.get(key);
            if (value == null) {
                misses++;
            } else {
                hits++;
            }
            return value;
        }

        synchronized void put(K key, V value) {
            values.put(key, value);
            trim();
        }

        synchronized V computeIfAbsent(K key, Function<K, V> factory) {
            V value = values.get(key);
            if (value != null) {
                hits++;
                return value;
            }
            misses++;
            value = factory.apply(key);
            values.put(key, value);
            trim();
            return value;
        }

        synchronized void clear() {
            values.clear();
            hits = 0;
            misses = 0;
        }

        synchronized long hits() {
            return hits;
        }

        synchronized long misses() {
            return misses;
        }

        private void trim() {
            while (values.size() > maximumSize) {
                K eldest = values.keySet().iterator().next();
                values.remove(eldest);
            }
        }
    }
}
