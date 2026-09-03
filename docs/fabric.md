# Fabric integration

The Fabric port keeps world-generation algorithms, asset identifiers, JSON
formats, profiles, saved-data identifiers, and commands shared with Lost Cities
11.0. Loader-specific behavior is isolated in the initializer and setup
classes.

The loader integration was reconciled against the earlier
[Arilas Fabric 26.2 port](https://github.com/Arilas/LostCities/tree/fabric/26.2),
while this repository's current sources and assets remain authoritative.

## Dependencies and entrypoints

`fabric.mod.json` declares common, client, and data-generation entrypoints. The
runtime requires Fabric Loader, Fabric API, and Forge Config API Port. Config
files remain under `config/lostcities` and retain their TOML schema.

The two configured features are registered directly in Minecraft's feature
registry. Fabric biome modifications inject `lostcities:lostcities` during raw
generation and `lostcities:spheres` during top-layer modification in overworld
biomes. Custom Lost Cities asset registries use Fabric dynamic registries and
remain server-side data-pack registries.

## Lifecycle and UI hooks

Fabric lifecycle callbacks handle commands, player joins, level ticks, server
cleanup, sleep checks, client disconnect cleanup, and networking. A small
`MinecraftServer` mixin replaces NeoForge's new-world spawn-position event; it
is also the signal used to initialize versioned street and highway generation
for genuinely new worlds. Existing worlds without that saved marker continue
to use legacy generation modes.

The world-creation `Cities` button is added with Fabric's screen API and is
visible on the More tab. A client-only render-state mixin retains the decorative
configuration icon beside it.

## Public generation events

Fabric consumers register callbacks through `mcjty.lostcities.api.LostCityEvents`:

- `CHARACTERISTICS`
- `PRE_GEN_CITY_CHUNK`
- `POST_GEN_CITY_CHUNK`
- `POST_GEN_OUTSIDE_CHUNK`
- `PRE_EXPLOSION`

Listeners cancel `PRE_GEN_CITY_CHUNK` or `PRE_EXPLOSION` by calling
`setCanceled(true)` on the event. Fabric has no NeoForge IMC equivalent;
integrations can access `LostCities.lostCitiesImp` directly after mod
initialization.
