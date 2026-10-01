package mcjty.lostcities.worldgen.highway;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

public final class HighwayPlanningService implements AutoCloseable {
    private static final int HUB_CACHE_SIZE = 4096;
    private static final int DEPENDENCY_CACHE_SIZE = 2048;

    private final IntercityHighwayPlanner planner;
    private final Executor executor;

    private final BoundedFutureCache<HubKey, Optional<HighwayHub>> hubFutures = new BoundedFutureCache<>(HUB_CACHE_SIZE);
    private final BoundedFutureCache<HubKey, List<IntercityHighwayPlanner.ConnectionCandidate>> candidateFutures = new BoundedFutureCache<>(DEPENDENCY_CACHE_SIZE);
    private final BoundedFutureCache<HubKey, List<HubKey>> selectionFutures = new BoundedFutureCache<>(DEPENDENCY_CACHE_SIZE);
    private final BoundedFutureCache<HubKey, List<HighwayRoute>> routeFutures = new BoundedFutureCache<>(DEPENDENCY_CACHE_SIZE);
    private final BoundedFutureCache<HubKey, Void> prepareFutures = new BoundedFutureCache<>(DEPENDENCY_CACHE_SIZE);

    private final LongAdder runningTasks = new LongAdder();
    private final LongAdder completedTasks = new LongAdder();
    private final LongAdder submittedTasks = new LongAdder();

    public HighwayPlanningService(IntercityHighwayPlanner planner) {
        this.planner = planner;
        this.executor = new ForkJoinPool(Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
    }

    public CompletableFuture<Optional<HighwayHub>> getHub(HubKey key) {
        return hubFutures.computeIfAbsent(key, this::scheduleHub);
    }

    private CompletableFuture<Optional<HighwayHub>> scheduleHub(HubKey key) {
        return submit(() -> planner.getHub(key));
    }

    public HighwayInfo getHighwayInfo(int chunkX, int chunkZ) {
        prepare(chunkX, chunkZ).join();
        return planner.getHighwayInfo(chunkX, chunkZ);
    }

    public CompletableFuture<Void> prepare(int chunkX, int chunkZ) {
        return prepareFutures.computeIfAbsent(planner.getPlanningCell(chunkX, chunkZ), this::schedulePrepare);
    }

    private CompletableFuture<Void> schedulePrepare(HubKey center) {
        int radius = planner.settings().hubSearchRadiusCells();
        List<CompletableFuture<List<HighwayRoute>>> routes = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                HubKey owner = new HubKey(center.planningCellX() + dx, center.planningCellZ() + dz);
                routes.add(getOwnedRoutes(owner));
            }
        }

        return CompletableFuture.allOf(routes.toArray(CompletableFuture[]::new));
    }

    public CompletableFuture<List<HighwayHub>> prepareHubs(int chunkX, int chunkZ) {
        HubKey center = planner.getPlanningCell(chunkX, chunkZ);
        int radius = planner.settings().hubSearchRadiusCells();

        List<CompletableFuture<Optional<HighwayHub>>> futures = new ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                HubKey key = new HubKey(center.planningCellX() + dx, center.planningCellZ() + dz);
                futures.add(getHub(key));
            }
        }

        CompletableFuture<?>[] all = futures.toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(all)
                .thenApply(ignored -> futures.stream()
                        .map(CompletableFuture::join)
                        .flatMap(Optional::stream)
                        .sorted()
                        .toList());
    }

    public CompletableFuture<List<IntercityHighwayPlanner.ConnectionCandidate>> getCandidates(HubKey source) {
        return candidateFutures.computeIfAbsent(
                source,
                key -> prepareCandidateHubs(key).thenApplyAsync(
                        ignored -> planner.getConnectionCandidates(key),
                        executor
                )
        );
    }

    private CompletableFuture<Void> prepareCandidateHubs(HubKey source) {
        return getHub(source).thenCompose(hub -> {
            if (hub.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }
            int radius = planner.settings().hubSearchRadiusCells();
            List<CompletableFuture<Optional<HighwayHub>>> neighbours = new ArrayList<>();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx != 0 || dz != 0) {
                        neighbours.add(getHub(new HubKey(source.planningCellX() + dx, source.planningCellZ() + dz)));
                    }
                }
            }
            return CompletableFuture.allOf(neighbours.toArray(CompletableFuture[]::new));
        });
    }

    public CompletableFuture<List<HubKey>> getSelectedNeighbours(HubKey source) {
        return selectionFutures.computeIfAbsent(
                source,
                key -> getCandidates(key).thenApplyAsync(
                        ignored -> planner.getSelectedNeighbours(key),
                        executor
                )
        );
    }

    public CompletableFuture<List<HighwayRoute>> getOwnedRoutes(HubKey owner) {
        return routeFutures.computeIfAbsent(
                owner,
                key -> getSelectedNeighbours(key).thenCompose(neighbours -> {
                    List<CompletableFuture<List<HubKey>>> reciprocal = new ArrayList<>();
                    for (HubKey neighbour : neighbours) {
                        if (key.compareTo(neighbour) < 0) {
                            reciprocal.add(getSelectedNeighbours(neighbour));
                        }
                    }
                    return CompletableFuture.allOf(reciprocal.toArray(CompletableFuture[]::new));
                }).thenApplyAsync(
                        ignored -> planner.getOwnedRoutes(key),
                        executor
                )
        );
    }

    private <T> CompletableFuture<T> submit(Supplier<T> supplier) {
        submittedTasks.increment();

        return CompletableFuture.supplyAsync(() -> {
            runningTasks.increment();
            try {
                return supplier.get();
            } finally {
                runningTasks.decrement();
                completedTasks.increment();
            }
        }, executor);
    }

    public void clear() {
        prepareFutures.clear();
        hubFutures.clear();
        candidateFutures.clear();
        selectionFutures.clear();
        routeFutures.clear();

        planner.clearCaches();
    }

    @Override
    public void close() {
        if (executor instanceof ForkJoinPool pool) {
            pool.shutdown();
        }
    }

    public String getParallelismStats() {
        if (executor instanceof ForkJoinPool pool) {
            return "Parallelism: " + pool.getParallelism()
                    + ", active: " + pool.getActiveThreadCount()
                    + ", running: " + pool.getRunningThreadCount()
                    + ", queued: " + pool.getQueuedTaskCount()
                    + ", submissions: " + pool.getQueuedSubmissionCount()
                    + ", runningTasks: " + runningTasks.sum()
                    + ", completedTasks: " + completedTasks.sum()
                    + ", submittedTasks: " + submittedTasks.sum()
                    + ", cachedFutures: " + cachedFutureCount();
        }

        return "Running tasks: " + runningTasks.sum()
                + ", completed tasks: " + completedTasks.sum()
                + ", submitted tasks: " + submittedTasks.sum()
                + ", cached futures: " + cachedFutureCount();
    }

    private int cachedFutureCount() {
        return hubFutures.size() + candidateFutures.size() + selectionFutures.size()
                + routeFutures.size() + prepareFutures.size();
    }
}
