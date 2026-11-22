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

    private static final long WRITE_FILE_DELAY = 30_000_000_000L;

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
    private final SettingsRegistry settingsRegistry;
    private final Settings settings;
    private final SettingsSerializer serializer;
    private File currentFile; // guarded by lock
    private volatile long profileGeneration; // changed under lock, on the client thread

    private ConfigStore() {
        settingsRegistry = new SettingsRegistry(lock, this::requestWrite);
        settings = new Settings(settingsRegistry);
        serializer = new SettingsSerializer(gson, settingsRegistry);
    }

    Settings getSettings() {
        return settings;
    }

    /** Startup only, before modules exist. Modules receive the INITIAL notification from {@link #initialNotify()}. */
    public void loadInitial(File file) {
        SettingsSnapshot config = readFile(file);
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

    /**
     * Known limitation, accepted: settings edits from other threads are not blocked during a profile switch.
     * <ul>
     *     <li>An edit that lands before the switch is published goes to the previous profile and is saved there.
     *     A web page that still shows the previous profile can also post its values after the switch.
     *     Either way, the edit doesn't appear in the profile the user just switched to.</li>
     *     <li>When the active profile is reloaded, an edit between the flush and the read is saved to disk,
     *     but the reloaded values don't include it. The next save of this profile overwrites it.</li>
     * </ul>
     * Both need an edit and a profile switch at the same moment, which takes a deliberate effort, so there is no
     * gate that rejects edits during a switch.
     */
    public void load(File file) {
        ClientThread.assertCurrent();
        // a pending write of this file holds newer values than the disk
        ConfigWriterQueue.INSTANCE.flush(file);
        publish(file, readFile(file), false);
    }

    public void createNew(File file) {
        ClientThread.assertCurrent();
        publish(file, settingsRegistry.createDefaults(), true);
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

    private void publish(File file, SettingsSnapshot config, boolean write) {
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

    private SettingsSnapshot readFile(File file) {
        if (!file.exists()) {
            return settingsRegistry.createDefaults();
        }

        JsonObject root;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JsonElement element = JsonParser.parseReader(reader);
            Migrations.run(element);
            if (!element.isJsonObject()) {
                throw new JsonParseException("Config root is not an object");
            }
            root = element.getAsJsonObject();
        } catch (IOException | RuntimeException e) {
            logger.error("Cannot read config {}", file.getName(), e);
            return settingsRegistry.createDefaults();
        }

        return serializer.fromJson(root);
    }

    private Runnable createWriteToFileTask() {
        File file = this.currentFile;
        // immutable: the writer thread saves exactly the profile that was active when the write was requested
        SettingsSnapshot snapshot = settingsRegistry.getSnapshot();
        return () -> {
            logger.debug("Saving config to file {}", file.getName());
            try {
                // fully serialized before the file is touched: a failure leaves the previous file
                ConfigWriterQueue.writeAtomically(file, gson.toJson(serializer.toJson(snapshot)));
            } catch (IOException | RuntimeException e) {
                logger.error("Cannot write config {}", file.getName(), e);
            }
        };
    }
}