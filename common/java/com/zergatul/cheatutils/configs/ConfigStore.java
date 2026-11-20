package com.zergatul.cheatutils.configs;

import com.google.gson.*;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.adapters.*;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.*;

public class ConfigStore {

    public static final ConfigStore INSTANCE = new ConfigStore();

    private static final long WRITE_FILE_DELAY = 15 * 1_000_000_000L;

    public final Gson gson = new GsonBuilder()
            .setExclusionStrategies(new GsonSkipExcludeStrategy())
            .registerTypeAdapterFactory(new BlockTypeAdapterFactory())
            .registerTypeAdapterFactory(new ItemTypeAdapterFactory())
            .registerTypeAdapterFactory(new EntityTypeTypeAdapterFactory())
            .registerTypeAdapterFactory(new KillAuraPriorityEntryTypeAdapterFactory())
            .registerTypeAdapterFactory(new ClassTypeAdapterFactory())
            .registerTypeAdapterFactory(new ImmutableConfigAdapterFactory())
            .registerTypeAdapter(BlockState.class, new BlockStateTypeAdapter())
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(ImmutableList.class, new ImmutableListSerializer())
            .setPrettyPrinting()
            .create();

    private final Logger logger = LogManager.getLogger(ConfigStore.class);
    private final SettingsRegistry<Config> settingsRegistry;
    private final Settings settings;
    private File currentFile;

    private ConfigStore() {
        settingsRegistry = new SettingsRegistry<>(ConfigLock.class, new Config(), this::requestWrite);
        settings = new Settings(settingsRegistry);
    }

    public void initialNotify() {
        synchronized (ConfigLock.class) {
            settingsRegistry.initialNotify();
        }
    }

    Settings getSettings() {
        return settings;
    }

    public void read(boolean isInitial, File file) {
        synchronized (ConfigLock.class) {
            Config config = new Config();
            if (file.exists()) {
                Config readCfg = null;
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    JsonElement element = JsonParser.parseReader(reader);
                    Migrations.run(element);
                    readCfg = gson.fromJson(element, Config.class);
                    reader.close();
                } catch (Throwable e) {
                    logger.error("Cannot read config");
                    logger.error(e);
                }

                if (readCfg != null) {
                    config = readCfg;
                }
            }

            ScriptExecutionManager.INSTANCE.cancelAll();
            currentFile = file;
            if (isInitial) {
                // modules are not initialized at this stage, they will receive notification later from initialNotify() call
                settingsRegistry.replaceNoNotify(config);
            } else {
                settingsRegistry.replace(SettingNotificationReason.PROFILE_LOAD, config);
            }
        }
    }

    public void switchFile(File file) {
        synchronized (ConfigLock.class) {
            currentFile = file;
            requestWrite();
        }
    }

    public void createNew(File file) {
        synchronized (ConfigLock.class) {
            ScriptExecutionManager.INSTANCE.cancelAll();
            currentFile = file;
            settingsRegistry.replace(SettingNotificationReason.PROFILE_LOAD, new Config());
            requestWrite();
        }
    }

    public void requestWrite() {
        synchronized (ConfigLock.class) {
            ConfigWriterQueue.INSTANCE.queue(this.currentFile, WRITE_FILE_DELAY, createWriteToFileTask());
        }
    }

    private Runnable createWriteToFileTask() {
        File file = this.currentFile;
        Config config = settingsRegistry.getConfig();
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
}