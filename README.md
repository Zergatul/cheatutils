# CheatUtils for Minecraft 1.12.2

Forge-only Java 8 project. The current batch contains mod initialization and a
required Minecraft initialization mixin. Modules, HTTP server, web UI, and scripting
will be added in later batches.

## Build

Use a Java 8 JDK for Gradle and the game. In PowerShell, for example:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk1.8.0_491'
.\gradlew.bat build
```

The reobfuscated jar is `build/libs/cheatutils-0.1.0-forge-1.12.2.jar`.
It includes Mixin; no separate Mixin mod is needed for a plain Forge installation.
Forge is pinned to `14.23.5.2860`, with mappings `snapshot_20180814-1.12`.

## Development

Import the root Gradle project into IntelliJ IDEA, use JDK 8 for the project and
Gradle JVM, and run:

```powershell
.\gradlew.bat genIntellijRuns
```

Run the generated `runClient` application configuration with JDK 8. Its working
directory is `run/`. Alternatively, launch with `.\gradlew.bat runClient`.
Generated IDE files and game data are ignored by Git.

Successful initialization logs both:

```text
CheatUtils 0.1.0 initializing on Forge 1.12.2.
CheatUtils client initialized; Minecraft mixin is active.
```

The development classpath includes upstream Mixin/ASM jars containing newer Java
classes that Forge's old scanner logs errors about. The development client can
still initialize. The packaged mod excludes the incompatible Mixin classes;
those development dependencies are not bundled wholesale into the release jar.

## Batch 1 verification

- Build, reobfuscation, and IDEA configuration generation passed.
- Gradle's development client initialized on Java 8 and logged the mixin marker.
- The jar's manifest and refmap were checked; all packaged classes target Java 8
  or earlier.
- Manual checkpoint: launch the generated configuration from IDEA, then test the
  jar in a separate Forge 1.12.2 installation and check for both log messages.
