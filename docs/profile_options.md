# Lost Cities profile options

A Lost Cities profile controls the world-generation choices a player can select when creating a world. Profiles are JSON files in `config/lostcities/profiles`; the file name without `.json` is the profile name. The built-in profiles are written to this directory at startup and make useful examples. Copy one to a new file name to customize it.

The options below are the JSON properties consumed by `LostCityProfile`. Defaults are the class defaults before a built-in or custom preset applies overrides (for example, the built-in `default` preset supplies its own icon). Numeric ranges are the limits presented by the configuration model. Chances are fractions: `0` never, `1` always.

Spatial options state their unit explicitly. One chunk is 16x16 blocks horizontally. A block distance is measured in Minecraft block coordinates, while a chunk distance counts chunk positions. Heights and vertical offsets are measured in blocks unless a description names another unit, such as city levels or six-block units.

## File format

Options are grouped into five objects. Properties may be omitted, in which case their default is used.

```json
{
  "public": true,
  "lostcity": {
    "description": "My custom profile",
    "landscapeType": "default",
    "buildingChance": 0.3
  },
  "cities": {
    "cityChance": 0.01
  },
  "explosions": {
    "explosionChance": 0.002
  },
  "cityspheres": {},
  "client": {}
}
```

`public` is a root-level boolean, not a category option. It defaults to `true` and controls whether users can select the profile. The generated `__readonly__` properties are informational and are ignored when a profile is read.

## `lostcity`

### Identity, world, and terrain

| Option | Default / values | Meaning |
| --- | --- | --- |
| `description` | `"Default generation, common cities, explosions"` | Main text shown for the profile. |
| `extraDescription` | `""` | Additional profile information. |
| `warning` | `""` | Warning shown when selecting the profile. |
| `worldStyle` | `"standard"` | World-style asset identifier used to select city styles and parts. |
| `icon` | `""` | Profile-screen icon resource, normally a 64×64 texture path such as `textures/gui/icon_default.png`. |
| `landscapeType` | `"default"` | Landscape algorithm. Values understood by the enum are `default`, `floating`, `space`, `cavern`, `spheres`, and `cavernspheres`; the normal editor offers the first four. |
| `liquidBlock` | `"minecraft:water"` | Namespaced block used as the profile's liquid. An invalid ID logs an error and falls back to water. |
| `baseBlock` | `"minecraft:stone"` | Namespaced block used as the world-generation base. An invalid ID logs an error and falls back to stone. |
| `groundLevel` | `71` (2–256) | Base ground Y coordinate, in blocks. |
| `seaLevel` | `-1` (-1–256) | Sea-level Y coordinate, in blocks; `-1` uses the world's default. |
| `bedrockLayer` | `1` (0–10) | Bedrock-layer height in blocks for applicable landscape types; `0` leaves default bedrock generation in place. |
| `terrainFixLowerMinOffset` | `-4` (-40–40) | Minimum lower-mesh vertical offset, in blocks, from city base level when raising adjacent terrain. |
| `terrainFixLowerMaxOffset` | `-3` (-40–40) | Maximum lower-mesh vertical offset, in blocks. |
| `terrainFixUpperMinOffset` | `-1` (-40–40) | Minimum upper-mesh vertical offset, in blocks, when lowering adjacent terrain. |
| `terrainFixUpperMaxOffset` | `1` (-40–40) | Maximum upper-mesh vertical offset, in blocks. |
| `avoidWater` | `false` | Replaces all water encountered by Lost Cities generation with air. |
| `editMode` | `false` | Enables the special world-generation edit mode. |
| `generateNether` | `false` | Generates the Nether using the cavern-style Lost Cities generator. |

### Spawn selection

| Option | Default / range | Meaning |
| --- | --- | --- |
| `spawnBiome` | `""` | Namespaced biome in which the player must spawn; empty imposes no biome restriction. |
| `spawnCity` | `""` | Predefined city in which the player must spawn. |
| `spawnSphere` | `""` | Predefined sphere in which to spawn. `<in>` selects any sphere; `<out>` requires a position outside every sphere. |
| `spawnNotInBuilding` | `false` | Requires the spawn position not to be in a building. May be combined with other spawn filters. |
| `forceSpawnInBuilding` | `false` | Requires a building spawn. Do not enable together with `spawnNotInBuilding`. |
| `forceSpawnBuildings` | `[]` | Allowed building IDs when `forceSpawnInBuilding` is enabled; empty allows any building. Combined with `forceSpawnParts`. |
| `forceSpawnParts` | `[]` | Allowed part IDs when forcing a building spawn; empty allows any part. |
| `spawnCheckRadius` | `200` (1–100000) | Initial search radius in blocks. The search starts at world center and rounds the radius up to complete chunk rings. |
| `spawnRadiusIncrease` | `100` (1–100000) | Number of blocks added to the search radius after an unsuccessful pass. |
| `spawnCheckAttempts` | `20000` (1–1000000) | Maximum chunks checked before spawn selection fails. |

### Buildings, streets, parks, and decay

| Option | Default / range | Meaning |
| --- | --- | --- |
| `buildingChance` | `0.3` (0–1) | Chance that a city chunk contains a building rather than a street. |
| `buildingMinFloors` | `0` (0–60) | Minimum floors above ground; `0` still means a ground floor. |
| `buildingMaxFloors` | `8` (0–60) | Hard cap on floors above ground. |
| `buildingMinFloorsChance` | `4` (1–60) | Low end of the city-factor-dependent random floor calculation. |
| `buildingMaxFloorsChance` | `6` (1–60) | High end of the city-factor-dependent random floor calculation. |
| `buildingMinCellars` | `0` (0–20) | Minimum cellar count; `0` permits no cellar. |
| `buildingMaxCellars` | `3` (0–20) | Maximum cellar count. |
| `buildingDoorwayChance` | `0.6` (0–1) | Chance per possible building side and level to create a doorway. |
| `buildingFrontChance` | `0.2` (0–1) | Chance to use a building's front part when it can face an adjacent street. |
| `parkChance` | `0.2` (0–1) | Chance that an eligible non-building city section is a park in legacy street generation. |
| `parkElevation` | `true` | Adds an elevation layer to parks. |
| `parkBorder` | `true` | Uses the street block as the base of a park border. |
| `parkStreetThreshold` | `3` (0–8) | Required surrounding-street count for park generation, in addition to `parkChance`. |
| `fountainChance` | `0.05` (0–1) | Chance that a street section contains a fountain. |
| `corridorChance` | `0.7` (0–1) | Chance a chunk is a corridor candidate; adjacent corridors are also required. |
| `bridgeChance` | `0.7` (0–1) | Legacy bridge candidate chance; connections and terrain impose further conditions. |
| `plannedPrimaryBridgeChance` | `1.0` (0–1) | Chance that an entire eligible hierarchical primary-road water crossing becomes a bridge. |
| `plannedPrimaryBridgeMaxLength` | `12` (1–64) | Longest water gap, in chunks, eligible for such a primary-road bridge. |
| `bridgeSupports` | `true` | Generates bridge supports where needed. Disable for bridges intended to cross void. |
| `multiUseCorner` | `false` | Uses only a multi-building's top-left corner level instead of its surrounding average level. |
| `useAvgHeightmap` | `false` | Derives normal city level from surrounding heightmaps. This needs extra heightmap lookups and is slower. |
| `scatteredChanceMultiplier` | `1.0` (0–100) | Multiplier for scattered-building chances; `0` disables them. |
| `vineChance` | `0.009` (0–1) | Chance that an exterior building block receives a vine. |
| `randomLeafBlockChance` | `0.1` (0–1) | Chance of leaf blocks along building/street borders. |
| `randomLeafBlockThickness` | `2` (1–8) | Side-visible frequency/thickness of those leaf blocks. |
| `avoidFoliage` | `false` | Removes trees and flowers from parks. |
| `rubbleLayer` | `true` | Enables the overgrown rubble dirt/stone/sand and leaf layers. |
| `rubbleDirtScale` | `3.0` (0–100) | Noise scale for the rubble dirt layer; smaller is broader and `0` disables it. |
| `rubbleLeaveScale` | `6.0` (0–100) | Noise scale for the rubble leaf layer; smaller is broader and `0` disables it. |
| `ruinChance` | `0.05` (0–1) | Chance that a building is ruined. |
| `ruinMinlevelPercent` | `0.8` (0–1) | Minimum relative height at which a ruined building's destruction begins. |
| `ruinMaxlevelPercent` | `1.0` (0–1) | Maximum relative height at which destruction begins. |

The random floor bound is based on `buildingMinFloorsChance + (cityFactor + 0.1) × (buildingMaxFloorsChance - buildingMinFloorsChance)`, added to `buildingMinFloors` as a random amount and capped by `buildingMaxFloors`.

Several city-local profile values have long-standing city-style overrides. After city-style inheritance, `buildingsettings.buildingchance`, its floor/cellar limits, `parkblocks.parkchance`, `avoidfoliage`, `parkborder`, `parkelevation`, `parkstreetthreshold`, `streetblocks.fountainchance`, `frontchance`, and `corridorblocks.corridorchance` override their corresponding profile fallbacks. Floor and cellar values remain additional constraints rather than simple replacements. The effective settings resolver does not mutate the selected profile.

### Hierarchical street generation

These options other than `streetGenerationMode` apply to `HIERARCHICAL_GRID_V1`. The selected mode is copied into persistent world data when a world is initialized; changing the profile later does not change an existing world's mode.

| Option | Default / values | Meaning |
| --- | --- | --- |
| `streetGenerationMode` | `HIERARCHICAL_GRID_V1` | `LEGACY` or `HIERARCHICAL_GRID_V1`. |
| `openLotParkChance` | `0.8` (0–1) | Chance that a hierarchical grass open lot receives a weighted park part. City-style `profile_overrides.openLotParkChance` overrides it after inheritance; legacy `parkblocks.parkchance` remains independent. |
| `primaryRoadSpacingX` | `8` (8–128) | Number of chunks between candidate north/south primary corridors (lines at fixed X coordinates). A candidate is not necessarily enabled. |
| `primaryRoadSpacingZ` | `8` (8–128) | Number of chunks between candidate east/west primary corridors (lines at fixed Z coordinates). A candidate is not necessarily enabled. |
| `primaryRoadOptionalChance` | `0.45` (0–1) | Independent chance that each non-forced candidate becomes an actual primary corridor. `0` omits all non-forced candidates; `1` enables every candidate. |
| `primaryRoadForceEvery` | `4` (1–16) | Enables every Nth candidate on both axes regardless of `primaryRoadOptionalChance`. Thus the maximum separation between enabled primary corridors is N × the corresponding spacing. A value of `1` enables every candidate. |
| `secondaryRoadMinCountX` | `0` (0–128) | Minimum requested internal north/south secondary roads in each primary block. |
| `secondaryRoadMaxCountX` | `2` (0–128) | Maximum requested internal north/south secondary roads. Must be at least the minimum. |
| `secondaryRoadMinCountZ` | `0` (0–128) | Minimum requested internal east/west secondary roads. |
| `secondaryRoadMaxCountZ` | `2` (0–128) | Maximum requested internal east/west secondary roads. Must be at least the minimum. |
| `minimumRoadSeparation` | `4` (2–32) | Minimum chunk distance between parallel secondary roads selected within the same primary block. It does not constrain tertiary roads. |
| `minimumRoadEdgeDistance` | `3` (2–32) | Minimum chunk distance from a secondary road to its bounding primary road. |
| `tertiaryRoadChance` | `0.4` (0–1) | Chance that a subdivided block gets one short tertiary access road. |
| `tertiaryRoadMinLength` | `2` (1–16) | Minimum tertiary-road length in chunks. |
| `tertiaryRoadMaxLength` | `5` (1–32) | Maximum tertiary-road length; must be at least the minimum. |
| `multiBuildingStreetConflict` | `OVERRIDE_MINOR` | Planned-road policy for random multi-buildings: `BLOCK_ALL` lets every road block them; `OVERRIDE_MINOR` lets only primary roads block them; `OVERRIDE_ALL` lets multi-buildings replace any road. Ignored in legacy mode. |

Primary-road spacing first creates a regular set of *candidate* corridors. `primaryRoadOptionalChance` randomly but deterministically enables non-forced candidates, while `primaryRoadForceEvery` guarantees that the gaps cannot grow indefinitely. For example, with both spacing values set to `12` and `primaryRoadForceEvery` also set to `12`, candidates occur every 12 chunks, but only every twelfth candidate is guaranteed: forced corridors are 144 chunks apart. Optional candidates between them may become corridors according to `primaryRoadOptionalChance`. The spacing and force values would describe the same interval only when `primaryRoadForceEvery` is `1`.

Secondary-road counts are requests rather than guarantees. If a primary block cannot fit the selected count while respecting `minimumRoadSeparation` and `minimumRoadEdgeDistance`, the planner places as many secondary roads as fit.

### Highways

`LEGACY` uses the Perlin and distance-mask options. `INTERCITY_NETWORK_V1` uses the hub/network options. Like the street mode, the highway mode is selected and persisted when a new world is initialized.

| Option | Default / values | Meaning |
| --- | --- | --- |
| `highwayGenerationMode` | `INTERCITY_NETWORK_V1` | `LEGACY` or `INTERCITY_NETWORK_V1`. |
| `highwayRequiresTwoCities` | `true` | Legacy highways require a valid city at both ends; `false` requires only one. |
| `highwayLevelFromCities` | `3` (0–4) | Shared height rule: `0` first endpoint, `1` lower endpoint, `2` higher endpoint, `3` integer endpoint average, `4` fixed `highwayNetworkLevel`. |
| `highwayDistanceMask` | `7` (≥0) | Legacy highway spacing bitmask applied to chunk coordinates; it is not a block distance. Use a power of two minus one; `0` disables legacy highways. |
| `highwayMainPerlinScale` | `50.0` (1–1000) | Legacy main-axis noise scale applied to chunk coordinates. Higher values produce more frequent, shorter highways. |
| `highwaySecondaryPerlinScale` | `10.0` (1–1000) | Legacy secondary-axis noise scale applied to chunk coordinates, controlling variation among nearby highways. |
| `highwayPerlinFactor` | `2.0` (-100–100) | Legacy noise threshold. `0` is roughly 50%; a high value suppresses highways. Legacy candidates occur only on chunks divisible by eight. |
| `highwaySupports` | `true` | Generates supports where needed; disable for highways spanning void. |
| `highwayPlanningCellSize` | `128` (32–512) | Planning-cell size in chunks for the intercity network. |
| `highwayHubSampleSpacing` | `16` (1–512) | Chunk spacing between deterministic city-potential samples in a cell; cannot exceed planning-cell size. Samples include terrain-height and biome-multiplier checks. |
| `highwayHubMinimumPotential` | `0.20` (0–1) | Minimum height- and biome-adjusted city-potential score needed to create a hub. Lower values make inter-city highway hubs more common. |
| `highwayHubSearchRadiusCells` | `2` (0–8) | Planning-cell radius in which a hub considers other hubs. |
| `highwayMinimumHubDistance` | `64` (0–4096) | Minimum Euclidean distance in chunks between connected hubs. |
| `highwayMaximumHubDistance` | `320` (0–4096) | Maximum connected-hub distance; cannot be below the minimum. |
| `highwayMaximumConnectionsPerHub` | `2` (1–8) | Maximum intercity connections incident to one hub. |
| `highwayMinimumRouteLength` | `40` (0–4096) | Minimum Manhattan route length in chunks. |
| `highwayRouteCityPenalty` | `1.0` (0–1000) | Weight of approximate city potential when selecting an L-shaped route's bend. |
| `highwayNetworkLevel` | `0` (0–32) | Fixed highway level used when `highwayLevelFromCities` is `4`. |

### Railways, loot, and generation details

| Option | Default / range | Meaning |
| --- | --- | --- |
| `railwayDungeonChance` | `0.01` (0–1) | Chance that a chunk next to a railway gets a railway dungeon. |
| `railwaysCanEnd` | `false` | Allows an ending rail part where a missing city would otherwise have supplied a station. Useful with rare cities. |
| `railwaysEnabled` | `true` | Enables rail lines. Stations are controlled separately and may still generate when this is false. |
| `railwayStationsEnabled` | `true` | Enables railway stations. |
| `railwaySurfaceStationsEnabled` | `true` | Enables surface stations; false restricts generation to underground stations. |
| `generateSpawners` | `true` | Allows configured spawners in buildings. |
| `generateLoot` | `true` | Allows configured loot in building chests. |
| `generateLighting` | `false` | Adds minimal building lighting. |
| `chestWithoutLootChance` | `0.2` (0–1) | Chance that an otherwise eligible chest is empty. |
| `buildingWithoutLootChance` | `0.2` (0–1) | Chance that a building has neither loot nor spawners. |

## `cities`

| Option | Default / range | Meaning |
| --- | --- | --- |
| `cityChance` | `0.01` (-1–1) | Chance that a chunk becomes a city center. Exactly `-1` selects the Perlin rarity variant. |
| `cityMinRadius` | `50` (1–2000) | Minimum city radius in blocks when using random city centers. |
| `cityMaxRadius` | `128` (1–2000) | Maximum city radius in blocks. |
| `cityPerlinScale` | `3.0` (-1000000–1000000) | Rarity-map scale applied to chunk coordinates when `cityChance` is `-1`. |
| `cityPerlinInnerScale` | `0.1` (-1000000–1000000) | Internal rarity-map scale applied to chunk coordinates in Perlin mode. |
| `cityPerlinOffset` | `0.1` (-1000000–1000000) | Dimensionless rarity-map value offset in Perlin mode; it is not a spatial offset. |
| `cityThreshold` | `0.2` (0–1) | City-factor threshold at which overlapping city circles count as city. |
| `citySpawnDistance1` | `0` (0–10000000) | First distance from spawn, in blocks, for city-factor scaling. |
| `citySpawnMultiplier1` | `1.0` (0–1) | City-factor multiplier at and below the first distance. |
| `citySpawnDistance2` | `0` (0–10000000) | Second distance from spawn, in blocks; `0` disables spawn-distance scaling. Between distances the multiplier is interpolated. |
| `citySpawnMultiplier2` | `1.0` (0–1) | City-factor multiplier at and beyond the second distance. |
| `cityStyleThreshold` | `-1` (disabled; editor range 0–1) | Uses `cityStyleAlternative` when city factor falls below this threshold. The default disables the switch. |
| `cityStyleAlternative` | `""` | Alternative city-style identifier used with `cityStyleThreshold`. |
| `cityAvoidVoid` | `true` | In floating landscapes, suppresses city chunks detected as void, avoiding cities on island edges. |
| `cityLevel0Height` | `75` (1–384) | Chunks below this terrain Y coordinate, in blocks, use city level 0. |
| `cityLevel1Height` | `83` (1–384) | Chunks below this terrain Y coordinate, in blocks, use city level 1. |
| `cityLevel2Height` | `91` (1–384) | Chunks below this terrain Y coordinate, in blocks, use city level 2. |
| `cityLevel3Height` | `99` (1–384) | Chunks below this terrain Y coordinate, in blocks, use city level 3. |
| `cityLevel4Height` | `107` (1–384) | Chunks below this terrain Y coordinate, in blocks, use city level 4. |
| `cityLevel5Height` | `115` (1–384) | Chunks below this terrain Y coordinate, in blocks, use city level 5. |
| `cityLevel6Height` | `123` (1–384) | Chunks below this terrain Y coordinate, in blocks, use city level 6. |
| `cityLevel7Height` | `131` (1–384) | Chunks below this terrain Y coordinate, in blocks, use city level 7. |
| `cityMinHeight` | `50` (-1024–2048) | No cities generate below this terrain Y coordinate, in blocks. |
| `cityMaxHeight` | `150` (-1024–2048) | No cities generate above this terrain Y coordinate, in blocks. |
| `oceanCorrectionBorder` | `4` (-255–255) | Vertical terrain-correction offset, in blocks, for ocean-biome chunks adjacent to cities. |

## `explosions`

The normal and mini systems are independent. Radius is in blocks and height is a Y coordinate.

| Option | Default / range | Meaning |
| --- | --- | --- |
| `explosionChance` | `0.002` (0–1) | Per-chunk chance of a normal explosion. |
| `explosionMinRadius` | `15` (1–1000) | Minimum normal explosion radius in blocks. |
| `explosionMaxRadius` | `35` (1–3000) | Maximum normal explosion radius in blocks. |
| `explosionMinHeight` | `75` (1–256) | Minimum normal explosion-center Y coordinate, in blocks. |
| `explosionMaxHeight` | `90` (1–256) | Maximum normal explosion-center Y coordinate, in blocks. |
| `miniExplosionChance` | `0.03` (0–1) | Per-chunk chance of a mini explosion. |
| `miniExplosionMinRadius` | `5` (1–1000) | Minimum mini explosion radius in blocks. |
| `miniExplosionMaxRadius` | `12` (1–3000) | Maximum mini explosion radius in blocks. |
| `miniExplosionMinHeight` | `60` (1–256) | Minimum mini explosion-center Y coordinate, in blocks. |
| `miniExplosionMaxHeight` | `100` (1–256) | Maximum mini explosion-center Y coordinate, in blocks. |
| `explosionsInCitiesOnly` | `true` | Requires blast centers to be in cities, although damage may extend outside them. |
| `debrisToNearbyChunkFactor` | `200` (1–10000) | Controls debris spilling from nearby damaged chunks; larger values produce less spillover. |

## `cityspheres`

These settings primarily affect sphere-based landscape types. Options mentioning an outside world control terrain beyond a sphere.

| Option | Default / range | Meaning |
| --- | --- | --- |
| `citySphereFactor` | `1.2` (0.1–10) | In `space`, multiplier from city radius to outer glass-sphere radius. |
| `citySphereChance` | `0.7` (0–1) | Chance that a city gets a sphere. |
| `citySphereClearAbove` | `0` (0–1024) | Blocks cleared above the top sphere glass; `0` disables clearing. |
| `citySphereClearAboveUntilAir` | `false` | After the fixed clearing, continues upward until air is reached. |
| `citySphereClearBelow` | `0` (0–1024) | Blocks cleared below the sphere top; `0` disables clearing. |
| `citySphereClearBelowUntilAir` | `false` | After the fixed clearing, continues downward until air is reached. |
| `sphereSurfaceVariation` | `1.0` (0–1) | Surface variation inside spheres; smaller values make terrain more varied. |
| `outsideSurfaceVariation` | `1.0` (0–1) | Surface variation outside spheres; smaller values make terrain more varied. |
| `monorailChance` | `0.8` (0–1) | Chance of requesting a monorail in each direction. A connection needs a sphere at the other end that also requests it. |
| `monorailOffset` | `-2` (-100–100) | Monorail vertical offset in blocks relative to the main sphere height. |
| `onlyPredefined` | `false` | Generates only spheres supplied by predefined assets. |
| `outsideProfile` | `""` | Optional profile name used to generate the world outside spheres. |
| `outsideGroundLevel` | `-1` (-1–256) | **Deprecated.** Outside ground Y coordinate in blocks; use the `groundLevel` of `outsideProfile` instead. |
| `grid32` | `false` | Aligns sphere centers to a 32×32 chunk grid instead of a 16×16 grid. |

## `client`

These options have an effect only when the client has Lost Cities installed. `-1` leaves the vanilla/default value unchanged.

| Option | Default / range | Meaning |
| --- | --- | --- |
| `horizon` | `-1` (-1–256) | Client horizon height. |
| `fogRed` | `-1` (-1–1) | Red fog component; normal explicit colors use 0–1. |
| `fogGreen` | `-1` (-1–1) | Green fog component. |
| `fogBlue` | `-1` (-1–1) | Blue fog component. |
| `fogDensity` | `-1` (-1–1) | Fog density override. |

## Validation and compatibility notes

- Minimum values must not exceed their corresponding maximums for secondary-road counts, tertiary-road lengths, or highway hub distances.
- `highwayHubSampleSpacing` cannot exceed `highwayPlanningCellSize`.
- Mode names and conflict-policy names are case-insensitive when read, but the generated profiles use the uppercase enum names shown above.
- Profile defaults and available options can change between mod versions. Keep custom profiles under version control and compare them with a newly generated default profile after upgrading.

## Common profile recipes

The snippets below contain only the relevant sections. Merge them into a copy of a generated profile rather than using them as complete profile files. Try changes in a new world first: generated chunks are not rebuilt, and an existing world's persisted street and highway generation modes do not change when the corresponding mode option is edited.

### Make cities rarer but larger

Lower `cityChance` to create fewer city centers, then raise both radius bounds to make each resulting city cover more ground:

```json
{
  "cities": {
    "cityChance": 0.003,
    "cityMinRadius": 100,
    "cityMaxRadius": 240
  }
}
```

These values are a starting point, not an equivalent exchange: larger cities can overlap, so inspect several seeds and tune `cityChance` first. Keep `cityMinRadius` below `cityMaxRadius`.

### Increase or decrease inter-city highways

For `INTERCITY_NETWORK_V1`, `highwayHubMinimumPotential` is the closest control to highway frequency. Lower it to let weaker city regions become hubs; raise it to require stronger city regions. Adjust it in steps of about `0.05`. `highwayMaximumConnectionsPerHub` separately controls how many routes a hub can have.

For example, this makes a denser network:

```json
{
  "lostcity": {
    "highwayGenerationMode": "INTERCITY_NETWORK_V1",
    "highwayHubMinimumPotential": 0.15,
    "highwayMaximumConnectionsPerHub": 3
  }
}
```

For fewer highways, try a threshold of `0.30` and leave the connection limit at `2`, or set it to `1` for a very sparse network. A value of `0` for `highwayHubSearchRadiusCells` prevents hubs from finding neighbors and therefore disables inter-city routes.

### Increase or decrease legacy highways

For `LEGACY`, lower `highwayPerlinFactor` to make candidates more common and raise it to make them rarer. `highwayDistanceMask` controls the candidate-line spacing and must be a power of two minus one; `7`, `15`, and `31` produce progressively wider spacing, while `0` disables legacy highways.

```json
{
  "lostcity": {
    "highwayGenerationMode": "LEGACY",
    "highwayPerlinFactor": 1.5,
    "highwayDistanceMask": 7,
    "highwayRequiresTwoCities": false
  }
}
```

Allowing one city endpoint with `highwayRequiresTwoCities: false` is especially useful in rare-city profiles.

### Make the city street grid denser

These settings enable every primary-road candidate, request more secondary roads inside each primary block, and make tertiary access roads more likely:

```json
{
  "lostcity": {
    "streetGenerationMode": "HIERARCHICAL_GRID_V1",
    "primaryRoadSpacingX": 8,
    "primaryRoadSpacingZ": 8,
    "primaryRoadOptionalChance": 1.0,
    "primaryRoadForceEvery": 1,
    "secondaryRoadMinCountX": 2,
    "secondaryRoadMaxCountX": 3,
    "secondaryRoadMinCountZ": 2,
    "secondaryRoadMaxCountZ": 3,
    "tertiaryRoadChance": 0.8
  }
}
```

Dense road settings leave fewer building lots. To make a sparser grid, increase both primary spacing values, lower `primaryRoadOptionalChance`, request fewer secondary roads, and lower `tertiaryRoadChance`.

### Make taller, more building-heavy cities

Raise `buildingChance` for fewer street or open-lot chunks, then increase the floor bounds. Taller buildings cost more generation and rendering time.

```json
{
  "lostcity": {
    "buildingChance": 0.6,
    "buildingMinFloors": 3,
    "buildingMinFloorsChance": 6,
    "buildingMaxFloorsChance": 12,
    "buildingMaxFloors": 16
  }
}
```
