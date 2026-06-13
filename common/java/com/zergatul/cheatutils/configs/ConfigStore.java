package com.zergatul.cheatutils.configs;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.adapters.*;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import com.zergatul.cheatutils.modules.esp.EntityTitle;
import com.zergatul.cheatutils.modules.esp.LightLevel;
import com.zergatul.cheatutils.modules.hacks.HitboxSize;
import com.zergatul.cheatutils.modules.hacks.KillAura;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.modules.scripting.BlockAutomation;
import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.modules.visuals.WorldMarkers;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import com.zergatul.scripting.compiler.CompilationResult;
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

        currentFile = file;
        setConfig(newConfig);
        onConfigLoaded();
    }

    public synchronized void switchFile(File file) {
        currentFile = file;
        requestWrite();
    }

    public synchronized void createNew(File file) {
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

        ScriptsController.instance.clear();
        if (config.keyBindingScriptsConfig.scripts.isEmpty()) {
            final String toggleEspName = "Toggle ESP";
            try {
                ScriptsController.instance.add(toggleEspName, "esp.toggle();", false);
                //KeyBindingsController.instance.keys[0].setKey(InputConstants.getKey("key.keyboard.backslash"));
                KeyBindingsController.instance.assign(0, toggleEspName);
            } catch (Throwable e) {
                logger.error("Toggle ESP script initialization failed", e);
            }

            final String toggleFreeCamName = "Toggle FreeCam";
            try {
                ScriptsController.instance.add(toggleFreeCamName, "freeCam.toggle();", false);
                KeyBindingsController.instance.keys[1].setKey(InputConstants.getKey("key.keyboard.f6"));
                KeyBindingsController.instance.assign(1, toggleFreeCamName);
            } catch (Throwable e) {
                logger.error("Toggle FreeCam script initialization failed", e);
            }
        } else {
            ArrayList<KeyBindingScriptsConfig.ScriptEntry> copy = new ArrayList<>(config.keyBindingScriptsConfig.scripts);
            config.keyBindingScriptsConfig.scripts.clear();
            copy.forEach(s -> {
                try {
                    ScriptsController.instance.add(s.name, s.code, true);
                } catch (Throwable e) {
                    logger.error("KeyBinding script '{}' initialization failed", s.name, e);
                }
            });

            String[] bindings = config.keyBindingsConfig.bindings;
            for (int i = 0; i < KeyBindingsConfig.KeysCount; i++) {
                if (bindings[i] != null) {
                    KeyBindingsController.instance.assign(i, bindings[i]);
                }
            }
        }

        if (config.statusOverlayConfig.code != null) {
            try {
                CompilationResult result = ScriptsController.instance.compileOverlay(config.statusOverlayConfig.code);
                if (result.getProgram() != null) {
                    StatusOverlay.instance.setScript(result.getProgram());
                } else {
                    result.getDiagnostics().forEach(m -> logger.error("Status Overlay: {}", m.message));
                }
            } catch (Throwable e) {
                logger.error("StatusOverlay script initialization failed", e);
            }
        }

        if (config.blockAutomationConfig.code != null) {
            try {
                CompilationResult result = ScriptsController.instance.compileBlockAutomation(config.blockAutomationConfig.code);
                if (result.getProgram() != null) {
                    BlockAutomation.instance.setScript(result.getProgram());
                } else {
                    result.getDiagnostics().forEach(m -> logger.error("Block Automation: {}", m.message));
                }
            } catch (Throwable e) {
                logger.error("BlockAutomation script initialization failed", e);
            }
        }

        if (config.villagerRollerConfig.code != null) {
            try {
                CompilationResult result = ScriptsController.instance.compileVillagerRoller(config.villagerRollerConfig.code);
                if (result.getProgram() != null) {
                    VillagerRoller.instance.setScript(result.getProgram());
                } else {
                    result.getDiagnostics().forEach(m -> logger.error("Villager Roller: {}", m.message));
                }
            } catch (Throwable e) {
                logger.error("VillagerRoller script initialization failed", e);
            }
        }

        if (config.eventsScriptingConfig.code != null) {
            try {
                CompilationResult result = ScriptsController.instance.compileEvents(config.eventsScriptingConfig.code);
                if (result.getProgram() != null) {
                    EventsScripting.instance.setScript(result.getProgram());
                } else {
                    result.getDiagnostics().forEach(m -> logger.error("Events Scripting: {}", m.message));
                }
            } catch (Throwable e) {
                logger.error("EventScripting script initialization failed", e);
            }
        }

        if (config.blocks != null && config.blocks.getBlockConfigs() != null) {
            for (BlockEspConfig blockConfig : config.blocks.getBlockConfigs()) {
                if (blockConfig.code != null && blockConfig.code.isBlank()) {
                    blockConfig.code = null;
                }

                if (blockConfig.code != null) {
                    try {
                        CompilationResult result = ScriptsController.instance.compileBlockEsp(blockConfig.code);
                        if (result.getProgram() != null) {
                            blockConfig.script = result.getProgram();
                        } else {
                            result.getDiagnostics().forEach(m -> logger.error("Block ESP for {}: {}", blockConfig.blocks.stream().findFirst().get().getName(), m.message));
                        }
                    } catch (Throwable e) {
                        logger.error("BlockESP script for '{}' initialization failed", blockConfig.blocks.stream().findFirst().get().getName(), e);
                    }
                }
            }
        }

        if (config.entities != null && config.entities.configs != null) {
            for (EntityEspConfig entityConfig : config.entities.configs) {
                if (entityConfig.code != null && entityConfig.code.isBlank()) {
                    entityConfig.code = null;
                }

                if (entityConfig.code != null) {
                    try {
                        CompilationResult result = ScriptsController.instance.compileEntityEsp(entityConfig.code);
                        if (result.getProgram() != null) {
                            entityConfig.script = result.getProgram();
                        } else {
                            result.getDiagnostics().forEach(m -> logger.error("Entity ESP for {}: {}", entityConfig.clazz.getName(), m.message));
                        }
                    } catch (Throwable e) {
                        logger.error("EntityESP script for {} initialization failed", entityConfig.clazz.getName(), e);
                    }
                }
            }
        }

        if (config.killAuraConfig.code != null) {
            try {
                CompilationResult result = ScriptsController.instance.compileKillAura(config.killAuraConfig.code);
                if (result.getProgram() != null) {
                    KillAura.instance.setScript(result.getProgram());
                } else {
                    result.getDiagnostics().forEach(m -> logger.error("Kill Aura: {}", m.message));
                }
            } catch (Throwable e) {
                logger.error("KillAura script initialization failed", e);
            }
        }

        if (config.hitboxSizeConfig.code != null) {
            try {
                CompilationResult result = ScriptsController.instance.compileHitboxSize(config.hitboxSizeConfig.code);
                if (result.getProgram() != null) {
                    HitboxSize.instance.setScript(result.getProgram());
                } else {
                    result.getDiagnostics().forEach(m -> logger.error("Hitbox Size: {}", m.message));
                }
            } catch (Throwable e) {
                logger.error("HitboxSize script initialization failed", e);
            }
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