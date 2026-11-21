package com.zergatul.cheatutils.configs;

import com.google.gson.*;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.ClientThread;
import com.zergatul.cheatutils.configs.adapters.*;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.*;

/**
 * Loads, publishes and persists profiles. The store lock guards the settings and the current file only:
 * it is held briefly, and nobody runs user code or waits for another thread while holding it.
 * Profile changes run on the client thread; {@link Events#ProfileLoaded} follows each of them outside the lock.
 */
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
    private final Object lock = new Object();
    private final SettingsRegistry<Config> settingsRegistry;
    private final Settings settings;
    private File currentFile; // guarded by lock
    private volatile long profileGeneration; // changed under lock, on the client thread

    private ConfigStore() {
        settingsRegistry = new SettingsRegistry<>(lock, new Config(), this::requestWrite);
        settings = new Settings(settingsRegistry);
    }

    Settings getSettings() {
        return settings;
    }

    /** Startup only, before modules exist. Modules receive the INITIAL notification from {@link #initialNotify()}. */
    public void loadInitial(File file) {
        Config config = readFile(file);
        synchronized (lock) {
            currentFile = file;
            settingsRegistry.replaceNoNotify(config);
        }
    }

    /**
     * Called once after all modules are constructed. Scripts are loaded at the end of the first client tick,
     * so user code doesn't run during mod initialization.
     */
    public void initialNotify() {
        synchronized (lock) {
            settingsRegistry.initialNotify();
        }
        ClientThread.post(Events.ProfileLoaded::trigger);
    }

    public void load(File file) {
        ClientThread.assertCurrent();
        publish(file, readFile(file), false);
    }

    public void createNew(File file) {
        ClientThread.assertCurrent();
        publish(file, new Config(), true);
    }

    public void switchFile(File file) {
        ClientThread.assertCurrent();
        synchronized (lock) {
            profileGeneration++;
            currentFile = file;
            requestWrite();
        }
    }

    /** Changes on every profile switch. Lets long operations detect that the profile they started with is gone. */
    public long getProfileGeneration() {
        return profileGeneration;
    }

    public boolean isLockHeldByCurrentThread() {
        return Thread.holdsLock(lock);
    }

    public void requestWrite() {
        synchronized (lock) {
            ConfigWriterQueue.INSTANCE.queue(currentFile, WRITE_FILE_DELAY, createWriteToFileTask());
        }
    }

    private void publish(File file, Config config, boolean write) {
        synchronized (lock) {
            profileGeneration++;
            currentFile = file;
            settingsRegistry.replace(SettingNotificationReason.PROFILE_LOAD, config);
            if (write) {
                requestWrite();
            }
        }

        // Outside the lock: listeners reload scripts, which runs user code.
        Events.ProfileLoaded.trigger();
    }

    private Config readFile(File file) {
        if (!file.exists()) {
            return new Config();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JsonElement element = JsonParser.parseReader(reader);
            Migrations.run(element);
            Config config = gson.fromJson(element, Config.class);
            if (config != null) {
                return config;
            }
        } catch (Throwable e) {
            logger.error("Cannot read config");
            logger.error(e);
        }

        return new Config();
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
