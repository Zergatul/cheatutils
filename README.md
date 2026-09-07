# CheatUtils for Minecraft 1.12.2

Forge-only Java 8 project. Includes initialization, configuration profiles, and
the HTTP backend. The Vue UI, FreeCam, keybindings, and scripting follow in later
batches.

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
- IDEA and normal launcher startup were manually verified.

## Configuration and HTTP backend

The server listens on `127.0.0.1`, starting at port `5005` and trying up to 99 higher
ports if occupied. The log reports the actual address. The current index page is
a placeholder for batch 3.

Configuration lives in the game's `config/` directory:

- `cheatutils.json`: default profile (name `""`).
- `cheatutils.<name>.json`: named profiles.
- `cheatutils-profile.json`: selected profile, restored at startup if it exists.

A fresh installation uses the default profile. Saves are delayed by 15 seconds,
capture a snapshot, and flush on normal shutdown. Invalid configuration is backed
up before defaults are used. Profile creation/copying, switching, deletion, and
reset are implemented in the backend; their UI and live verification are deferred.
Reset deletes profile files and disables saving until Minecraft restarts.

Existing 26.2 route conventions are retained:

- `GET/POST /api/core`: core settings (`port`, `advancedScripting`).
- `GET /api/profiles/current` and `/api/profiles/list`.
- `POST /api/profiles` with `{"command":"new|copy|change","name":"..."}`
  (choose one command).
- `DELETE /api/profiles/<name>`; the default profile cannot be deleted.
- `POST /api/reset-config`.

API operations run on the client thread. Port changes restart the listener after
a short delay. `/local/` serves files under the game's `mods/` directory for the
future CDN fallback loader. Packaged files are served from `web/`; set the Java
system property `cheatutils.web.dir` to override that directory during UI work.

`gradlew.bat build` includes Java 8 infrastructure checks for save coalescing,
cancellation, shutdown flushing, independent file snapshots, invalid config
recovery, static/local HTTP responses, and traversal rejection. These do not
launch Minecraft. Batch 2 still needs live startup/shutdown and API verification.
