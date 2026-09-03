# Development setup

Lost Cities targets Minecraft 26.2 with Fabric Loader and Java 25. The Gradle
wrapper uses Gradle 9.5.1. Its checked-in
`gradle/gradle-daemon-jvm.properties` requires Java 25 for the Gradle daemon,
even when the wrapper is launched from a Java 17 shell. Gradle detects a local
Java 25 installation or provisions one through the Foojay resolver, then uses
the same Java version for the compiler toolchain.

IntelliJ game run configurations must also use Java 25. Set the Project SDK to
Java 25 before launching a generated run configuration. The Gradle JVM itself
is selected by the daemon criteria file when the IDE delegates to Gradle.

When changing the project's `java_version`, regenerate and commit the daemon
criteria file with:

```bash
./gradlew updateDaemonJvm
```

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

## Publishing

The Mod Publisher integration retains the release workflow used by the
NeoForge project:

```bash
./gradlew publishMod
```

It publishes the Fabric `jar` artifact to the configured CurseForge and
Modrinth projects, labels it for the Fabric loader, and declares Fabric API and
Forge Config API Port as required dependencies. Supply `CURSEFORGE_TOKEN` (or
`CURSE_TOKEN`) and/or `MODRINTH_TOKEN` for the destinations to enable. Do not
invoke the task merely to verify configuration; use `./gradlew help --task
publishMod` instead.

Lost Cities' world-level caches use Minecraft's global `SavedDataStorage` with
namespaced `SavedDataType` identifiers. New saved data is written below the
world data directory in the `lostcities` namespace.
