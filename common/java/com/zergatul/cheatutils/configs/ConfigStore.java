package com.zergatul.cheatutils.configs;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.adapters.*;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.modules.automation.AutoDisconnect;
import com.zergatul.cheatutils.modules.esp.EntityTitle;
import com.zergatul.cheatutils.modules.esp.LightLevel;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.modules.visuals.WorldMarkers;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import com.zergatul.cheatutils.scripting.generated.ParseException;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigStore {

    public static final ConfigStore instance = new ConfigStore();

    private static final long WRITE_FILE_DELAY = 15 * 1_000_000_000L;

    public final Gson gson = new GsonBuilder()
            .setExclusionStrategies(new GsonSkipExcludeStrategy())
            .registerTypeAdapterFactory(new BlockTypeAdapterFactory())
            .registerTypeAdapterFactory(new ItemTypeAdapterFactory())
            .registerTypeAdapterFactory(new KillAuraConfig$PriorityEntryTypeAdapterFactory())
            .registerTypeAdapter(BlockTracerConfig.class, new BlockTracerConfigTypeAdapter())
            .registerTypeAdapter(BlockState.class, new BlockStateTypeAdapter())
            .registerTypeAdapter(Class.class, new ClassTypeAdapter())
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(ImmutableList.class, new ImmutableListSerializer())
            .setPrettyPrinting()
            .create();

    private final Logger logger = LogManager.getLogger(ConfigStore.class);
    private Config config;
    private File currentFile;

    private ConfigStore() {
        setConfig(new Config());
    }

    public Config getConfig() {
        return config;
    }

    public synchronized void read(File file) {
        Config newConfig = new Config();
        if (file.exists()) {
            Config readCfg = null;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                JsonElement element = JsonParser.parseReader(reader);
                migrateConfigTree(element);
                readCfg = gson.fromJson(element, Config.class);
            } catch (Exception e) {
                logger.error("Cannot read config", e);
            }

            if (readCfg != null) {
                newConfig = readCfg;
            }
        }

        ScriptExecutionManager.instance.cancelAll();
        currentFile = file;
        setConfig(newConfig);
        onConfigLoaded();
    }

    public void requestWrite() {
        ConfigWriterQueue.instance.queue(this.currentFile, WRITE_FILE_DELAY, getWriteToFileTask());
    }

    public Runnable getWriteToFileTask() {
        File file = this.currentFile;
        Config config = this.config;
        return () -> {
            logger.debug("Saving config to file {}", file.getName());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                gson.toJson(config, writer);
            } catch (Throwable e) {
                logger.error("Cannot write config", e);
            }
        };
    }

    public static <T> void updateFromApi(Function<Config, T> extract, Consumer<T> update) {
        ConfigStore store = instance;
        Config config = store.getConfig();
        T moduleConfig = extract.apply(config);
        update.accept(moduleConfig);
        if (moduleConfig instanceof Sanitizable sanitizable) {
            sanitizable.sanitize();
        }
        store.requestWrite();
    }

    public static <T> void replaceFromApi(T moduleConfig, Consumer<T> replace) {
        if (moduleConfig instanceof Sanitizable sanitizable) {
            sanitizable.sanitize();
        }
        replace.accept(moduleConfig);
        instance.requestWrite();
    }

    // only this method should update this.config
    private void setConfig(Config config) {
        this.config = config;
    }

    private void onConfigLoaded() {
        config.sanitize();
        config.blocks.apply();

        LightLevel.instance.onChanged();

        EntityTitle.instance.onFontChange(config.entityTitleConfig);
        EntityTitle.instance.onEnchantmentFontChange(config.entityTitleConfig);
        WorldMarkers.instance.onFontChange(config.worldMarkersConfig);

        KeyBindings.instance.clear();
        if (config.keyBindingScriptsConfig.scripts.isEmpty()) {
            final String toggleEspName = "Toggle ESP";
            try {
                KeyBindings.instance.add(toggleEspName, "esp.toggle();", false);
                KeyBindings.instance.assign(0, toggleEspName);
            } catch (Throwable e) {
                logger.error("Toggle ESP script initialization failed", e);
            }

            final String toggleFreeCamName = "Toggle FreeCam";
            try {
                KeyBindings.instance.add(toggleFreeCamName, "freeCam.toggle();", false);
                KeyBindings.instance.getKeyMappingByIndex(1).setKey(InputConstants.getKey("key.keyboard.f6"));
                KeyBindings.instance.assign(1, toggleFreeCamName);
            } catch (Throwable e) {
                logger.error("Toggle FreeCam script initialization failed", e);
            }
        } else {
            ArrayList<KeyBindingScriptsConfig.ScriptEntry> copy = new ArrayList<>(config.keyBindingScriptsConfig.scripts);
            config.keyBindingScriptsConfig.scripts.clear();
            copy.forEach(s -> {
                try {
                    KeyBindings.instance.add(s.name, s.code, true);
                } catch (Throwable e) {
                    logger.error("Key binding script '{}' initialization failed", s.name, e);
                }
            });

            String[] bindings = config.keyBindingsConfig.bindings;
            for (int i = 0; i < KeyBindingsConfig.KeysCount; i++) {
                if (bindings[i] != null) {
                    KeyBindings.instance.assign(i, bindings[i]);
                }
            }
        }

        try {
            ScriptSaveResult result = ScriptWorkspace.INSTANCE.get(ScriptType.OVERLAY).init(config.statusOverlayConfig.code);
            if (!result.isSuccess()) {
                result.getDiagnostics().forEach(diagnostic -> logger.error(
                        "Status Overlay script initialization failed at {}:{}: {}",
                        diagnostic.range.getLine1(),
                        diagnostic.range.getColumn1(),
                        diagnostic.message));
            }
        } catch (Throwable e) {
            logger.error("Status Overlay script initialization failed", e);
        }

        try {
            ScriptSaveResult result = ScriptWorkspace.INSTANCE.get(ScriptType.EVENTS).init(config.eventsScriptingConfig.code);
            if (!result.isSuccess()) {
                result.getDiagnostics().forEach(diagnostic -> logger.error(
                        "Events Scripting initialization failed at {}:{}: {}",
                        diagnostic.range.getLine1(),
                        diagnostic.range.getColumn1(),
                        diagnostic.message));
            }
        } catch (Throwable e) {
            logger.error("Events Scripting initialization failed", e);
        }

        try {
            ScriptSaveResult result = ScriptWorkspace.INSTANCE.get(ScriptType.BLOCK_AUTOMATION).init(config.blockAutomationConfig.code);
            if (!result.isSuccess()) {
                result.getDiagnostics().forEach(diagnostic -> logger.error(
                        "Block Automation script initialization failed at {}:{}: {}",
                        diagnostic.range.getLine1(),
                        diagnostic.range.getColumn1(),
                        diagnostic.message));
            }
        } catch (Throwable e) {
            logger.error("Block Automation script initialization failed", e);
        }

        if (config.autoDisconnectConfig.code != null) {
            try {
                Runnable script = ScriptController.instance.compileAutoDisconnect(config.autoDisconnectConfig.code);
                AutoDisconnect.instance.setScript(script);
            } catch (ParseException | ScriptCompileException e) {
                logger.error("Auto Disconnect script initialization failed", e);
            }
        }

        try {
            ScriptSaveResult result = ScriptWorkspace.INSTANCE.get(ScriptType.VILLAGER_ROLLER).init(config.villagerRollerConfig.code);
            if (!result.isSuccess()) {
                result.getDiagnostics().forEach(diagnostic -> logger.error(
                        "Villager Roller script initialization failed at {}:{}: {}",
                        diagnostic.range.getLine1(),
                        diagnostic.range.getColumn1(),
                        diagnostic.message));
            }
        } catch (Throwable e) {
            logger.error("Villager Roller script initialization failed", e);
        }
    }

    static void migrateConfigTree(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject root = element.getAsJsonObject();
        if (root.has("scriptsConfig") && !root.has("keyBindingScriptsConfig")) {
            root.add("keyBindingScriptsConfig", root.remove("scriptsConfig"));
        }
        if (root.has("scriptedBlockPlacerConfig") && !root.has("blockAutomationConfig")) {
            root.add("blockAutomationConfig", root.remove("scriptedBlockPlacerConfig"));
        }
        root.remove("gameTickScriptingConfig");
    }
}