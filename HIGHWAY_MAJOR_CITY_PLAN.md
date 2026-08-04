# Major-City Highway Coverage Plan

## Status

This document is an implementation plan for improving the unreleased
`INTERCITY_NETWORK_V1` highway planner before Lost Cities 7.5.0. It does not
describe behavior that has already been implemented.

The work should evolve the current highway mode in place. A new persisted
highway-generation mode is unnecessary while 7.5.0 remains unreleased.

## Goal

Make the highway network approximate the following rule:

> Every major city concentration should have a highway connection to at least
> one nearby major city, when a compatible city and route exist within a
> bounded search distance.

Here, a "major city" means a sufficiently large concentration of approximate
city potential, not merely one chunk whose potential crosses a threshold.

The result does not need to identify final cities perfectly. Highway planning
must remain independent of `BuildingInfo`, generated chunks, query order, and
other state that would make remote planning recursive or nondeterministic.

## Non-goals

- Do not return to fixed highway rows or columns.
- Do not require the complete world highway graph to be built in memory.
- Do not guarantee a highway in a region where no viable city concentration
  exists.
- Do not guarantee that the complete network is one connected component.
- Do not make final city decisions depend on highways or vice versa.
- Do not change highway assets or rendering unless testing exposes a separate
  rendering problem.

## Problems in the current planner

The current planner selects at most one hub from each planning cell by taking
the greatest point potential on a cell-local sample lattice. This has several
limitations:

1. The default 16-chunk sample spacing can miss useful parts of a city region.
2. Point potential is a weak proxy for the total size of a city concentration.
3. Point potential is clamped to `1`, so a very strong concentration may be
   indistinguishable from an ordinary strong point.
4. Planning-cell boundaries can cause one concentration to be represented
   poorly or by competing hubs in adjacent cells.
5. A hub is not a highway guarantee. The mutual-selection rule can leave a
   valid hub with no accepted connections.
6. Railway-line avoidance, distance limits, and minimum route length can
   remove all otherwise suitable neighbours.
7. Lowering `highwayHubMinimumPotential` only affects hub eligibility. It does
   not solve sparse sampling or rejected connections.

## Constraints to preserve

- All results must be deterministic for world seed, dimension, profile, and
  coordinates.
- Results must be independent of chunk generation and query order.
- Every query must inspect a bounded coordinate neighbourhood.
- Hash ties and iteration order must be explicit and stable.
- Caches may improve performance but must never affect results.
- Negative coordinates must continue to use floor-based cell arithmetic.
- The planner must not force-load or generate remote chunks.
- Existing terrain-height and biome restrictions must remain part of hub
  viability.
- Highway queries must remain safe during concurrent world generation.

## Proposed direction

Keep planning cells as spatial indexes and cache keys, but make their candidate
represent a detected city concentration rather than the strongest isolated
point. Then give every accepted major-city hub one mandatory connection when a
valid nearby target exists. Additional connections can retain the current
sector-diverse, mutual-selection behavior.

This is deliberately an incremental approximation. Initially, a planning cell
may still contribute at most one hub. Supporting multiple independent major
cities in one planning cell can be considered later if visual testing shows it
is necessary.

## Phase 1: Establish diagnostics and acceptance criteria

Before changing placement, extend `/lostcities debug` output for the current
and nearby planning cells with:

- the best point potential;
- the calculated city-concentration score;
- the winning sample coordinate;
- whether the raw candidate was suppressed as a duplicate;
- why a candidate did not become a hub;
- why each possible connection was rejected;
- which connection is the hub's mandatory connection; and
- whether the hub has zero accepted connections.

Use concise reason identifiers such as `BELOW_MASS`, `BELOW_ENDPOINT`,
`SUPPRESSED`, `TOO_NEAR`, `TOO_FAR`, `TOO_SHORT`, and `RAILWAY_CONFLICT` so
seed comparisons are practical.

Evaluate seeds using measurements rather than only total highway count:

- detected major-city hubs per square region;
- percentage of detected hubs with at least one accepted connection;
- distribution of distance from a detected hub to its first highway segment;
- count and length of dry travel corridors in several directions from spawn;
- number of apparent duplicate hubs inside one visible city; and
- typical and worst remote-planning cost per queried chunk.

Primary acceptance target:

> Every detected major-city hub with at least one route-compatible major-city
> hub inside the emergency search bound has at least one accepted connection.

## Phase 2: Calculate a robust city-concentration score

### Sampling

Continue using `ApproximateCityPotential`, including spawn-distance, terrain,
and biome adjustments. Reduce the effective sample spacing to 8 chunks for the
first implementation and compare its planning cost with the current default of
16.

Prefer a globally stable jittered sample lattice over one shared random X/Z
offset for an entire planning cell:

1. Divide space into sample tiles of `highwayHubSampleSpacing` chunks.
2. Hash the world seed, dimension, and sample-tile coordinates.
3. Select one deterministic coordinate inside each tile.
4. Enumerate all tiles overlapping a planning cell.

This retains bounded sampling, avoids straight visible placement patterns, and
does not introduce a discontinuous sampling pattern at planning-cell borders.

If changing the lattice is judged too invasive for 7.5.0, first use spacing 8
with the current cell-local lattice. The concentration scoring and connection
guarantee are more important than perfect sampling.

### Concentration score

For every candidate sample, calculate a bounded weighted average of potential
around it. A suitable first stencil is the center plus samples 8 and 16 chunks
away on the cardinal and diagonal directions. Nearby samples should have
greater weight.

Conceptually:

```text
cityMass(sample) = weightedAverage(
    potential at sample,
    potential at nearby stencil points
)
```

Although each individual potential is clamped, their weighted average measures
the breadth of the city-like area. A broad concentration therefore scores
higher than one narrow peak. This avoids requiring a new raw-potential API in
the initial implementation.

Keep two distinct tests:

- **city mass:** Is the surrounding concentration large enough to deserve a
  highway hub?
- **endpoint viability:** Is the selected hub chunk itself sufficiently city-
  like and allowed by terrain and biome constraints?

Do not let a high surrounding mass promote an endpoint that has zero potential
because its terrain or biome forbids cities.

### Selecting the endpoint

Rank samples within a planning cell by:

1. greater quantized city mass;
2. greater endpoint potential;
3. fewer railway routing restrictions;
4. stable unsigned coordinate hash; and
5. fixed coordinate order.

Consider a small deterministic local endpoint search around the mass maximum.
This allows the hub to remain associated with the same city while avoiding a
chunk coordinate that makes both L-shaped routes conflict with reserved
railway lines.

All floating-point scores used for ordering should be quantized before
comparison, as point potential is today.

## Phase 3: Suppress duplicate representations

A broad city near a planning-cell boundary can produce strong candidates in
multiple cells. Add a raw-candidate cache separate from the final hub cache so
final hub calculation can inspect neighbouring raw candidates without
recursion.

A raw candidate survives when it is the strongest candidate within a bounded
suppression distance. Compare candidates using the same quantized mass,
endpoint potential, stable hash, and coordinate ordering everywhere.

Start with a suppression distance based on the concentration stencil radius,
not the complete planning-cell size. This should remove obvious duplicates
without merging visibly separate cities.

If suppression makes the behavior worse or adds too much risk before 7.5.0,
defer it. Duplicate hubs are less harmful than missing or disconnected major
cities.

## Phase 4: Guarantee the first connection

Split neighbour selection into two layers.

### Mandatory connection

Every hub selects its best route-compatible target within the ordinary search
bounds. Accept this first connection when either endpoint selects the other;
mutual selection is not required.

Rank the mandatory target primarily by distance, then by target city mass,
route penalty, sector, and stable pair hashes. Exact ordering must be documented
and covered by deterministic checks already appropriate to this codebase.

This gives every hub at least one accepted edge when it has a candidate. A
popular major city may receive more incoming edges than
`highwayMaximumConnectionsPerHub`. Treat that setting as the maximum number of
outgoing selections, not a strict accepted-degree cap. A regional city acting
as a highway nexus is preferable to disconnecting surrounding cities.

### Additional connections

After the mandatory connection, use the current sector-diverse selection for
the remaining configured selections. These optional edges may retain mutual
acceptance to control parallel routes and overall density.

The first implementation should therefore behave as:

```text
accepted edge =
    either endpoint chose the edge as mandatory
    OR both endpoints chose the edge as optional
```

Canonical connection keys and lexicographic route ownership remain unchanged.

## Phase 5: Bounded emergency neighbour search

If a major-city hub has no compatible target in the ordinary search radius,
perform a second bounded search with a larger emergency radius and maximum
distance. Only the mandatory connection uses this search.

The emergency search should:

- consider detected major-city hubs only;
- retain the minimum-distance and minimum-route-length rules;
- reject routes with no railway-safe L shape;
- have a conservative hard maximum to bound generation cost; and
- prefer the nearest valid target strongly enough to avoid unnecessary
  cross-country routes.

If no compatible target exists inside that bound, an isolated hub is an
expected result and should be reported clearly by the debug command.

Do not silently reuse `highwayHubSearchRadiusCells` for this behavior unless
its documented meaning is changed. An explicit emergency radius makes the
coverage/performance tradeoff understandable.

## Configuration approach

Avoid exposing every implementation constant. Initially retain the existing
options and add only controls that have clear user-facing meaning:

- a minimum major-city concentration score;
- a concentration sampling radius or preset, if one fixed radius is not
  adequate across profiles; and
- an emergency hub search radius or distance.

Reassess `highwayHubMinimumPotential`: it should describe endpoint viability,
while the new concentration threshold describes whether a city is major
enough. The names and descriptions must make that distinction clear.

If an existing option changes meaning before release, update its description
rather than retaining misleading compatibility with an unreleased algorithm.
Validate all radius and distance relationships in `LostCityProfile` and
`HighwayPlannerSettings`.

Any implementation must update `docs/profile_options.md` with defaults,
ranges, and behavior, and update the highway section of
`docs/city_generation.md`. User-visible changes must also receive a concise
entry in the current 7.5.0 section of `changelog.txt`.

## Performance safeguards

Concentration scoring multiplies potential queries, and server-side potential
queries include remote height and biome lookup. Control that cost by:

- using a small fixed stencil;
- caching quantized point potentials by chunk coordinate;
- caching raw cell candidates separately from final hubs;
- bounding suppression and emergency searches in planning-cell units;
- avoiding allocation-heavy collections in inner sample loops; and
- retaining bounded caches whose eviction cannot alter results.

Measure cold-cache and warm-cache behavior. Do not assume that a lower sample
spacing is acceptable merely because an individual debug query is fast.

## Implementation sequence

1. Add diagnostic reason data and coverage-oriented debug output.
2. Add cached concentration scoring while retaining the current hub and graph
   decisions; compare representative seeds.
3. Select hubs using concentration plus endpoint viability.
4. Reduce sample spacing or introduce the global jittered lattice, based on
   measured cost.
5. Add the mandatory first-connection rule.
6. Add the bounded emergency search.
7. Add duplicate suppression if visual results show repeated hubs within one
   city.
8. Tune defaults across ordinary and Perlin/noise city profiles.
9. Update configuration documentation, architecture documentation, and the
   7.5.0 changelog entry as implementation behavior lands.
10. Compile with `./gradlew compileJava`, then perform the in-game validation
    below.

The mandatory connection may be implemented before duplicate suppression if a
smaller initial change is desired. It directly addresses disconnected major
cities and is largely independent of the scoring refinement.

## In-game validation

For each selected seed, inspect the same broad area before and after the change
using at least:

- a normal positive-`cityChance` profile;
- a `cityChance = -1` noise-city profile;
- a profile with restrictive biome multipliers;
- terrain near the configured minimum and maximum city heights;
- an area with railways enabled; and
- a sparse-city or spawn-distance-modified profile.

Verify visually that:

- the largest visible city regions normally have a highway departure;
- departures reach another visible city region rather than arbitrary terrain;
- small isolated city fragments are not all promoted to hubs;
- one city is not represented by several short redundant highways;
- highway placement does not reveal a regular global grid;
- railway stations and descents do not replace highway chunks;
- emergency connections are not excessively long or common; and
- querying or generating the same chunks in a different order produces the
  same network.

Record seeds and coordinates for both good and bad examples. Those locations
should remain the manual regression set while tuning the unreleased V1 mode.

## Possible later improvements

These are intentionally outside the first implementation:

- multiple hubs per planning cell for two clearly separate major cities;
- connected-component repair across a larger regional hierarchy;
- shared highway trunks for several nearby cities;
- route geometry beyond one- or two-segment L shapes;
- separate handling of predefined cities and city spheres; and
- a dedicated visualization command or map export for planner diagnostics.

The initial work should be considered successful when hub detection tracks
large visible city concentrations more reliably and almost every detected hub
with a reachable neighbour receives at least one highway, even if the complete
network is not globally connected.
