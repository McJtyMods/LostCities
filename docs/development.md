# Development setup

Lost Cities targets Minecraft 26.2 with Fabric Loader and Java 25. The Gradle
wrapper uses Gradle 9.5.1 and resolves a matching compiler toolchain through the
Foojay resolver when toolchain downloads are available.

IntelliJ run configurations must also use Java 25; the Gradle compiler
toolchain does not override the runtime selected by the IDE. Set both the
Project SDK and Gradle JVM to Java 25 before regenerating or launching a run
configuration.

Use the wrapper for normal checks:

```bash
./gradlew compileJava
./gradlew build
```

Fabric Loom defines `client`, `server`, and `datagen` run configurations. Run
data generation with:

```bash
./gradlew runDatagen
```

Generated tags are written to `src/generated/resources`, which is included in
the main resource source set. Do not edit build output or Loom's generated run
directories as source.

The build uses official Minecraft names, Fabric Loader, Fabric API, and Forge
Config API Port. The latter keeps the existing `ModConfigSpec` definitions and
the `config/lostcities/*.toml` file format compatible. NeoForge access
transformers are represented by `META-INF/lostcities.accesswidener`.

Lost Cities' world-level caches use Minecraft's global `SavedDataStorage` with
namespaced `SavedDataType` identifiers. New saved data is written below the
world data directory in the `lostcities` namespace.
