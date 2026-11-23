package com.zergatul.cheatutils.common;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.features.Features;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.scripting.keybindings.KeyBindings;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.web.WebServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;

/**
 * The startup order of the mod. Loaders only decide where each step is called from.
 * On every loader both steps run on the client (render) thread inside the Minecraft constructor, in this order.
 */
public final class ModLifecycle {

    private static final Logger LOGGER = LogManager.getLogger(ModLifecycle.class);
    private static Stage stage = Stage.NEW; // client thread only

    private ModLifecycle() {}

    /**
     * Step 1, from the loader's key mapping registration, before {@code Options} loads key bindings from options.txt.
     * Loads the profile first, so it is in place before any module exists.
     * On Fabric this runs before Minecraft sets its game thread: don't use
     * {@link com.zergatul.cheatutils.concurrent.ClientThread} here.
     */
    public static void registerKeyMappings(IKeyBindingRegistry registry) {
        expect(Stage.NEW);

        SystemFonts.initAsync();
        Profiles.INSTANCE.init();
        KeyBindings.INSTANCE.registerKeyMappings(registry);

        stage = Stage.KEY_MAPPINGS_REGISTERED;
    }

    /** Step 2, at the end of the Minecraft constructor: options, font and renderers exist, nothing has ticked yet. */
    public static void onClientCreated() {
        if (stage == Stage.NEW) {
            // the loader didn't run step 1, for example because its mod loading failed: let it report that
            LOGGER.error("Key mappings were not registered, CheatUtils is not started.");
            return;
        }

        expect(Stage.KEY_MAPPINGS_REGISTERED);

        Modules.register();
        Features.register();
        // triggers initial SettingHandle<T>.observer handles registered by all modules constructors
        ConfigStore.INSTANCE.initialNotify();
        // last, so every web API is registered before the first request
        WebServer.INSTANCE.start();

        stage = Stage.STARTED;

        if (MixinPlugin.isStrictMixinsEnabled()) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }
    }

    private static void expect(Stage expected) {
        if (stage != expected) {
            throw new IllegalStateException(String.format("Startup step is out of order: %s, expected %s.", stage, expected));
        }
    }

    private enum Stage { NEW, KEY_MAPPINGS_REGISTERED, STARTED }
}