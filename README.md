# Cheat Utils

## Warning

If you use download button on github, repository will not work since it is using git submodules.

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

This is where most of your modules code will be written.

First, navigate to the modules folder located in [`common/java/com/zergatul/cheatutils/modules`](common/java/com/zergatul/cheatutils/modules).
Select the folder that best fits your module type.
from here on, we will refer to a theoretical module named "AutoPearl", however you should follow the same naming conventions

Create a new  file : [`"AutoPearl.java"`](common/java/com/zergatul/cheatutils/modules).

the class should be in this format:

```java
package com.zergatul.cheatutils.modules.automation;
import com.zergatul.cheatutils.modules.Module;

public class AutoPearl implements Module {
    public static final AutoPearl instance = new AutoPearl();
    AutoPearl(){

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

Navigate to [`common/java/com/zergatul/cheatutils/configs`](./common/java/com/zergatul/cheatutils/configs)

create a new file `AutoPearlConfig.java`

```java
package com.zergatul.cheatutils.configs;

public class AutoPearlConfig extends ModuleConfig implements Sanitizable {

    AutoPearlConfig() {

    }

    @Override
    public void validate() {
        return;
    }
}
```

add your config validation inside the `Sanitize()` function, this is where you will add limits to your variables, for example clamping the range of a value.
If your API will be accessible from the scripting, it is highly recommended to include validation for any variables.

>[!NOTE]
> In case if your module does not require validation, you can skip `implements Sanitizable` and the overridden `public void Sanitize()` function. When doing this, also skip adding validation to [`Root.java`](#adding-a-module-to-the-mod--)
>
> If your module does not require enable / disable, you can skip `extends ModuleConfig` as well. This also means you cannot use the `enabled` boolean or the `isEnabled()`  inherited functions. Keep this in mind when creating the website / code for it.

you can also add any more functions or values to change here.
for example:

```java
package com.zergatul.cheatutils.configs;

public class AutoPearlConfig extends ModuleConfig implements Sanitizable {
    public boolean bl1;
    public double db1;
    
    AutoPearlConfig() {
        bl1 = true;
        db1 = 100;        
    }

    @Override
    public void Sanitize() {
        return;
    }
}
```

And so on. These must be public, you can also use functions internally, however functions will not work with the website API.
That API directly modifies variables.

Any assignments in the class constructor are only used if your saved configuration in the mod has **no matching values**
This means that these assignments act as the **default configuration** of your module before the user makes any changes.

an example of a minimal config which does not need validation or enable variable:

```java
    public class AutoPearlConfig {}
```
\
\
**Next, navigate to [`Config.java`](./common/java/com/zergatul/cheatutils/configs/Config.java), located in the same directory.**

Add your config to the class

```java
    public AutoPearlConfig autoPearlConfig = new AutoPearlConfig();
```
\
**IF your module uses `sanitize()` method, make sure to add these lines under `public void sanitize()`**

```
    autoPearlConfig.sanitize();
```

#### Step 4: Adding the module API for the website  to work with.

Navigate to [`common/java/com/zergatul/cheatutils/scripting/modules/`](./common/java/com/zergatul/cheatutils/scripting/modules/)
make a new file `AutoPearlApi.java`

add these lines

```java
package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoPearlConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoPearlApi extends ModuleApi<AutoPearlConfig> {


    @Override
    protected AutoPearlConfig getConfig() {
        return ConfigStore.instance.getConfig().autoPearlConfig;
    }
}
```

#### Step 5 Allow the API to be accessed from scripting tools 

(refers to user side scripting)

This step is **OPTIONAL** and not required for module to function.
If you want your module configuration to be accessed from scripting
However, it is recommended to do this step


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

[Refer to this Document for more information and examples](./common/resources/web/Web%20Examples.md)
