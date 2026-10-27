# Development setup

This branch targets Minecraft 26.2 with NeoForge and Java 25. Set `JAVA_HOME` to a Java 25 installation and use the Gradle wrapper.

From the multiworkspace root, run `./gradlew :LostCities:build`. From the Lost Cities checkout, run `./gradlew build`.

The build uses the workspace root's `gradletools.gradle` when included in a multiworkspace. Standalone checkouts also support the sibling `../Multi_neo/gradletools.gradle`, the parent script in a nested checkout, and a local script. If no local script is available, the fallback is the `26.2_neo` branch of MultiWorkspace. Keep the fallback on the matching Minecraft branch: the `1.21_neo` script supplies an incompatible classpath and causes missing classes such as `net.minecraft.resources.Identifier`.

After changing build configuration, reload the Gradle project in IntelliJ to refresh dependencies and generated run configurations. Use the workspace client run to check Lost Cities world creation with the default profile and confirm the configuration screen opens. Compilation does not replace visual testing in Minecraft.
