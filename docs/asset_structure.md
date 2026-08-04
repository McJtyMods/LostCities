# Lost Cities asset system

This document describes the Lost Cities 1.20.1 asset system for modpack developers who want to replace the built-in content or distribute an expansion containing new buildings, palettes, city styles, and other assets. The source of truth is the codecs under `worldgen/lost/regassets` and the built-in examples under `src/main/resources/data/lostcities/lostcities`.

Assets are server data. They use Minecraft datapack registries and can therefore be supplied by a datapack or by a Forge low-code mod. A low-code mod is usually the more convenient format for a modpack: it lives in the `mods` directory, can declare Lost Cities as a required dependency, and needs no Java code.

## Mental model

Lost Cities structures are assembled in layers:

```text
profile -> world style -> city style -> palette style -> palettes -> blocks
                         |             |
                         |             +-> variants
                         +-> buildings -> parts
                         +-> multibuildings -> buildings -> parts
                         +-> parks, bridges, streets, rails, and other parts

world style -> scattered definitions -> buildings or multibuildings
predefined city -> buildings or multibuildings at fixed chunk positions
```

A **part** is the actual three-dimensional character grid. A **palette** maps those characters to block states. A **building** chooses one part for each floor. A **city style** chooses buildings and other city content. A **world style** chooses city styles and the world-wide infrastructure assets. This separation lets one structure shape use many materials and lets a pack add a building without replacing all of Lost Cities.

All choices made by the generator are deterministic for a world seed. Do not rely on datapack file order, JSON object order, or chunk generation order to control selection.

## Registry paths and asset identifiers

There are thirteen Lost Cities asset registries:

| Asset | Registry folder | Purpose |
|---|---|---|
| Building | `buildings` | Selects the parts used for the floors of one chunk-sized building |
| Part | `parts` | A three-dimensional character grid used for structures |
| Palette | `palettes` | Maps part characters to blocks and special behavior |
| Variant | `variants` | Reusable weighted block choices for palettes |
| Style | `styles` | Combines and randomizes palettes for a city chunk |
| City style | `citystyles` | Chooses buildings and city details and overrides generation settings |
| World style | `worldstyles` | Chooses city styles and world-wide infrastructure/scattered content |
| Condition | `conditions` | Weighted contextual choices, primarily mob and loot identifiers |
| Multibuilding | `multibuildings` | Arranges buildings over multiple chunks |
| Scattered | `scattered` | Describes a building or multibuilding placed outside a city |
| Stuff | `stuff` | Places small palette-defined columns in city chunks |
| Predefined city | `predefinedcities` | Places a city and optional structures at fixed coordinates |
| Predefined sphere | `predefinedspheres` | Places a city sphere at fixed coordinates |

The path for an asset is:

```text
data/<namespace>/lostcities/<registry-folder>/<path>.json
```

For example, this file defines the asset `myexpansion:towers/office` in the building registry:

```text
data/myexpansion/lostcities/buildings/towers/office.json
```

The namespace and path form a normal resource location. Use lowercase names. A reference containing a colon is used as written, such as `myexpansion:towers/office`. An unqualified reference such as `building1` is interpreted as `lostcities:building1`, **not** as an asset in the namespace of the referencing file. Always qualify references to your expansion's assets.

The JSON file contains only the asset value. Older Lost Cities formats wrapped entries in an array and used `type` and `name` properties; those properties do not belong in the current datapack format. The file path is the name and the registry folder is the type.

### Adding and overriding

To add an asset, put it under your own namespace. It coexists with all built-in assets. It will only appear in generation after something reachable selects it: for example, a new building must be referenced by a city style, and a new city style must be referenced by the active world style.

To override an asset, supply the exact same namespace, registry folder, and path in a higher-priority pack. For example:

```text
data/lostcities/lostcities/conditions/easymobs.json
```

replaces `lostcities:easymobs`. References already pointing at that ID automatically see the replacement. Overrides are whole registry values; JSON objects and lists are not merged field by field. Copy the current built-in file and modify it, or supply a complete valid replacement.

Normal datapack priority decides which duplicate resource wins. A low-code mod's built-in datapack is enabled with the mod. If several mods replace the same ID, pack ordering matters; avoid that ambiguity where possible by adding namespaced content and replacing only the smallest built-in selector that must know about it.

Asset data is bound to the world's datapack registry. Develop against a disposable world, watch `latest.log` for datapack/codec errors, and restart or recreate the world after changing registry data. Existing generated chunks are not regenerated when assets change, and changing an asset can make newly generated chunks differ from older chunks in the same world.

## A minimal custom-building expansion

A new building normally needs at least a part, a palette (or palette entries already supplied by the active style), a building, and a reference from a city style. This example keeps everything under `myexpansion` and inherits from a built-in city style so the normal content remains available.

First define `data/myexpansion/lostcities/palettes/office.json`:

```json
{
  "palette": [
    { "char": "#", "block": "minecraft:bricks", "damaged": "minecraft:iron_bars" },
    { "char": "G", "block": "minecraft:light_blue_stained_glass" },
    { "char": " ", "block": "minecraft:air" }
  ]
}
```

Then define one six-block-high floor as `data/myexpansion/lostcities/parts/office_floor.json`:

```json
{
  "xsize": 16,
  "zsize": 16,
  "slices": [
    [
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################"
    ],
    [
      "#######GG#######",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "G              G",
      "G              G",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#######GG#######"
    ],
    [
      "#######GG#######",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "G              G",
      "G              G",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#######GG#######"
    ],
    [
      "#######GG#######",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "G              G",
      "G              G",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#######GG#######"
    ],
    [
      "#######GG#######",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "G              G",
      "G              G",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#              #",
      "#######GG#######"
    ],
    [
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################",
      "################"
    ]
  ]
}
```

Define `data/myexpansion/lostcities/buildings/office.json`:

```json
{
  "filler": "#",
  "rubble": "#",
  "refpalette": "myexpansion:office",
  "minfloors": 1,
  "maxfloors": 4,
  "parts": [
    { "part": "myexpansion:office_floor", "top": false },
    { "part": "myexpansion:office_floor", "top": true }
  ]
}
```

Finally make it reachable. The least invasive option is usually a custom city style inheriting a built-in style, followed by a custom world style that selects it. Inheritance appends selector entries from the parent, so this retains the standard buildings:

`data/myexpansion/lostcities/citystyles/office_city.json`:

```json
{
  "inherit": "citystyle_standard",
  "selectors": {
    "buildings": [
      { "factor": 1.0, "value": "myexpansion:office" }
    ]
  }
}
```

Copy `lostcities:standard` to `data/myexpansion/lostcities/worldstyles/office_world.json`, preserve the settings you want, and change or extend its `citystyles` list to select `myexpansion:office_city`. Set the profile's `worldStyle` to `myexpansion:office_world`; see [profile_options.md](profile_options.md). Alternatively, replace a built-in city style or world style under the `lostcities` namespace. That is simpler but makes the expansion more likely to conflict with other packs.

## Shared value formats

### Asset references

All Lost Cities asset references are strings. Unqualified names use the `lostcities` namespace. Blocks, biomes, dimensions, loot tables, and other vanilla/Forge registry values should use fully qualified resource locations.

### Block states

Block fields accept a block ID with optional state properties:

```json
"minecraft:quartz_slab[type=bottom]"
```

The property names and values must be valid for that block. Lost Cities does not add missing blocks or silently translate names from older Minecraft versions.

### Weighted choices

`factor` is a relative floating-point weight. If candidates have factors `1`, `2`, and `7`, their approximate shares are 10%, 20%, and 70% after contextual filtering. Keep factors positive.

Palette and variant `random` values are integer slot counts in a 128-entry table, evaluated in list order. Their sum must reach at least 128; entries after the first 128 filled slots have no effect. A common pattern is to give the last fallback a large value such as `1000`.

### Biome matchers

Biome matchers occur in world styles, scattered references, and stuff. Each member is a list of biome IDs or biome tags. Tags start with `#`.

```json
{
  "if_all": ["#minecraft:is_overworld"],
  "if_any": ["minecraft:plains", "#minecraft:is_forest"],
  "excluding": ["#minecraft:is_ocean"]
}
```

- Every set in `if_all` must contain the biome.
- At least one set in `if_any` must contain the biome.
- No set in `excluding` may contain the biome.
- Omitted members impose no restriction. An empty matcher matches every biome.

### Block and building matchers

Stuff assets use block matchers with `if_all`, `if_any`, and `excluding`. Values are block IDs or block tags such as `#minecraft:logs`. Building matchers use `if_any` and `excluding` with building asset IDs. In both cases, omitted members impose no restriction.

### Floor/context tests

Building part references and condition values share these optional tests. All specified tests must pass. `belowpart`, `inpart`, `inbuilding`, and `inbiome` accept either one string or a list of strings.

| Field | Type | Meaning |
|---|---|---|
| `top` | boolean | Whether this is the part above the highest normal floor |
| `ground` | boolean | Whether the current floor is floor 0 |
| `cellar` | boolean | Whether the floor number is below 0 |
| `isbuilding` | boolean | Whether the context belongs to a building |
| `issphere` | boolean | Whether the context is inside a city sphere |
| `floor` | integer | Exact floor number; ground is 0 and cellars are negative |
| `range` | string | Inclusive floor range formatted as `"min,max"` |
| `chunkx`, `chunkz` | integer | Exact absolute chunk coordinate |
| `inpart` | string or list | Current part name must be included |
| `belowpart` | string or list | Intended to match the part below; in the current 1.20.1 implementation it is evaluated against the current part, so avoid relying on it |
| `inbuilding` | string or list | Current building name must be included |
| `inbiome` | string or list | Current biome resource location must be included |

Do not confuse these context tests with biome matcher objects. Context `inbiome` is a literal string/list comparison and does not accept tags.

## Asset reference

Required fields are marked **required**. Other fields may be omitted unless a runtime note says otherwise.

### Variants

Path: `lostcities/variants`.

A variant is a named weighted list of blocks. It is reusable from palette entries.

```json
{
  "blocks": [
    { "random": 24, "block": "minecraft:cracked_stone_bricks" },
    { "random": 104, "block": "minecraft:stone_bricks" }
  ]
}
```

- `blocks` (**required**) is a list of `{random, block}` entries.
- `random` (**required**) is the number of slots assigned to the block.
- `block` (**required**) is a block-state string.

### Palettes

Path: `lostcities/palettes`.

A palette maps one-character symbols to block states. `palette` is a required list. Every entry requires `char`; only its first character is used. Each entry must provide exactly one practical source: `block`, `variant`, `blocks`, or `frompalette`.

```json
{
  "palette": [
    {
      "char": "#",
      "variant": "myexpansion:walls",
      "damaged": "minecraft:iron_bars"
    },
    {
      "char": "C",
      "block": "minecraft:chest[facing=north]",
      "loot": "chestloot"
    },
    {
      "char": "T",
      "block": "minecraft:wall_torch[facing=north]",
      "torch": true
    }
  ]
}
```

Palette-entry fields:

| Field | Type | Meaning |
|---|---|---|
| `char` | string, **required** | Character used in part slices; only the first character is used |
| `block` | string | One block state |
| `variant` | string | Variant asset ID |
| `blocks` | list | Inline weighted `{random, block}` entries, following the 128-slot rule |
| `frompalette` | string | Alias this character to another palette character; references are resolved after direct entries |
| `damaged` | string | Replacement block state used when explosion damage exposes/replaces this material |
| `mob` | string | Condition asset ID used to choose the mob for a spawner block |
| `loot` | string | Condition asset ID used to choose a loot-table ID for a container |
| `torch` | boolean | Marks a light/torch block so profile lighting settings can suppress it |
| `tag` | NBT object | Block-entity NBT. Lost Cities supplies position and block-entity `id` when placing it |

The active style palettes are merged in order; later definitions of the same character win. A building or part local palette is then layered over the chunk palette. `minecraft:air` means “leave the existing world block unchanged” during part placement. `minecraft:structure_void` is Lost Cities' hard-air marker: depending on the placement context it becomes air, water below sea level, or leaves the existing block unchanged. The built-in `common` and `default` palettes demonstrate the conventional space, water, and structure-void characters.

Block states are rotated when a part is rotated. This is relevant for stairs, wall torches, doors, rails, and other directional blocks.

### Styles

Path: `lostcities/styles`.

A style assembles the palette for a chunk. `randompalettes` is a required list of groups. Exactly one palette is selected from each group using `factor`, then the selected palettes are merged in group order.

```json
{
  "randompalettes": [
    [
      { "factor": 1.0, "palette": "common" }
    ],
    [
      { "factor": 3.0, "palette": "myexpansion:clean_walls" },
      { "factor": 1.0, "palette": "myexpansion:ruined_walls" }
    ]
  ]
}
```

Every choice requires `factor` and `palette`. Include every palette layer needed by the parts selected in that city style. Undefined characters cause a generation error when encountered.

### Parts

Path: `lostcities/parts`.

A part is a stack of horizontal slices. Each slice is a list of `zsize` strings, and every string must contain exactly `xsize` characters. The list index is Y, each row is Z, and each character in a row is X.

| Field | Type | Meaning |
|---|---|---|
| `xsize` | integer, **required** | X size, normally no more than one 16-block chunk |
| `zsize` | integer, **required** | Z size, normally no more than one 16-block chunk |
| `slices` | list, **required** | Y slices, each containing `zsize` strings of `xsize` characters |
| `refpalette` | string | Palette asset layered over the active chunk palette |
| `palette` | palette object | Inline local palette layered over the active chunk palette |
| `meta` | list | Typed metadata consumed by special generators |

Use either `refpalette` or `palette`, not both. Normal building floors are conventionally six blocks high so that doors, streets, rails, highways, and neighboring floors align, although the codec accepts other heights. Infrastructure parts often use different heights required by their generator.

Each metadata entry has a `key` and one typed value: `boolean`, `char`, `string`, `integer`, or `float`. Built-in metadata keys are:

| Key and value | Effect |
|---|---|
| `dontconnect` boolean | Prevents Lost Cities from cutting a connection/door through that building floor |
| `support` char | Palette character used for downward bridge or highway supports |
| `z1`, `z2` integer | Z bounds used when clearing/generating stair access |
| `nowater` boolean | Prevents hard-air cells in this part from becoming water below water level |
| `forcedair` boolean | Makes hard-air (`minecraft:structure_void`) cells overwrite existing blocks with air in every placement context |

Parts are used for much more than floors: parks, fountains, bridges, building fronts, street patterns, railways, rail dungeons, highway sections, monorails, stairs, sphere centers, and scattered structures are all parts or buildings assembled from parts.

### Buildings

Path: `lostcities/buildings`.

A building selects eligible parts independently for each floor. Eligible entries in `parts` have equal probability; unlike most selectors there is no `factor` field. `parts2`, when present, selects a second part at the same floor and places it after the first.

| Field | Type | Meaning/default |
|---|---|---|
| `filler` | string, **required** | Palette character used to close/fill generated areas; only the first character is used |
| `parts` | list, **required** | `{part, ...context tests}` entries |
| `parts2` | list | Optional second layer of part entries |
| `refpalette` | string | Palette asset local to the building |
| `palette` | palette object | Inline local palette |
| `rubble` | string | Palette character used for debris; filler is used if it is absent or undefined |
| `minfloors`, `maxfloors` | integer | Building-specific above-ground limits; absent means use city style/profile |
| `mincellars`, `maxcellars` | integer | Building-specific cellar limits; absent means use city style/profile |
| `allowDoors` | boolean | Whether Lost Cities may create adjacent doors; default `true` |
| `allowFillers` | boolean | Whether cellar fillers may be generated; default `true` |
| `overrideFloors` | boolean | Use only this building's floor/cellar limits instead of combining with style/profile; default `false` |
| `preferslonely` | float | Tendency for the building to avoid an adjacent building; `1.0` means always prefer being alone, default `0` |

The three camel-case field names are case-sensitive. Each part entry requires `part` and accepts all floor/context tests listed earlier. A valid general building should normally include both non-top entries and a `top: true` roof entry. Fully constrain special-purpose buildings if they should not participate in ordinary floor selection.

### Multibuildings

Path: `lostcities/multibuildings`.

A multibuilding arranges ordinary building assets over a rectangular chunk footprint:

```json
{
  "dimx": 2,
  "dimz": 2,
  "buildings": [
    ["myexpansion:north_west", "myexpansion:south_west"],
    ["myexpansion:north_east", "myexpansion:south_east"]
  ]
}
```

- `dimx` and `dimz` are required footprint dimensions.
- `buildings` is required and indexed as `buildings[x][z]`; its outer list must have `dimx` entries and every inner list must have `dimz` entries.
- Every cell is a building asset reference. Use an empty string only when a consumer explicitly tolerates an empty position; complete rectangular definitions are safest.

Multibuildings are selected by a city style or referenced by scattered/predefined content. The world style's `multisettings` controls placement attempts and density.

### Conditions

Path: `lostcities/conditions`.

A condition filters `values` by the current context and makes a weighted choice among the survivors:

```json
{
  "values": [
    { "factor": 5, "value": "minecraft:chests/simple_dungeon" },
    { "factor": 20, "value": "myexpansion:chests/office", "ground": true }
  ]
}
```

`values` is required. Every entry requires `factor` and `value` and accepts all floor/context tests. A palette's `mob` or `loot` field contains the **condition asset ID**, not the final mob or loot-table ID. The selected `value` is the final ID. If no entry matches, the condition returns no value.

Mob values should be valid entity resource locations for current content. Loot values should be loot-table resource locations; a low-code expansion can include its own tables under the normal `data/<namespace>/loot_tables/...` path.

### City styles

Path: `lostcities/citystyles`.

A city style controls the content and many local settings for a city. Every field is optional at the codec level because a style may inherit them, but a style actually used for generation must resolve to a usable palette style and selectors.

Top-level fields:

| Field | Meaning |
|---|---|
| `inherit` | Parent city-style asset. Most scalar settings fall back to the parent; selector lists and stuff tags are appended |
| `style` | Palette-style asset used for chunks in this city style |
| `explosionchance` | Additional explosion chance factor for the style |
| `stuff_tags` | Stuff categories enabled here. The tag `all` is always present |
| `profile_overrides` | Optional city-local profile overrides; absent values fall back to the selected profile |
| `generalblocks` | Palette characters for generator-wide materials |
| `buildingsettings` | Building count/chance overrides |
| `corridorblocks` | Corridor chance/materials |
| `parkblocks` | Park chance/behavior/materials |
| `railblocks` | Rail material |
| `sphereblocks` | City-sphere materials |
| `streetblocks` | Street chances/materials and street-part families |
| `selectors` | Weighted buildings, multibuildings, and detail parts |

Inheritance is additive for selector lists. If a child adds one building to a parent, both the parent's buildings and the child's building remain eligible. Replacing a selector list requires replacing the parent asset or defining a city style that does not inherit that list. Nullable `profile_overrides` values use ordinary scalar inheritance: an omitted child value inherits the resolved parent value, while an explicit value such as `0` is retained. Only after inheritance does an absent value fall back to the profile. In the current implementation the `generalblocks` characters and `parkblocks.grass` are not copied from a parent; repeat those fields in a child that needs them instead of relying on inheritance.

Nested setting fields:

- `generalblocks`: `ironbars`, `glowstone`, `leaves`, `rubbledirt` (palette characters).
- `buildingsettings`: `minfloors`, `mincellars`, `maxfloors`, `maxcellars` (integers), `buildingchance` (float).
- `corridorblocks`: `corridorchance` (float), `roof`, `glass` (palette characters).
- `parkblocks`: `parkchance` (float), `avoidfoliage`, `parkborder`, `parkelevation` (booleans), `parkstreetthreshold` (integer), `elevation`, `grass` (palette characters).
- `railblocks`: `railmain` (palette character).
- `sphereblocks`: `inner`, `border`, `glass` (palette characters).
- `streetblocks`: `fountainchance`, `frontchance` (floats), `width` (integer), `street`, `streetbase`, `streetvariant`, `border`, `wall` (palette characters), plus `parts`, `largeparts`, and `tertiaryparts`.

The first supported `profile_overrides` field is `openLotParkChance`, a float from `0` to `1`. It controls whether a non-road hierarchical open lot receives a weighted park part. It is deliberately independent from the legacy `parkblocks.parkchance` value:

```json
{
  "inherit": "lostcities:citystyle_common",
  "profile_overrides": {
    "openLotParkChance": 0.25
  }
}
```

With no `profile_overrides` object, or with `openLotParkChance` omitted, generation uses the profile's `openLotParkChance`. Existing city-style settings that already override profile behavior (`buildingsettings.buildingchance` and floor/cellar constraints, `parkblocks` behavior, `streetblocks` chances, and `corridorblocks.corridorchance`) are resolved through the same effective city-settings layer. Their existing JSON names and constraint semantics are unchanged. Field capitalization is significant.

Modern streets are part-driven. `streetblocks.parts`, `largeparts`, and `tertiaryparts` each accept `full`, `straight`, `end`, `bend`, `t`, `none`, `all`, `connector`, and `stair`. Every field accepts either one part ID or a list of part IDs; a list gives deterministic visual variants. Omitted fields inside a family use the built-in names (`street_full`, `street_straight`, and so on). `largeparts` is used by hierarchical primary roads. Optional `tertiaryparts` is used by hierarchical tertiary roads and defaults to the resolved `parts` family when omitted, so existing city styles continue to use the same assets for secondary and tertiary roads.

`stair` is the full-chunk sloped road part used only by hierarchical street generation when two eligible road levels differ by one building floor. Its unrotated form rises toward X-min. Set the part's `z1` and `z2` metadata to the inclusive road-width bounds at its high edge; the renderer uses those bounds to open the retaining wall in the neighboring upper chunk. All alternatives in a `stair` list should use the same bounds. The built-in `street_stair` rises six blocks across the chunk with alternating full-block and slab steps. The default `largeparts` family uses `street_large_stair`, which keeps the fourteen-block primary-road surface and its centered quartz marking.

`selectors` accepts these optional lists:

| Selector | Asset selected |
|---|---|
| `buildings` | Building |
| `multibuildings` | Multibuilding |
| `bridges` | Part used for ordinary bridges |
| `largebridges` | Part used for hierarchical large bridges |
| `parks` | Part |
| `fountains` | Part |
| `stairs` | Part |
| `fronts` | Part placed in front of a building |
| `raildungeons` | Part |

Each entry has required `factor` and `value`. It also accepts `minSpawnDistance` (default `0`), `maxSpawnDistance` (default unlimited), and `feather` (default `0`), all measured in blocks from world spawn. Outside the min/max band the weight becomes zero; `feather` makes the weight ramp near both boundaries. Field capitalization is significant.

### World styles

Path: `lostcities/worldstyles`.

The profile's `worldStyle` selects this asset. It is the root of most asset reachability.

| Field | Type | Meaning |
|---|---|---|
| `outsidestyle` | string, **required** | City style used outside/at the context of city generation |
| `citystyles` | list, **required** | Weighted city-style selectors |
| `multisettings` | object | Multibuilding planning settings |
| `settings` | object | Miscellaneous world-wide settings |
| `cityspheres` | object | Optional center part for sphere profiles |
| `scattered` | object | Scattered-placement configuration |
| `parts` | object | Monorail, highway, and railway part families |
| `citybiomemultipliers` | list | Biome-dependent multipliers applied to city chance |

Every `citystyles` entry requires `factor` and `citystyle` and optionally has a `biomes` matcher. The generator filters by biome and makes a weighted choice. A city-biome multiplier entry requires `multiplier` and a `biomes` matcher. Multiplier entries are tested in list order and the first match wins; the multiplier is `1.0` if none matches. They are alternatives, not cumulative multipliers.

`multisettings` fields are `areasize`, `minimum`, and `maximum` (required integers), `correctstylefactor` (default `0.8`), and `attempts` (default `50`). The world is divided into `areasize`-chunk planning regions. The planner attempts to place between the minimum and maximum number of multibuildings; the correction factor controls how strongly placement requires the city style selected by every footprint chunk. If `multisettings` is omitted entirely, its defaults are `10`, `1`, `5`, `0.8`, and `50` respectively.

`settings` fields:

- `railwayavoidance` (**required when `settings` is present**): `ignore` or `block_railway`.
- `railpartheight6`: vertical rail-part offset in six-block units, default `1`.
- `vinewest`, `vineeast`, `vinesouth`, `vinenorth`: block-state values used for directionally attached vines. These use Minecraft's structured block-state codec, for example `{ "Name": "minecraft:vine", "Properties": { "west": "true" } }`, rather than the palette block-state string format.

If `settings` is omitted, Lost Cities uses `ignore`, a rail-part height of `1`, and ordinary vines attached in the corresponding direction.

`cityspheres` fields:

- `centerpart`: part asset placed at a sphere center.
- `centertype`: `default`, `street`, `building`, or `normal`.
- `centerpartorigin`: `fixed`, `center`, `firstfloor`, `ground`, or `top`; default `top`.
- `centerpartoffset`: vertical offset in blocks, default `0`.

`parts.monorails` accepts `both`, `vertical`, and `station`, each a single part ID. `parts.highways` accepts `tunnel`, `open`, and `bridge` for straight sections; `tunnel_bi`, `open_bi`, and `bridge_bi` for four-way crossings; `tunnel_bend`, `open_bend`, and `bridge_bend` for bends; and `tunnel_t`, `open_t`, and `bridge_t` for T-junctions. Each highway field accepts either one ID or a list. The unrotated bend connects west and south; the unrotated T-junction connects west, east, and south. The generator rotates these parts to match the planned connections. `parts.railways` accepts `stationunderground`, `stationopen`, `stationopenroof`, `stationundergroundstairs`, `stationstaircase`, `stationstaircasesurface`, `railshorizontal`, `railshorizontalend`, `railshorizontalwater`, `railsvertical`, `railsverticalwater`, `rails3split`, `railsbend`, `railsflat`, `railsdown1`, and `railsdown2`, again as one ID or a list. Omitted values use the corresponding built-in part names.

`scattered` is described with the scattered asset below because both layers are needed.

### Scattered assets

Path: `lostcities/scattered`.

A scattered asset says what structure to place and how it interacts with terrain:

```json
{
  "buildings": ["myexpansion:cabin"],
  "rotatable": true,
  "clearhighwayrailing": false,
  "supportpart": "myexpansion:foundation_columns",
  "terrainheight": "highest",
  "terrainfix": "repeatslice",
  "heightoffset": -1
}
```

| Field | Type | Meaning |
|---|---|---|
| `buildings` | list | One or more building IDs from which one is selected |
| `multibuilding` | string | A multibuilding ID instead of `buildings` |
| `rotatable` | boolean | Whether a `nearhighway` placement may rotate in 90-degree steps so its connection edge faces the highway; default `false` |
| `clearhighwayrailing` | boolean | Removes iron-bar railing from the connected highway edge for access; default `false` |
| `supportpart` | string | Optional part whose bottom slice is repeated downward by `repeatslice` instead of repeating the generated building part |
| `terrainheight` | enum, **required** | `lowest`, `average`, `highest`, or `ocean` |
| `terrainfix` | enum, **required** | `none`, `clear`, or `repeatslice` |
| `heightoffset` | integer | Final vertical offset in blocks, default `0` |

Supply `buildings` or `multibuilding`, not both. `clear` clears above the placement level. `repeatslice` extends non-space blocks in a bottom slice downward until each column reaches solid terrain. It uses `supportpart` when supplied and otherwise preserves the older behavior of using the generated part. A support part is useful for defining sparse pillars independently of the building or parking-lot floor.

For a multibuilding, `lowest`, `average`, and `highest` use the corresponding measurement across its complete footprint. For a single-chunk scattered building, the current implementation uses the chunk heightmap's representative height for all three values; `ocean` uses sea level in both cases.

The asset does not spawn until the active world style references it in `scattered.list`. World-style scattered settings are:

```json
{
  "scattered": {
    "areasize": 8,
    "chance": 0.7,
    "weightnone": 30,
    "list": [
      {
        "name": "myexpansion:cabin_scattered",
        "weight": 10,
        "nearhighway": false,
        "allowvoid": false,
        "maxheightdiff": 3,
        "biomes": { "excluding": ["#minecraft:is_ocean"] }
      }
    ]
  }
}
```

`areasize`, `chance`, `weightnone`, and `list` are required. `areasize` is measured in chunks on both horizontal axes. `chance`, multiplied by the profile's scattered-chance multiplier, is the initial chance that a planning region attempts a placement. Each list entry requires `name` and integer `weight`, and optionally accepts `nearhighway`, `allowvoid`, a `biomes` matcher, and `maxheightdiff`. `weightnone` is added as a no-structure outcome after that chance check. `maxheightdiff` limits the terrain-height difference in blocks across the complete footprint. `allowvoid` permits placement where the sampled terrain is effectively void.

`nearhighway: true` is a connection constraint rather than a loose distance check. The planner searches the area for a complete footprint whose connection edge directly borders a surface or bridge highway running parallel to that edge. Every highway chunk along that edge must have the same deck height; tunnels are rejected. The scattered structure is generated at that deck height plus `heightoffset`. The unrotated connection edge is north. With `rotatable: true`, the complete footprint, its multibuilding layout, its individual parts, and its optional support part rotate together to face a qualifying highway on any side. Set `clearhighwayrailing: true` when that connection must also remove iron-bar railing along the bordering highway chunks.

### Stuff assets

Path: `lostcities/stuff`.

Stuff places small vertical strings of palette characters into eligible city chunks. It is suitable for chains, poles, weeds, rubble details, and similar decoration.

```json
{
  "tags": ["mydecor"],
  "column": "pp",
  "mincount": 2,
  "maxcount": 6,
  "attempts": 8,
  "inbuilding": false,
  "seesky": true,
  "blocks": { "if_any": ["minecraft:stone_bricks"] },
  "upperblocks": { "if_any": ["minecraft:air"] }
}
```

| Field | Type | Meaning |
|---|---|---|
| `tags` | list | Categories that activate this object; default empty |
| `column` | string, **required** | Bottom-to-top palette characters to place |
| `minheight`, `maxheight` | integer | Absolute Y sampling bounds; contextual defaults are used when absent |
| `mincount`, `maxcount` | integer, **required** | Number of successful placements to attempt; runtime selection uses a value from `mincount` up to but excluding `maxcount` |
| `attempts` | integer, **required** | Position attempts per desired placement |
| `inbuilding` | boolean | `true` for building chunks or `false` for street chunks; **must be present in the current implementation or the object is skipped** |
| `seesky` | boolean | Optional exact sky-visibility requirement |
| `biomes` | biome matcher | Eligible biomes |
| `blocks` | block matcher | Block immediately below the candidate column |
| `upperblocks` | block matcher | Block immediately above the complete candidate column |
| `buildings` | building matcher | Eligible/excluded building asset IDs |

The active city style must include a matching string in `stuff_tags`; every city style automatically includes `all`. The column characters must exist in the compiled palette. Use `maxcount` greater than `mincount`.

### Predefined cities

Path: `lostcities/predefinedcities`.

A predefined city anchors a city at an absolute chunk position:

```json
{
  "dimension": "minecraft:overworld",
  "chunkx": 100,
  "chunkz": 100,
  "radius": 200,
  "citystyle": "myexpansion:office_city",
  "buildings": [
    {
      "building": "myexpansion:headquarters",
      "chunkx": 0,
      "chunkz": 0,
      "multi": true,
      "preventruins": true
    }
  ],
  "streets": [
    { "chunkx": 1, "chunkz": 0 }
  ]
}
```

`dimension`, `chunkx`, `chunkz`, `radius`, and `citystyle` are required. `dimension` is a dimension resource location. City position is in chunks and radius is in blocks. Entries in `buildings` and `streets` use chunk offsets relative to the city center.

Each building entry requires `building`, `chunkx`, and `chunkz`. `multi` defaults to `false`; when true, `building` names a multibuilding rather than a building. `preventruins` defaults to `false` and protects that predefined structure from normal ruin processing. Street entries require `chunkx` and `chunkz`.

Predefined assets only affect a dimension using Lost Cities. Profile options such as `onlyPredefined`, `spawnCity`, and normal city chance determine how fixed and random cities coexist.

### Predefined spheres

Path: `lostcities/predefinedspheres`.

```json
{
  "dimension": "minecraft:overworld",
  "chunkx": 100,
  "chunkz": 100,
  "centerx": 1608,
  "centerz": 1608,
  "radius": 200
}
```

All six fields are required. `chunkx` and `chunkz` identify the sphere's controlling chunk. `centerx` and `centerz` are exact block coordinates, allowing a center that is not at the chunk corner. `radius` is in blocks. These assets are relevant only to profiles/landscape modes that support city spheres; profile settings control whether random spheres also generate.

## Packaging as a Forge low-code mod

For Minecraft 1.20.1/Forge 47, create this directory tree:

```text
MyExpansion/
|-- META-INF/
|   `-- mods.toml
|-- data/
|   |-- myexpansion/
|   |   |-- lostcities/
|   |   |   |-- buildings/
|   |   |   |-- citystyles/
|   |   |   |-- palettes/
|   |   |   |-- parts/
|   |   |   `-- worldstyles/
|   |   `-- loot_tables/          # optional normal datapack content
|   `-- lostcities/
|       `-- lostcities/           # only exact-ID overrides
|           `-- citystyles/
`-- pack.mcmeta
```

`META-INF/mods.toml`:

```toml
modLoader="lowcodefml"
loaderVersion="[47,)"
license="All Rights Reserved"

[[mods]]
modId="myexpansion"
version="1.0.0"
displayName="My Lost Cities Expansion"
description='''
Custom buildings and city assets for Lost Cities.
'''

[[dependencies.myexpansion]]
modId="forge"
mandatory=true
versionRange="[47,)"
ordering="NONE"
side="BOTH"

[[dependencies.myexpansion]]
modId="minecraft"
mandatory=true
versionRange="[1.20.1,1.21)"
ordering="NONE"
side="BOTH"

[[dependencies.myexpansion]]
modId="lostcities"
mandatory=true
versionRange="[1.20-7.5.0,)"
ordering="AFTER"
side="BOTH"
```

Change the license, IDs, version, display name, description, and minimum Lost Cities version to match your release. The mod ID must be lowercase and must match the namespace under `data` and the dependency table suffix.

`pack.mcmeta`:

```json
{
  "pack": {
    "pack_format": 15,
    "description": "My Lost Cities Expansion data"
  }
}
```

Zip the **contents** of `MyExpansion`, not the containing directory. From inside that directory, for example:

```shell
zip -r ../myexpansion-1.0.0.jar META-INF data pack.mcmeta
```

Opening the jar should show `META-INF`, `data`, and `pack.mcmeta` at its root. Put the jar in the modpack's `mods` directory on both client and server. Although the assets are server data, distributing the same mod list avoids dependency and pack-management surprises. The loader range and pack format above are specifically for Minecraft 1.20.1/Forge 47 and must be updated when porting the expansion.

A low-code mod cannot register a profile in Java. Lost Cities profiles are JSON files in `config/lostcities/profiles`; distribute a matching profile through the modpack's config-file distribution mechanism, tell users which existing profile to edit, or override the world style already named by the target profile. Prefer a new namespaced world style plus an explicitly distributed profile when compatibility with other asset packs matters.

## Development workflow and troubleshooting

### In-game part tools

Lost Cities includes operator commands that can shorten the part-design loop:

- `/lostcities createpart <part-id> <x y z>` materializes an already loaded part at a position and starts an editing session.
- `/lostcities createbuilding <building-id> <floors> <cellars> <x y z>` previews an already loaded building. It clears the target chunk from the supplied Y coordinate upward, so use it only in a disposable test world.
- `/lostcities debug` writes the current chunk's generation details to the server console, including both its individual building type and containing multibuilding name when applicable.
- `/lostcities editpart`, `resumeedit`, `listparts`, and `locatepart` help work with parts generated in a profile whose `editMode` is enabled.
- `/lostcities exportpart <filename>` writes the current edit to the server working directory. Its output is an aid, not a directly loadable asset: copy the `exportedpart` object into the part's JSON file and merge any entries under `missingpalette` into a palette.

The alias `/lost` can be used instead of `/lostcities`. These commands operate on assets already loaded into the current world, so add a placeholder asset and restart the test world before beginning a brand-new part.

### Recommended loop

1. Start by copying the smallest current built-in asset that demonstrates the feature. The files under `data/lostcities/lostcities` are executable examples of the current schema.
2. Move the copy to your namespace and qualify every reference to your own assets.
3. Build from the bottom upward: variant/palette, part, building, city style, world style.
4. Test in a new world with a profile that makes cities common and disables unrelated damage/ruin effects where useful.
5. Watch `logs/latest.log`. A codec error normally identifies the registry and field that prevented the world datapacks from loading.
6. Use `/locate`-style exploration, a fixed seed, or Lost Cities' editor/commands to compare revisions. Test newly generated chunks only.

Common failures:

- **Asset never appears:** it exists but is not reachable from the active world style/city style, has a zero/very small effective weight, fails a biome/distance test, or the profile names another world style.
- **Missing asset error:** a custom reference was left unqualified and resolved to `lostcities:<name>`.
- **Undefined palette character:** the active style, building palette, and part palette together do not define every character used by the part.
- **Datapack validation fails:** a required field is absent, an enum/case-sensitive field is misspelled, a block state is invalid, or an old array/`type`/`name` format was used.
- **Only some weighted blocks appear:** palette weights fill 128 slots in order; later entries may be truncated.
- **Override has lost inherited content:** an override replaces the complete asset value. Copy required fields and list entries into the replacement.
- **Old chunks do not change:** assets run during world generation; they do not retrofit generated terrain.
- **Seams after an update:** changing asset dimensions, floor height, palettes, or selection rules in an existing world can make new chunks incompatible with old ones.

For a human visual check, create a fresh world with the intended profile and seed, confirm the custom world style is active, and inspect ordinary buildings, roofs and cellars, multibuilding boundaries, street connections, rotated/infrastructure parts, palette randomization, loot/spawners, scattered terrain fitting, and transitions between city and non-city chunks as applicable to the expansion.
