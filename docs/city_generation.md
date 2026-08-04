# Lost Cities city generation

This document describes the city-generation path that starts at
`LostCityTerrainFeature.generate()`. That method is called once for every 16x16
Minecraft chunk. The emphasis here is on city chunks and, in particular, the
decision between a normal street, a park, a single-chunk building, and a
multi-chunk building.

There are two explicitly versioned street-generation modes:

- `LEGACY` preserves the original per-chunk building/street decisions described
  later in this document.
- `HIERARCHICAL_GRID_V1` supplies a deterministic global primary, secondary and
  tertiary road field. It is the requested default for newly created worlds.

Highway generation is versioned independently:

- `LEGACY` preserves the Perlin-run placement described in section 6.
- `INTERCITY_NETWORK_V1` creates sparse deterministic routes between approximate
  city-region hubs. It is the requested default for newly created worlds.

A world can therefore use any street/highway mode combination. Hierarchical
streets do not implicitly enable the inter-city highway planner.

World-generation concurrency and cache invariants are documented separately in
[`thread_safety.md`](thread_safety.md).

## Persisted mode selection and old-world compatibility

The actual mode is stored per dimension in the overworld's existing Minecraft
`SavedData` system by `LostCityWorldGenData`. Its data name is
`LostCityWorldGenData`; it contains independent street/highway new-world markers
and independent maps from dimension resource-location strings to mode names.
The NBT keys are `newWorldStreetModes`, `newWorldHighwayModes`, `streetModes`
and `highwayModes`.

Old and new worlds are distinguished as follows:

1. A default-constructed `LostCityWorldGenData` means no saved file was found.
   It has no new-world markers, so any missing dimension mode resolves to
   `LEGACY`. Merely loading an old world does not opt it into new generation.
2. `LevelEvent.CreateSpawnPosition` is the explicit new-world lifecycle signal.
   At the beginning of that event Lost Cities writes the new-world marker.
3. The first `DefaultDimensionInfo` for a dimension then persists that profile's
   requested `streetGenerationMode` and `highwayGenerationMode` separately.
   Their new-world defaults are `HIERARCHICAL_GRID_V1` and
   `INTERCITY_NETWORK_V1`; a profile can request `LEGACY` for either.
4. On reload, each persisted dimension value wins independently. Later profile
   edits cannot silently switch either selected mode.

No generated-chunk test is used. An old world with no Lost Cities generation
data remains legacy even if its current profile requests a new mode. The
highway marker has its own NBT key, so a world which already persisted a street
mode before highway versioning was added still resolves a missing highway mode
to `LEGACY`. If dimension info was requested unusually early during new-world
startup, the create-spawn handler invalidates that cached info before resolving
it again.

Inter-city hub results use a separate overworld `SavedData` named
`LostCityHighwayData`. This is a performance cache rather than mode-selection
state: it stores the zero-or-one hub result, including an empty result, for each
evaluated planning cell and dimension. A present hub includes its city level;
routes and per-chunk highway occupancy remain derived data. A versioned
signature covers the world seed, hub settings, city-potential profile inputs,
terrain-height limits, city-level height thresholds and selected world-style
ID. A mismatch drops only that dimension's hub cache and lets the planner
recalculate it.

## `HIERARCHICAL_GRID_V1` mathematical street field

`HierarchicalStreetPlanner` is a pure global function of world seed, stable
dimension ID, versioned settings and chunk coordinates. It has no dependency on
`BuildingInfo`, generated chunks, mutable random generators, cache contents,
query order, or thread scheduling. It currently uses no cache.

The planner uses a stable FNV-derived dimension hash, a SplitMix-style 64-bit
mixing function, a fixed version salt, and dedicated fixed salts for primary X,
primary Z, density, secondary counts, secondary positions, and each tertiary
property. Java object hash codes and unordered collection iteration are not part
of persistent seed logic.

### Primary roads

Primary roads use an irregular but bounded candidate lattice. Candidate
north/south and east/west corridors default to eight chunks apart. Their exact
world/dimension offsets are:

```text
primaryOffsetX = floorMod(hash(PRIMARY_X_SALT, 0, 0, 0), candidateSpacingX)
primaryOffsetZ = floorMod(hash(PRIMARY_Z_SALT, 0, 0, 0), candidateSpacingZ)

candidateX = floorDiv(chunkX - primaryOffsetX, candidateSpacingX)
candidateZ = floorDiv(chunkZ - primaryOffsetZ, candidateSpacingZ)

verticalCandidate   = floorMod(chunkX - primaryOffsetX, candidateSpacingX) == 0
horizontalCandidate = floorMod(chunkZ - primaryOffsetZ, candidateSpacingZ) == 0
```

Every `primaryRoadForceEvery`th candidate is active. Each remaining candidate
is independently active when its dedicated axis activation hash is below
`primaryRoadOptionalChance`. With the defaults, candidate lines are eight chunks
apart, every fourth candidate is forced, and optional candidates have a 0.45
chance. Consecutive active primary gaps are therefore 8, 16, 24 or at most 32
chunks. A skipped candidate is the mathematical equivalent of a missing primary
road; adjacent active candidates produce unusually close primary roads.

Activation of a vertical corridor depends only on its X candidate index, and
activation of a horizontal corridor only on its Z candidate index. Accepted
roads are consequently straight and continuous across the entire dimension.
Here `hash` includes the world seed, stable dimension hash, V1 version salt,
stream salt and explicitly mixed integer coordinates. City clipping may still
prevent portions from rendering.

### Primary-block coordinates and negative chunks

For any chunk, floor-safe division first finds the candidate at or before the
coordinate. The planner then checks backward for the first active west/north
candidate and forward for the next active east/south candidate:

```text
candidateX = floorDiv(chunkX - primaryOffsetX, candidateSpacingX)
candidateZ = floorDiv(chunkZ - primaryOffsetZ, candidateSpacingZ)

primaryBlockX = activeCandidateAtOrBefore(candidateX)
primaryBlockZ = activeCandidateAtOrBefore(candidateZ)

primaryWestX  = primaryOffsetX + primaryBlockX * candidateSpacingX
primaryNorthZ = primaryOffsetZ + primaryBlockZ * candidateSpacingZ
primaryEastX  = coordinateOf(nextActiveCandidate(primaryBlockX))
primarySouthZ = coordinateOf(nextActiveCandidate(primaryBlockZ))
```

`Math.floorDiv` and `Math.floorMod` are intentional. Truncating division would
assign negative chunks inconsistently and create a seam around coordinate zero.
Forced candidates guarantee each directional lookup examines at most
`primaryRoadForceEvery` candidates. There are no independently planned finite
regions, mutable searches or planner-region borders to reconcile. The active
candidate indices serve as stable variable-sized primary-block identifiers.

### Secondary roads and density

Each primary block has one density value in `[0,1)`, obtained from its block
coordinates with the dedicated density salt. For each axis:

1. The common block density contributes 75 percent and a dedicated axis count
   hash contributes 25 percent.
2. That value chooses a count in the configured inclusive min/max range. The
   defaults are zero through two internal roads per axis.
3. Every integer position between `minimumRoadEdgeDistance` and the same
   distance from the opposite primary boundary becomes a candidate.
4. Candidates are ordered by their dedicated coordinate hash, with numeric
   position as a deterministic tie-breaker.
5. Candidates are greedily accepted while remaining at least
   `minimumRoadSeparation` chunks from already selected parallel roads.
6. Accepted positions are sorted numerically for stable diagnostics.

A vertical secondary line spans its entire primary block from the north primary
road to the south primary road; a horizontal one spans west to east. Thus every
secondary connects to both bounding primary roads. If settings request more
roads than can physically fit, the bounded candidate pass safely yields fewer.

The density is deliberately based on stable block randomness only in V1. It does
not sample final buildings, terrain, generated chunks, or cached city state.

### Tertiary access roads

Primary and secondary lines divide a primary block into smaller open cells. Each
cell independently reconstructs at most one tertiary segment:

1. A dedicated cell hash is compared with `tertiaryRoadChance` (default 0.40).
2. Another hash selects one of the north, south, west or east bounding roads.
   If that side cannot physically fit the configured minimum length and a safe
   origin, the other sides are checked in deterministic order. This avoids
   silently losing access roads in narrow rectangular cells.
3. The origin is placed on that existing road, at least two chunks away from its
   intersections.
4. The direction points straight into the cell.
5. Independent hashes choose a configured length (default 2 through 5), clamped
   so at least one non-road chunk remains before the opposite road.

The origin itself retains its primary/secondary type. Chunks after it are
`TERTIARY`; the segment is contiguous and ends deliberately. This produces
access roads and dead ends without per-chunk threshold noise or zigzags.

`PlannedStreetInfo` retains primary-block data, density, secondary positions,
four directional connections and optional tertiary identity/origin/direction/
length for diagnostics and later renderers.

### Raw field versus effective city roads

The planner always returns the raw mathematical road field, including outside
cities. `BuildingInfo` separately calculates an effective road:

```text
effective road = raw road
                 AND current chunk isCityRaw
                 AND at least one connected road neighbor isCityRaw
                 AND no higher-precedence explicit/accepted content occupies it
```

The neighbor test removes isolated fragments at one-chunk city protrusions and
uses only lower-level raw city membership. Consequently it preserves existing
height, biome, rarity, sphere, void and city-factor clipping without depending
on final building decisions. Roads can end at the irregular city boundary.

Raw queries intentionally do not perform city clipping. This is also the bridge
abstraction boundary: `HierarchicalBridgePlanner` can follow a raw `PRIMARY`
line through terrain where the road itself is clipped out, without making the
global street field depend on terrain.

### Planned primary bridges

In hierarchical mode, a non-city chunk on a raw primary line is a bridge
candidate when it is water-like. Water-like means either an existing Lost
Cities water biome or a deterministic base height below sea level; the latter
catches inland lakes whose biome is still plains or forest. The bridge resolver
scans in both directions along that same primary line, up to
`plannedPrimaryBridgeMaxLength` chunks (default 12), and accepts the canonical
span only when:

- every intervening chunk is non-city, water-like and on the raw primary line;
- both ends are effective primary-road city chunks at city level zero; and
- one deterministic chance roll for the whole span passes.

The chance hash uses the world seed, stable dimension identifier, orientation,
both endpoint coordinates and a dedicated bridge salt. Therefore every chunk
in a span reconstructs the same result without generated-neighbor state or a
shared random stream. At a crossing of horizontal and vertical candidate spans,
a seed/dimension-stable orientation wins; this prevents two bridge parts from
trying to occupy the same chunk. Higher-level bridges need ramp assets and are
intentionally not supported yet.

The existing bridge renderer is reused, but hierarchical bridges select from
the city style's optional `largebridges` list. The built-in
`bridge_large_open` part carries the same fourteen-block smooth-stone deck and
two-row quartz center marking as a primary road. Datapacks can replace or add
weighted bridge parts through that selector. A style without `largebridges`
falls back to its ordinary bridge part. Primary-road endpoint topology treats
the bridge as a straight primary continuation.

### Hierarchical content precedence

The effective order in hierarchical mode is:

1. Existing hard exclusions and special sphere/infrastructure constraints.
2. Predefined buildings and predefined multi-buildings.
3. Predefined streets.
4. Accepted random multi-buildings.
5. Effective planned roads.
6. Ordinary single-building chance and the existing lonely-building veto.
7. A bounded non-road fallback.

An effective planned road forces `couldHaveBuilding`/`hasBuilding` false and is
always assigned `StreetType.NORMAL`; it cannot be nominated as a park. The
existing street renderer selects its topology from final neighboring road
classifications. Primary roads use the city style's `largeparts` set, secondary
roads use `parts`, and tertiary roads use optional `tertiaryparts`. When
`tertiaryparts` is omitted it falls back to `parts`. The built-in large pieces form
a fourteen-block-wide full-height smooth-stone-slab surface, retain the normal
outermost block on each side, and use two centered rows of smooth quartz. The
set includes straight, end, bend, T, four-way, isolated and full pieces. Because
both widths are centered in the chunk, the existing eight-block minor streets
enter the fourteen-block primary streets without a gap. Only neighboring primary
roads participate in large-part topology selection: a touching secondary or
tertiary street does not turn a primary end or straight piece into a bend or
junction, so the smooth-quartz center marking never points into a minor street.
After the main part is placed, the renderer rotates and overlays the large part
set's configurable `connector` part on every edge with a connected minor road.
The built-in connector fills only the centered eight blocks in the primary
road's otherwise untouched outer row, closing the verge gap without adding a
quartz marking. A datapack can replace it, provide a list of alternatives, or
use an empty list to disable connector overlays for a city style.

Hierarchical roads can also bridge a one-level city-height difference with the
full-chunk `stair` street part. The lower road chunk becomes a slope only when
it has exactly one same-class road one level higher, same-level roads of that
class continuing directly behind and beyond the transition, and no same-level
same-class side-road branches at either end. Primary roads form one class;
secondary and tertiary roads form the minor-road class. This keeps bends and
intersections flat and avoids ambiguous slopes without allowing a connected
minor road to block an otherwise straight primary slope. The upper road
includes the slope in its topology, so it continues to the chunk edge instead
of ending. Its retaining wall is opened only across the stair part's `z1`/`z2`
bounds. The default primary-road slope is `street_large_stair`, with the same
fourteen-block surface and centered quartz marking as the other large street
parts. Legacy street generation does not use this rule. Fountain and park
parts, random vegetation, building-front overlays, and the older narrow stair
decoration are suppressed on a sloped street chunk so that its route remains
clear. Buildings alongside a sloped primary, secondary, or tertiary street do
not generate doors facing that slope, because the changing street surface would
partially or completely obstruct the doorway.

When a non-road city chunk fails its ordinary building roll or a later building
veto, it becomes a grass open lot rendered through the existing park surface.
The hierarchical `OPEN_LOT_PARK_CHANCE`, which defaults to `0.8`, decides
whether a weighted park part is placed in that lot. It does not turn the lot
into a road. This prevents failed building rolls from recreating the old dense
random street network while keeping most open areas visually varied. The
legacy `PARK_CHANCE` and city-style `parkchance` override do not affect
hierarchical open lots.

Predefined streets remain normal streets. Same-level highways and surface
stations can still suppress ordinary street surface rendering through the
existing `generateStreet()` checks; railways, clearance, structure exclusion,
city spheres and void handling otherwise retain their existing paths.

### Random multi-building conflicts

`MultiChunk.canPlaceBuilding()` queries the pure raw road field before accepting
a random candidate. The profile's `multiBuildingStreetConflict` means:

- `BLOCK_ALL`: primary, secondary and tertiary intersections reject the
  candidate.
- `OVERRIDE_MINOR` (default): only primary intersections reject it. Accepted
  complexes suppress secondary or tertiary roads under their footprint.
- `OVERRIDE_ALL`: no automatic road intersection rejects it; every covered road
  is suppressed after acceptance.

Predefined multi-buildings bypass this policy and always override automatic
roads. Legacy mode never consults the planner in `MultiChunk`. Since the planner
does not know about multi-buildings and `MultiChunk` only reads the planner,
there is no cycle and querying either first cannot change the result.

### Profile settings

All settings are in the existing `lostcity` profile category:

| Setting | Default |
| --- | ---: |
| `streetGenerationMode` | `HIERARCHICAL_GRID_V1` |
| `primaryRoadSpacingX` / `primaryRoadSpacingZ` | 8 / 8 candidate spacing |
| `primaryRoadOptionalChance` | 0.45 |
| `primaryRoadForceEvery` | 4 candidates |
| `secondaryRoadMinCountX` / `secondaryRoadMaxCountX` | 0 / 2 |
| `secondaryRoadMinCountZ` / `secondaryRoadMaxCountZ` | 0 / 2 |
| `minimumRoadSeparation` | 4 |
| `minimumRoadEdgeDistance` | 3 |
| `tertiaryRoadChance` | 0.40 |
| `tertiaryRoadMinLength` / `tertiaryRoadMaxLength` | 2 / 5 |
| `plannedPrimaryBridgeChance` | 1.0 |
| `plannedPrimaryBridgeMaxLength` | 12 chunks |
| `multiBuildingStreetConflict` | `OVERRIDE_MINOR` |

Profile JSON without these fields receives these defaults, but an existing
world's missing persisted mode still forces `LEGACY`. Invalid enum names and
inverted min/max ranges produce clear profile errors; numeric fields use the
existing profile configuration ranges.

`/lost debug` prints persisted mode, raw/effective type, connections, primary
block coordinates, secondary positions, density, tertiary data, planned bridge
spans for the current chunk, conflict policy, multi-building suppression and
final city content. Nothing is logged during ordinary generation.

The most important architectural point is that `doCityChunk()` does **not** make
that decision. By the time it is called, `BuildingInfo` already contains the
decision. `doCityChunk()` is mainly the renderer that turns those characteristics
into blocks.

## `INTERCITY_NETWORK_V1` highway network

`IntercityHighwayPlanner` replaces only highway occupancy and level selection.
It is a pure planner constructed per dimension from the world seed, stable
dimension resource-location string, validated `HighwayPlannerSettings`, and a
lower-level `CityPotential` function. The existing `lost.Highway` class remains
the mode-aware facade, and `gen.Highways` remains the asset renderer.

The planner uses a fixed V1 salt, a stable FNV-derived dimension hash, SplitMix
64-bit mixing, and dedicated salts for sample positions, hub ranking,
connection ranking/acceptance, and route shape. Every decision hashes its full
coordinates directly. It does not use Java object hash codes, unordered-map
iteration, shared random state, generated chunks, final `BuildingInfo` state,
or query history. Once a hub wins, a separate deterministic level source reads
the terrain-derived city level at that coordinate.

### Approximate city potential

`ApproximateCityPotential` deliberately answers a lower-level question than
`BuildingInfo.isCityRaw()`: how strongly does this coordinate resemble the
center area of a substantial city region using data safe for remote planning?

For `CITY_CHANCE >= 0`, V1 reproduces the coordinate-seeded center and radius
math from `City` without calling `BuildingInfo`:

```text
searchRadiusInChunks = ceil(CITY_MAXRADIUS / 16)

for every coordinate-seeded city center in that range:
    radius = coordinate-seeded value in [CITY_MINRADIUS, CITY_MAXRADIUS)
    if distanceInBlocks < radius:
        potential += (radius - distanceInBlocks) / radius
```

For `CITY_CHANCE < 0`, it uses `CityRarityMap` with the world seed and the
profile's Perlin scale, inner scale and offset. The existing spawn-distance
multiplier is then applied. Server-side planning next calculates the terrain
height at the sample's chunk center without loading or generating the chunk.
Samples outside `CITY_MINHEIGHT` through `CITY_MAXHEIGHT` are rejected. The
generator's noise biome is sampled at that center height and the matching world
style `citybiomemultipliers` value is applied. The result is finally clamped to
`[0,1]`.

The approximation still intentionally omits final profile switching, generated
structures, city-sphere geometry, predefined assets and final `BuildingInfo`
decisions. Those inputs either require generated world state or pass through
higher-level code which already depends on highways. Existing city-sphere
intersection exclusion is still applied later by the mode-aware `Highway`
facade as a hard rendering constraint; it is not allowed to alter the canonical
planned route.

### Planning cells and hubs

The world is indexed by 128x128-chunk planning cells by default:

```text
planningCellX = floorDiv(chunkX, highwayPlanningCellSize)
planningCellZ = floorDiv(chunkZ, highwayPlanningCellSize)
```

`Math.floorDiv` is required for negative coordinates. A `HubKey` consists only
of these two cell coordinates and is the canonical hub identity. Each cell has
zero or one `HighwayHub`.

Within a cell, V1 derives independent X/Z sample-grid offsets from the world,
dimension, version, cell coordinates and hub-position salt. It samples every
`highwayHubSampleSpacing` chunks from those offsets; the default spacing is 16,
so a normal cell evaluates at most 8x8 points. Potentials are quantized to an
integer millionth before comparison. The highest score wins; equal scores use
an unsigned stable sample hash and then the fixed scan order. No hub is created
unless the winning score is at least `highwayHubMinimumPotential` (default
0.25). A sample on a reserved subway or station corridor moves one chunk within
the same planning cell before its potential is evaluated. This guarantees that
normal hub endpoints can form both canonical route shapes without losing
connections to railway avoidance. The chosen coordinate is always inside its
planning cell. Both present and absent hub decisions are persisted after their
first calculation. Present hubs include their endpoint city level, avoiding the
terrain-height, biome and city-level sampling cost when the world is opened
again.

### Candidate connections, sectors and symmetric acceptance

A hub considers the bounded square of cells within
`highwayHubSearchRadiusCells` (default two, or 5x5 cells). Candidate hubs must:

- exist and be distinct;
- be at least `highwayMinimumHubDistance` chunks away (default 64);
- be no farther than `highwayMaximumHubDistance` (default 320); and
- produce a Manhattan route of at least `highwayMinimumRouteLength` chunks
  (default 40).

The min/max hub checks use squared Euclidean distance, avoiding floating-point
ordering. Candidate ranking is the following explicit lexicographic order:

1. smaller squared geometric distance;
2. greater quantized target-hub potential;
3. unsigned canonical-pair rank hash;
4. unsigned canonical-pair acceptance tie hash;
5. target `HubKey`.

The direction to a candidate is classified as north, south, east or west using
the dominant absolute coordinate delta; exact diagonal ties prefer the X
sector. A hub first selects at most one ranked candidate per sector. Only if its
configured degree is larger than the available sectors does it fill remaining
slots from the overall ranked list. The default maximum degree is two.

An edge is accepted only by **mutual selection**: A must select B and B must
select A. This is the symmetric acceptance rule. It guarantees undirected
agreement and bounds accepted degree without a global graph pass. The canonical
`HighwayConnectionKey` sorts the two `HubKey` values, eliminating A-to-B versus
B-to-A duplicates. Sector preference plus the hard degree bound are the V1
parallel-cluster suppression mechanism; geometric parallel-route suppression
and shared trunks are deferred.

### Canonical route geometry and ownership

The lexicographically smaller hub key owns every accepted connection. Ownership
controls caching and enumeration only; all intersected chunks can reconstruct
the route.

Aligned hubs produce one straight inclusive `HighwaySegment`. Non-aligned hubs
have two candidates:

```text
horizontal then vertical: (Ax,Az) -> (Bx,Az) -> (Bx,Bz)
vertical then horizontal: (Ax,Az) -> (Ax,Bz) -> (Bx,Bz)
```

Both have the same Manhattan length. V1 samples approximate city potential
every eight chunks along both alternatives, ignoring samples within sixteen
Manhattan chunks of either endpoint. The sum, multiplied by
`highwayRouteCityPenalty`, is the route score; the lower score wins. An unsigned
canonical-pair route-shape hash breaks equal scores. A zero penalty therefore
selects entirely by this stable hash.

Both segments include the bend chunk. The route itself is enumerated once by
its canonical key, while its bend membership reports both X and Z axes. The
renderer reconstructs the same-level connections contributed by every route
through that chunk. Two perpendicular connections select and rotate a dedicated
bend part. Three connections select and rotate a T-junction part, which can
occur when independently planned connections terminate on or overlap another
route. Four connections continue to use the bidirectional crossing part.

The regular subway grid reserves its parallel chunk lines during route
selection. Horizontal highway segments cannot use the subway's repeating
horizontal line, while vertical segments cannot use either its repeating
vertical line or the repeating columns that contain surface-access stations.
Crossing an ordinary subway line remains valid because the underground track
has vertical clearance. Hub samples are moved off all these corridors during
discovery, so this constraint does not normally remove hub connections. The
route-level check remains as a defensive constraint and excludes a hub pair
only if neither canonical L-shape is clear.

Every connection uses one level for its entire length. The shared
`highwayLevelFromCities` setting selects the first endpoint, lower endpoint,
higher endpoint, integer endpoint average, or fixed `highwayNetworkLevel`.
Endpoint city levels are stored in the persisted hub records, so remote chunks
reconstruct the same elevation without consulting final city or highway state.
The default mode is the integer average of both endpoint city levels.

### Bounded per-chunk reconstruction and caching

For a queried chunk, the planner calculates its planning cell and enumerates
route owners within the same configured cell radius used for hub search. This
is sufficient because each connection's endpoint cells differ by at most that
radius and every point on an axis-aligned Manhattan route lies inside the
endpoint cell-coordinate bounds. Thus an owner farther away cannot have a V1
route crossing the query cell.

Owned routes are tested for inclusive segment membership and deduplicated in a
sorted map by canonical key. The result records route hits, segment axes, bend
membership and the route level, then classifies the chunk as `NONE`,
`X_HIGHWAY`, `Z_HIGHWAY`, `SAME_LEVEL_INTERSECTION`, or
`MULTI_LEVEL_INTERSECTION`. Connections derived from different endpoint pairs
can use different levels, so both intersection classifications can occur.

Per-planner hub, candidate, selection, owned-route and final-chunk caches are
synchronized access-order LRU maps with fixed maximum sizes (4096, 2048, 2048,
2048 and 8192 entries respectively). A hub-cache miss first checks the
dimension's persistent planning-cell result before performing city-potential,
height and biome sampling. Settings, seed and dimension do not need to appear
in individual in-memory cache keys because each immutable planner instance is
scoped to exactly that tuple. Cache eviction or clearing can only reload or
recompute the same pure value. There is no recursive connection acceptance:
neighbour selection reads only hubs and ranked candidates, so all work remains
bounded.

### Integration with existing highways

`Highway.getHighwayInfo()` is the mode-aware boundary. In `LEGACY`, existing
X/Z calls enter the original Perlin algorithm and its original static caches.
In `INTERCITY_NETWORK_V1`, they read the planner result. The public
`getXHighwayLevel()`, `getZHighwayLevel()` and `hasHighway()` methods retain
their signatures, so all existing consumers use one occupancy decision:

- `MultiChunk` rejects random multi-building footprints on network routes;
- ordinary buildings require the existing full-level vertical clearance;
- cellar counts are capped above the route's selected level;
- same-level street and park surfaces and street decorations are suppressed;
- railway avoidance and scattered-content proximity see the same route;
- normal and city chunks both call the existing `gen.Highways` renderer; and
- tunnel/open/bridge selection, rotation, crossing assets, clearing, palette
  validation and supports remain shared and unchanged.

The planner never truncates an accepted route based on later city membership.
Routes run hub-to-hub; the endpoint height and biome checks prevent ordinary
terrain exclusions from leaving a route without its expected city.

### Highway profile settings and compatibility categories

| Network and shared level setting | Default |
| --- | ---: |
| `highwayGenerationMode` | `INTERCITY_NETWORK_V1` for new worlds |
| `highwayPlanningCellSize` | 128 chunks |
| `highwayHubSampleSpacing` | 16 chunks |
| `highwayHubMinimumPotential` | 0.25 |
| `highwayHubSearchRadiusCells` | 2 cells |
| `highwayMinimumHubDistance` | 64 chunks |
| `highwayMaximumHubDistance` | 320 chunks |
| `highwayMaximumConnectionsPerHub` | 2 |
| `highwayMinimumRouteLength` | 40 chunks |
| `highwayRouteCityPenalty` | 1.0 |
| `highwayLevelFromCities` | 3 (endpoint average) |
| `highwayNetworkLevel` | 0 (used by mode 4) |

`HIGHWAY_DISTANCE_MASK`, `HIGHWAY_MAINPERLIN_SCALE`,
`HIGHWAY_SECONDARYPERLIN_SCALE`, `HIGHWAY_PERLIN_FACTOR`,
and `HIGHWAY_REQUIRES_TWO_CITIES` are legacy-only.
`HIGHWAY_LEVEL_FROM_CITIES_MODE`, `HIGHWAY_NETWORK_LEVEL`,
`HIGHWAY_SUPPORTS`, and the world style's `HighwayParts` selectors are shared.
Profiles missing new JSON fields receive the defaults above, but missing
persisted world data always selects legacy.

`/lost debug` reports the persisted highway mode, cell, current/nearby hubs and
strengths, ranked candidates and sectors, selected neighbours, mutual accepted
keys, owners, route shapes/segments/lengths/levels/penalties, chunk membership,
X/Z classification and cache statistics. Legacy mode reports its X/Z result.
Nothing is logged during normal generation.

### V1 limitations and phase-2 boundary

- Hubs approximate city regions rather than exact final cities, but their
  terrain height and biome multiplier are validated.
- Routes connect hub positions, not city-edge gateways, and may cross endpoint
  cities.
- Routes have at most one bend; the existing crossing part renders that bend.
- Terrain-aware route-path scoring is absent; only endpoint city constraints
  and approximate city-interior penalty are considered.
- Routes do not merge into shared trunks and have no detailed parallel-route
  suppression beyond degree and sector limits.
- Elevation is fixed for the entire connection.
- Gateways are not aligned to hierarchical primary streets.
- Route-interior water and unrelated-city avoidance is deferred. Parallel
  subway-line and surface-access-station avoidance are hard route constraints.

Phase 2 should move endpoints to city-edge gateways aligned with hierarchical
primary streets, strengthen city-interior penalties, score route terrain and
water, and suppress or merge close parallel routes.

## Deterministic scattered-area generation

Scattered structures are planned per scattered area rather than independently
by every footprint chunk. `Scattered` caches an immutable plan keyed by
dimension, world seed, and area coordinates. The plan contains the selected
asset, top-left position, dimensions, complete-footprint validity, common
generation height, and single-building choice where applicable.

The entire footprint is checked for biome, city, bridge, highway, void, and
height-difference constraints before the plan is accepted. A `nearhighway`
asset searches deterministic candidates whose complete connection edge borders
a parallel, non-tunnel highway at one deck height. Its base height is taken
from that highway, and a rotatable asset turns its footprint, multibuilding
piece lookup, blocks, and optional repeating support part toward the connected
edge. The unrotated connection edge is north.
An asset with `clearhighwayrailing` removes iron-bar railing only from that
connected highway edge, providing access without changing other highway sides.

Every participating chunk then reads the same plan and generates only its own
building piece. The area random sequence is reconstructed independently for
each piece; no mutable random object or first-generated chunk can change the
decision. Other chunks are never generated eagerly.

## Generation concurrency boundary

Normal and sphere generation enter through
`LostCityFeature.runWithDimensionInfo()`. Lifecycle cleanup is excluded while
generation callbacks are active. Non-overlapping chunks in one dimension may
generate concurrently, while ordered striped locks protect each active 3x3
chunk neighbourhood because vine post-processing can cross a chunk edge.

Mutable per-call data, including the active world, `ChunkDriver`, random source,
street character, and noise buffers, belongs to the worker-local
`GenerationContext`. See [`thread_safety.md`](thread_safety.md) before adding
new generation caches, random sources, or cross-chunk writes.

## Shared and legacy city-generation flow

```text
LostCityTerrainFeature.generate(chunk)
    |
    +-- obtain terrain heightmap
    |
    +-- BuildingInfo.getBuildingInfo(chunk)
    |      |
    |      +-- decide raw city membership
    |      +-- reserve predefined/random multi-buildings
    |      +-- reject chunks or whole multibuilding footprints covered by structure/village avoidance
    |      +-- decide building versus street/park
    |      +-- select city style, building asset, levels and details
    |
    +-- reject floating void chunks when configured
    |
    +-- city? ---- yes ---> doCityChunk()
    |                         +-- hasBuilding -> generateBuilding()
    |                         +-- otherwise   -> generateStreet()
    |
    +-- no ----------------> doNormalChunk()
    |
    +-- add rails, damage/explosions, debris and final fixes
```

Relevant entry points:

- `LostCityTerrainFeature.generate()` and
  `LostCityTerrainFeature.doCityChunk()` in
  `src/main/java/mcjty/lostcities/worldgen/LostCityTerrainFeature.java`
- `BuildingInfo.getChunkCharacteristics()` and `BuildingInfo` construction in
  `src/main/java/mcjty/lostcities/worldgen/lost/BuildingInfo.java`
- `Scattered.calculatePlan()` in
  `src/main/java/mcjty/lostcities/worldgen/gen/Scattered.java`

Structure and village avoidance is resolved while chunk characteristics are
being built, before `BuildingInfo.isCity`, building/street selection, and other
derived city state are finalized. The resolver only reads
`STRUCTURE_REFERENCES` from the active `WorldGenRegion`; it never asks the
server chunk cache to load or generate another chunk. A characteristics or
building-info request can also happen speculatively for a coordinate whose
required references are outside that region. Such an unknown result is not
cached for an ordinary chunk, so generation can calculate the final avoidance
decision once that coordinate has the required references. Multibuildings
instead commit one shared immutable decision for their complete footprint and
configured adjacent margin. A visible avoided structure rejects the complete
footprint. If no avoided structure is visible, the footprint is allowed even
when some references are unavailable, preserving the earlier fail-open
behavior without cutting off individual chunks of a random or predefined
multibuilding.

## 1. Deciding whether a chunk belongs to a city

`BuildingInfo.isCityRaw()` computes a `cityFactor` through
`City.getCityFactor()` and compares it with `profile.CITY_THRESHOLD`:

```text
isCity = cityFactor > CITY_THRESHOLD
```

There are two main ways to calculate the factor:

1. When `CITY_CHANCE >= 0`, chunks can deterministically become city centers.
   Each center has a deterministic radius. For every city circle containing the
   current chunk, `(radius - distance) / radius` is added to the factor. This
   makes the factor strongest at a city center and allows overlapping cities to
   reinforce each other.
2. When `CITY_CHANCE < 0`, `CityRarityMap` supplies a noise-based city factor.

The result is then affected by terrain height limits, the world style's
biome-dependent city chance multiplier, and optional distance-from-spawn
multipliers. It is finally clamped to `[0, 1]`. Predefined streets and buildings
force a factor of `1` at their occupied positions.

Void chunks, city-sphere borders, and monorail-station chunks can be rejected
before the factor comparison. See `BuildingInfo.isCityRaw()` and
`City.getCityFactor()`.

## 2. The city-chunk content decision

For a chunk whose `isCity` flag is true in `LEGACY`, the effective decision order is:

```text
predefined multi-building
    > predefined single building or predefined street
    > randomly placed multi-building
    > ordinary per-chunk building chance
    > park nomination
    > ordinary street
```

More precisely:

### 2.1 Reserve multi-building sections first

`initMultiBuildingSection()` first checks whether a predefined city asset
already occupies the chunk:

- A predefined multi-building records the chunk's offset and the complete
  multi-building dimensions.
- A predefined single building or predefined street leaves the chunk marked as
  a single section. Its exact building/street override is handled shortly
  afterwards.

If nothing predefined occupies the chunk, it consults `MultiChunk`. Therefore
random multi-building placement happens before the ordinary building chance is
tested. Every accepted cell of a random multi-building is marked with the same
multi-building name and its `(x,z)` offset inside that structure.

### 2.2 Decide whether the section has a building

`checkBuildingPossibility()` sets the initial `couldHaveBuilding` value:

1. A predefined building always returns `true`.
2. A predefined street always returns `false`.
3. Every cell belonging to a multi-building returns `true`.
4. Otherwise a deterministic per-chunk random float is compared with the city
   style's `buildingChance`, falling back to `profile.BUILDING_CHANCE`.
5. A highway or railway can still disallow the building when there is not enough
   vertical room. A chunk directly over an underground station is also forced
   to have no building.

For single-chunk candidates, the `BuildingInfo` constructor applies an
additional “prefers lonely” veto. It samples the `prefersLonely` value of the
selected building type in each of the four neighboring chunks; any successful
sample turns this chunk back into a non-building city section. Multi-building
cells skip this veto.

City-sphere center settings may finally force the center to be a street, a
building, or a normal non-city chunk.

The final boolean is stored as `BuildingInfo.hasBuilding`:

```text
isCity && hasBuilding       -> building renderer
isCity && !hasBuilding      -> street/park renderer
!isCity                     -> normal terrain renderer
```

### 2.3 Select the building asset

The building asset is selected while chunk characteristics are assembled:

- A cell in a multi-building reads the building name at its local `(x,z)`
  position from the `MultiBuilding` asset.
- A predefined single building uses its configured building name.
- Otherwise `CityStyle.getRandomBuilding()` makes a weighted deterministic
  selection from the style's building selector.

The code selects a `buildingType` even for chunks that later render as streets.
Among other things, neighboring chunks use that type's `prefersLonely` value.

## 3. How random multi-buildings are placed

`MultiChunk` divides the world into fixed `areasize x areasize` groups of
chunks. Each group is calculated and cached as a unit, using a random generator
seeded from the group's coordinates.

For each group it:

1. Chooses a desired count between the world style's `minimum` and `maximum`.
2. Counts the city styles of all chunks in the group.
3. For each desired structure, chooses a city style weighted by how common it is
   in the group, then chooses a multi-building from that style.
4. Sorts the chosen structures largest-first.
5. Gives each structure up to `attempts` random placement attempts.
6. On success, writes the structure and local offsets into the group's grid.

A candidate footprint is rejected if any covered chunk:

- is already occupied by another random multi-building;
- is occupied by a predefined building or street;
- is not a city chunk;
- contains a highway;
- contains a surface railway or station;
- has a railway/cellar clearance conflict; or
- causes fewer than `correctStyleFactor` of its chunks to have the city style
  against which that candidate is checked.

There is an implementation caveat in the last test. The chosen buildings and
their chosen city styles are initially stored in parallel lists, but only the
building list is sorted largest-first. After that sort, a building can therefore
be checked against the style at the same old list index rather than the style
that originally selected it. This does not affect the selected building asset,
but it can affect whether a candidate footprint passes `correctStyleFactor`.

Consequently, the configured desired count is not guaranteed: a structure is
simply omitted if no valid position is found within its attempt limit.

All chunks in an accepted multi-building are forced to be building chunks.
Non-top-left cells copy shared properties such as floor count, cellar count,
palette, street metadata and ruin height from the top-left cell, while each cell
uses the building asset assigned to its own position in the multi-building.

See `src/main/java/mcjty/lostcities/worldgen/lost/MultiChunk.java` and
`BuildingInfo.initMultiBuildingSection()`.

## 4. Streets versus parks

Only a city chunk without a building reaches the street renderer. Legacy park
selection has two stages, which is easy to miss when reading only the initial
random test. Hierarchical generation handles planned roads and open lots
separately as described below.

### Legacy stage A: nominate a park

While constructing the top-left or single chunk's `BuildingInfo`, the effective
city settings use the inherited city style's `parkChance`, falling back to
`profile.PARK_CHANCE`:

```text
random < parkChance  -> PARK
otherwise            -> NORMAL (with the current enum bound)
```

The style also supplies a weighted park part asset. Fountain selection is an
independent chance and can decorate a non-park street.

### Legacy stage B: require a sufficiently open neighborhood

`generateStreet()` calls `isElevatedParkSection()`. A nominated park is accepted
only when at least `parkStreetThreshold` of the eight surrounding chunks are
city chunks without buildings. The threshold comes from the city style, or from
`profile.PARK_STREET_THRESHOLD` as fallback.

If the threshold is met, the chunk remains a park and `generateParkSection()` is
used. Depending on configuration, its surface may be raised by one block.

If the threshold is not met, `generateStreet()` deliberately replaces the
`PARK` value using a separately seeded deterministic enum selection. In the
current code, both this selection and the initial non-park selection call
`nextInt(0, StreetType.values().length - 2)`. Because the upper bound is
exclusive and the enum has three members, the only possible index is `0`, or
`NORMAL`. This looks as though it may have been intended to choose between
`NORMAL` and `FULL`, but `FULL` is not reachable through this ordinary selection
path as currently written.

Thus `PARK_CHANCE` is a nomination probability, not by itself the final
probability of seeing a park. Parks are biased toward larger open regions rather
than isolated holes between buildings.

### Hierarchical open lots

In `HIERARCHICAL_GRID_V1`, planned and predefined streets remain normal streets.
Other non-building city chunks become grass open lots and retain the `PARK`
street type so that they use the park surface rather than reconstructing the
legacy road network. `OPEN_LOT_PARK_CHANCE` independently controls whether an
open lot receives a weighted park part; its default is `0.8`. An inherited city
style can override it with `profile_overrides.openLotParkChance`. Absence falls
back to the profile, and an explicit `0` remains an override. The legacy
`parkblocks.parkchance` value remains independent and affects only legacy park
nomination. The selected chunk's city style is the canonical owner of the open
lot decision; style selection happens before the chance roll and does not
depend on its result. A style with no eligible park parts still produces a
plain grass lot.

The same immutable effective-city resolver owns the existing local fallbacks
for building chance and floor/cellar constraints, legacy parks, fountains,
fronts, corridors, foliage, park borders/elevation, and park street thresholds.
This centralizes precedence without changing those existing asset keys or
their random-number consumption.

The three final street types mean:

- `NORMAL`: a road layout with the city style's normal street width and edges.
- `FULL`: the full chunk is paved as street. The renderer supports this value,
  but the normal selection code described above does not currently produce it.
- `PARK`: the park surface and selected park part are generated.

Highways at the same city level and surface stations can suppress the street or
park surface entirely; borders and the infrastructure itself are handled
separately.

## 5. What `doCityChunk()` actually generates

`doCityChunk()` performs the common city rendering work:

1. Adds bedrock and high-water handling for applicable landscape profiles.
2. Levels every terrain column to the calculated city ground level.
3. Fires the pre-generation event.
4. Dispatches solely on the previously calculated `info.hasBuilding` flag:
   - `true`: `generateBuilding()` clears/prepares the footprint, generates all
     cellar, floor and roof parts in six-block-high levels, adds doors, and makes
     corridor connections.
   - `false`: `generateStreet()` handles corridors, the final street/park type,
     optional park/fountain parts, vegetation, fronts and borders.
5. Fires the post-generation event.
6. Applies ruins, street decorations, highways, rubble and style-defined stuff.

After `doCityChunk()` returns, `generate()` still adds railways, explosion
damage, debris, torch fixes and final chunk fixes.

## 6. Legacy Perlin highways and shared rendering

Legacy highways are separate from both street modes. They can cross city and
non-city chunks. Their implementation is split between two similarly named
classes, with the first now also acting as the mode-aware facade:

- `lost.Highway` delegates to the original Perlin decision below in `LEGACY` or
  to `IntercityHighwayPlanner` in `INTERCITY_NETWORK_V1`.
- `gen.Highways` chooses the appropriate highway asset and places its blocks.

The complete legacy flow is:

```text
Highway.getXHighwayLevel() / getZHighwayLevel()
    |
    +-- restrict candidates to regularly spaced chunk rows/columns
    +-- threshold seeded Perlin noise
    +-- find the complete contiguous noise run
    +-- require a sufficiently long run connected to one or two cities
    +-- derive one city level for the complete run
    |
    +-- cache and return -1 (absent) or the highway level
            |
            +-- Highways.generateHighways()
                    +-- resolve X/Z crossing and rotation
                    +-- choose tunnel, open or bridge part
                    +-- place the part, clear headroom and add supports
```

### 6.1 Candidate corridors and Perlin runs

X and Z highways are calculated independently. An X highway runs along the X
axis and can only occur on selected Z chunk rows; a Z highway runs along Z and
can only occur on selected X chunk columns. A coordinate is eligible when:

```text
oppositeAxisCoordinate & HIGHWAY_DISTANCE_MASK == 0
```

The mask is intended to be a power of two minus one. The default mask is `7`,
so eligible rows or columns occur every eight chunks. A value of zero disables
highways entirely. Space and sphere profiles also reject candidate chunks that
intersect a city sphere.

On an eligible row or column, `Highway` samples a four-octave Perlin generator
seeded from the world seed. The highway's long axis uses
`HIGHWAY_MAINPERLIN_SCALE`, while its perpendicular axis uses
`HIGHWAY_SECONDARYPERLIN_SCALE`; the axes are swapped for Z highways. A chunk is
a raw candidate when the sampled value is greater than
`HIGHWAY_PERLIN_FACTOR`:

```text
X candidate = perlinX(chunkX / mainScale,
                      chunkZ / secondaryScale) > factor

Z candidate = perlinZ(chunkX / secondaryScale,
                      chunkZ / mainScale) > factor
```

The current chunk is not accepted in isolation. The code walks backward and
forward along the highway axis until the Perlin test fails, thereby finding the
two endpoints of the complete contiguous noise run. The endpoint coordinate
difference must be at least five, so the run contains at least six chunks.

The endpoints must then connect the run to city generation. With
`HIGHWAY_REQUIRES_TWO_CITIES` enabled, both endpoint chunks must satisfy
`BuildingInfo.isCityRaw()`; when disabled, either endpoint is sufficient. This
tests the endpoints of the Perlin run, not whether the run happens to pass
through a city somewhere in its middle.

### 6.2 Selecting one height for the run

Every accepted run receives a single city level. The value of
`HIGHWAY_LEVEL_FROM_CITIES_MODE` selects how it is derived from the raw city
levels at the lower and higher endpoints:

| Mode | Highway level |
| ---: | --- |
| `0` | Lower endpoint level |
| `1` | Minimum of both endpoint levels |
| `2` | Maximum of both endpoint levels |
| `3` | Integer average of both endpoint levels |
| `4` | Fixed `HIGHWAY_NETWORK_LEVEL` |

The resulting value is written to the X- or Z-highway cache for every chunk in
the run. `-1` means no highway. The block-space base of a highway at level `L`
is:

```text
highwayGroundLevel = groundLevel + L * FLOORHEIGHT
```

`FLOORHEIGHT` is six blocks. Although some comments still describe highway
levels as only zero or one, the selected value comes from the endpoint city
levels and is not clamped to that range here.

### 6.3 X/Z crossings

`Highways.generateHighways()` queries both cached levels for the current chunk:

- One X highway is placed without rotation; one Z highway uses a 90-degree
  rotation of the same assets.
- If X and Z have the same non-negative level, inter-city route geometry is
  inspected to distinguish a bend, T-junction, or four-way crossing. The
  matching part is rotated to its connected sides. Legacy highways, which do
  not retain route geometry, continue to use the bidirectional crossing part.
- If both exist at different levels, two ordinary parts are placed. The code is
  written around the expected level-zero/level-one case and places the
  level-zero highway first, because generating the lower part clears space
  above itself.

The world style's `HighwayParts` selector supplies tunnel, open and bridge
asset lists for four shapes: ordinary straight highways, `_bi` four-way
crossings, `_bend` bends, and `_t` T-junctions. One name is selected from the
applicable list and resolved as a `BuildingPart`. The default names follow the
`highway_<environment>[_<shape>]` pattern, such as `highway_open`,
`highway_bridge_bend`, and `highway_tunnel_t`. Built-in bridge junctions leave
their connected chunk edges open and place guard and upper-frame walls along
exposed edges.

### 6.4 Tunnel, open and bridge selection

For each highway part, `BuildingInfo.isTunnel(level)` is checked first:

- In a city chunk, it is a tunnel when the city's surface level is above the
  highway level: `cityLevel > highwayLevel`.
- Outside a city, it is a tunnel when the cached terrain height is above
  `groundLevel + highwayLevel * FLOORHEIGHT + 3`.

If it is not a tunnel, the renderer chooses an open highway only when the
current chunk and the relevant adjacent chunks are cities whose city levels are
at least as high as the highway. Every other exposed segment uses the bridge
part. In this context, "bridge" is the exposed/elevated fallback asset; it does
not require the chunk to be over water and is unrelated to the planned primary
bridge-span algorithm described earlier.

The selected part is generated at `highwayGroundLevel`. Open and bridge parts
also clear fifteen blocks of extra headroom unless the profile is cavernous.
The clearing predicate preserves logs and leaves. Tunnel parts rely on their
own asset carving and do not perform this extra pass.

Finally, if `HIGHWAY_SUPPORTS` is enabled and the selected part declares a
support palette character through its `META_SUPPORT` metadata, the renderer
builds transformed corner supports. Straight and four-way parts use the two
corners of the unrotated highway frame. Bend and T-junction parts add the third
corner contributed by the rotated highway frame. Each support descends from
one block below the highway for at most forty blocks, filling empty blocks
until it meets solid terrain. A missing palette mapping for the declared
support character is treated as a configuration error.

### 6.5 Interaction with city content

Highway placement participates in city decisions before it is rendered:

- Random multi-buildings reject any footprint containing a highway.
- An ordinary single building above a highway is allowed only when
  `cityLevel > highestHighwayLevel + 1`, leaving a full level of clearance.
- When such a building is allowed, its cellar count is capped so cellars do not
  descend into the highway.
- A street or park surface is skipped when a highway occupies that exact city
  level, and street decorations are omitted from any highway chunk.

Rendering occurs late in both chunk paths. `doNormalChunk()` generates bridges
and then highways over normal terrain. `doCityChunk()` first renders the chosen
building or street and applies ruins, then overlays highways before rubble and
style-defined stuff. Thus `BuildingInfo` reserves the necessary vertical space
in advance, while `Highways` performs the final asset placement.

Relevant implementation files are
`src/main/java/mcjty/lostcities/worldgen/lost/Highway.java`,
`src/main/java/mcjty/lostcities/worldgen/gen/Highways.java`, and
`BuildingInfo.isTunnel()`.

## 7. Determinism and caching

Most choices are reproducible for a world seed and chunk coordinate. Building
selection uses a `QualityRandom` seeded from the world seed and chunk position;
city centers, radii, styles, street fallback type, and multi-building groups use
their own seeds. Several are derived from coordinates alone, while others also
include the world seed. This separation helps keep one category of choice stable
when another code path consumes a different number of random values.

`BuildingInfo`, raw chunk characteristics, city styles, heightmaps and
multi-chunk grids are cached. Neighbor queries during decisions therefore refer
to the same deterministic characteristics that will later be used when those
neighbors are actually generated.

## Condensed decision table

| Condition, in precedence order | Final city content |
| --- | --- |
| Chunk is not considered city | Normal terrain (`doNormalChunk`) |
| Predefined street occupies chunk | Street or eligible park; never a building |
| Predefined building occupies chunk | Its single or multi-building |
| Random `MultiChunk` placement covers chunk | Its assigned multi-building cell |
| Per-chunk building test passes and no later veto applies | Selected single-chunk building |
| No building, park roll passes, and neighborhood threshold passes | Park |
| No building and park does not survive both stages | Normal street in the current implementation |
