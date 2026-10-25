# Cheat Utils

## Warning

If you use download button on GitHub, repository will not work since it is using git submodules.

To download repository with submodules use below command:

`git clone --recurse-submodules https://github.com/Zergatul/cheatutils.git`

## Build

To build mod by yourself go to Forge or Fabric directory and run `gradlew build`.
Requires JDK 25.

## Debugging/Customizing Web App

Download repo (or just `/common/resources/web` directory), and add JVM argument in Minecraft launcher like this:

```bat
-Dcheatutils.web.dir=C:\full\path\to\web\directory
```

Now local website uses static files from this directory instead of mod resources.

## Code Examples :-

### Adding a module to the mod :-

#### Step 1: Create your main class file.

This is where most of your module code will be written.

First, navigate to the modules folder located in [`common/java/com/zergatul/cheatutils/modules`](common/java/com/zergatul/cheatutils/modules).
Select the folder that best fits your module type.
from here on, we will refer to a theoretical module named "AutoPearl", however you should follow the same naming conventions

Create a new  file : [`AutoPearl.java`](common/java/com/zergatul/cheatutils/modules).

the class should be in this format:

```java
package com.zergatul.cheatutils.modules.automation;
import com.zergatul.cheatutils.modules.Module;

public class AutoPearl implements Module {
    public static final AutoPearl instance = new AutoPearl();
    private AutoPearl() {

    }
}
```
Make sure to add the line:
`package com.zergatul.cheatutils.modules.<folder>;`  
`<folder>` should be where your class file lives inside the [`modules`](./common/java/com/zergatul/cheatutils/modules) folder.
in this example, we are putting this under automation:
`package com.zergatul.cheatutils.modules.automation;`

#### Step 2: Register your module in the mod.

For the purposes of this explanation, it is assumed your module is named as "AutoPearl"
Navigate to [`Modules.java`](./common/java/com/zergatul/cheatutils/modules/Modules.java)
Add your module to the following function

```java
public static void register() {
    register(AutoPearl.instance);
}
```

Make sure you place it in the right position, modules are initialized in the order listed here. This includes the order of their events added to EventsApi

>[!NOTE]
> This only applies to legacy modules that do not specify their own importance. Default importance for attached events is `0`

#### Step 3: Add your configuration class

Module configuration is currently being refactored to make all configs immutable. Both styles coexist:

- Legacy mutable configs live directly in [`configs`](./common/java/com/zergatul/cheatutils/configs). They use public fields, may extend `ModuleConfig`, and may implement `Sanitizable`.
- New immutable module configs live in [`configs/modules`](./common/java/com/zergatul/cheatutils/configs/modules). They are records implementing `ImmutableConfig`, with defaults and validation defined in the record itself.
- [`Config.java`](./common/java/com/zergatul/cheatutils/configs/Config.java) still contains the root fields for both styles and defines their saved JSON layout.
- [`Settings.java`](./common/java/com/zergatul/cheatutils/configs/Settings.java) binds immutable config types to those root fields. Modules access them through `Settings.get(...)` and `SettingHandle2<T>`.

##### New style: immutable configs

For a new module, follow the immutable style. Create `AutoPearlConfig.java` in `configs/modules`:

```java
package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record AutoPearlConfig(
        boolean enabled,
        double range
) implements ImmutableConfig, ToggleableConfig<AutoPearlConfig> {

    public AutoPearlConfig {
        range = MathUtils.clamp(range, 1, 5);
    }

    // <generated-code>
    // </generated-code>
}
```

Put validation and normalization in the compact constructor so they apply whenever a config is created, including updates and JSON loading. Keep nested values and collections immutable too. `ToggleableConfig<T>` is optional; use it when the module has an `enabled` component and needs the standard enable/disable behavior.

Generate the `withX(...)` methods and nested `Builder` by running this command from the repository root:

```bat
.\fabric\gradlew.bat -p fabric generateImmutableConfigs
```

This task is manually triggered; compilation does not run it automatically. It scans Java files under `configs` (including `configs/modules`) and replaces the regions between `// <generated-code>` and `// </generated-code>` in marked records. Keep one record component per line and rerun it after changing components. The generated `withEnabled(...)` method also satisfies `ToggleableConfig<T>`.

After this add static `defautls` method like this. Prefer `Builder` instead of calling public constructor directly. This way it is easier to understand parameters, when looking at git diffs, or simple non-IDE text editors:
```java
public static AutoPearlConfig defaults() {
    return new Builder()
            .enabled(false)
            .range(3)
            .build();
}
```

The public static `defaults()` method supplies the initial configuration. [`ImmutableConfigAdapterFactory`](./common/java/com/zergatul/cheatutils/configs/adapters/ImmutableConfigAdapterFactory.java) also uses it when loading JSON: missing or null properties retain their defaults, and the record constructor validates the resulting values.

Add a root field in [`Config.java`](./common/java/com/zergatul/cheatutils/configs/Config.java), importing the new config type:

```java
public AutoPearlConfig autoPearlConfig = AutoPearlConfig.defaults();
```

Register its binding in the `Settings` constructor:

```java
registrar.register(
        AutoPearlConfig.class,
        config -> config.autoPearlConfig,
        (config, value) -> config.autoPearlConfig = value);
```

Prefer alphabetical order by config class name for registrations in [`Settings.java`](./common/java/com/zergatul/cheatutils/configs/Settings.java). This is a readability convention, not a functional requirement.

In your module, obtain a handle instead of accessing the root config directly:

```java
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.AutoPearlConfig;

// Members of AutoPearl:
private final SettingHandle2<AutoPearlConfig> settings = Settings.get(AutoPearlConfig.class);

public SettingHandle2<AutoPearlConfig> getSettings() {
    return settings;
}
```

Read record components through accessors and replace values through the handle:

```java
AutoPearlConfig config = settings.get();
if (!config.enabled()) {
    return;
}
double range = config.range();

settings.update(current -> current.withRange(3));
settings.update(current -> current.withEnabled(!current.enabled()));
```

The handle follows the active profile, requests persistence for changes, and notifies `onChanged((previous, current) -> ...)` listeners for value changes, including profile switches. Keep the handle as a module field; read its current value when needed. Registering a listener does not invoke it, so initial runtime setup belongs to the module. Listeners run synchronously under the store lock and must schedule work on the owning thread without waiting for it.

See [`AirPlaceConfig`](./common/java/com/zergatul/cheatutils/configs/modules/AirPlaceConfig.java) for constructor validation and generated methods, and [`EntityTitle`](./common/java/com/zergatul/cheatutils/modules/esp/EntityTitle.java) for change listeners.

##### Old style: mutable configs

Modules that have not been migrated still use classes in `configs`, such as [`LightLevelConfig`](./common/java/com/zergatul/cheatutils/configs/LightLevelConfig.java). Public field initializers or constructors supply defaults. `ModuleConfig` supplies the mutable `enabled` field and `isEnabled()`; `Sanitizable.sanitize()` validates fields in place.

Their root fields in `Config.java` use `new SomeConfig()`. Configs implementing `Sanitizable` also need a call in `Config.sanitize()`. Existing code may access these values with `ConfigStore.instance.getConfig()` or through the older `SettingHandle<T>` / `ModuleConfigSettingHandle<T>` classes. Those handles support in-place mutation and are separate from the immutable `SettingHandle2<T>` path.

When migrating a config, move it to `configs/modules`, update imports, replace root initialization with `defaults()`, add its `Settings` binding, and update module, web, and scripting access to use the immutable handle. Move validation into the record constructor and remove the old call from `Config.sanitize()`.

#### Step 4: Register the module's web API

Register the config endpoint in the module constructor using the same settings handle:

```java
WebApiRegistry.INSTANCE.register("auto-pearl", settings, AutoPearlConfig.class);
```

Import `com.zergatul.cheatutils.web.WebApiRegistry`. This exposes `/api/auto-pearl` for the website configuration component. See [`AirPlace`](./common/java/com/zergatul/cheatutils/modules/hacks/AirPlace.java) for an existing registration.

#### Step 5 Allow the API to be accessed from scripting tools 

(refers to user side scripting)

This step is **OPTIONAL** and not required for module to function.
If you want your module configuration to be accessed from scripting
However, it is recommended to do this step

Create `AutoPearlApi.java` in [`scripting/modules`](./common/java/com/zergatul/cheatutils/scripting/modules). Read and update settings through the module's handle:

```java
package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.automation.AutoPearl;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

public class AutoPearlApi {

    public boolean isEnabled() {
        return AutoPearl.instance.getSettings().get().enabled();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        AutoPearl.instance.getSettings().update(config -> config.withEnabled(!config.enabled()));
    }
}
```

Navigate to [`common/java/com/zergatul/cheatutils/scripting/Root.java`](./common/java/com/zergatul/cheatutils/scripting/Root.java)

Add the following lines inside the `Root` class.

```java
public static AutoPearlApi autoPearl = new AutoPearlApi();
```

#### Step 6

Adding your module to the website

Navigate to [`common/resources/web/modules.js`](./common/resources/web/modules.js)

Add your module under `line 21` with the following format:

```javascript
module({
    group: 'groupName',
    name: 'Display Name',
    component: 'componentName',
    path: 'class-name',
    tags: ['search', 'terms', 'identifers']
});
```
The search terms (tags) used should be reflective of the function of the module.
Make sure you add your module to the correct section with the rest of its group
These are the following groups that can be used:

```javascript
'automation'
'esp'
'hacks'
'visuals'
'scripting'
'utility'
```
\
\
In our example, we use `'automation'`, change this to match your module type:  
Example:

```javascript
module({
    group: 'automation',
    name: 'Auto Pearl',
    component: 'AutoPearl',
    path: 'auto-pearl',
    tags: ['pearl', 'automation', 'throw']
});
```

now navigate to [`common/resources/web/components`](./common/resources/web/components)  
next, navigate to the relevant folder for your module from the available folders. This is mapped to the groups used, so ensure you use the same folder.

**Create 2 new files:**

* AutoPearl.js
* AutoPearl.html

inside the `JavaScript` file, add the following:

```javascript
import { createSimpleComponent } from '/components/SimpleModule.js';

export function createComponent(template) {
    return createSimpleComponent('/api/auto-pearl', template);
}

```

if you use any new components as listed in [`common/resources/web/components.js`](./common/resources/web/components.js) in your html, make sure to include them in your javascript file

for example:

```javascript
import { createSimpleComponent } from '/components/SimpleModule.js'

export function createComponent(template) {
    return createSimpleComponent('/api/auto-pearl', template, {
        components: ['CodeBlock', 'ColorBox']
    });
}
```

##### How to make your html file

This guide will focus on the working.  
For style and other formatting / component usage, look at the html of other modules already implemented

**your barebones html file should look like this:**

```html
<div class="module-main" v-if="config">

</div>
```

all components can be found at `common/resources/web/components/common`

[Refer to this Document for more information and examples](./docs/Web%20Examples.md)