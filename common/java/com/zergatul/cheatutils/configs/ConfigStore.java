package com.zergatul.cheatutils.configs;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.adapters.*;
import com.zergatul.cheatutils.modules.esp.EntityTitle;
import com.zergatul.cheatutils.modules.esp.LightLevel;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.modules.visuals.WorldMarkers;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.MultiScriptSlot;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import com.zergatul.scripting.DiagnosticMessage;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConfigStore {

    public static final ConfigStore instance = new ConfigStore();

    private static final long WRITE_FILE_DELAY = 15 * 1_000_000_000L;

    public final Gson gson = new GsonBuilder()
            .setExclusionStrategies(new GsonSkipExcludeStrategy())
            .registerTypeAdapterFactory(new BlockTypeAdapterFactory())
            .registerTypeAdapterFactory(new ItemTypeAdapterFactory())
            .registerTypeAdapterFactory(new EntityTypeTypeAdapterFactory())
            .registerTypeAdapterFactory(new KillAuraConfig$PriorityEntryTypeAdapterFactory())
            .registerTypeAdapterFactory(new ClassTypeAdapterFactory())
            .registerTypeAdapter(BlockState.class, new BlockStateTypeAdapter())
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
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                JsonElement element = JsonParser.parseReader(reader);
                migration1(element);
                migration2(element);
                migration3(element);
                migration4(element);
                migration5(element);
                readCfg = gson.fromJson(element, Config.class);
                reader.close();
            } catch (Exception e) {
                logger.error("Cannot read config");
                logger.error(e);
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

    public synchronized void switchFile(File file) {
        currentFile = file;
        requestWrite();
    }

    public synchronized void createNew(File file) {
        ScriptExecutionManager.instance.cancelAll();
        currentFile = file;
        setConfig(new Config());
        onConfigLoaded();
        requestWrite();
    }

    public void requestWrite() {
        ConfigWriterQueue.instance.queue(this.currentFile, WRITE_FILE_DELAY, getWriteToFileTask());
    }

    public Runnable getWriteToFileTask() {
        File file = this.currentFile;
        Config config = this.config;
        return () -> {
            logger.debug("Saving config to file {}", file.getName());
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                gson.toJson(config, writer);
                writer.close();
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

    // only this method should update this.config
    private void setConfig(Config config) {
        config.blocks.refreshMap();
        this.config = config;
    }

    private void onConfigLoaded() {
        config.sanitize();
        config.blocks.apply();

        LightLevel.instance.onChanged();
        ConfigHttpServer.instance.onConfigUpdated();
        EntityTitle.instance.onTitleFontChange();
        EntityTitle.instance.onEnchantmentFontChange();
        WorldMarkers.instance.onFontChange();
        StatusOverlay.instance.onFontChange();

        KeyBindings.instance.clear();
        if (config.keyBindingScriptsConfig.scripts.isEmpty()) {
            final String toggleEspName = "Toggle ESP";
            try {
                List<DiagnosticMessage> diagnostics = KeyBindings.instance.add(toggleEspName, "esp.toggle();", false);
                if (!diagnostics.isEmpty()) {
                    ScriptRuntimeFailureHandler.instance.reportCompilationFailure(
                            new ScriptRef(ScriptType.KEYBINDING, toggleEspName), diagnostics);
                }
                //KeyBindings.instance.getKeyMappingByIndex(0).setKey(InputConstants.getKey("key.keyboard.backslash"));
                KeyBindings.instance.assign(0, toggleEspName);
            } catch (Throwable e) {
                ScriptRuntimeFailureHandler.instance.reportInitializationFailure(
                        new ScriptRef(ScriptType.KEYBINDING, toggleEspName), e);
            }

            final String toggleFreeCamName = "Toggle FreeCam";
            try {
                List<DiagnosticMessage> diagnostics = KeyBindings.instance.add(toggleFreeCamName, "freeCam.toggle();", false);
                if (!diagnostics.isEmpty()) {
                    ScriptRuntimeFailureHandler.instance.reportCompilationFailure(
                            new ScriptRef(ScriptType.KEYBINDING, toggleFreeCamName), diagnostics);
                }
                KeyBindings.instance.getKeyMappingByIndex(1).setKey(InputConstants.getKey("key.keyboard.f6"));
                KeyBindings.instance.assign(1, toggleFreeCamName);
            } catch (Throwable e) {
                ScriptRuntimeFailureHandler.instance.reportInitializationFailure(
                        new ScriptRef(ScriptType.KEYBINDING, toggleFreeCamName), e);
            }
        } else {
            ArrayList<KeyBindingScriptsConfig.ScriptEntry> copy = new ArrayList<>(config.keyBindingScriptsConfig.scripts);
            config.keyBindingScriptsConfig.scripts.clear();
            copy.forEach(s -> {
                try {
                    List<DiagnosticMessage> diagnostics = KeyBindings.instance.add(s.name, s.code, true);
                    if (!diagnostics.isEmpty()) {
                        ScriptRuntimeFailureHandler.instance.reportCompilationFailure(
                                new ScriptRef(ScriptType.KEYBINDING, s.name), diagnostics);
                    }
                } catch (Throwable e) {
                    ScriptRuntimeFailureHandler.instance.reportInitializationFailure(
                            new ScriptRef(ScriptType.KEYBINDING, s.name), e);
                }
            });

            String[] bindings = config.keyBindingsConfig.bindings;
            for (int i = 0; i < KeyBindingsConfig.KeysCount; i++) {
                if (bindings[i] != null) {
                    KeyBindings.instance.assign(i, bindings[i]);
                }
            }
        }

        initializeScript(ScriptType.OVERLAY, null, config.statusOverlayConfig.code);
        initializeScript(ScriptType.BLOCK_AUTOMATION, null, config.blockAutomationConfig.code);
        initializeScript(ScriptType.VILLAGER_ROLLER, null, config.villagerRollerConfig.code);
        initializeScript(ScriptType.EVENTS, null, config.eventsScriptingConfig.code);

        ((MultiScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.BLOCK_ESP)).clear();
        if (config.blocks != null && config.blocks.getBlockConfigs() != null) {
            for (BlockEspConfig blockConfig : config.blocks.getBlockConfigs()) {
                if (blockConfig.code != null && blockConfig.code.isBlank()) {
                    blockConfig.code = null;
                }

                if (blockConfig.code != null) {
                    try {
                        String identifier = Registries.BLOCKS.getKey(blockConfig.blocks.stream().findFirst().orElseThrow()).toString();
                        initializeScript(ScriptType.BLOCK_ESP, identifier, blockConfig.code);
                    } catch (Throwable e) {
                        ScriptRuntimeFailureHandler.instance.reportInitializationFailure(new ScriptRef(ScriptType.BLOCK_ESP), e);
                    }
                }
            }
        }

        ((MultiScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.ENTITY_ESP)).clear();
        if (config.entities != null && config.entities.configs != null) {
            for (EntityEspConfig entityConfig : config.entities.configs) {
                if (entityConfig.code != null && entityConfig.code.isBlank()) {
                    entityConfig.code = null;
                }

                if (entityConfig.code != null) {
                    try {
                        initializeScript(ScriptType.ENTITY_ESP, entityConfig.clazz.getName(), entityConfig.code);
                    } catch (Throwable e) {
                        ScriptRuntimeFailureHandler.instance.reportInitializationFailure(new ScriptRef(ScriptType.ENTITY_ESP), e);
                    }
                }
            }
        }
    }

    private void initializeScript(ScriptType type, @Nullable String identifier, @Nullable String code) {
        ScriptRef ref = new ScriptRef(type, identifier);
        try {
            ScriptSaveResult result = ScriptWorkspace.INSTANCE.get(type).init(identifier, code);
            if (!result.isSuccess()) {
                ScriptRuntimeFailureHandler.instance.reportCompilationFailure(ref, result.getDiagnostics());
            }
        } catch (Throwable e) {
            ScriptRuntimeFailureHandler.instance.reportInitializationFailure(ref, e);
        }
    }

    private void migration1(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject root = element.getAsJsonObject();
        if (!root.has("blocks")) {
            return;
        }

        element = root.get("blocks");
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject blocks = element.getAsJsonObject();
        if (!blocks.has("configs")) {
            return;
        }

        element = blocks.get("configs");
        if (!element.isJsonArray()) {
            return;
        }

        JsonArray configs = element.getAsJsonArray();
        for (JsonElement item: configs) {
            if (!item.isJsonObject()) {
                continue;
            }

            JsonObject config = item.getAsJsonObject();
            if (config.has("block")) {
                JsonArray array = new JsonArray();
                array.add(config.get("block"));
                config.add("blocks", array);
                config.remove("block");
            }
        }
    }

    private void migration2(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject root = element.getAsJsonObject();
        if (!root.has("scriptedBlockPlacerConfig")) {
            return;
        }

        JsonElement config = root.get("scriptedBlockPlacerConfig");
        root.remove("scriptedBlockPlacerConfig");
        root.add("blockAutomationConfig", config);
    }

    private void migration3(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject root = element.getAsJsonObject();
        if (!root.has("fogConfig")) {
            return;
        }

        JsonElement config = root.get("fogConfig");
        if (!config.isJsonObject()) {
            return;
        }

        JsonObject fog = config.getAsJsonObject();
        if (fog.has("disableFog")) {
            JsonElement value = fog.remove("disableFog");
            fog.add("enabled", value);
        }
    }

    private void migration4(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject root = element.getAsJsonObject();
        if (!root.has("scriptsConfig")) {
            return;
        }

        JsonElement config = root.get("scriptsConfig");
        if (!config.isJsonObject()) {
            return;
        }

        root.remove("scriptsConfig");
        root.add("keyBindingScriptsConfig", config);
    }

    private void migration5(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject root = element.getAsJsonObject();
        migrateBlockEspConfigFields(root);
        migrateEntityEspConfigFields(root);
    }

    private void migrateBlockEspConfigFields(JsonObject root) {
        migrateEspConfigs(root, "blocks", config -> {
            renameField(config, "drawOutline", "drawBoundingBox");
            renameField(config, "outlineWidth", "boundingBoxWidth");
            renameField(config, "outlineColor", "boundingBoxColor");
            renameField(config, "outlineMaxDistance", "boundingBoxMaxDistance");
        });
    }

    private void migrateEntityEspConfigFields(JsonObject root) {
        migrateEspConfigs(root, "entities", config -> {
            if (config.has("glow")) {
                // we need this check because 'drawOutline' field exists pre- and post-migration
                // and they have different meaning
                renameField(config, "drawOutline", "drawBoundingBox");
                renameField(config, "outlineWidth", "boundingBoxWidth");
                renameField(config, "outlineColor", "boundingBoxColor");
                renameField(config, "outlineMaxDistance", "boundingBoxMaxDistance");
            }

            renameField(config, "glow", "drawOutline");
            renameField(config, "glowColor", "outlineColor");
            renameField(config, "glowMaxDistance", "outlineMaxDistance");
        });
    }

    private void migrateEspConfigs(JsonObject root, String key, Consumer<JsonObject> migration) {
        if (!root.has(key)) {
            return;
        }

        JsonElement element = root.get(key);
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject group = element.getAsJsonObject();
        if (!group.has("configs")) {
            return;
        }

        element = group.get("configs");
        if (!element.isJsonArray()) {
            return;
        }

        for (JsonElement item : element.getAsJsonArray()) {
            if (!item.isJsonObject()) {
                continue;
            }

            JsonObject config = item.getAsJsonObject();
            migration.accept(config);
        }
    }

    private void renameField(JsonObject object, String oldName, String newName) {
        JsonElement value = object.remove(oldName);
        if (value != null && !object.has(newName)) {
            object.add(newName, value);
        }
    }
}