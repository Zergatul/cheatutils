package com.zergatul.cheatutils.configs;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.adapters.*;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.modules.automation.AutoDisconnect;
import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import com.zergatul.cheatutils.modules.esp.LightLevel;
import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import com.zergatul.cheatutils.scripting.generated.ParseException;
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
                readCfg = gson.fromJson(element, Config.class);
            } catch (Exception e) {
                logger.error("Cannot read config", e);
            }

            if (readCfg != null) {
                newConfig = readCfg;
            }
        }

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

    public void onClose() {
        ConfigWriterQueue.instance.onClose();
    }

    // only this method should update this.config
    private void setConfig(Config config) {
        this.config = config;
    }

    private void onConfigLoaded() {
        config.sanitize();
        config.blocks.apply();

        LightLevel.instance.onChanged();

        EntityTitleController.instance.onFontChange(config.entityTitleConfig);
        EntityTitleController.instance.onEnchantmentFontChange(config.entityTitleConfig);
        WorldMarkersController.instance.onFontChange(config.worldMarkersConfig);

        if (config.scriptsConfig.scripts.size() == 0) {
            final String toggleEspName = "Toggle ESP";
            try {
                ScriptController.instance.add(toggleEspName, "main.toggleEsp();", false);
                KeyBindingsController.instance.keys[0].setKey(InputConstants.getKey("key.keyboard.backslash"));
                KeyBindingsController.instance.assign(0, toggleEspName);
            } catch (ParseException | ScriptCompileException e) {
                e.printStackTrace();
            }

            final String toggleFreeCamName = "Toggle FreeCam";
            try {
                ScriptController.instance.add(toggleFreeCamName, "freeCam.toggle();", false);
                KeyBindingsController.instance.keys[1].setKey(InputConstants.getKey("key.keyboard.f6"));
                KeyBindingsController.instance.assign(1, toggleFreeCamName);
            } catch (ParseException | ScriptCompileException e) {
                e.printStackTrace();
            }
        } else {
            ArrayList<ScriptsConfig.ScriptEntry> copy = new ArrayList<>(config.scriptsConfig.scripts);
            config.scriptsConfig.scripts.clear();
            copy.forEach(s -> {
                try {
                    ScriptController.instance.add(s.name, s.code, true);
                } catch (ParseException | ScriptCompileException e) {
                    e.printStackTrace();
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
                Runnable script = ScriptController.instance.compileOverlay(config.statusOverlayConfig.code);
                StatusOverlay.instance.setScript(script);
            } catch (ParseException | ScriptCompileException e) {
                e.printStackTrace();
            }
        }

        if (config.gameTickScriptingConfig.code != null) {
            try {
                Runnable script = ScriptController.instance.compileKeys(config.gameTickScriptingConfig.code);
                GameTickScriptingController.instance.setScript(script);
            } catch (ParseException | ScriptCompileException e) {
                e.printStackTrace();
            }
        }

        if (config.scriptedBlockPlacerConfig.code != null) {
            try {
                Runnable script = ScriptController.instance.compileBlockPlacer(config.scriptedBlockPlacerConfig.code);
                ScriptedBlockPlacerController.instance.setScript(script);
            } catch (ParseException | ScriptCompileException e) {
                e.printStackTrace();
            }
        }

        if (config.autoDisconnectConfig.code != null) {
            try {
                Runnable script = ScriptController.instance.compileAutoDisconnect(config.autoDisconnectConfig.code);
                AutoDisconnect.instance.setScript(script);
            } catch (ParseException | ScriptCompileException e) {
                e.printStackTrace();
            }
        }

        if (config.villagerRollerConfig.code != null) {
            try {
                Runnable script = ScriptController.instance.compileVillagerRoller(config.villagerRollerConfig.code);
                VillagerRoller.instance.setScript(script);
            } catch (ParseException | ScriptCompileException e) {
                e.printStackTrace();
            }
        }
    }
}