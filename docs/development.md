# Development setup

Lost Cities targets Minecraft 26.2 with NeoForge and Java 25. The Gradle
wrapper uses Gradle 9.2.1 and resolves a matching Java toolchain through the
Foojay resolver, so a local Java 25 installation is optional when toolchain
downloads are available.

IntelliJ run configurations must also use Java 25; the Gradle compiler
toolchain does not override the runtime selected by the IDE. Set both the
Project SDK and the Gradle JVM to a Java 25 SDK before regenerating or launching
the client configuration. An older runtime rejects NeoForge's
`--sun-misc-unsafe-memory-access=allow` JVM option.

Use the wrapper for build tasks:

```bash
./gradlew compileJava
./gradlew build
```

NeoGradle's centralized execution cache is disabled for this project. With the
26.x toolchain, a normal `build` can otherwise consume and retain an incomplete
NeoForm recompile output, causing large groups of unrelated Minecraft classes
such as `BlockPos`, `Identifier`, `Level`, and `ProtoChunk` to disappear from
the compilation classpath. Gradle's normal project-local up-to-date checks
remain enabled, but NeoForm intermediates are not shared between checkouts.

The build continues to use the shared `gradletools.gradle` layout. Minecraft,
NeoForge, dependency, run, access-transformer, jar, and publishing settings are
defined there so sibling mods can reuse the same helper structure. This project
uses NeoGradle 7.1.38 and no longer configures Parchment because Minecraft 26.x
ships unobfuscated names and official parameter names.

Lost Cities' world-level caches use Minecraft's global `SavedDataStorage` with
namespaced `SavedDataType` identifiers. New saved data is written below the
world data directory in the `lostcities` namespace.
