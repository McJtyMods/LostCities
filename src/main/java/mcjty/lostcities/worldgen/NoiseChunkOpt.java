package mcjty.lostcities.worldgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.DensityFunctions.Marker;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoiseChunkOpt implements DensityFunction.ContextProvider, DensityFunction.FunctionContext {
    private final NoiseSettings noiseSettings;
    final int cellCountXZ;
    final int cellCountY;
    final int cellNoiseMinY;
    private final int firstCellX;
    private final int firstCellZ;
    final int firstNoiseX;
    final int firstNoiseZ;
    final List<NoiseChunkOpt.NoiseInterpolator> interpolators;
    final List<NoiseChunkOpt.CacheAllInCell> cellCaches;
    private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();
    private final Long2IntMap preliminarySurfaceLevel = new Long2IntOpenHashMap();
    private final Aquifer aquifer;
    private final DensityFunction initialDensityNoJaggedness;
    private final NoiseChunkOpt.BlockStateFiller blockStateRule;
    private final DensityFunctions.BeardifierOrMarker beardifier;
    final int noiseSizeXZ;
    final int cellWidth;
    final int cellHeight;
    boolean interpolating;
    boolean fillingCell;
    private int cellStartBlockX;
    int cellStartBlockY;
    private int cellStartBlockZ;
    int inCellX;
    int inCellY;
    int inCellZ;
    long interpolationCounter;
    long arrayInterpolationCounter;
    int arrayIndex;
    private final DensityFunction.ContextProvider sliceFillingContextProvider;

    public NoiseChunkOpt(int pCellCountXZ, RandomState pRandom, int pFirstNoiseX, int pFirstNoiseZ, NoiseSettings pNoiseSettings, DensityFunctions.BeardifierOrMarker pBeardifier, NoiseGeneratorSettings pNoiseGeneratorSettings, NoiseChunkOpt.FluidStatusV fluidStatus) {
        sliceFillingContextProvider = new DensityFunction.ContextProvider() {
            public DensityFunction.FunctionContext forIndex(int p_209253_) {
                NoiseChunkOpt.this.cellStartBlockY = (p_209253_ + NoiseChunkOpt.this.cellNoiseMinY) * NoiseChunkOpt.this.cellHeight;
                ++NoiseChunkOpt.this.interpolationCounter;
                NoiseChunkOpt.this.inCellY = 0;
                NoiseChunkOpt.this.arrayIndex = p_209253_;
                return NoiseChunkOpt.this;
            }

            public void fillAllDirectly(double[] p_209255_, DensityFunction p_209256_) {
                for (int i2 = 0; i2 < NoiseChunkOpt.this.cellCountY + 1; ++i2) {
                    NoiseChunkOpt.this.cellStartBlockY = (i2 + NoiseChunkOpt.this.cellNoiseMinY) * NoiseChunkOpt.this.cellHeight;
                    ++NoiseChunkOpt.this.interpolationCounter;
                    NoiseChunkOpt.this.inCellY = 0;
                    NoiseChunkOpt.this.arrayIndex = i2;
                    p_209255_[i2] = p_209256_.compute(NoiseChunkOpt.this);
                }

            }
        };
        this.noiseSettings = pNoiseSettings;
        this.cellWidth = pNoiseSettings.getCellWidth();
        this.cellHeight = pNoiseSettings.getCellHeight();
        this.cellCountXZ = pCellCountXZ;
        this.cellCountY = Mth.floorDiv(pNoiseSettings.height(), this.cellHeight);
        this.cellNoiseMinY = Mth.floorDiv(pNoiseSettings.minY(), this.cellHeight);
        this.firstCellX = Math.floorDiv(pFirstNoiseX, this.cellWidth);
        this.firstCellZ = Math.floorDiv(pFirstNoiseZ, this.cellWidth);
        this.interpolators = Lists.newArrayList();
        this.cellCaches = Lists.newArrayList();
        this.firstNoiseX = QuartPos.fromBlock(pFirstNoiseX);
        this.firstNoiseZ = QuartPos.fromBlock(pFirstNoiseZ);
        this.noiseSizeXZ = QuartPos.fromBlock(pCellCountXZ * this.cellWidth);
        this.beardifier = pBeardifier;

        NoiseRouter noiserouter = pRandom.router();
        NoiseRouter noiserouter1 = new NoiseRouter(
                noiserouter.barrierNoise().mapAll(this::wrap),
                noiserouter.fluidLevelFloodednessNoise().mapAll(this::wrap),
                noiserouter.fluidLevelSpreadNoise().mapAll(this::wrap),
                null,//noiserouter.lavaNoise().mapAll(this::wrap),
                null,//noiserouter.temperature().mapAll(this::wrap),
                null,//noiserouter.vegetation().mapAll(this::wrap),
                null, //noiserouter.continents().mapAll(this::wrap),
                noiserouter.erosion().mapAll(this::wrap),
                noiserouter.depth().mapAll(this::wrap),
                null, //noiserouter.ridges().mapAll(this::wrap),
                noiserouter.preliminarySurfaceLevel().mapAll(this::wrap),
                noiserouter.finalDensity().mapAll(this::wrap),
                null, //noiserouter.veinToggle().mapAll(this::wrap),
                null, //noiserouter.veinRidged().mapAll(this::wrap),
                null //noiserouter.veinGap().mapAll(this::wrap)
        );
        if (!pNoiseGeneratorSettings.isAquifersEnabled()) {
            this.aquifer = new Aquifer() {
                @Nullable
                public BlockState computeSubstance(DensityFunction.FunctionContext context, double substance) {
                    return substance > 0.0D ? null : fluidStatus.at(context.blockY());
                }

                public boolean shouldScheduleFluidUpdate() {
                    return false;
                }
            };
        } else {
            int sx = SectionPos.blockToSectionCoord(pFirstNoiseX);
            int sz = SectionPos.blockToSectionCoord(pFirstNoiseZ);
            this.aquifer = new NoiseBasedAquifer(this, new ChunkPos(sx, sz), noiserouter1, pRandom.aquiferRandom(), pNoiseSettings.minY(), pNoiseSettings.height(), fluidStatus);
        }

        ImmutableList.Builder<NoiseChunkOpt.BlockStateFiller> builder = ImmutableList.builder();
        DensityFunction densityfunction = DensityFunctions.cacheAllInCell(DensityFunctions.add(noiserouter1.finalDensity(), BeardifierMarker.INSTANCE)).mapAll(this::wrap);
        builder.add((context) -> this.aquifer.computeSubstance(context, densityfunction.compute(context)));

        this.blockStateRule = new MaterialRuleList(builder.build());
        this.initialDensityNoJaggedness = noiserouter1.preliminarySurfaceLevel();
    }

    public int preliminarySurfaceLevel(int pX, int pZ) {
        int i = QuartPos.toBlock(QuartPos.fromBlock(pX));
        int j = QuartPos.toBlock(QuartPos.fromBlock(pZ));
        return this.preliminarySurfaceLevel.computeIfAbsent(ColumnPos.asLong(i, j), this::computePreliminarySurfaceLevel);
    }

    private int computePreliminarySurfaceLevel(long p_198250_) {
        int i = ColumnPos.getX(p_198250_);
        int j = ColumnPos.getZ(p_198250_);
        int k = this.noiseSettings.minY();

        for (int l = k + this.noiseSettings.height(); l >= k; l -= this.cellHeight) {
            if (this.initialDensityNoJaggedness.compute(new DensityFunction.SinglePointContext(i, l, j)) > 0.390625D) {
                return l;
            }
        }

        return Integer.MAX_VALUE;
    }


    @Nullable
    public BlockState getInterpolatedState() {
        return this.blockStateRule.calculate(this);
    }

    @Override
    public int blockX() {
        return this.cellStartBlockX + this.inCellX;
    }

    @Override
    public int blockY() {
        return this.cellStartBlockY + this.inCellY;
    }

    @Override
    public int blockZ() {
        return this.cellStartBlockZ + this.inCellZ;
    }

    @Override
    public Blender getBlender() {
        return Blender.empty();
    }

    public @NotNull NoiseChunkOpt forIndex(int pArrayIndex) {
        int i = Math.floorMod(pArrayIndex, this.cellWidth);
        int j = Math.floorDiv(pArrayIndex, this.cellWidth);
        int k = Math.floorMod(j, this.cellWidth);
        int l = this.cellHeight - 1 - Math.floorDiv(j, this.cellWidth);
        this.inCellX = k;
        this.inCellY = l;
        this.inCellZ = i;
        this.arrayIndex = pArrayIndex;
        return this;
    }

    @Override
    public void fillAllDirectly(double[] pValues, DensityFunction pFunction) {
        this.arrayIndex = 0;

        for (int i = this.cellHeight - 1; i >= 0; --i) {
            this.inCellY = i;

            for (int j = 0; j < this.cellWidth; ++j) {
                this.inCellX = j;

                for (int k = 0; k < this.cellWidth; ++k) {
                    this.inCellZ = k;
                    pValues[this.arrayIndex++] = pFunction.compute(this);
                }
            }
        }

    }

    private void fillSlice(boolean pIsSlice0, int pStart) {
        this.cellStartBlockX = pStart * this.cellWidth;
        this.inCellX = 0;

        for (int i = 0; i < this.cellCountXZ + 1; ++i) {
            int j = this.firstCellZ + i;
            this.cellStartBlockZ = j * this.cellWidth;
            this.inCellZ = 0;
            ++this.arrayInterpolationCounter;

            for (NoiseChunkOpt.NoiseInterpolator interpolator : this.interpolators) {
                double[] adouble = (pIsSlice0 ? interpolator.slice0 : interpolator.slice1)[i];
                interpolator.fillArray(adouble, this.sliceFillingContextProvider);
            }
        }

        ++this.arrayInterpolationCounter;
    }

    public void initializeForFirstCellX() {
        if (this.interpolating) {
            throw new IllegalStateException("Staring interpolation twice");
        } else {
            this.interpolating = true;
            this.interpolationCounter = 0L;
            this.fillSlice(true, this.firstCellX);
        }
    }

    public void advanceCellX(int pIncrement) {
        this.fillSlice(false, this.firstCellX + pIncrement + 1);
        this.cellStartBlockX = (this.firstCellX + pIncrement) * this.cellWidth;
    }

    public void selectCellYZ(int pY, int pZ) {
        this.interpolators.forEach((interpolator) -> {
            interpolator.selectCellYZ(pY, pZ);
        });
        this.fillingCell = true;
        this.cellStartBlockY = (pY + this.cellNoiseMinY) * this.cellHeight;
        this.cellStartBlockZ = (this.firstCellZ + pZ) * this.cellWidth;
        ++this.arrayInterpolationCounter;

        for (NoiseChunkOpt.CacheAllInCell cache : this.cellCaches) {
            cache.noiseFiller.fillArray(cache.values, this);
        }

        ++this.arrayInterpolationCounter;
        this.fillingCell = false;
    }

    public void updateForYXZ(int cx, int cy, int cz, double x, double y, double z) {
        this.inCellX = cx - this.cellStartBlockX;
        this.inCellY = cy - this.cellStartBlockY;
        this.inCellZ = cz - this.cellStartBlockZ;
        this.interpolators.forEach((interpolator) -> {
            interpolator.updateForYXZ(x, y, z);
        });
        ++this.interpolationCounter;
    }

    public void stopInterpolation() {
        if (!this.interpolating) {
            throw new IllegalStateException("Staring interpolation twice");
        } else {
            this.interpolating = false;
        }
    }

    protected DensityFunction wrap(DensityFunction fn) {
        return this.wrapped.computeIfAbsent(fn, this::wrapNew);
    }

    private DensityFunction wrapNew(DensityFunction fn) {
        if (fn instanceof Marker marker) {
            Object object = switch (marker.type()) {
                case Interpolated -> new NoiseInterpolator(marker.wrapped());
                case FlatCache -> new FlatCache(marker.wrapped(), true);
                case Cache2D -> new Cache2D(marker.wrapped());
                case CacheOnce -> new CacheOnce(marker.wrapped());
                case CacheAllInCell -> new CacheAllInCell(marker.wrapped());
            };
            return (DensityFunction) object;
        } else {
            if (fn == BeardifierMarker.INSTANCE) {
                return this.beardifier;
            } else if (fn instanceof DensityFunctions.HolderHolder hh) {
                return hh.function().value();
            } else {
                return fn;
            }
        }
    }

    @FunctionalInterface
    public interface BlockStateFiller {
        @Nullable
        BlockState calculate(DensityFunction.FunctionContext pContext);
    }

    public record MaterialRuleList(List<BlockStateFiller> materialRuleList) implements BlockStateFiller {
        @Nullable
        public BlockState calculate(DensityFunction.FunctionContext context) {
            for (BlockStateFiller noisechunk$blockstatefiller : this.materialRuleList) {
                BlockState blockstate = noisechunk$blockstatefiller.calculate(context);
                if (blockstate != null) {
                    return blockstate;
                }
            }

            return null;
        }
    }

    static class Cache2D implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
        private final DensityFunction function;
        private long lastPos2D = ChunkPos.INVALID_CHUNK_POS;
        private double lastValue;

        Cache2D(DensityFunction pFunction) {
            this.function = pFunction;
        }

        public double compute(FunctionContext pContext) {
            int i = pContext.blockX();
            int j = pContext.blockZ();
            long k = ChunkPos.pack(i, j);
            if (this.lastPos2D == k) {
                return this.lastValue;
            } else {
                this.lastPos2D = k;
                double d0 = this.function.compute(pContext);
                this.lastValue = d0;
                return d0;
            }
        }

        public void fillArray(double[] pArray, ContextProvider pContextProvider) {
            this.function.fillArray(pArray, pContextProvider);
        }

        public DensityFunction wrapped() {
            return this.function;
        }

        @Override
        public Marker.Type type() {
            return Marker.Type.Cache2D;
        }
    }

    class CacheAllInCell implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
        final DensityFunction noiseFiller;
        final double[] values;

        CacheAllInCell(DensityFunction pNoiseFilter) {
            this.noiseFiller = pNoiseFilter;
            this.values = new double[NoiseChunkOpt.this.cellWidth * NoiseChunkOpt.this.cellWidth * NoiseChunkOpt.this.cellHeight];
            NoiseChunkOpt.this.cellCaches.add(this);
        }

        public double compute(FunctionContext pContext) {
            if (pContext != NoiseChunkOpt.this) {
                return this.noiseFiller.compute(pContext);
            } else if (!NoiseChunkOpt.this.interpolating) {
                throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
            } else {
                int i = NoiseChunkOpt.this.inCellX;
                int j = NoiseChunkOpt.this.inCellY;
                int k = NoiseChunkOpt.this.inCellZ;
                return i >= 0 && j >= 0 && k >= 0 && i < NoiseChunkOpt.this.cellWidth && j < NoiseChunkOpt.this.cellHeight && k < NoiseChunkOpt.this.cellWidth ? this.values[((NoiseChunkOpt.this.cellHeight - 1 - j) * NoiseChunkOpt.this.cellWidth + i) * NoiseChunkOpt.this.cellWidth + k] : this.noiseFiller.compute(pContext);
            }
        }

        public void fillArray(double[] pArray, ContextProvider pContextProvider) {
            pContextProvider.fillAllDirectly(pArray, this);
        }

        public DensityFunction wrapped() {
            return this.noiseFiller;
        }

        public Marker.Type type() {
            return Marker.Type.CacheAllInCell;
        }
    }

    class CacheOnce implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
        private final DensityFunction function;
        private long lastCounter;
        private long lastArrayCounter;
        private double lastValue;
        @Nullable
        private double[] lastArray;

        CacheOnce(DensityFunction pFunction) {
            this.function = pFunction;
        }

        public double compute(FunctionContext pContext) {
            if (pContext != NoiseChunkOpt.this) {
                return this.function.compute(pContext);
            } else if (this.lastArray != null && this.lastArrayCounter == NoiseChunkOpt.this.arrayInterpolationCounter) {
                return this.lastArray[NoiseChunkOpt.this.arrayIndex];
            } else if (this.lastCounter == NoiseChunkOpt.this.interpolationCounter) {
                return this.lastValue;
            } else {
                this.lastCounter = NoiseChunkOpt.this.interpolationCounter;
                double d0 = this.function.compute(pContext);
                this.lastValue = d0;
                return d0;
            }
        }

        public void fillArray(double[] pArray, ContextProvider pContextProvider) {
            if (this.lastArray != null && this.lastArrayCounter == NoiseChunkOpt.this.arrayInterpolationCounter) {
                System.arraycopy(this.lastArray, 0, pArray, 0, pArray.length);
            } else {
                this.wrapped().fillArray(pArray, pContextProvider);
                if (this.lastArray != null && this.lastArray.length == pArray.length) {
                    System.arraycopy(pArray, 0, this.lastArray, 0, pArray.length);
                } else {
                    this.lastArray = (double[]) pArray.clone();
                }

                this.lastArrayCounter = NoiseChunkOpt.this.arrayInterpolationCounter;
            }
        }

        public DensityFunction wrapped() {
            return this.function;
        }

        public Marker.Type type() {
            return Marker.Type.CacheOnce;
        }
    }

    class FlatCache implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
        private final DensityFunction noiseFiller;
        final double[][] values;

        FlatCache(DensityFunction pNoiseFiller, boolean pComputeValues) {
            this.noiseFiller = pNoiseFiller;
            this.values = new double[NoiseChunkOpt.this.noiseSizeXZ + 1][NoiseChunkOpt.this.noiseSizeXZ + 1];
            if (pComputeValues) {
                for (int i = 0; i <= NoiseChunkOpt.this.noiseSizeXZ; ++i) {
                    int j = NoiseChunkOpt.this.firstNoiseX + i;
                    int k = QuartPos.toBlock(j);

                    for (int l = 0; l <= NoiseChunkOpt.this.noiseSizeXZ; ++l) {
                        int i1 = NoiseChunkOpt.this.firstNoiseZ + l;
                        int j1 = QuartPos.toBlock(i1);
                        this.values[i][l] = pNoiseFiller.compute(new SinglePointContext(k, 0, j1));
                    }
                }
            }

        }

        public double compute(FunctionContext pContext) {
            int i = QuartPos.fromBlock(pContext.blockX());
            int j = QuartPos.fromBlock(pContext.blockZ());
            int k = i - NoiseChunkOpt.this.firstNoiseX;
            int l = j - NoiseChunkOpt.this.firstNoiseZ;
            int i1 = this.values.length;
            return k >= 0 && l >= 0 && k < i1 && l < i1 ? this.values[k][l] : this.noiseFiller.compute(pContext);
        }

        public void fillArray(double[] pArray, ContextProvider pContextProvider) {
            pContextProvider.fillAllDirectly(pArray, this);
        }

        public DensityFunction wrapped() {
            return this.noiseFiller;
        }

        public Marker.Type type() {
            return Marker.Type.FlatCache;
        }
    }

    public class NoiseInterpolator implements DensityFunctions.MarkerOrMarked, NoiseChunk.NoiseChunkDensityFunction {
        double[][] slice0;
        double[][] slice1;
        private final DensityFunction noiseFiller;
        private double noise000;
        private double noise001;
        private double noise100;
        private double noise101;
        private double noise010;
        private double noise011;
        private double noise110;
        private double noise111;
        private double value;

        NoiseInterpolator(DensityFunction pNoiseFilter) {
            this.noiseFiller = pNoiseFilter;
            this.slice0 = this.allocateSlice(NoiseChunkOpt.this.cellCountY, NoiseChunkOpt.this.cellCountXZ);
            this.slice1 = this.allocateSlice(NoiseChunkOpt.this.cellCountY, NoiseChunkOpt.this.cellCountXZ);
            NoiseChunkOpt.this.interpolators.add(this);
        }

        private double[][] allocateSlice(int pCellCountY, int pCellCountXZ) {
            int i = pCellCountXZ + 1;
            int j = pCellCountY + 1;
            double[][] adouble = new double[i][j];

            for (int k = 0; k < i; ++k) {
                adouble[k] = new double[j];
            }

            return adouble;
        }

        void selectCellYZ(int pY, int pZ) {
            this.noise000 = this.slice0[pZ][pY];
            this.noise001 = this.slice0[pZ + 1][pY];
            this.noise100 = this.slice1[pZ][pY];
            this.noise101 = this.slice1[pZ + 1][pY];
            this.noise010 = this.slice0[pZ][pY + 1];
            this.noise011 = this.slice0[pZ + 1][pY + 1];
            this.noise110 = this.slice1[pZ][pY + 1];
            this.noise111 = this.slice1[pZ + 1][pY + 1];
        }

        void updateForYXZ(double pX, double pY, double pZ) {
            double valueXZ00 = Mth.lerp(pY, this.noise000, this.noise010);
            double valueXZ10 = Mth.lerp(pY, this.noise100, this.noise110);
            double valueXZ01 = Mth.lerp(pY, this.noise001, this.noise011);
            double valueXZ11 = Mth.lerp(pY, this.noise101, this.noise111);
            double valueZ0 = Mth.lerp(pX, valueXZ00, valueXZ10);
            double valueZ1 = Mth.lerp(pX, valueXZ01, valueXZ11);
            this.value = Mth.lerp(pZ, valueZ0, valueZ1);
        }

        public double compute(FunctionContext pContext) {
            if (pContext != NoiseChunkOpt.this) {
                return this.noiseFiller.compute(pContext);
            } else if (!NoiseChunkOpt.this.interpolating) {
                throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
            } else {
                return NoiseChunkOpt.this.fillingCell ? Mth.lerp3((double) NoiseChunkOpt.this.inCellX / (double) NoiseChunkOpt.this.cellWidth, (double) NoiseChunkOpt.this.inCellY / (double) NoiseChunkOpt.this.cellHeight, (double) NoiseChunkOpt.this.inCellZ / (double) NoiseChunkOpt.this.cellWidth, this.noise000, this.noise100, this.noise010, this.noise110, this.noise001, this.noise101, this.noise011, this.noise111) : this.value;
            }
        }

        public void fillArray(double[] pArray, ContextProvider pContextProvider) {
            if (NoiseChunkOpt.this.fillingCell) {
                pContextProvider.fillAllDirectly(pArray, this);
            } else {
                this.wrapped().fillArray(pArray, pContextProvider);
            }
        }

        public DensityFunction wrapped() {
            return this.noiseFiller;
        }

        public Marker.Type type() {
            return Marker.Type.Interpolated;
        }
    }

    private enum BeardifierMarker implements DensityFunctions.BeardifierOrMarker {
        INSTANCE;

        public double compute(DensityFunction.FunctionContext p_208515_) {
            return 0.0D;
        }

        public void fillArray(double[] p_208517_, DensityFunction.ContextProvider p_208518_) {
            Arrays.fill(p_208517_, 0.0D);
        }

        public double minValue() {
            return 0.0D;
        }

        public double maxValue() {
            return 0.0D;
        }
    }

    public static final class FluidStatusV {
        /**
         * The y height of the aquifer.
         */
        final int fluidLevel;
        /**
         * The fluid state the aquifer is filled with.
         */
        final BlockState fluidType;

        public FluidStatusV(int pFluidLevel, BlockState pFluidType) {
            this.fluidLevel = pFluidLevel;
            this.fluidType = pFluidType;
        }

        public BlockState at(int pY) {
            return pY < this.fluidLevel ? this.fluidType : Blocks.AIR.defaultBlockState();
        }
    }

    public static class NoiseBasedAquifer implements Aquifer {
        private static final int X_RANGE = 10;
        private static final int Y_RANGE = 9;
        private static final int Z_RANGE = 10;
        private static final int X_SEPARATION = 6;
        private static final int Y_SEPARATION = 3;
        private static final int Z_SEPARATION = 6;
        private static final int X_SPACING = 16;
        private static final int Y_SPACING = 12;
        private static final int Z_SPACING = 16;
        private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
        private static final double FLOWING_UPDATE_SIMULARITY = similarity(Mth.square(10), Mth.square(12));
        private final NoiseChunkOpt noiseChunk;
        protected final DensityFunction barrierNoise;
        private final DensityFunction fluidLevelFloodednessNoise;
        private final DensityFunction fluidLevelSpreadNoise;
        private final PositionalRandomFactory positionalRandomFactory;
        protected final FluidStatusV[] aquiferCache;
        protected final long[] aquiferLocationCache;
        private final NoiseChunkOpt.FluidStatusV fluidStatus;
        private final DensityFunction erosion;
        private final DensityFunction depth;
        protected final int minGridX;
        protected final int minGridY;
        protected final int minGridZ;
        protected final int gridSizeX;
        protected final int gridSizeZ;
        private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{{0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}};

        NoiseBasedAquifer(NoiseChunkOpt pNoiseChunk, ChunkPos pChunkPos, NoiseRouter pNoiseRouter, PositionalRandomFactory pPositionalRandomFactory, int pMinY, int pHeight, NoiseChunkOpt.FluidStatusV fluidStatus) {
            this.noiseChunk = pNoiseChunk;
            this.barrierNoise = pNoiseRouter.barrierNoise();
            this.fluidLevelFloodednessNoise = pNoiseRouter.fluidLevelFloodednessNoise();
            this.fluidLevelSpreadNoise = pNoiseRouter.fluidLevelSpreadNoise();
//            this.lavaNoise = pNoiseRouter.lavaNoise();
            this.erosion = pNoiseRouter.erosion();
            this.depth = pNoiseRouter.depth();
            this.positionalRandomFactory = pPositionalRandomFactory;
            this.minGridX = this.gridX(pChunkPos.getMinBlockX()) - 1;
            this.fluidStatus = fluidStatus;
            int i = this.gridX(pChunkPos.getMaxBlockX()) + 1;
            this.gridSizeX = i - this.minGridX + 1;
            this.minGridY = this.gridY(pMinY) - 1;
            int j = this.gridY(pMinY + pHeight) + 1;
            int k = j - this.minGridY + 1;
            this.minGridZ = this.gridZ(pChunkPos.getMinBlockZ()) - 1;
            int l = this.gridZ(pChunkPos.getMaxBlockZ()) + 1;
            this.gridSizeZ = l - this.minGridZ + 1;
            int i1 = this.gridSizeX * k * this.gridSizeZ;
            this.aquiferCache = new FluidStatusV[i1];
            this.aquiferLocationCache = new long[i1];
            Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
        }

        /**
         * @return A cache index based on grid positions.
         */
        protected int getIndex(int pGridX, int pGridY, int pGridZ) {
            int i = pGridX - this.minGridX;
            int j = pGridY - this.minGridY;
            int k = pGridZ - this.minGridZ;
            return (j * this.gridSizeZ + k) * this.gridSizeX + i;
        }

        @Nullable
        public BlockState computeSubstance(DensityFunction.FunctionContext pContext, double pSubstance) {
            int bx = pContext.blockX();
            int by = pContext.blockY();
            int bz = pContext.blockZ();
            if (pSubstance > 0.0D) {
                return null;
            } else {
                int fx = Math.floorDiv(bx - 5, 16);
                int fy = Math.floorDiv(by + 1, 12);
                int fz = Math.floorDiv(bz - 5, 16);
                int k1 = Integer.MAX_VALUE;
                int l1 = Integer.MAX_VALUE;
                int i2 = Integer.MAX_VALUE;
                long packedPos = 0L;
                long k2 = 0L;
                long l2 = 0L;

                for (int dx = 0; dx <= 1; ++dx) {
                    for (int dy = -1; dy <= 1; ++dy) {
                        for (int dz = 0; dz <= 1; ++dz) {
                            int ix = fx + dx;
                            int iy = fy + dy;
                            int iz = fz + dz;
                            int cacheIdx = this.getIndex(ix, iy, iz);
                            long pp5 = this.aquiferLocationCache[cacheIdx];
                            long pp4;
                            if (pp5 != Long.MAX_VALUE) {
                                pp4 = pp5;
                            } else {
                                RandomSource randomsource = this.positionalRandomFactory.at(ix, iy, iz);
                                pp4 = BlockPos.asLong(ix * 16 + randomsource.nextInt(10), iy * 12 + randomsource.nextInt(9), iz * 16 + randomsource.nextInt(10));
                                this.aquiferLocationCache[cacheIdx] = pp4;
                            }

                            int x4 = BlockPos.getX(pp4) - bx;
                            int y4 = BlockPos.getY(pp4) - by;
                            int z4 = BlockPos.getZ(pp4) - bz;
                            int sq4 = x4 * x4 + y4 * y4 + z4 * z4;
                            if (k1 >= sq4) {
                                l2 = k2;
                                k2 = packedPos;
                                packedPos = pp4;
                                i2 = l1;
                                l1 = k1;
                                k1 = sq4;
                            } else if (l1 >= sq4) {
                                l2 = k2;
                                k2 = pp4;
                                i2 = l1;
                                l1 = sq4;
                            } else if (i2 >= sq4) {
                                l2 = pp4;
                                i2 = sq4;
                            }
                        }
                    }
                }

                FluidStatusV status1 = this.getAquiferStatus(packedPos);
                double d1 = similarity(k1, l1);
                BlockState blockstate = status1.at(by);
                if (d1 <= 0.0D) {
                    return blockstate;
                } else if (blockstate.is(Blocks.WATER) && this.fluidStatus.at(by - 1).is(Blocks.LAVA)) {
                    return blockstate;
                } else {
                    MutableDouble mutabledouble = new MutableDouble(Double.NaN);
                    FluidStatusV status2 = this.getAquiferStatus(k2);
                    double d2 = d1 * this.calculatePressure(pContext, mutabledouble, status1, status2);
                    if (pSubstance + d2 > 0.0D) {
                        return null;
                    } else {
                        FluidStatusV status3 = this.getAquiferStatus(l2);
                        double d0 = similarity(k1, i2);
                        if (d0 > 0.0D) {
                            double d3 = d1 * d0 * this.calculatePressure(pContext, mutabledouble, status1, status3);
                            if (pSubstance + d3 > 0.0D) {
                                return null;
                            }
                        }

                        double d4 = similarity(l1, i2);
                        if (d4 > 0.0D) {
                            double d5 = d1 * d4 * this.calculatePressure(pContext, mutabledouble, status2, status3);
                            if (pSubstance + d5 > 0.0D) {
                                return null;
                            }
                        }

                        return blockstate;
                    }
                }
            }
        }

        /**
         * Returns {@code true} if there should be a fluid update scheduled - due to a fluid block being placed in a
         * possibly unsteady position - at the last position passed into computeState.
         * This <strong>must</strong> be invoked only after computeState, and will be using the same parameters
         * as that method.
         */
        public boolean shouldScheduleFluidUpdate() {
            return false;
        }

        /**
         * Compares two distances (between aquifers).
         *
         * @return {@code 1.0} if the distances are equal, and returns smaller values the more different in absolute value
         * the two distances are.
         */
        protected static double similarity(int pFirstDistance, int pSecondDistance) {
            return 1.0D - (double) Math.abs(pSecondDistance - pFirstDistance) / 25.0D;
        }

        private double calculatePressure(DensityFunction.FunctionContext pContext, MutableDouble pSubstance, FluidStatusV pFirstFluid, FluidStatusV pSecondFluid) {
            int i = pContext.blockY();
            BlockState blockstate = pFirstFluid.at(i);
            BlockState blockstate1 = pSecondFluid.at(i);
            if ((!blockstate.is(Blocks.LAVA) || !blockstate1.is(Blocks.WATER)) && (!blockstate.is(Blocks.WATER) || !blockstate1.is(Blocks.LAVA))) {
                int j = Math.abs(pFirstFluid.fluidLevel - pSecondFluid.fluidLevel);
                if (j == 0) {
                    return 0.0D;
                } else {
                    double d0 = 0.5D * (double) (pFirstFluid.fluidLevel + pSecondFluid.fluidLevel);
                    double d1 = (double) i + 0.5D - d0;
                    double d2 = (double) j / 2.0D;
                    double d9 = d2 - Math.abs(d1);
                    double d10;
                    if (d1 > 0.0D) {
                        double d11 = 0.0D + d9;
                        if (d11 > 0.0D) {
                            d10 = d11 / 1.5D;
                        } else {
                            d10 = d11 / 2.5D;
                        }
                    } else {
                        double d15 = 3.0D + d9;
                        if (d15 > 0.0D) {
                            d10 = d15 / 3.0D;
                        } else {
                            d10 = d15 / 10.0D;
                        }
                    }

                    double d12;
                    if (!(d10 < -2.0D) && !(d10 > 2.0D)) {
                        double d13 = pSubstance.getValue();
                        if (Double.isNaN(d13)) {
                            double d14 = this.barrierNoise.compute(pContext);
                            pSubstance.setValue(d14);
                            d12 = d14;
                        } else {
                            d12 = d13;
                        }
                    } else {
                        d12 = 0.0D;
                    }

                    return 2.0D * (d12 + d10);
                }
            } else {
                return 2.0D;
            }
        }

        protected int gridX(int pX) {
            return Math.floorDiv(pX, 16);
        }

        protected int gridY(int pY) {
            return Math.floorDiv(pY, 12);
        }

        protected int gridZ(int pZ) {
            return Math.floorDiv(pZ, 16);
        }

        /**
         * Calculates the aquifer at a given location. Internally references a cache using the grid positions as an index.
         * If the cache is not populated, computes a new aquifer at that grid location using {@link #computeFluid}.
         *
         * @param pPackedPos The aquifer block position, packed into a {@code long}.
         */
        private FluidStatusV getAquiferStatus(long pPackedPos) {
            int x = BlockPos.getX(pPackedPos);
            int y = BlockPos.getY(pPackedPos);
            int z = BlockPos.getZ(pPackedPos);
            int gx = this.gridX(x);
            int gy = this.gridY(y);
            int gz = this.gridZ(z);
            int idx = this.getIndex(gx, gy, gz);
            FluidStatusV status = this.aquiferCache[idx];
            if (status == null) {
                status = this.computeFluid(x, y, z);
                this.aquiferCache[idx] = status;
            }
            return status;
        }

        private FluidStatusV computeFluid(int pX, int pY, int pZ) {
            int minlevel = Integer.MAX_VALUE;
            int yUp = pY + 12;
            int yDown = pY - 12;
            boolean doClamp = false;

            for (int[] offsets : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
                int px2 = pX + SectionPos.sectionToBlockCoord(offsets[0]);
                int pz2 = pZ + SectionPos.sectionToBlockCoord(offsets[1]);
                int level = this.noiseChunk.preliminarySurfaceLevel(px2, pz2);
                int level8 = level + 8;
                boolean flag1 = offsets[0] == 0 && offsets[1] == 0;
                if (flag1 && yDown > level8) {
                    return this.fluidStatus;
                }

                boolean flag2 = yUp > level8;
                if (flag2 || flag1) {
                    if (!this.fluidStatus.at(level8).isAir()) {
                        if (flag1) {
                            doClamp = true;
                        }

                        if (flag2) {
                            return this.fluidStatus;
                        }
                    }
                }

                minlevel = Math.min(minlevel, level);
            }

            int level = this.computeSurfaceLevel(pX, pY, pZ, this.fluidStatus, minlevel, doClamp);
            return new FluidStatusV(level, this.fluidStatus.fluidType);
        }

        private int computeSurfaceLevel(int pX, int pY, int pZ, FluidStatusV pFluidStatus, int pMaxSurfaceLevel, boolean doClamp) {
            DensityFunction.SinglePointContext fn = new DensityFunction.SinglePointContext(pX, pY, pZ);
            double d0;
            double d1;
            if (OverworldBiomeBuilder.isDeepDarkRegion(this.erosion, this.depth, fn)) {
                d0 = -1.0D;
                d1 = -1.0D;
            } else {
                int i = pMaxSurfaceLevel + 8 - pY;
                double d2 = doClamp ? Mth.clampedMap(i, 0.0D, 64.0D, 1.0D, 0.0D) : 0.0D;
                double d3 = Mth.clamp(this.fluidLevelFloodednessNoise.compute(fn), -1.0D, 1.0D);
                double d4 = Mth.map(d2, 1.0D, 0.0D, -0.3D, 0.8D);
                double d5 = Mth.map(d2, 1.0D, 0.0D, -0.8D, 0.4D);
                d0 = d3 - d5;
                d1 = d3 - d4;
            }

            if (d1 > 0.0D) {
                return pFluidStatus.fluidLevel;
            } else if (d0 > 0.0D) {
                return this.computeRandomizedFluidSurfaceLevel(pX, pY, pZ, pMaxSurfaceLevel);
            } else {
                return DimensionType.WAY_BELOW_MIN_Y;
            }
        }

        private int computeRandomizedFluidSurfaceLevel(int pX, int pY, int pZ, int pMaxSurfaceLevel) {
            int k = Math.floorDiv(pX, 16);
            int l = Math.floorDiv(pY, 40);
            int i1 = Math.floorDiv(pZ, 16);
            int lev1 = l * 40 + 20;
            double d0 = this.fluidLevelSpreadNoise.compute(new DensityFunction.SinglePointContext(k, l, i1)) * 10.0D;
            int lev2 = Mth.quantize(d0, 3);
            int level = lev1 + lev2;
            return Math.min(pMaxSurfaceLevel, level);
        }

    }
}
