# Lost Cities performance evaluation plan

## Purpose

This plan is for finding the remaining **significant** performance
opportunities in Lost Cities before implementing optimizations. It focuses on:

- chunk-generation throughput and tail latency;
- safe use of Minecraft's existing world-generation parallelism;
- `BuildingInfo` construction, caching, and neighbour expansion;
- block materialization through `ChunkDriver`;
- client chunk meshing and GPU upload;
- allocation rate, cache behaviour, and memory retained by generation.

The source code is authoritative. No optimization should be accepted merely
because it looks cheaper in isolation. It must improve a representative
measurement, preserve deterministic world output, and pass an in-game visual
check.

## Current architecture and initial conclusions

These conclusions establish where investigation should start. They are
hypotheses and ownership boundaries, not performance results.

### World generation is already concurrent

`LostCityFeature.runWithDimensionInfo()` holds a lifecycle read lock and takes
ordered striped locks for the active chunk's 3x3 neighbourhood. Non-overlapping
chunks in the same dimension can already generate concurrently. Per-chunk
mutable state, random state, scratch arrays, and `ChunkDriver` are isolated in
`GenerationContext`.

The main remaining serialization point visible in Lost Cities is
`BuildingInfo`:

- `getBuildingInfo()`, `getChunkCharacteristics()`, and `getCityLevel()` use
  one stable memoization lock per dimension;
- a cold `BuildingInfo` request can recursively request characteristics,
  levels, a multibuilding top-left chunk, and several neighbours;
- streets, slopes, terrain correction, debris, bridges, doors, and other
  consumers can extend that neighbourhood further.

This makes `BuildingInfo` lock contention a high-priority scaling hypothesis.
It does **not** prove that the lock is expensive: warm-cache hit rates and time
inside the lock must be measured first.

### Lost Cities does not upload GPU chunk geometry

`ChunkDriver.actuallyGenerate()` copies staged `BlockState` values into
Minecraft `LevelChunkSection` instances using `BulkSectionAccess`. This is a
server/world-generation block-state commit, not a vertex or GPU upload.

The repository has client GUI and fog integration, but no chunk renderer,
section compiler, vertex-buffer uploader, or mixin into Minecraft's chunk
rendering pipeline. Minecraft, Forge, and any installed renderer mod own:

1. turning received chunk sections into render-layer meshes; and
2. uploading those meshes to GPU buffers.

Consequently, a direct "faster geometry uploader" is not currently an
optimization inside Lost Cities. The evaluation must first distinguish:

- slow server generation or chunk delivery;
- slow client section meshing caused by dense/complex Lost Cities blocks;
- a render-thread/GPU upload bottleneck owned outside this mod.

Lost Cities can still reduce client work indirectly if measurements identify
specific assets or block-state patterns that create excessive geometry,
occlusion checks, translucent layers, model complexity, block entities, or
section rebuilds. Replacing Minecraft's uploader from this mod would have a
large compatibility and maintenance cost and should be considered only after
proving that upload itself, rather than mesh construction, is the bottleneck.

### The block-generation hot path has several plausible costs

`LostCityTerrainFeature.generate()` currently performs planning, building or
street generation, railways, torch handling, damage, debris, and the final
section commit in one timed operation. The existing rolling statistic uses
millisecond wall-clock totals and cannot attribute cost to a subsystem.

Likely candidates to measure are:

- `BuildingInfo` and `LostChunkCharacteristics` cold construction;
- city, street, highway, railway, sphere, scattered, and structure-avoidance
  planning;
- part/palette expansion in `generatePart()`;
- per-block state correction in `ChunkDriver.correct()`, especially stairs,
  walls, fences, and cross-chunk reads;
- staging and copying blocks through `SectionCache`;
- heightmap updates;
- terrain correction, damage, rubble, leaves, ruins, and debris;
- block-entity, loot, POI, lighting, and post-generation todo handling;
- allocation from condition contexts, temporary collections, palettes,
  positions, and section arrays.

## Questions the evaluation must answer

1. What consumes CPU time in a cold generated city chunk, and how does that
   differ from a normal, scattered, sphere, floating, or damaged chunk?
2. Is the limiting resource CPU work, lock contention, allocation/GC, chunk
   I/O/delivery, client mesh building, render-thread upload, or the GPU?
3. Does throughput scale with Minecraft's generation workers? If not, which
   Lost Cities lock or shared service prevents it?
4. How many cold `BuildingInfo`, characteristic, city-level, heightmap, and
   planner calculations does one requested chunk cause?
5. How often are cached values reused before the timed cache evicts them?
6. How much time is spent staging blocks versus committing staged states to
   `LevelChunkSection`?
7. Do repeated block writes overwrite the same position, and could generation
   avoid or coalesce them?
8. On the client, is time spent compiling mesh geometry or uploading completed
   buffers? Which block types, render layers, or assets dominate?
9. Which candidate changes retain byte-for-byte world determinism and work
   with Forge and renderer mods?

## Candidate areas, priority, and expected risk

| Priority | Area | Why it may be significant | Stability risk |
|---|---|---|---|
| 1 | Phase timing and allocation observability | The current single total hides all actionable costs | Low |
| 1 | `BuildingInfo` cold work and dimension-lock contention | It serializes recursive planning within a dimension and can expand to neighbours | Medium to high |
| 1 | Part expansion and `ChunkDriver` writes | Dense buildings execute large nested loops and per-block correction before a second section-copy pass | Medium |
| 1 | Existing chunk-level parallel scaling | The mod already supports safe concurrent chunks; improving scaling may give more benefit than intra-chunk threading | Medium |
| 2 | Terrain correction, damage, debris, and vegetation | These scan many block positions and noise buffers, but only some profiles/chunks pay the cost | Medium |
| 2 | Palette and asset-derived caches | Local palette combinations and transformed states may be rebuilt or looked up repeatedly | Low to medium |
| 2 | Cache TTL, footprint, and cleanup | Five-minute expiry may cause cold recalculation during exploration or retain a large graph | Medium; memory trade-off |
| 2 | Client mesh complexity by asset/render layer | Lost Cities content is denser than vanilla terrain and may stress standard section compilation | Medium; visual/data-pack compatibility |
| 3 | Direct client GPU upload changes | This path is outside Lost Cities and is likely best left to Minecraft or renderer mods | Very high |
| 3 | Parallel writes inside one chunk | World/section mutation, events, block entities, RNG order, and neighbour correction are unsafe to split casually | Very high |

Priority is provisional. Phase 1 measurements may reorder or eliminate these
areas.

## Phase 0: freeze the benchmark contract

Before profiling, record:

- exact Lost Cities commit and dirty-worktree state;
- Java 17 runtime, Forge/Minecraft version, JVM flags, CPU, RAM, GPU, driver,
  operating system, and storage;
- client and server render/simulation distances;
- world seed, dimension, profile, street mode, highway mode, and relevant
  datapacks;
- worker/thread configuration and whether the run is integrated or dedicated
  server;
- all performance or renderer mods.

Use at least these scenarios:

1. a dense default city with tall buildings;
2. a city/non-city border where terrain correction is active;
3. hierarchical streets and inter-city highways;
4. a damaged city with rubble, vegetation, and debris;
5. scattered multibuildings outside a city;
6. a sphere/space profile;
7. a floating or cavern profile;
8. a control area where Lost Cities does little or no work.

For each scenario, save a coordinate list that contains city chunks, streets,
buildings, transport infrastructure, and normal terrain. Use a fresh world or
a copied pre-generation save for cold runs. Never compare a cold baseline with
a warm candidate.

Run three distinct modes:

- **server generation:** dedicated server, no local rendering;
- **integrated end to end:** single-player exploration or fixed teleport path;
- **client rendering:** pre-generated identical world, fixed camera path, no
  new server generation.

Report medians and tail values (at least p50, p95, and p99), total chunks per
second, CPU utilization, allocation rate, GC pauses, and peak/steady memory.
Do not use only average milliseconds per chunk.

## Practical profiling guide for Linux

The recommended free profiler stack is:

1. **Java Flight Recorder (JFR)** for a low-overhead overview of CPU samples,
   allocations, garbage collection, thread activity, locks, and JVM events.
2. **async-profiler** for focused CPU, wall-time, allocation, and lock flame
   graphs, including Java and native frames.
3. **VisualVM** as an optional live-monitoring, thread, heap, and heap-dump
   tool. It is useful for orientation but should not be the primary CPU
   benchmark.

JFR is included in OpenJDK 17. async-profiler is free under the Apache 2.0
license and provides Linux x64 and arm64 builds. VisualVM is also free and runs
on Linux.

IntelliJ's built-in profiler integrates JFR and async-profiler and normally
runs them together, but the profiler UI is an IntelliJ Ultimate feature. With
Ultimate, an already-running Java process can be selected in the Profiler tool
window and profiled with **Attach IntelliJ Profiler**. Without Ultimate, use
the same free engines externally:

- capture JFR recordings with `jcmd` and inspect them in the free JDK Mission
  Control application;
- produce interactive HTML flame graphs with async-profiler;
- optionally install the third-party VisualVM Launcher plugin for IntelliJ.
  The plugin starts VisualVM alongside an application; analysis still occurs
  in the VisualVM window.

Official references:

- [IntelliJ profiler configuration and Linux
  setup](https://www.jetbrains.com/help/idea/custom-profiler-configurations.html)
- [IntelliJ profiler snapshot
  workflow](https://www.jetbrains.com/help/idea/create-a-profiling-report.html)
- [async-profiler project and
  downloads](https://github.com/async-profiler/async-profiler)
- [JDK Flight Recorder](https://openjdk.org/jeps/328)
- [JDK Mission Control](https://jdk.java.net/jmc/)
- [VisualVM downloads](https://visualvm.github.io/download)
- [VisualVM IDE integrations](https://visualvm.github.io/idesupport.html)

### How the profilers operate

JFR is an event recorder built into the JVM. It records periodic execution
samples together with runtime events such as allocation samples, garbage
collections, thread parks, monitor contention, class loading, and I/O. It
writes these events to a `.jfr` file for later correlation on a timeline. The
standard `default` recording is suitable for long low-overhead observation;
the `profile` settings collect more detail and are appropriate for controlled
benchmark runs.

async-profiler is primarily a statistical sampling profiler. At a configured
interval it captures the stack of the sampled threads instead of instrumenting
every method call. If approximately 20% of CPU samples pass through a method,
approximately 20% of sampled CPU time passed through it. This keeps overhead
low and avoids the JVM safepoint bias found in simpler stack samplers.

The async-profiler event determines what a sample means:

- `cpu` records threads actively using CPU;
- `wall` records elapsed time, including CPU work, waiting, locking, sleeping,
  and I/O;
- `alloc` attributes newly allocated objects or bytes to allocation call
  paths;
- `lock` attributes contended locking to the relevant call paths.

Allocation profiling identifies allocation pressure, not retained memory or a
memory leak. Use a heap dump and VisualVM, JDK Mission Control, or another heap
analyzer when retained objects are the question.

VisualVM can sample or instrument methods. Instrumentation modifies methods to
count entries and exits and can add substantial overhead to tight generation
loops. Prefer its sampler for orientation and use JFR/async-profiler for
performance decisions.

### Start with a dedicated server

For server generation, start with Forge's dedicated development server rather
than the integrated client. A dedicated server avoids mixing client rendering,
the integrated server, and world generation in one JVM.

Use Java 17 and run normally, not under the debugger. The debugger and
breakpoints can distort thread scheduling and timings. Warm up class loading
and JIT compilation before recording, then generate a fixed route through
fresh chunks.

An integrated-client recording is still useful for end-to-end behaviour, but
server and client results must be separated. In single-player they share a JVM
and must be distinguished by thread and call path.

### Capture a JFR recording

Use `jcmd` from the same Java 17 installation and operating-system user as the
Minecraft process.

Find the process:

```bash
jcmd -l
```

Start a detailed recording:

```bash
jcmd <pid> JFR.start name=lostcities settings=profile
```

Perform the fixed benchmark workload for approximately 60 to 120 seconds, then
save and stop the recording:

```bash
jcmd <pid> JFR.dump name=lostcities filename=/tmp/lostcities.jfr
jcmd <pid> JFR.stop name=lostcities
```

Open `/tmp/lostcities.jfr` in IntelliJ Profiler or JDK Mission Control. Inspect:

- hot methods and method profiling;
- allocation by class and stack trace;
- garbage-collection frequency and pause duration;
- thread activity and worker utilization;
- Java monitor blocking and thread parks;
- CPU load over the generation interval.

Filter initially for `mcjty.lostcities`, but retain the surrounding Minecraft
and Forge callers. A cost inside `LevelChunkSection`, biome lookup, chunk
scheduling, registry access, or an event subscriber can still be caused by a
Lost Cities call path.

### Capture async-profiler recordings

Download and unpack the appropriate Linux release, locate `asprof`, and attach
it to the Minecraft Java process. Capture each mode in a separate run using
the same seed, coordinate route, cache state, and benchmark duration:

```bash
asprof -e cpu -d 60 -f lostcities-cpu.html <pid>
asprof -e wall -d 60 -f lostcities-wall.html <pid>
asprof -e alloc -d 60 -f lostcities-alloc.html <pid>
asprof -e lock -d 60 -f lostcities-lock.html <pid>
```

Do not combine all four modes in one benchmark result. Their overhead and
meaning differ, and each must observe an equivalent workload.

On Linux, CPU profiling may require allowing non-root users to access
performance events for the current boot:

```bash
sudo sh -c 'echo 1 >/proc/sys/kernel/perf_event_paranoid'
```

Resolving kernel symbols may additionally require:

```bash
sudo sh -c 'echo 0 >/proc/sys/kernel/kptr_restrict'
```

The second change is optional when only Java application frames are needed.
Review the security implications before making either setting persistent.
IntelliJ's Linux profiler documentation contains the corresponding persistent
`sysctl` configuration if it is appropriate for the development machine.

### Use IntelliJ Profiler when Ultimate is available

The safest workflow for ForgeGradle is usually to attach after launch:

1. start `runClient` or `runServer` normally;
2. open the IntelliJ **Profiler** tool window;
3. find the correct Java process;
4. right-click it and select **Attach IntelliJ Profiler**;
5. reproduce only the fixed benchmark workload;
6. select **Stop Profiling and Show Results**;
7. save the snapshot with the benchmark manifest.

IntelliJ presents a flame graph, call tree, method list, allocation and thread
timelines, source navigation, and snapshot comparison. Enable native calls
only when investigating JNI, OpenGL, JVM, or operating-system work; Java-only
captures are easier to read for initial world-generation analysis.

### Read a flame graph

In a flame graph:

- horizontal width is the proportion of collected samples;
- vertical position is call depth;
- a wide lower frame can simply be the caller of an expensive child;
- the top edge shows where sampled stacks ended and is often the direct hot
  work;
- total or inclusive time includes callees;
- self time is attributed directly to the selected method.

For example, a wide stack ending in the constructor suggests expensive cold
construction:

```text
LostCityTerrainFeature.generate
  BuildingInfo.getBuildingInfo
    BuildingInfo.<init>
```

If a `BuildingInfo` path is wide in the wall/lock profile but narrow in the CPU
profile, workers are probably waiting rather than calculating:

```text
BuildingInfo.getBuildingInfo
  ReentrantLock.lock
    LockSupport.park
```

A wide section-commit path makes `ChunkDriver` materialization a candidate:

```text
ChunkDriver.actuallyGenerate
  LevelChunkSection.setBlockState
```

Large allocation stacks ending in `ConditionContext`, `BlockPos`,
`BlockState[]`, `CompiledPalette`, or temporary collections indicate
allocation pressure. Confirm that this pressure produces meaningful GC or
runtime cost before optimizing it.

Always compare CPU and wall results. A method can dominate elapsed time because
it waits for a lock, chunk dependency, disk, or another worker while consuming
little CPU.

### Profile client chunk meshing separately

Use a fully pre-generated copy of the world so server generation does not
contaminate client results. Capture separate cases:

1. stand still after all visible sections are compiled;
2. rotate the camera without loading chunks;
3. move through already generated chunks;
4. load pre-generated chunks;
5. force or trigger section rebuilds.

Inspect the render thread, chunk render/section compiler workers, client
network/decode threads, and lighting workers:

- busy compiler workers and a growing work queue indicate mesh-construction
  cost;
- completed meshes waiting while the render thread is busy indicate upload or
  render-thread scheduling;
- Java stacks ending in OpenGL buffer calls indicate possible driver/GPU
  synchronization;
- low CPU utilization with a saturated GPU indicates rendering, fill rate, or
  memory bandwidth rather than Java upload;
- smooth dedicated-server generation but slow integrated play confirms that
  the primary problem is client-side.

JFR and async-profiler can show time at Java/native upload boundaries, but
cannot fully explain GPU execution. Use a frame debugger such as RenderDoc only
after profiles prove that client rendering or buffer upload, rather than
server generation or mesh compilation, is significant.

### Initial recording bundle

The first useful evidence bundle should contain:

1. dedicated-server JFR, dense fresh city, 120 seconds;
2. dedicated-server async-profiler CPU, equivalent route, 60 seconds;
3. dedicated-server async-profiler wall, equivalent route, 60 seconds;
4. dedicated-server async-profiler allocation, equivalent route, 60 seconds;
5. dedicated-server async-profiler lock profile while distant chunks generate
   concurrently, 60 seconds;
6. client CPU profile in the same fully pre-generated city, 60 seconds.

Store beside every recording:

- Lost Cities commit and dirty-worktree state;
- seed, profile, dimension, and street/highway modes;
- starting coordinate, route, and workload duration;
- Java, Minecraft, and Forge versions;
- JVM flags and generation-worker count;
- simulation and render distance;
- CPU, RAM, GPU, driver, and operating system;
- mod and datapack list;
- whether relevant caches and world chunks were cold or warm.

Do not compare recordings until their manifests describe equivalent workloads.

## Phase 1: add temporary observability

Instrumentation should be behind a development flag and have a cheap disabled
path. Prefer JFR events or sampled profiling over logging every block.

### Server/world-generation measurements

Split `LostCityTerrainFeature.generate()` into timers/counters for:

- heightmap acquisition;
- `BuildingInfo.getBuildingInfo()`;
- city/normal chunk body;
- streets/buildings and `generatePart()`;
- highways, railways, bridges, scattered, and spheres;
- terrain correction;
- damage/explosions;
- rubble, ruins, vegetation, and debris;
- torch, POI, block-entity, lighting, and todo work;
- `ChunkDriver.actuallyGenerate()`;
- `ChunkFixer.fix()`.

Add diagnostic counters for:

- staged block writes, unique positions, same-value writes, and overwritten
  positions;
- calls to `correct()` by block category;
- cross-chunk reads and writes;
- non-empty staged sections and committed states;
- number and total volume of generated parts;
- local palette combinations built;
- block entities, loot entries, POI updates, lighting updates, and todos.

Use nanosecond monotonic timing. Keep aggregation per worker or use contention-
free counters; measurement code must not introduce a new global lock.

### `BuildingInfo` measurements

Measure the three caches separately:

- hit, miss, expiry, and provisional-result counts;
- construction time and time waiting for the dimension memoization lock;
- recursive depth and number of nested lookups;
- unique coordinates touched per requested/generated chunk;
- neighbour and multibuilding top-left expansions;
- work attributed to city factor/style, city level, structure avoidance,
  multibuildings, roads, highways, railways, spheres, floor selection, palette
  selection, bridge/stair decisions, and terrain-height memoization.

Also measure retained entry counts and approximate memory. A speedup that keeps
an unbounded graph of `BuildingInfo` neighbours is not acceptable.

### JVM and contention profiles

Capture:

- JFR CPU samples, allocation samples, lock contention, thread states, and GC;
- one wall-clock flame graph during dense city generation;
- one allocation flame graph;
- one lock-contention view while generating distant chunks concurrently;
- worker utilization over time.

Confirm whether samples agree with the explicit phase counters. If they do not,
investigate instrumentation bias before optimizing.

### Client measurements

With the world fully pre-generated, capture client profiles that distinguish:

- network/chunk decode;
- light updates;
- section mesh compilation;
- model/occlusion work by render layer;
- completed mesh queueing;
- render-thread/GPU buffer upload;
- frame rendering and GPU saturation.

Record section rebuild counts, compiled/uploaded bytes and vertices if the
active renderer exposes them, and the block/render-layer mix in the slow
sections. Repeat with vanilla Forge first. Renderer-mod comparisons are a
separate compatibility experiment, not the baseline.

### Phase 1 exit criteria

Produce a ranked table where every candidate has:

- inclusive and self time;
- percentage of total generation or client frame time;
- p95/p99 contribution;
- allocation contribution;
- contention or parallel-scaling evidence;
- affected profiles/chunk types;
- estimated upper bound from removing the cost completely.

Only an area with a meaningful upper bound should proceed. As an initial
threshold, require at least one of:

- 10% of relevant total CPU time;
- 10% improvement potential in chunks per second;
- a material p95/p99 pause reduction;
- a material allocation/GC reduction demonstrated to affect runtime.

## Phase 2: evaluate `BuildingInfo`

### 2A. Separate cache-hit cost from cold construction

Benchmark:

- cold cache;
- warm cache;
- cache expiry during continuous exploration;
- forward, reverse, and randomized chunk request order;
- one worker versus normal worker count;
- adjacent chunks versus distant non-overlapping chunks.

This determines whether the primary issue is computation, lock contention,
cache churn, or neighbour fan-out.

### 2B. Map dependencies before changing locking

Document a dependency graph for:

- raw city membership and profile;
- city level;
- multibuilding placement and top-left ownership;
- structure avoidance;
- planned/effective streets;
- city style and building selection;
- highway/railway interaction;
- complete building/floor/palette decisions;
- lazy bridge, stair, monorail, damage, ocean, and terrain-height values.

Classify each value as:

- coordinate-pure and independently computable;
- dependent only on an immutable lower-level result;
- dependent on neighbouring results;
- recursive/cyclic;
- dependent on `WorldGenRegion` availability;
- able to produce a provisional result;
- mutable after publication.

This graph is required because replacing the dimension lock with naive
`ConcurrentHashMap.computeIfAbsent()` can recursively update the same map,
duplicate expensive work, deadlock through opposing neighbour order, or
publish incomplete objects.

### 2C. Evaluate optimizations in increasing-risk order

1. **Avoid repeated access and derived work**
   - reuse already-held characteristics and style instead of calling cache
     accessors again;
   - memoize repeated railway/highway/biome/style queries where measurements
     show duplication;
   - avoid constructing full `BuildingInfo` for consumers that only need a
     lower-level immutable fact;
   - replace repeated neighbour chains with a request-scoped neighbourhood
     snapshot only if it reduces lookups without increasing retained graphs.

2. **Split lightweight planning from full building materialization**
   - make an immutable chunk-plan/core record containing commonly requested
     decisions;
   - defer floor arrays, combined palettes, damage information, bridge/stair
     details, and other expensive data until a consumer needs them;
   - verify that deferral does not alter random consumption for legacy worlds.

3. **Improve cache shape**
   - test a single immutable plan cache versus three overlapping caches;
   - test expiry policy and cleanup cost under exploration;
   - consider weight/size bounds in addition to time only if memory profiles
     show a problem;
   - do not cache provisional structure-avoidance results as authoritative.

4. **Reduce lock scope**
   - calculate proven-pure inputs outside the dimension lock;
   - lock only publication or recursive legacy decisions;
   - retain stable lock identity across cleanup.

5. **Permit concurrent cold plans**
   - prototype per-key immutable futures/promises or ordered region ownership
     only after the dependency graph proves cycles are controlled;
   - never wait on two neighbour plans in inconsistent coordinate order;
   - provide same-thread recursive calculation where necessary rather than
     blocking on one's own incomplete future;
   - abandon the prototype if complexity or contention outweighs measured
     benefit.

For every prototype, record construction count, lock wait, throughput,
allocation, memory, and output hashes. A lock-free implementation that
duplicates large neighbourhood calculations may be slower than the current
lock.

### `BuildingInfo` success criteria

A change is worthwhile only if it delivers a repeatable improvement in a
scenario where `BuildingInfo` is a measured bottleneck, with:

- no generation-order dependence;
- no incomplete or partial publication;
- no cache growth regression;
- no change to legacy random decisions or generated output;
- no worse p99 latency from duplicated cold calculations.

## Phase 3: evaluate block materialization and section commit

### 3A. Measure the staging design

`ChunkDriver` stages a full nullable `BlockState[4096]` for every vertical
section in the build height, then scans non-empty staged sections and calls
`LevelChunkSection.setBlockState()` for non-null entries.

Measure:

- allocation and clearing cost of section arrays;
- percentage of allocated sections that receive writes;
- occupancy of non-empty sections;
- time in `SectionCache.put`, `putRange`, heightmap repair, and `generate`;
- time in `LevelChunkSection.setBlockState`;
- repeated writes to the same cell;
- cost of the mid-building `actuallyGenerate()` used before second-part
  overlays.

### 3B. Prototype measured alternatives

Evaluate independently:

- lazily allocate `S`/section arrays only when first written;
- track dirty index ranges or dirty bitsets to avoid scanning 4096 cells in
  sparse sections;
- specialize uniform vertical fills and air clears;
- reuse section scratch storage per worker without retaining large dirty
  object graphs;
- avoid redundant same-state writes and coalesce overwritten writes;
- reduce or remove the mid-building flush if overlay semantics can be
  preserved;
- cache safe transformed block states and part-local compiled palettes;
- bypass expensive adjacency correction for block types that cannot require
  it, while preserving structure-void semantics;
- batch heightmap effects and verify them against Minecraft's expected
  heightmap state.

Do not write directly into internal palettes or storage containers unless
Forge/Minecraft contracts, heightmaps, lighting, POI, block entities, and
upgrade compatibility are fully understood. The current
`LevelChunkSection.setBlockState(..., false)` path is a valuable correctness
boundary.

### 3C. Evaluate `generatePart()`

Measure and prototype:

- precomputed vertical-slice metadata for ordinary, air, hard-air, palette
  metadata, and transform-sensitive blocks;
- cached transformed states per `(BlockState, Transform)`;
- cached combined palettes per immutable building/part palette pair;
- fewer `ConditionContext`, `BlockPos`, `Pair`, and temporary-list
  allocations;
- fast paths for plain blocks with no loot, entity, POI, light, todo, or
  transform handling;
- run/range emission for repeated vertical values instead of one call per
  block.

Treat built-in building-part and palette formats as public data-pack
interfaces. Optimizations must compile the same input rather than narrow the
supported format.

## Phase 4: evaluate multithreading safely

### Preferred approach: scale independent chunks

Do not create a private Lost Cities executor initially. Minecraft already
schedules world-generation work and owns chunk dependencies. A second executor
can oversubscribe CPUs, violate chunk-status assumptions, complicate shutdown,
and compete with the server.

First measure scaling at 1, 2, 4, and the normal generation-worker count for
distant chunks. Record:

- chunks per second;
- CPU utilization;
- `BuildingInfo` lock wait;
- neighbourhood-lock wait and stripe collisions;
- allocation and GC;
- p95/p99 generation time.

If scaling stalls:

1. remove measured Lost Cities contention or excess work;
2. check whether the 3x3 neighbourhood is still required only for vine
   post-processing;
3. consider moving cross-edge vine correction to a safe later phase so the
   lock footprint can shrink, but only with deterministic edge ownership;
4. remeasure before adding any new parallel layer.

### Potentially safe parallel work

Work may be eligible for parallelism only when it:

- consumes immutable assets and coordinate-seeded inputs;
- does not access or load mutable chunks;
- does not use `WorldGenRegion`, registries during reload, Forge event buses,
  block entities, or mutable `BuildingInfo`;
- returns a fully built immutable result;
- is independent of request order and thread identity;
- can be cancelled or discarded safely during server shutdown/reload.

Possible examples to investigate are large pure road/network plans or
precompiled asset-derived tables. Even these should normally be computed on
demand by Minecraft's existing workers rather than eagerly submitted to a new
pool.

### Work that should remain single-owner per chunk

Keep these on the active generation worker unless a later proof establishes a
safe ownership model:

- `ChunkAccess` and `WorldGenRegion` mutation;
- `ChunkDriver` staging and section commit;
- heightmap updates;
- block-entity NBT and loot assignment;
- Forge generation events;
- POI and lighting todos;
- neighbour block corrections and vine fixes;
- any legacy random sequence whose consumption order affects existing worlds.

### Multithreading acceptance gate

Any concurrency change requires repeated sequential-versus-parallel generation
of identical seeds and coordinates in forward, reverse, and randomized order.
The resulting chunks must match in:

- block states;
- block entities and NBT;
- heightmaps;
- scheduled/post-generation results;
- structures, roads, railways, damage, vegetation, and loot-table identity.

It must also survive cache cleanup and client preview refresh between batches.
In addition, visually inspect seams, vines, doors, stairs, walls, rails,
multibuildings, spheres, and damaged structures.

If timing-dependent output or instability appears, restore the conservative
ownership boundary and reject the optimization.

## Phase 5: evaluate client meshing and geometry upload

### 5A. Prove which client phase is slow

Use a pre-generated world and fixed camera route so no server generation
occurs. Compare:

- standing still after all sections are compiled;
- rotating the camera without loading chunks;
- moving through already loaded chunks;
- loading pre-generated chunks;
- forcing section rebuilds.

Interpretation:

- slow only while new chunks arrive suggests decode, lighting, meshing, or
  upload;
- high worker CPU with a growing compile queue suggests mesh construction;
- completed meshes waiting on the render thread suggests upload or render
  scheduling;
- low CPU with saturated GPU suggests rendering complexity, fill rate, or
  memory bandwidth rather than upload;
- smooth dedicated-server generation but slow integrated play confirms a
  client-side problem.

### 5B. Attribute mesh cost to content

For slow Lost Cities sections, inventory:

- visible faces/vertices and uploaded bytes by render layer;
- solid versus cutout versus translucent blocks;
- fences, walls, panes, stairs, leaves, and other multipart/model-heavy
  blocks;
- block entities and animated renderers;
- light/POI-driven rebuilds;
- sections rebuilt more than once;
- differences between a dense building and a volume-equivalent simple-block
  control.

### 5C. Decide ownership

Use this decision:

1. **Lost Cities content causes excessive geometry:** optimize selected
   built-in assets or placement patterns only if appearance and data-pack
   compatibility can be retained.
2. **Repeated updates cause rebuilds:** reduce or batch Lost Cities updates
   after generation, if the profiler traces them to this mod.
3. **Minecraft section compilation is the bottleneck:** document renderer-mod
   compatibility and avoid duplicating a general renderer optimization here.
4. **GPU upload is independently dominant:** first reproduce on vanilla Forge
   and current drivers. Any Lost Cities hook would require a separate design
   covering render-thread rules, buffer lifetime, resource reload, multiple
   renderers, and graceful fallback. The default recommendation is to leave
   this to Minecraft or a dedicated renderer mod.

Do not make renderer-mod-specific classes a required dependency and do not
replace vanilla rendering from a world-generation mod without a separately
approved compatibility plan.

## Phase 6: evaluate other measured hotspots

Only execute these investigations if Phase 1 ranks them highly.

### Terrain and damage

- hoist invariant profile and height values out of inner loops;
- skip noise and block scans when profile settings make their contribution
  zero;
- reuse or flatten small arrays where allocation profiles justify it;
- reduce repeated `getBlock()` calls without losing overlay semantics;
- use sparse explosion/damage bounds where damage occupancy is low;
- distinguish algorithmic savings from changes that merely remove visual
  detail.

### Planner and remote sampling costs

- count terrain-height, biome, city-factor, and network-plan samples;
- confirm caches are keyed by all compatibility inputs;
- batch or memoize repeated coordinate-pure samples;
- avoid constructing full `BuildingInfo` when a raw city or level query is
  sufficient;
- do not eagerly load distant chunks to precompute roads or structures.

### Cache and memory behaviour

- profile steady exploration for at least twice `cacheCleanupSeconds`;
- record cache entry count, expiry churn, cleanup duration, and retained
  memory;
- compare a continuously moving player with repeated movement inside one city;
- tune policy only from the combined CPU/memory result;
- keep cleanup coordinated by the lifecycle lock.

## Benchmark comparison protocol

For each candidate:

1. use the same commit except for the isolated candidate;
2. use the same Java 17 runtime, JVM settings, hardware, seed, coordinate list,
   profile, and mod set;
3. start from equivalent world/cache state;
4. alternate baseline and candidate runs to reduce thermal or background bias;
5. collect enough repetitions for stable p95/p99 values;
6. compare server output before interpreting performance;
7. inspect flame graphs to verify that the intended cost actually moved;
8. report regressions as well as wins, including memory and other profiles.

Reject results when:

- chunk output differs unexpectedly;
- the candidate changes generated content density;
- warmup/cache conditions differ;
- measurement overhead is a material fraction of runtime;
- the improvement exists only in an unrepresentative microbenchmark;
- CPU time moves to another thread or later phase without improving end-to-end
  throughput or latency.

## Recommended implementation order after evaluation

The likely order, subject to measurements, is:

1. retain development-only phase/counter instrumentation;
2. take low-risk duplicate-work, allocation, and cache-hit improvements;
3. optimize `generatePart()` and `ChunkDriver` fast paths;
4. reduce `BuildingInfo` work and only then its lock scope;
5. improve independent-chunk scaling and reconsider the 3x3 lock footprint;
6. address content-driven client mesh costs if demonstrated;
7. avoid direct GPU upload or intra-chunk parallel mutation unless all safer
   opportunities are exhausted and the measured upper bound remains large.

Each implementation phase should be a separately reviewable change with its
own before/after evidence. Do not combine locking, generation semantics,
section storage, and client rendering changes in one patch.

## Deliverables

The evaluation is complete when it produces:

- a reproducible benchmark manifest and coordinate/scenario set;
- server CPU, allocation, contention, memory, and scaling profiles;
- client mesh/upload/GPU attribution on a pre-generated world;
- a dependency and fan-out map for `BuildingInfo`;
- a ranked optimization table with measured upper bounds;
- small A/B prototypes for only the top candidates;
- deterministic output comparisons and in-game visual inspection notes;
- a final recommendation divided into:
  - implement now;
  - investigate further;
  - leave to Minecraft/Forge/renderer mods;
  - reject because stability or compatibility risk exceeds the benefit.

## Human in-game verification checklist

Use the benchmark seed and inspect the same coordinates before and after every
accepted optimization. At minimum verify:

- dense default buildings, cellars, doors, stairs, walls, torches, loot, and
  block entities;
- city borders and corrected terrain;
- hierarchical streets, slopes, bridges, and inter-city highways;
- railways, stations, corridors, and dungeons;
- damaged buildings, rubble, vegetation, and debris;
- complete scattered multibuildings;
- sphere/space, floating, and cavern generation;
- chunk seams while approaching from different directions;
- client chunk appearance while loading, rebuilding, and reloading resources.

Performance work is not complete if it is faster but changes deterministic
generation, leaves partial structures, introduces visible seams, or makes
world loading less stable.
