# Development setup

Use Java 25 and the Gradle wrapper for Minecraft 26.1.2 with NeoForge. Run `./gradlew :LostCities:build` from Multi_neo, or `./gradlew build` from the Lost Cities checkout with the 26.1.2 NeoForge Gradle user home.

The build looks for `gradletools.gradle` in the workspace root, the sibling `Multi_neo` checkout, the parent directory, and the mod checkout. It uses the MultiWorkspace `26.1.2_neo` branch if those scripts are unavailable.
