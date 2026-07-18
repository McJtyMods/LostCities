# AGENTS.md

## Project overview

Lost Cities is a Minecraft Forge 1.20.1 mod built with Java 17 and Gradle. Most behavior is split between Java world-generation code and data-driven JSON assets that describe buildings, city styles, palettes, parts, and related content.

## Repository structure

- `src/main/java/mcjty/lostcities/` contains the mod implementation.
  - `worldgen/` contains terrain and city generation, including the `gen/`, `highway/`, `lost/`, and `street/` subsystems.
  - `config/` contains profiles and generation settings.
  - `api/` contains the public Lost Cities API that is also packaged into the API jar.
  - `commands/`, `editor/`, and `gui/` contain player-facing tools and configuration screens.
  - `setup/` and `network/` contain Forge registration, event wiring, and packets.
  - `datagen/` generates selected resources and tags.
- `src/main/resources/` contains Forge metadata, language files, textures, dimension/worldgen data, and the built-in Lost Cities assets under `data/lostcities/lostcities/`.
- `src/generated/resources/` contains generated data resources and is included in the main resource source set.
- `src/api/java/` contains compatibility API sources used by the project.
- `docs/` contains developer and configuration documentation. In particular, `docs/profile_options.md` documents the options exposed by `LostCityProfile`.
- `changelog.txt` records release-facing changes, with the newest version first.
- `build/` and `run/` are generated/local working directories and should not be treated as source.

## Development guidelines

- Keep changes focused and consistent with the existing package and data-asset organization.
- Preserve deterministic world generation. Be especially careful with random-number usage, chunk ordering, caches, shared mutable state, and compatibility of existing worlds.
- Treat identifiers and JSON formats in the built-in assets as public, data-pack-facing interfaces unless a migration is intentional.
- Use Java 17-compatible code. `./gradlew compileJava` is the basic compilation check when one is useful.
- Do not edit generated or build output when the corresponding source or generator should be changed instead.

## Testing

Automated tests do not need to be generated or maintained for changes to this project. Testing is always performed visually by a human in Minecraft. Do not add tests merely to accompany an implementation change. Compilation and static checks may still be used to catch basic errors, but they do not replace the human visual verification.

When handing off a change, briefly describe the in-game scenario a human should inspect, including the relevant profile, world-generation mode, asset, command, or GUI flow.

## Documentation and changelog

Documentation under `docs/` must be kept up to date at all times. Any change that affects documented behavior, architecture, configuration, asset formats, or developer workflows must update the relevant documentation in the same change.

Profile-option changes must also update `docs/profile_options.md`, including defaults, valid ranges or values, and behavior. Add a new document when no existing page is an appropriate home, and link important new developer documentation from `README.md`.

Every new feature must add an entry to the current top section of `changelog.txt`. Bug fixes and other user-visible behavior changes should also be recorded there. Keep entries concise and written from the user's perspective.
