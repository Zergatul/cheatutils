### Warning
If you use download button on github, repository will not work since it is using git submodules.

To download repository with submodules use below command:

`git clone --recurse-submodules https://github.com/Zergatul/cheatutils.git`

### Build
To build mod by yourself go to Forge or Fabric directory and run `gradlew build`.

### Debugging/Customizing Web App
Download repo (or just `/common/resources/web` directory), and add JVM argument in Minecraft launcher like this:
```
-Dcheatutils.web.dir=C:\full\path\to\web\directory
```
Now local website uses static files from this directory instead of mod resources.