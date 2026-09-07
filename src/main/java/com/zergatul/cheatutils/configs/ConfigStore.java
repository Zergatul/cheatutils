package com.zergatul.cheatutils.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.zergatul.cheatutils.common.Events;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/** Configuration mutations and snapshot creation belong to the client thread. */
public class ConfigStore {
    public static final ConfigStore instance = new ConfigStore();
    public static final long WRITE_FILE_DELAY = TimeUnit.SECONDS.toNanos(15);
    private static final Logger LOGGER = LogManager.getLogger(ConfigStore.class);

    public final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Config config = new Config();
    private File currentFile;

    public Config getConfig() {
        return config;
    }

    public void read(File file) {
        Config next = new Config();
        if (file.exists()) {
            try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                next = gson.fromJson(reader, Config.class);
                if (next == null) {
                    throw new JsonParseException("Expected a configuration object.");
                }
            } catch (IOException | RuntimeException e) {
                LOGGER.error("Cannot read config {}", file, e);
                // Preserve the original before subsequent saves can replace it.
                try {
                    Files.copy(file.toPath(), new File(file + ".invalid-" + System.currentTimeMillis()).toPath());
                } catch (IOException backupError) {
                    throw new IllegalStateException("Cannot preserve invalid config " + file, backupError);
                }
                next = new Config();
            }
        }
        currentFile = file;
        config = next;
        onConfigLoaded();
    }

    public void switchFile(File file) {
        currentFile = file;
        requestWrite();
    }

    public void createNew(File file) {
        currentFile = file;
        config = new Config();
        onConfigLoaded();
        requestWrite();
    }

    public void requestWrite() {
        if (currentFile != null && !com.zergatul.cheatutils.modules.utilities.Profiles.instance.isInResetState()) {
            ConfigWriterQueue.instance.queue(currentFile, WRITE_FILE_DELAY, getWriteToFileTask());
        }
    }

    public Runnable getWriteToFileTask() {
        File file = currentFile;
        String json = gson.toJson(config);
        return () -> write(file, json);
    }

    public static void write(File file, String json) {
        Path temporary = null;
        try {
            Path target = file.toPath().toAbsolutePath();
            Files.createDirectories(target.getParent());
            temporary = Files.createTempFile(target.getParent(), file.getName(), ".tmp");
            Files.write(temporary, json.getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.error("Cannot write config {}", file, e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    LOGGER.warn("Cannot remove temporary config {}", temporary, e);
                }
            }
        }
    }

    public static <T> void updateFromApi(Function<Config, T> extract, Consumer<T> update) {
        T value = extract.apply(instance.config);
        update.accept(value);
        if (value instanceof Sanitizable) {
            ((Sanitizable) value).sanitize();
        }
        instance.requestWrite();
    }

    private void onConfigLoaded() {
        config.sanitize();
        Events.ConfigLoaded.trigger();
    }
}
