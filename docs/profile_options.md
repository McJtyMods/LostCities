# Profile options

## Cavern spawn

`cavern` and `cavernspheres` landscapes place new players and players who respawn at world spawn at the saved underground position. A safe position has solid ground other than bedrock or magma and two air blocks for the player. Saved player positions and valid bed and respawn anchor positions still apply.

`spawnCheckAttempts` defaults to 20,000 chunks and accepts values from 1 to 1,000,000. The initial search honors profile rules such as `spawnSphere` and forced building spawns. If that search runs out of attempts, cavern landscapes search nearby columns for safe ground without those restrictions. A world spawn that becomes unsafe also uses this nearby search.

If no natural location works, Lost Cities builds a small cobblestone shelter underground. The shelter search skips bedrock and blocks with stored data. The chosen position becomes world spawn for later players. If the surrounding terrain or world border prevents placing a shelter, Lost Cities reports a spawn error.

## Minimum city height

`cities.cityMinHeight` sets the lowest terrain height that can qualify for city generation. It accepts values from -1024 to 2048 and defaults to 50. The built-in `cavern` profile uses 40, which matches its fallback ground level when the height check encounters the cave roof.
