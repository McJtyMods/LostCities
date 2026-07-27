# Lost Cities world-generation thread safety

This document describes the concurrency model used by Lost Cities world
generation. It is intended for developers changing generation code or writing
companion features that call into Lost Cities. The iteration history later in
the document records why the current boundaries exist.

## Current architecture

`LostCityFeature.runWithDimensionInfo()` is the required entry point for normal
and companion world-generation features. It provides two independent safety
boundaries:

- A lifecycle read lock covers the complete generation callback. Cache and
  registry cleanup takes the corresponding write lock and therefore cannot run
  while any chunk is generating.
- Ordered striped locks cover the active chunk's 3x3 neighbourhood. This lets
  distant chunks in the same dimension generate concurrently while preventing
  edge-touching work, currently vine post-processing, from racing with a
  neighbouring chunk. Locks are always acquired in numeric order and released
  in reverse order.

The old monitor which serialized an entire dimension no longer exists.
Different dimensions and non-overlapping neighbourhoods in one dimension can
run at the same time.

### Per-call generation state

`GenerationContext.open()` installs state for exactly one chunk on the current
worker thread and restores the previous context with `try`/`finally`. It owns:

- the active `WorldGenLevel`;
- the `ChunkDriver`, primer/region binding, positions, and section cache;
- deterministic palette variation and the terrain `RandomSource`;
- the current street palette character; and
- reusable rubble, leaves, ruins, and floating-bottom noise buffers.

The context reuses buffers and driver state on a worker thread to avoid adding
per-chunk allocation pressure. Nested contexts receive independent scratch
state. Code which needs the current driver, world, or generation random source
must use this context instead of adding mutable fields to
`LostCityTerrainFeature` or rebinding `DefaultDimensionInfo`.

### Shared state

Shared caches use concurrent maps or `TimedCache`. Expensive recursive
`BuildingInfo` calculations use one reentrant memoization lock per dimension;
this preserves recursive neighbour discovery without globally blocking other
dimensions. Lazy asset data is constructed completely before volatile
publication. Cleanup never replaces stable calculation locks while older
cached objects may still reference them.

Inter-city highway hub decisions also have an overworld `SavedData` backing
cache. Its dimension maps and dirty-state transitions are synchronized, while
the expensive terrain and biome calculation happens outside that monitor. The
planner publishes only the resulting immutable `HighwayHub` or empty-cell
decision. Never hold the persistence monitor while calculating a hub: one cold
planning query can sample thousands of remote terrain columns.

Scattered generation has an additional immutable area plan keyed by dimension,
world seed, and scattered-area coordinates. The plan contains the chosen
asset, footprint, complete-footprint validity, common height, highway-facing
rotation, and building choice. Highway-aligned candidate enumeration is
deterministic and is completed before the plan is published. Individual chunks
only read it and generate their own piece. Never
replace this with a decision based on whichever chunk happens to generate
first, and never eagerly generate the other chunks in a footprint.

### Rules for future changes

- Do not retain an `IDimensionInfo` beyond a
  `runWithDimensionInfo()` callback in companion generation code.
- Do not use a shared reseeded `Random`, `RandomSource`, `ChunkDriver`, mutable
  block position, or noise buffer. Put per-chunk state in `GenerationContext`
  or use a coordinate-seeded local value.
- A cached value must be immutable after publication, safely synchronized, or
  explicitly concurrent. `get` followed by `put` is not atomic construction.
- Do not clear or replace stable lock objects while stale cached values can
  still exist.
- Any generator which writes outside its own chunk must remain inside the
  neighbourhood boundary, or introduce an equally explicit ordered ownership
  scheme before widening that range.
- Deterministic decisions may depend on seed, dimension, coordinates, profile,
  and immutable assets. They must not depend on query order, cache warmth,
  thread identity, or generation timing.
- Keep cleanup under the lifecycle write lock. Datapack/asset reload must not
  overlap generation that reads those assets.

### Recommended verification

For concurrency-sensitive changes, generate the same seed repeatedly with
different starting positions and compare the result. Include colored scattered
multibuildings, normal cities, damage, rubble, vegetation, railways, and, when
the companion setup is available, sphere or space generation. A useful stress
test requests distant chunks in the same dimension concurrently and confirms
that sequential and parallel output is identical.

## Implementation history

The implementation was introduced incrementally so every checkpoint remained
runnable and retained a conservative synchronization boundary around state that
had not yet been isolated. The statuses below describe those implementation
checkpoints; the current design is documented above.

## Current correctness baseline

`LostCityFeature.runWithDimensionInfo()` holds a lifecycle read lock and ordered striped locks for the active chunk's 3x3 neighbourhood. `LostCitySphereFeature` uses that same entry point. Cleanup takes the lifecycle write lock, so it cannot overlap generation. Different dimensions and non-overlapping chunks in the same dimension may generate concurrently.

The local neighbourhood boundary remains because vine post-processing can touch blocks across chunk edges. All broader per-dimension generation serialization has been removed.

## Iteration 1: harden the serialized baseline

Status: implemented.

- Synchronize `LostCityFeature.place()`.
- Synchronize dimension-info lookup and cleanup.
- Make the global dirty counter visible across threads.
- Make sphere generation use the same monitor as normal Lost Cities generation.
- Keep scattered placement based on immutable raw city calculation.

Verification:

- Compile successfully.
- Generate several fresh worlds and confirm scattered multibuildings are either complete or absent.
- Exercise a sphere profile as well as the default profile.
- Check logs for chunk-generation exceptions.

## Iteration 2: harden hot shared utilities

Status: implemented.

- Make `TimedCache` concurrent without adding one coarse monitor to every lookup.
- Make global todo maps and queues safe for concurrent producers and server-thread draining.
- Replace shared reseeded coordinate and vegetation RNGs with reusable thread-local RNGs while preserving their existing Java `Random` sequences.
- Keep the serialized generation baseline active.

Verification:

- Compile and run the complete test task.
- Generate fresh default and sphere worlds under the existing correctness lock.
- Confirm todo processing neither loses newly queued work nor throws during concurrent additions.

## Iteration 3: replace the global lock with safe per-dimension locking

Status: implementation complete; runtime concurrency verification pending. Stable per-dimension locks, lifecycle coordination, and the shared companion-generation API are active. Different dimensions may generate concurrently, while chunks within one dimension remain serialized.

Completed prerequisites:

- Scope legacy highway noise by dimension and make its result caches concurrent.
- Safely publish immutable predefined-city and occupied-chunk lookup maps.
- Make city rarity caching concurrent.
- Serialize one-time asset loading and safely publish registry assets and inherited city styles.
- Add a lifecycle read/write boundary so cleanup cannot overlap generation once the global fallback is removed.
- Replace the global city-sphere cache monitor with stable per-dimension cache locks.
- Make public Lost Cities information caching concurrent.
- Make palette-entry deduplication safe during concurrent datapack decoding or reload.
- Remove the global generation monitor after auditing shared cross-dimension state.

Introduce stable generation locks keyed by dimension. Protect dimension-info creation and lifecycle cleanup separately so two threads can never create different `DefaultDimensionInfo` instances for the same dimension.

Required properties:

- Normal and sphere generation for the same dimension use the same lock.
- Different dimensions may generate concurrently.
- Cleanup cannot clear registries or cached dimension information while any dimension is generating.
- Companion entry points have one documented generation-lock API instead of duplicating synchronization.

The global monitor was kept active while per-dimension locking, lifecycle coordination, and shared-cache protection were introduced. It has now been removed after the shared cross-dimension state audit; runtime concurrent-dimension verification remains required before Iteration 3 is considered fully verified.

Verification:

- Repeat all Iteration 1 and Iteration 2 checks.
- Generate two Lost Cities dimensions concurrently.
- Add a stress test that requests the same dimension from multiple threads and asserts that only one dimension-info instance is created.

## Iteration 4: isolate per-chunk generation state

Status: implementation complete; runtime verification pending. The per-dimension generation lock remains active.

Completed:

- Add a scoped thread-local `GenerationContext` installed for one terrain chunk with `try`/`finally` semantics.
- Seed palette, leaf, and rubble block variation deterministically from world seed, dimension, and chunk coordinates.
- Remove the shared generation-order-dependent `gSeed` sequence.
- Restore the previous chunk-driver binding even when generation throws.
- Keep non-worldgen palette callers functional with a thread-local fallback sequence.
- Move the current street palette character into the per-call context.
- Move rubble, leaves, ruins, and floating-bottom noise buffers into reusable thread-local scratch storage.
- Reuse scratch arrays between chunks on the same worker thread to avoid per-chunk allocation pressure.
- Move the terrain `RandomSource` into reusable per-worker context state and reset it deterministically for every chunk.
- Preserve the existing explicit random reseeds for stuff placement and explosion/debris generation.
- Move `ChunkDriver`, including its active region, primer, section cache, and mutable positions, into reusable per-worker context state.
- Route normal terrain helpers and the separate sphere feature through the scoped context.
- Restore sphere and normal driver bindings with `try`/`finally` semantics.
- Safely publish immutable lazy block-state lookup sets and variation arrays.
- Make the block-entity-type lookup cache concurrent.
- Initialize base and liquid block states as immutable constructor state.

The per-call/thread-local generation context now owns:

- `ChunkDriver` and its active region, primer, section cache, and mutable positions.
- Per-chunk random source.
- Current street palette character.
- Noise output arrays and small scratch buffers.
- Other values that are reseeded or overwritten for each chunk.

The per-dimension lock remains in place. Parallel same-dimension chunk generation must only be enabled in a test configuration until output is deterministic and the later cache audits are complete.

Verification:

- Compare generated chunk output between sequential and randomized parallel generation for identical seeds and coordinates.
- Repeat scattered multibuilding tests with deliberately colored pieces.
- Confirm normal cities, damage, rubble, vegetation, railways, and spheres remain deterministic.

## Iteration 5: make remaining shared helpers and caches concurrency-safe

Status: implementation complete; runtime verification pending. The per-dimension generation lock remains active.

Completed first slice:

- Make rolling generation statistics safe for concurrent generation updates and command reads. The only write-side synchronization occurs once per generated chunk.
- Atomically compute and publish immutable biome cache entries.
- Remove the global `MultiChunk.getOrCreate()` class monitor now that `TimedCache.computeIfAbsent()` provides per-key atomic construction.
- Replace the railway synchronized-map wrapper and its compound `containsKey`/`get` access with a concurrent map and `putIfAbsent` publication.
- Preserve railway removal precedence when calculation and collision suppression overlap.

Completed second slice:

- Safely publish cached `BuildingInfo` neighbours, monorail decisions, and ocean classification.
- Fully construct compiled palettes and damage areas before publishing them, with lock-free cached reads.
- Coordinate recursive legacy bridge and stair memoization with one stable lock per dimension instead of per-object locks that could deadlock in opposing neighbour order.
- Keep memoization locks stable across cache cleanup so stale and newly created `BuildingInfo` references cannot use different locks for the same dimension.

Completed final slice:

- Replace global synchronized `BuildingInfo` creation, chunk-characteristic, and city-level calculation with one reentrant lock per dimension.
- Preserve recursive same-dimension calculation behavior while allowing unrelated dimensions to calculate concurrently.
- Coordinate terrain-correction height memoization through the same dimension lock.
- Build vertical building-part slices completely before volatile publication.
- Safely resolve referenced building and building-part palettes once, while keeping cached reads lock-free.
- Audit world-generation random sources; remaining coordinate and vegetation reseeding uses thread-local instances, while other generation random sources are locally constructed or context-owned.

Audit and fix shared mutable helpers before removing the per-dimension lock:

- Replace static reseeded `Random` instances with local coordinate-seeded random sources.
- Verify that no shared generation-order-dependent random sequence remains.
- Make `TimedCache` and other shared maps safe for concurrent access.
- Continue auditing lazy initialization outside `LostCityTerrainFeature`; its generation lookup state is already safely published.
- Review mutable bridge calculations and `BuildingInfo` memoization.
- Make statistics and diagnostic counters concurrency-safe.

Verification:

- Run focused cache tests under concurrent access.
- Run the sequential-versus-parallel chunk comparison again.
- Run generation repeatedly with cache cleanup occurring between batches.

## Iteration 6: cache an immutable scattered-area plan

Status: implementation complete; runtime verification pending. The per-dimension generation lock remains active.

Completed:

- Cache one immutable plan per dimension, world seed, and scattered-area coordinate.
- Select the scattered asset from the area's canonical origin instead of whichever footprint chunk happens to query first.
- Store the selected asset, footprint, complete-footprint validity, common generation height, and building choice in the plan.
- Replay the deterministic area-random call sequence independently for each participating chunk instead of sharing mutable random state.
- Keep block placement chunk-local; calculating a plan never generates or loads distant chunks eagerly.
- Clear plans during server lifecycle cleanup and client preview refresh.

Represent the decision for one scatter area as an immutable value containing the selected asset, footprint, validity, and common generation height. Compute it once with a thread-safe `computeIfAbsent` keyed by dimension, seed, and scatter-area coordinates.

Chunks must only read this plan; generating one chunk must not mutate the decision observed by another chunk. Do not generate distant chunks eagerly from the anchor chunk because that would violate normal world-generation boundaries.

Verification:

- Query every chunk in an area in forward, reverse, and randomized parallel order and assert an identical plan.
- Confirm invalid structures generate no pieces and valid multibuildings generate every piece once their chunks reach full generation.

## Iteration 7: remove the per-dimension generation lock

Status: implementation complete; runtime parallel-generation verification pending.

Completed:

- Remove the stable per-dimension generation monitor.
- Retain the lifecycle read/write boundary so cleanup cannot overlap active generation.
- Allow non-overlapping chunks in the same dimension to generate concurrently.
- Protect the 3x3 neighbourhood around each active chunk with ordered striped locks because vine post-processing can touch blocks across chunk edges.
- Store the active `WorldGenLevel` in the scoped `GenerationContext` instead of rebinding shared dimension state for every chunk.
- Keep immutable seed and dimension identity in `DefaultDimensionInfo`, with only a volatile fallback world for non-generation callers.
- Replace shared dimension and world random use in damage and vine generation with deterministic context-owned random state.
- Return a thread-local compatibility `Random` from `DefaultDimensionInfo.getRandom()` for companion callers.

Remove or relax the lock only after all per-call state is isolated and all shared caches used by generation are concurrency-safe.

Final verification:

- Sequential and parallel generation produce identical chunk data for the same seed.
- Repeated fresh-world tests produce no partial scattered multibuildings.
- Default, sphere, floating, cavern, and space profiles complete without generation exceptions.
- Companion mods can use the documented entry point without bypassing required lifecycle protection.

If any verification fails, restore the most recent conservative lock before continuing. No iteration should be handed off with a known timing-dependent generation path.
