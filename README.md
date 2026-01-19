# Cheat Utils

## Warning

If you use download button on github, repository will not work since it is using git submodules.

To download repository with submodules use below command:

`git clone --recurse-submodules https://github.com/Zergatul/cheatutils.git`

## Build

To build mod by yourself go to Forge or Fabric directory and run `gradlew build`.

## Debugging/Customizing Web App

Download repo (or just `/common/resources/web` directory), and add JVM argument in Minecraft launcher like this:

```bat
-Dcheatutils.web.dir=C:\full\path\to\web\directory
```

Now local website uses static files from this directory instead of mod resources.

## Code Examples :-

### Adding a module to the mod :-

#### Step 1

Create your main class file.
This is where most of your modules code will be written.

First, navigate to the modules folder located in `common/java/com/zergatul/cheatutils/modules`.
Select the folder that best fits your module type.
Create a new  file : `"ClassName.java"`.
the class should be in this format:

```java
import com.zergatul.cheatutils.modules.Module;

public class ClassName implements Module {
    public static final ClassName instance = new ClassName();
    ClassName(){

    }
}
```

#### Step 2

For the purposes of this explaination, it is assumed your mod is named as "ClassName"
Register your module in the mod.
Navigate to `Modules.java` in the same directory.
Add your module to the following function

```java
public static void register() {
    register(ClassName.instance);
}
```

Make sure you place it in the right position, modules are initialized in the order listed here. This includes the order of their events added to EventsApi

#### Step 3

Add your configuration class

Navigate to `common/java/com/zergatul/cheatutils/configs`

create a new file `ClassNameConfig.java`

```java
package com.zergatul.cheatutils.configs;

public class ClassNameConfig extends ModuleConfig implements ValidatableConfig {

    ClassNameConfig() {

    }

    @Override
    public void validate() {
        return;
    }
}
```

add your config validation inside the `validate()` function, this is where you will add limits to your variables, for example clampign the range of a value.
If your API will be accessible from the scripting, it is highly recomended to include validation for any variables.

>[!NOTE]
> In case if your module does not require validation, you can skip `implements ValidatableConfig` and the overriden `public void validate()` function. When doing this, also skip adding validation to [`Root.java`](#adding-a-module-to-the-mod--)
>
> If your module does not require enable / disable, you can skip `extends ModuleConfig` as well. This also means you cannot use the `enabled` boolean or the `isEnabled()`  inherited functions. Keep this in mind when creating the website / code for it.

you can also add any more functions or values to change here.
for example:

```java
public boolean bl1;
public double db1;
```

and so on. These must be public, you can also use functions internally, however functions will not work with the website API.
The  API directly changes variables.

an example of a minimal config which does not need validation or enable variable:

```java
    public class ClassNameConfig {}
```

Next, navigate to `Config.java`, located in the same directory.

Add your config to the class

```java
    public ClassNameConfig classNameConfig = new ClassNameConfig();
```

Now, navigate to `configStore.java`, located in the same directory and add these lines

```java
    private void onConfigLoaded() {
            config.classNameConfig.validate();
```

#### Step 4

Adding the module API for the website  to work with.
Navigate to `common/java/com/zergatul/cheatutils/scripting/modules/`
make a new file `ClassNameConfig.java`

add these lines

```java
package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ClassNameConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class ClassNameApi extends ModuleApi<ClassNameConfig> {


    @Override
    protected ClassNameConfig getConfig() {
        return ConfigStore.instance.getConfig().classNameconfig;
    }
}
```

#### Step 5

Allow the API to be accessed from scripting tools (this refers to user side scripting)

Navigate to `common/java/com/zergatul/cheatutils/scripting/Root.java`

Add the following lines inside the Root class.

```java
public static ClassNameApi className = new ClassNameApi();
```

This step is OPTIONAL and not required for module to function.
If you want your module configuration to be accessed from scripting
However, it is recomended to do this step

#### Step 6

Adding your module to the website

Navigate to `common/resources/web/modules.js`

Add your module under line 21 with the following format:

```javascript
module({
    group: 'automation',
    name: 'Display Name',
    component: 'ClassName',
    path: 'class-name',
    tags: ['search', 'terms', 'identifers']
});
```

Make sure you add your module to the correct section with the rest of its group
There are the following groups that can be used:

```javascript
`automation`
`esp`
`hacks`
`visuals`
`scripting`
`utility`
```

In our example, we used `'automation'`, you can change this to match your module type

now navigate to `common/resources/web/components`
next, navigate to the relevant folder for your module from the available folders. This is mapped to the groups used, so ensure you use the same folder.

Create 2 new files:

* ClassName.js
* ClassName.html

inside the javascript file, add the following:

```javascript
import { createSimpleComponent } from '/components/SimpleModule.js';

export function createComponent(template) {
    return createSimpleComponent('/api/class-name', template);
}

```

if you use any new components as listed in `common/resources/web/components.js` in your html, make sure to include them in your javascript file

for example:

```javascript
import { createSimpleComponent } from '/components/SimpleModule.js'

export function createComponent(template) {
    return createSimpleComponent('/api/class-name', template, {
        components: ['CodeBlock', 'ColorBox']
    });
}
```

##### How to make your html file

This guide will focus on the working, for style and other formatting / component usage, look at the html of other modules already implemented

your html file should look like this:

```html
<div class="module-main" v-if="config">

</div>
```

all components can be found at `common/resources/web/components/common`

[Refer to this Document for more information and examples](./common/\/resources/web/Web%20Examples.md)
