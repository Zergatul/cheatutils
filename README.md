# Cheat Utils

## Warning

The GitHub **Download ZIP** archive does not include the required Git submodules.

Clone the repository with its submodules instead:

```bat
git clone --recurse-submodules https://github.com/Zergatul/cheatutils.git
```

## Build

JDK 17 is required. Run the Gradle wrapper from the directory for the desired mod loader:

```sh
cd fabric
gradlew build
```

or:

```sh
cd forge
gradlew build
```

The resulting jar is written to that loader's `build/libs` directory.

## Debugging/Customizing Web App

Download the repository (or just the `/common/resources/web` directory) and add this JVM argument in the Minecraft Launcher:

```
-Dcheatutils.web.dir=C:\full\path\to\web\directory
```

The local web interface will use static files from this directory instead of the files packaged in the mod jar.