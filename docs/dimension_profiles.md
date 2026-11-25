# Which dimensions get cities

Lost Cities only generates in a dimension that has a city generation profile. The profile for a dimension comes from:

- `dimensionsWithProfiles` in `config/lostcities/common.toml`: a list of `<dimensionid>=<profilename>` entries.
- `selectedProfile` in the server config `lostcities-server.toml` (per world in `serverconfig/`), for the overworld. The default `<CHECK>` means: use the profile picked with the `Cities` button when the world was created, and save it there. An empty value means no profile for the overworld.
- The Nether gets the `cavern` profile when the overworld's profile has `GENERATE_NETHER` set.
- Other mods can register a profile for a dimension through the API (`ILostCities.registerDimension` and `setOverworldProfile`).

## Keeping cities out of a dimension

`dimensionsWithoutCities` in `config/lostcities/common.toml` lists dimensions that never get a profile, whatever the sources above say. Default: empty.

For example, to keep cities in their own `lostcities:lostcity` dimension only:

```toml
dimensionsWithProfiles = ["lostcities:lostcity=default"]
dimensionsWithoutCities = ["minecraft:overworld"]
```

When the overworld is listed, the `Cities` button on the world creation screen is hidden, and the Nether doesn't get the `cavern` profile through the overworld either. Lost Cities logs a line when it skips a profile because of this option.
