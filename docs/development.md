# Development setup

Use Java 25 and the checked-in Gradle wrapper. This branch targets Minecraft 26.3;
Minecraft and NeoForge versions come from the shared `gradletools.gradle`.

The build searches for that script in this order:

1. The Gradle root project's `gradletools.gradle`, including an integrated Multi_neo build.
2. The sibling `../Multi_neo/gradletools.gradle` for standalone sibling checkouts.
3. The parent `../gradletools.gradle` for checkouts nested in a workspace.
4. The published `MultiWorkspace/26.3_neo/gradletools.gradle` when no local shared script exists.

A separate copy of `gradletools.gradle` inside Lost Cities is unnecessary.

For standalone compilation, run this from the Lost Cities checkout:

```sh
./gradlew compileJava
```

For integrated development, open `Multi_neo/build.gradle` in IntelliJ and run this
from Multi_neo:

```sh
./gradlew :LostCities:compileJava
./gradlew ideSync
./gradlew :workspace:runClient
```

After changing the shared-script lookup, use **Reload All Gradle Projects** in
IntelliJ before rebuilding. An import of the old Minecraft 1.21 script can leave
the Lost Cities module using NeoForge 21.1 dependencies, which lack 26.3 classes
such as `net.minecraft.resources.Identifier`.

Check gameplay visually in Minecraft after changes to generation or rendering;
successful compilation does not replace that verification.
