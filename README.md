### Warning
If you use download button on github, repository will not work since it is using symlinks and git submodules.

To download repository with submodules use below command:

`git clone --recurse-submodules https://github.com/Zergatul/cheatutils.git`

Before this make sure you have enabled symlinks on Git level and on system level (system level only required on Windows).

Explanation:

Repository is using symlinks to reuse files by both Forge and Fabric builds. If you want to build/work with repository by yourself in Windows you should enable symlinks in git config (during installation, or by running command `git config --global core.symlinks true`) and when you run `git clone` it should have enough permissions to create symlinks. By default Windows require Administrators permissions for this. You can change this behavior and allow to create symlinks without Administrator priviledges. Go to settings -> Update & Security -> For developers -> enable Developer Mode switch.

### Build
To build mod by yourself go to Forge or Fabric directory and run `gradlew build`.