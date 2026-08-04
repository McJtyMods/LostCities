package mcjty.lostcities.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.LegacyRandomSource;

/**
 * Scratch state belonging to one chunk-generation call. Keeping this state thread-local lets
 * deeply nested generation helpers use it without storing mutable state in shared palettes.
 */
public final class GenerationContext {

    private static final ThreadLocal<GenerationContext> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<GenerationContext> FALLBACK =
            ThreadLocal.withInitial(() -> new GenerationContext(123456789));
    private static final ThreadLocal<ScratchBuffers> REUSABLE_BUFFERS =
            ThreadLocal.withInitial(ScratchBuffers::new);

    private int paletteSeed;
    private final ScratchBuffers buffers;
    private final WorldGenLevel world;
    private char street;

    private GenerationContext(int paletteSeed) {
        this(paletteSeed, new ScratchBuffers(), null);
    }

    private GenerationContext(int paletteSeed, ScratchBuffers buffers, WorldGenLevel world) {
        this.paletteSeed = paletteSeed;
        this.buffers = buffers;
        this.world = world;
    }

    public static Scope open(WorldGenLevel world, long worldSeed, ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        GenerationContext previous = CURRENT.get();
        ScratchBuffers buffers = previous == null ? REUSABLE_BUFFERS.get() : new ScratchBuffers();
        long seed = mixSeed(worldSeed, dimension, chunkX, chunkZ);
        buffers.random.setSeed(seed);
        CURRENT.set(new GenerationContext((int) (seed ^ (seed >>> 32)), buffers, world));
        return new Scope(previous);
    }

    static GenerationContext current() {
        GenerationContext context = CURRENT.get();
        if (context == null) {
            throw new IllegalStateException("Lost Cities generation context is not active");
        }
        return context;
    }

    static WorldGenLevel currentWorld() {
        GenerationContext context = CURRENT.get();
        return context == null ? null : context.world;
    }

    public static int nextPaletteIndex() {
        GenerationContext context = CURRENT.get();
        // Palette lookups are also used by editor and command code outside world generation.
        // Keep those callers functional without reintroducing one shared mutable sequence.
        return (context == null ? FALLBACK.get() : context).next128();
    }

    private int next128() {
        paletteSeed = 214013 * paletteSeed + 2531011;
        return (paletteSeed >> 16) & 0x7f;
    }

    char street() {
        return street;
    }

    void setStreet(char street) {
        this.street = street;
    }

    double[] rubbleBuffer() {
        return buffers.rubble;
    }

    void setRubbleBuffer(double[] rubble) {
        buffers.rubble = rubble;
    }

    double[] leavesBuffer() {
        return buffers.leaves;
    }

    void setLeavesBuffer(double[] leaves) {
        buffers.leaves = leaves;
    }

    double[] ruinBuffer() {
        return buffers.ruin;
    }

    void setRuinBuffer(double[] ruin) {
        buffers.ruin = ruin;
    }

    double[] bottomLayerBuffer() {
        return buffers.bottomLayer;
    }

    void setBottomLayerBuffer(double[] bottomLayer) {
        buffers.bottomLayer = bottomLayer;
    }

    RandomSource random() {
        return buffers.random;
    }

    ChunkDriver driver() {
        return buffers.driver;
    }

    private static long mixSeed(long worldSeed, ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        long mixed = worldSeed;
        mixed ^= (long) dimension.location().hashCode() * 0x9e3779b97f4a7c15L;
        mixed ^= (long) chunkX * 257017164707L;
        mixed ^= (long) chunkZ * 101754694003L;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdl;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;
        return mixed;
    }

    public static final class Scope implements AutoCloseable {
        private final GenerationContext previous;
        private boolean closed;

        private Scope(GenerationContext previous) {
            this.previous = previous;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }

    private static final class ScratchBuffers {
        private double[] rubble = new double[256];
        private double[] leaves = new double[256];
        private double[] ruin = new double[256];
        private double[] bottomLayer = new double[256];
        private final RandomSource random = new LegacyRandomSource(0L);
        private final ChunkDriver driver = new ChunkDriver();
    }
}
