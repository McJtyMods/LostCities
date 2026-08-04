# Profile options in the asset system

## Goal

Allow asset packs to override profile-controlled generation where the setting
logically belongs to a world style or a city style, while keeping every current
profile option valid and preserving current generation when an asset does not
opt in to an override.

This is a plan only. It does not propose removing profile fields or changing
the built-in assets as part of the first implementation.

## Current ownership and behavior

The current selection chain is:

```text
profile -> world style -> city style -> palette style/content
```

The profile chooses one world style. The world style controls world-wide
selection and infrastructure and chooses city styles. A city style controls
content and presentation for a selected city/chunk.

There is already a partial version of the desired fallback system. The
following nullable city-style values fall back to, or constrain a value started
from, the profile at their use sites:

- `buildingChance`
- `minfloors`, `maxfloors`, `mincellars`, and `maxcellars`
- `parkChance`
- `fountainChance`
- `frontChance`
- `corridorChance`
- `avoidFoliage`
- `parkBorder`
- `parkElevation`
- `parkStreetThreshold`

The floor and cellar fields are currently constraints rather than simple
replacements in all cases: building limits can further constrain the
profile/city-style result. That behavior must remain intact.

The existing top-level city-style `explosionchance` is **not** a profile
override. The profile first decides whether a normal or mini explosion exists,
and `explosionchance` then acts as an additional filter when the explosion is
collected for an affected chunk. It applies to both explosion sizes. This
legacy factor is a public asset behavior and must not be silently reinterpreted
as an override.

`OPEN_LOT_PARK_CHANCE` does not use the existing city-style `parkChance`.
Hierarchical generation reads it directly from the profile. This was
intentional when hierarchical open lots were added, so that their default
frequency could differ from legacy parks, but it is now the clearest missing
city-style override.

World styles currently have world settings, multi-building settings, sphere
settings, scattered settings, infrastructure part selectors, city-style
selectors, and biome multipliers. Most of these codec objects use concrete
defaults. They cannot be reused directly for profile fallback because an
omitted asset value must remain distinguishable from an explicit value.

## Compatibility contract

The implementation should adopt these rules as hard requirements:

1. All existing profile JSON keys, Java fields, defaults, ranges, setup APIs,
   network serialization, and GUI behavior remain supported.
2. An absent asset override means "use the profile value." It must not mean a
   new codec default.
3. With current profiles and assets, effective values and random-number
   consumption remain unchanged.
4. Built-in world styles and city styles should not gain new override values in
   the compatibility phase. Adding such values would intentionally change all
   profiles that select those assets.
5. City-style inheritance resolves asset values first. Only after inheritance
   is complete does an unresolved value fall back to the profile.
6. An explicit `false`, `0`, empty string, or empty list is an override. Only
   absence means fallback.
7. Global topology never depends on a city-style override. City style is often
   selected after topology, and neighboring chunks may resolve different
   styles. Letting it affect a global planner would create dependency cycles or
   query-order-sensitive generation.
8. Cross-chunk decisions use one documented canonical owner. A setting must
   not be read independently from both ends of a bridge, doorway, or other
   shared feature.
9. Existing-world behavior follows the existing asset model: overrides affect
   only newly generated chunks. Persisted street/highway *mode* selection still
   wins over requested profile or asset modes.

The effective-value precedence is deliberately simple:

```text
city-owned option:  inherited city-style override -> profile
world-owned option: world-style override          -> profile
profile-owned option: profile only
```

Do not put the same new option in both world style and city style. That avoids
an unnecessary third precedence layer and makes the spatial scope obvious.

## Proposed asset representation

Add an optional `profile_overrides` object to both `WorldStyleRE` and
`CityStyleRE`. The nested keys should use the existing profile JSON names so
pack authors do not have to learn a second vocabulary. Each codec field must be
nullable/optional with no value default.

Only the keys assigned to that asset type in the audit below are legal. Reject
a city-topology field in a city style instead of silently ignoring it.

Example:

```json
{
  "style": "standard",
  "inherit": "citystyle_common",
  "profile_overrides": {
    "buildingDoorwayChance": 0.9,
    "openLotParkChance": 0.25,
    "ruinChance": 0.5
  }
}
```

```json
{
  "outsidestyle": "outside",
  "profile_overrides": {
    "cityChance": 0.02,
    "streetGenerationMode": "HIERARCHICAL_GRID_V1",
    "primaryRoadSpacingX": 12,
    "primaryRoadSpacingZ": 12,
    "railwaysEnabled": false
  },
  "citystyles": [
    {
      "factor": 1.0,
      "citystyle": "citystyle_standard"
    }
  ]
}
```

Implementation records should be split by concern to keep codecs and
validation manageable, even if JSON presents a single flat
`profile_overrides` object. Suggested internal groups are `TerrainOverrides`,
`CityOverrides`, `StreetOverrides`, `HighwayOverrides`, `RailwayOverrides`,
`SphereOverrides`, `DamageOverrides`, `BuildingOverrides`,
`ParkOverrides`, `DecayOverrides`, and `LootOverrides`.

Existing city-style keys such as `buildingsettings.buildingchance` and
`parkblocks.parkchance` must remain supported. Where their semantics already
match a profile fallback, they should populate the same resolved override model
as the new keys. If both the old city-style key and the new
`profile_overrides` spelling are present for the same option, fail codec
validation with a duplicate-setting error rather than choosing silently. A
later cleanup may document one spelling as preferred, but should not remove the
old spelling.

The legacy top-level `explosionchance` is the exception: keep it as a separate
post-filter because it does not have override semantics. New
`profile_overrides.explosionChance` and
`profile_overrides.miniExplosionChance` values control the initial per-center
chances, after which the legacy filter still applies.

## Complete option audit

The audit includes all public generation fields in `LostCityProfile`, the two
public array fields, and the private profile values exposed as JSON options.

### Keep in the profile

These describe the selectable profile, choose the asset root, control an
operational mode, impose server/modpack policy, or select a player spawn. They
do not describe a particular world style or city style.

| Options | Reason |
| --- | --- |
| `description`, `extraDescription`, `warning`, `icon` | Profile-selection UI metadata. |
| `worldStyle` | Chooses the world-style asset and therefore cannot be overridden by that asset. |
| `editMode` | Operational developer mode, not generated content. |
| `generateNether` | Chooses whether the mod installs Nether generation; it is lifecycle/configuration policy rather than styling. |
| `generateSpawners`, `generateLoot` | Modpack/player safety and reward policy. Assets must not be able to turn these back on after a profile disables them. City styles can still vary the chances below these global gates. |
| `spawnBiome`, `spawnCity`, `spawnSphere`, `spawnNotInBuilding`, `forceSpawnInBuilding`, `forceSpawnBuildings`, `forceSpawnParts`, `spawnCheckRadius`, `spawnRadiusIncrease`, `spawnCheckAttempts` | Player spawn selection happens at profile/world lifecycle scope and may itself search for cities/styles. |
| `outsideProfile` | Refers to another config profile. Keeping profile-to-profile linkage out of datapack assets avoids an asset requiring a machine-local profile name. |
| `outsideGroundLevel` | Deprecated. Keep readable for compatibility, but do not add a new asset representation. |
| `horizon`, `fogRed`, `fogGreen`, `fogBlue`, `fogDensity` | Client presentation attached to the selected profile, not server datapack city content. |

`public` and the generated `__readonly__` metadata also remain profile-format
concerns.

### Move to world-style override scope

"Move" here means adding an optional world-style override. The profile field
remains the fallback and public compatibility surface.

#### Base world and terrain

| Options | Reason / constraint |
| --- | --- |
| `landscapeType` | Defines the world-wide terrain algorithm. Resolve it immediately after the profile selects its world style. This is a bootstrap-sensitive change and should be implemented after simpler overrides. |
| `liquidBlock`, `baseBlock` | World-wide base materials used before a city style is known. Validate block IDs during effective world settings creation. |
| `groundLevel`, `seaLevel`, `bedrockLayer` | Dimension-wide vertical frame. Per-city values would produce incompatible boundaries and infrastructure heights. |
| `terrainFixLowerMinOffset`, `terrainFixLowerMaxOffset`, `terrainFixUpperMinOffset`, `terrainFixUpperMaxOffset`, `oceanCorrectionBorder` | Terrain/city boundary algorithms need one consistent world value. |
| `avoidWater` | Changes base placement behavior across the generated world. |
| `scatteredChanceMultiplier` | Scattered placement is owned by the world style already and happens outside city-style selection. |
| `useAvgHeightmap`, `multiUseCorner` | City-level and multi-building placement algorithms operate across chunks and need one policy. |

#### City occurrence, selection, and vertical levels

| Options | Reason / constraint |
| --- | --- |
| `cityChance`, `cityMinRadius`, `cityMaxRadius` | Determine whether and where cities exist, before a city style can be selected. |
| `cityPerlinScale`, `cityPerlinInnerScale`, `cityPerlinOffset`, `cityThreshold` | Define the world-wide city potential field. |
| `citySpawnDistance1`, `citySpawnDistance2`, `citySpawnMultiplier1`, `citySpawnMultiplier2` | Modify that same city field relative to spawn. |
| `cityStyleThreshold`, `cityStyleAlternative` | These are selection rules between city styles and therefore belong to the selector's owner, the world style. Longer term, express the threshold as a proper world-style selector condition, but retain these override keys for exact profile compatibility. |
| `cityAvoidVoid` | Suppresses city membership before style selection. |
| `cityLevel0Height`, `cityLevel1Height`, `cityLevel2Height`, `cityLevel3Height`, `cityLevel4Height`, `cityLevel5Height`, `cityLevel6Height`, `cityLevel7Height` | Shared mapping from terrain height to city/infrastructure level. |
| `cityMinHeight`, `cityMaxHeight` | World-wide city eligibility and highway-hub inputs. |

The effective values of every city-potential input must be included in the
inter-city highway hub-cache signature. The selected world-style ID alone is
not sufficient because datapacks can replace an asset under the same ID.

#### Street and multi-building topology

| Options | Reason / constraint |
| --- | --- |
| `streetGenerationMode` | Chooses a dimension-wide planner. The persisted per-dimension mode continues to win in existing worlds. |
| `primaryRoadSpacingX`, `primaryRoadSpacingZ`, `primaryRoadOptionalChance`, `primaryRoadForceEvery` | Inputs to one global deterministic primary-road field. |
| `secondaryRoadMinCountX`, `secondaryRoadMaxCountX`, `secondaryRoadMinCountZ`, `secondaryRoadMaxCountZ`, `minimumRoadSeparation`, `minimumRoadEdgeDistance` | Inputs to primary-block planning, which can span many cities/styles. |
| `tertiaryRoadChance`, `tertiaryRoadMinLength`, `tertiaryRoadMaxLength` | Inputs to the same global street planner. |
| `multiBuildingStreetConflict` | Resolves conflicts between world-planned roads and multi-buildings. It must not vary midway through a planning area. |
| `plannedPrimaryBridgeChance`, `plannedPrimaryBridgeMaxLength` | Determine canonical bridge spans on the global primary-road field. |

`StreetPlannerSettings` and `HierarchicalBridgePlanner` should consume one
immutable effective world settings snapshot rather than reading the profile
directly.

#### Highways

| Options | Reason / constraint |
| --- | --- |
| `highwayGenerationMode` | Chooses a dimension-wide planner. Preserve the independently persisted existing-world mode. |
| `highwayRequiresTwoCities`, `highwayMainPerlinScale`, `highwaySecondaryPerlinScale`, `highwayPerlinFactor`, `highwayDistanceMask` | Define the global legacy highway field. |
| `highwayLevelFromCities`, `highwayNetworkLevel` | Shared height policy for complete highway routes. |
| `highwaySupports` | Infrastructure-wide rendering policy; a highway can pass through several city styles and outside terrain. |
| `highwayPlanningCellSize`, `highwayHubSampleSpacing`, `highwayHubMinimumPotential`, `highwayHubSearchRadiusCells`, `highwayMinimumHubDistance`, `highwayMaximumHubDistance`, `highwayMaximumConnectionsPerHub`, `highwayMinimumRouteLength`, `highwayRouteCityPenalty` | Define global inter-city hub and route topology. |

All effective inter-city settings must participate in the persisted highway
cache signature. Changing a world-style override under the same asset ID must
invalidate only the affected dimension's derived hub cache, matching current
profile-change behavior.

#### Railways

| Options | Reason / constraint |
| --- | --- |
| `railwaysCanEnd`, `railwaysEnabled`, `railwayStationsEnabled`, `railwaySurfaceStationsEnabled` | Define cross-chunk railway topology and station eligibility. A railway may connect cities with different styles. |

The city style still chooses railway parts/materials. Only topology belongs in
the world-style override.

#### City spheres

| Options | Reason / constraint |
| --- | --- |
| `grid32`, `citySphereFactor`, `citySphereChance`, `onlyPredefined` | Determine sphere existence, grid, and radius before a city style is available. |
| `monorailChance`, `monorailOffset` | Monorails connect multiple spheres and need one world policy. |
| `citySphereClearAbove`, `citySphereClearBelow`, `citySphereClearAboveUntilAir`, `citySphereClearBelowUntilAir` | Terrain operations at sphere scope. |

`sphereSurfaceVariation` and `outsideSurfaceVariation` are loaded and presets
assign `outsideSurfaceVariation`, but neither value currently has a runtime
consumer. Do not add asset overrides that pretend to work. First decide whether
to restore their intended terrain effect or deprecate them. If restored, they
belong to world-style sphere overrides and must retain the profile fallback.

#### Global damage constraints

| Options | Reason / constraint |
| --- | --- |
| `explosionMinRadius`, `explosionMaxRadius`, `explosionMinHeight`, `explosionMaxHeight` | Damage can cross city/style boundaries, and maximum radius controls the neighbor search area. Keep one world-wide shape. |
| `miniExplosionMinRadius`, `miniExplosionMaxRadius`, `miniExplosionMinHeight`, `miniExplosionMaxHeight` | Same constraint for mini explosions. |
| `explosionsInCitiesOnly`, `debrisToNearbyChunkFactor` | Cross-chunk damage policy. |

Explosion-center chances remain city-owned. A world style does not need
duplicate overrides for them because the profile is already the global
default.

### Move to city-style override scope

These values can be resolved from the selected, inherited city style at the
point where the current profile value is read. The profile remains the default.

#### Buildings and local rewards

| Options | Reason / constraint |
| --- | --- |
| `buildingChance` | Already supported as a nullable city-style override. Preserve behavior. |
| `buildingMinFloors`, `buildingMaxFloors`, `buildingMinCellars`, `buildingMaxCellars` | Already supported as city-style constraints. Preserve the current interaction with building-specific limits. |
| `buildingMinFloorsChance`, `buildingMaxFloorsChance` | Control the city-factor-dependent building-height distribution and can safely vary with the selected city style. |
| `buildingDoorwayChance` | Door attempts are local to a building. For a shared edge, the building that owns the doorway decision supplies the style. |
| `buildingFrontChance` | Already supported as `streetblocks.frontchance`. Preserve behavior. |
| `buildingWithoutLootChance` | Building-local reward/spawner suppression. It remains subordinate to profile `generateLoot` and `generateSpawners`. For a multibuilding, use the canonical multibuilding style selected by existing placement logic. |
| `chestWithoutLootChance` | Resolve from the owning building chunk's city style when processing the chest. Profile `generateLoot=false` remains an unconditional global gate. |
| `generateLighting` | Aesthetic building generation that can vary by city theme. Resolve from the owning chunk's style without changing the existing random stream. |

#### Parks, streets, corridors, and bridges

| Options | Reason / constraint |
| --- | --- |
| `parkChance` | Existing city-style override for legacy park selection. |
| `openLotParkChance` | Add a separate override for hierarchical open lots. Do not alias it to `parkChance`; the profile intentionally has two independent defaults. |
| `parkElevation`, `parkBorder`, `parkStreetThreshold` | Existing city-style overrides. |
| `fountainChance`, `corridorChance` | Existing city-style overrides. |
| `bridgeChance` | Legacy bridge candidacy can use the style of the canonical city-side anchor from which the bridge decision is made. Document that owner and never combine independent endpoint chances. |
| `bridgeSupports` | The bridge asset/style owns its visual support policy. Resolve it from the same canonical bridge owner as `bridgeChance`; this is distinct from world-wide `highwaySupports`. |

The style of a park/open lot must be resolved before applying its chance, but
style selection must not depend on whether the lot becomes a park. This avoids
a decision cycle.

#### Decay and vegetation

| Options | Reason / constraint |
| --- | --- |
| `vineChance`, `randomLeafBlockChance`, `randomLeafBlockThickness` | Local visual aging of city blocks. The city style already supplies the actual leaf/vine-related materials. |
| `avoidFoliage` | Existing city-style override. |
| `rubbleLayer`, `rubbleDirtScale`, `rubbleLeaveScale` | Local visual decay; city style already supplies rubble/leaves palette characters. |
| `ruinChance`, `ruinMinlevelPercent`, `ruinMaxlevelPercent` | Building-local ruin distribution and cut height. Resolve once from the building's canonical style. |

`rubbleLeaveScale` retains its misspelled historical profile name for
compatibility. An asset alias such as `rubbleLeafScale` should not be introduced
unless profile parsing also gains a documented alias.

#### Local rail and damage frequency

| Options | Reason / constraint |
| --- | --- |
| `railwayDungeonChance` | The dungeon is selected beside a railway from the local city style, which already owns the rail-dungeon part selector. It does not alter railway topology. |
| `explosionChance` | Add a true local override for the initial normal-blast center chance. The existing city-style `explosionchance` factor is a later, separate compatibility filter and must still run. |
| `miniExplosionChance` | Add the analogous local override for initial mini-blast center chance. Explosion shape and cross-chunk policy stay world-owned, and the legacy `explosionchance` filter still applies. |

For damage chance, select the style deterministically from the candidate blast
center before deciding whether the candidate exists. A missing/non-city style
uses the profile value. The neighbor scan must continue to use the effective
world-wide maximum radii. Preserve the legacy second-stage style filter and its
random consumption exactly when no new override is present.

## Options that need special handling

### Landscape bootstrap

`landscapeType` is logically world-style-owned, but the profile currently helps
select generation behavior very early. Implement the resolver in two stages:

1. Read the profile's `worldStyle`.
2. Load that world style and resolve bootstrap-safe world overrides, including
   `landscapeType`.
3. Construct the immutable dimension settings used by generation.

If registry availability makes that unsafe in every dimension/bootstrap path,
leave `landscapeType` profile-only in the first release rather than adding a
partially applied override.

### Outside sphere profile

The outside world has its own profile today. Its effective settings must be
resolved against *its own* selected world style. The outer profile's world
style must not accidentally override the nested profile's values. Detect
profile-reference cycles explicitly.

### Modes versus mode settings

The actual street and highway modes are persisted per dimension. Asset
overrides can request a mode only for a new world/dimension under the same
lifecycle rules as the profile. In an existing world:

```text
persisted mode -> effective requested mode (world style -> profile)
```

All non-mode planner settings follow the existing behavior for ungenerated
chunks and caches; they are not frozen merely because the mode is persisted.

### Current dormant values

Before implementing overrides, run a usage audit as a compilation-phase
checklist. At present `sphereSurfaceVariation` and
`outsideSurfaceVariation` have no runtime read. They should be separately
fixed or deprecated, not silently included in the new schema.

## Implementation structure

### Phase 1: Effective settings and schema

1. Introduce immutable nullable override records for the world and city scopes.
2. Add `profile_overrides` to `WorldStyleRE` and `CityStyleRE`.
3. Add range and cross-field validation matching `LostCityProfile`, including
   min/max pairs, street count/length pairs, highway distances, and hub sample
   spacing versus cell size.
4. Extend city-style inheritance to copy every unresolved override field.
5. Introduce immutable resolved views:
   - `EffectiveWorldSettings`, created once per `IDimensionInfo`;
   - `EffectiveCitySettings`, resolved from an initialized `CityStyle` plus the
     profile when local information is needed.
6. Keep `LostCityProfile` as the serialization, API, GUI, and fallback owner.
   Do not mutate a profile instance with asset values.

Resolved views prevent scattered ad-hoc ternaries and make it possible to audit
which scope every consumer uses. They also avoid shared mutable profile state
when dimensions use the same profile with different world-style assets.

### Phase 2: Complete the city-style path

1. Route all current city-style fallbacks through `EffectiveCitySettings`
   without changing semantics.
2. Add `openLotParkChance` first, covering both the weighted park-part decision
   and the no-part fallback.
3. Add the remaining building, loot, park/bridge, decay, rail-dungeon, lighting,
   ruin, and mini-explosion frequency consumers.
4. For every cross-chunk consumer, document and use its canonical style owner.
5. Ensure city-style lookup itself does not consume new random values or depend
   on the outcome controlled by the override.

### Phase 3: Add world-style settings

1. Resolve base terrain/city settings during dimension-info construction.
2. Convert `StreetPlannerSettings` and `HighwayPlannerSettings` from profile
   reads to `EffectiveWorldSettings`.
3. Convert rail, sphere, terrain correction, city-potential, and global damage
   consumers.
4. Include all effective inputs in derived-data signatures. In particular,
   update `LostCityHighwayData` hashing for world-style overrides.
5. Implement bootstrap-sensitive `landscapeType`, `baseBlock`, and
   `liquidBlock` last, after every dimension and outside-profile construction
   path is known to have registry access.

### Phase 4: Documentation and intentional built-in use

1. Update `docs/asset_structure.md` with schema, precedence, inheritance,
   duplicate-key validation, and examples.
2. Update `docs/profile_options.md` so every eligible option states its asset
   scope and fallback behavior. Keep all profile entries documented.
3. Update `docs/city_generation.md` for effective settings, canonical
   cross-chunk ownership, and planner/cache behavior.
4. Add a concise user-facing entry to the current top section of
   `changelog.txt`.
5. Only in a separate, intentional content change should built-in assets add
   overrides. Such a change needs explicit release notes because it can alter
   newly generated chunks for multiple profiles.

## Verification

Project policy calls for human in-game testing rather than new automated tests.
Compilation and static inspection should still be used.

### Static checks

- Compile with Java 17 using `./gradlew compileJava`.
- Search production code for direct reads of every migrated profile field.
  Each remaining read must be a deliberate profile-only fallback, GUI/API
  serialization, validation, or preset assignment.
- Verify all optional asset fields remain null when absent and that city-style
  inheritance does not replace an explicit `false` or `0`.
- Verify no new values are written into shared `LostCityProfile` objects.
- Verify effective world inputs are covered by relevant cache signatures.

### Human compatibility scenario

Using a fixed seed, create fresh worlds with representative built-in profiles
for default, floating, cavern, and sphere/space landscapes, with unchanged
built-in assets. Inspect the same chunk coordinates before and after the code
change. City membership, styles, building/street/park decisions, floor counts,
railways, highways, spheres, damage, and palette results should be unchanged.
No-override compatibility includes preserving the same random stream, not just
producing a superficially similar city.

### Human override scenarios

1. Make two city styles selected by one world style. Give them contrasting
   `parkChance` and `openLotParkChance` values and inspect both legacy and
   hierarchical modes. Each style should use its own values while a third
   style with no override follows the profile.
2. Give the styles contrasting building height, ruin, foliage/rubble, doorway,
   loot, and railway-dungeon settings. Inspect ordinary buildings and a
   multibuilding, including a boundary between styles.
3. Set world-style road/highway/rail/sphere overrides and verify complete
   cross-city infrastructure remains continuous and deterministic after a
   reload.
4. Set profile `generateLoot=false` and `generateSpawners=false` while city
   styles request permissive local chances. Confirm the profile gates still
   prevent rewards/spawners.
5. Load an existing world with persisted legacy modes while its world style
   requests modern modes. Confirm persisted modes win and no already generated
   chunks are modified.
6. Replace a world-style asset under the same ID with changed inter-city inputs.
   Confirm the affected dimension's highway hub signature invalidates and
   regeneration remains deterministic.

## Recommended first deliverable

The smallest useful implementation is the city-style completion slice:

- add `openLotParkChance` as a nullable city-style override;
- route the already-supported city settings through one resolver;
- preserve `PARK_CHANCE` and `OPEN_LOT_PARK_CHANCE` as independent profile
  fallbacks;
- update asset/profile/city-generation documentation and the changelog;
- visually compare a fixed-seed legacy city and hierarchical city with and
  without the override.

That delivers the motivating use case without touching global planners,
bootstrap ordering, persistent modes, or highway caches. The world-style
portion can then build on the same effective-settings model.
