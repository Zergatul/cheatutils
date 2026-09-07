# CheatUtils for Minecraft 1.12.2

Forge-only Java 8 project. Includes initialization, configuration profiles, and
the HTTP backend, the Vue web UI, FreeCam, and saved keybinding scripts with a
Monaco editor.

## Build

Initialize the scripting submodule and install JDK 8. Gradle, all source
compilation, and the game use Java 8. In PowerShell, for example:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk1.8.0_491'
git submodule update --init --recursive
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
Reimport the Gradle project after initializing the submodule.

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
ports if occupied. The log reports the actual address. The home page keeps all six
module categories visible, including empty groups. ESP contains Free Cam; Utility
contains Core Config and Profiles. Other modules will be registered as implemented.

Configuration lives in the game's `config/` directory:

- `cheatutils.json`: default profile (name `""`).
- `cheatutils.<name>.json`: named profiles.
- `cheatutils-profile.json`: selected profile, restored at startup if it exists.

A fresh installation uses the default profile. Saves are delayed by 15 seconds,
capture a snapshot, and flush on normal shutdown. Invalid configuration is backed
up before defaults are used. Profile creation/copying, switching, deletion, and
reset are implemented in the backend. The Profiles page exposes creation, copying,
switching, and deletion. Live profile verification is deferred.
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
CDN fallback loader. Packaged files are served from `web/`; set the Java
system property `cheatutils.web.dir` to override that directory during UI work.

`gradlew.bat build` includes Java 8 infrastructure checks for save coalescing,
cancellation, shutdown flushing, independent file snapshots, invalid config
recovery, static/local HTTP responses, and traversal rejection. These do not
launch Minecraft. Batch 2 still needs live startup/shutdown and API verification.

The UI uses the 26.2 Vue loader unchanged: Vue 3.2.33 loads from jsDelivr first,
then `/local/vue.esm-browser.prod.js` if the CDN fails. To use the fallback, place
that Vue distribution file in the game's `mods/` directory. Google fonts are
optional; the browser uses system fonts when unavailable. Monaco loads lazily
from jsDelivr with `/local/monaco-editor.js` as its fallback (place the compatible
ES module bundle and any required assets in the game's `mods/` directory).

Browser fixture checks cover all six categories, navigation, search, Core Config
saving, and narrow-screen layout with both CDN and local Vue loading. These checks
use mock API responses; live Minecraft integration remains a separate checkpoint.

## FreeCam

Open ESP → Free Cam in the web UI, join a world, and click Enable FreeCam. Return
to the game to move the camera with WASD, jump, and sneak. Use the web button to
disable it, or use the default F6 binding.

Settings retain the 26.2 names and `/api/free-cam` route. Runtime state is separate:
`GET /api/free-cam-state` returns `active` and `available`; POST accepts the JSON
string `"enable"`, `"disable"`, or `"toggle"`. Activation is never saved in a profile.
Profile/world changes disable FreeCam. Camera motion pauses in screens or when
the game loses focus. Remember Input State deliberately preserves the player's
movement input captured at activation; leave it off to keep movement input idle.

Batch 4 live checks:

- Enable/disable from first and third person; verify camera/input restoration.
- Fly through blocks, change speed/flight mode, and check mouse look and F3 coordinates.
- Toggle targeting and hand rendering; check block/entity selection from the camera.
- Open screens and switch focus; verify the camera does not jump on return.
- Disconnect/rejoin, change dimensions, respawn, and switch profiles while active.
- Repeat with the packaged jar. Camera paths and lock/follow modes are not included.

## Minimal scripting (batch 5)

The `java-scripting-language` submodule tracks branch `java-8`, pinned by Git to
commit `b21408ec8aab7e2c43951bb9e1f4455de385188e`. Updating the branch name alone
does not update the pinned commit.

Java 8 nullness annotation stubs in `src/scriptingAnnotations/java` replace the
JSpecify dependency for compilation only. They are not included in the mod jar.

The scripting sources compile directly as part of the main source set and use
Forge's ASM 5.2. No separate ASM copy is bundled. The scripting engine's license
is included in the jar.

Available APIs:

- `freeCam.toggle()`, `freeCam.enable()`, `freeCam.disable()`, `freeCam.isEnabled()`.
- `ui.systemMessage("Hello");` displays a local chat message without sending it to
  the server.
- `esp.toggle();` is a no-op placeholder for the default Toggle ESP script.

Scripts are synchronous, one-shot programs on the client thread. This batch has
no async scheduling, event scripts, Java interop, or execution time limit. The
`advancedScripting` config flag does not enable additional APIs yet.

Use the existing HTTP address from the log. `POST /api/script-compile` compiles
without executing; `POST /api/script-exec` compiles and executes. Both accept
`{"code":"..."}` and return `success`, `executed`, and `diagnostics`. Diagnostics
include a message and source range. Runtime failures return `error` and are logged;
they do not disable later executions. `executed` means execution was attempted.

Live checks from PowerShell (adjust the port if needed):

```powershell
$scriptUrl = 'http://127.0.0.1:5005/api/script-exec'
$body = @{ code = 'ui.systemMessage("Scripting works"); freeCam.toggle();' } | ConvertTo-Json
Invoke-RestMethod -Uri $scriptUrl -Method Post -ContentType 'application/json' -Body $body
```

Join a world first to see the FreeCam change. Repeat to turn it off. Then try
`freeCam.unknown();` for a compilation diagnostic, and
`int zero = 0; int value = 1 / zero;` for a runtime failure, followed by a valid
script to confirm recovery. Repeat with the release jar.

`build` includes `verifyScripting`, which executes generated code from the
reobfuscated jar on Java 8 with the older ASM present. It checks API compilation,
diagnostics, failure recovery, absence of bundled ASM and annotation stubs, and
class-file versions.
Minecraft-side API execution still needs the live checks above.

## Keybinding scripts (batch 6)

Open Scripting → Keybinding Scripts to create, edit, rename, remove, or assign
scripts. The Monaco editor provides syntax highlighting, delayed compile
diagnostics, fullscreen mode, and Run once without saving. Vue and Monaco retain
their CDN-first loading and local-file fallbacks. Semantic completion and hover
integration are not included yet.

Minecraft Controls → CheatUtils contains Key 0 through Key 29. Physical key codes
are saved by Minecraft in `options.txt`; script names, source, and assignments
belong to the selected CheatUtils profile. New configurations include Toggle ESP
on Key 0 (unbound) and Toggle FreeCam on Key 1 (F6). Existing Controls assignments
are preserved. Removing all scripts leaves the list empty after a restart.

Invalid saves leave the previous script and assignments intact. Invalid scripts
loaded from a profile remain editable but inactive. A runtime failure disables
only that keybinding script until it is successfully saved again or its profile
is reloaded. Keys are drained while outside gameplay, in screens, or unfocused.

`/api/keybinding-scripts` supports GET and POST; its encoded script-name suffix
supports GET, PUT, and DELETE. POST/PUT accept `{"name":"...","code":"..."}` and
return `ok` plus compile `diagnostics`. PUT to
`/api/keybinding-scripts-assign/<encoded-name>` accepts an integer slot, or -1 to
unassign. Each script has at most one slot; assigning an occupied slot replaces it.

Build checks cover default scripts, failed saves, renames, assignment replacement,
deletion, runtime recovery, and profile persistence. Browser fixture checks cover
Monaco loading, editing, execution feedback, fullscreen mode, saving, and assignment
replacement with mock API responses.

Live review:

- In a world, press F6 twice to enable/disable FreeCam; repeat from the release jar.
- Create `ui.systemMessage("Key works");`, assign it a slot, and bind that slot in
  Controls. Check that it runs in gameplay and not while typing in chat or menus.
- Rename it and verify the assignment remains; remove it and verify the key is idle.
- Check an invalid script in the editor, then test a runtime failure and save a fix.
- Switch profiles and restart Minecraft to check script and physical-key persistence.
