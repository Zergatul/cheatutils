package com.zergatul.cheatutils.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.adapters.BlockTypeAdapterFactory;
import com.zergatul.cheatutils.configs.adapters.ColorTypeAdapter;
import com.zergatul.cheatutils.configs.adapters.GsonSkipExcludeStrategy;
import com.zergatul.cheatutils.configs.adapters.ImmutableListSerializer;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.scripting.DiagnosticMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/** Configuration mutations and snapshot creation belong to the client thread. */
public class ConfigStore {

    public static final ConfigStore instance = new ConfigStore();
    public static final long WRITE_FILE_DELAY = TimeUnit.SECONDS.toNanos(15);

    public final Gson gson = new GsonBuilder()
            .setExclusionStrategies(new GsonSkipExcludeStrategy())
            .registerTypeAdapterFactory(new BlockTypeAdapterFactory())
            .registerTypeAdapter(ImmutableList.class, new ImmutableListSerializer())
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .setPrettyPrinting()
            .create();

    private final Logger logger = LogManager.getLogger(ConfigStore.class);
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
                logger.error("Cannot read config {}", file, e);
                next = new Config();
            }
        }

        currentFile = file;
        setConfig(next);
        onConfigLoaded();
    }

    public void switchFile(File file) {
        currentFile = file;
        requestWrite();
    }

    public void createNew(File file) {
        currentFile = file;
        setConfig(new Config());
        onConfigLoaded();
        requestWrite();
    }

    public void requestWrite() {
        if (currentFile != null && !Profiles.instance.isInResetState()) {
            ConfigWriterQueue.instance.queue(currentFile, WRITE_FILE_DELAY, getWriteToFileTask());
        }
    }

    public Runnable getWriteToFileTask() {
        File file = currentFile;
        String json = gson.toJson(config);
        return () -> write(file, json);
    }

    public void write(File file, String json) {
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
            logger.error("Cannot write config {}", file, e);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    logger.warn("Cannot remove temporary config {}", temporary, e);
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
        config.blocks.apply();
        Events.ConfigLoaded.trigger();

        KeyBindings.INSTANCE.clear();
        if (config.keyBindingScriptsConfig.scripts.isEmpty()) {
            final String toggleEspName = "Toggle ESP";
            try {
                List<DiagnosticMessage> diagnostics = KeyBindings.INSTANCE.add(toggleEspName, "esp.toggle();", false);
                if (!diagnostics.isEmpty()) {
                    ScriptRuntimeFailureHandler.instance.reportCompilationFailure(new ScriptRef(ScriptType.KEYBINDING, toggleEspName), diagnostics);
                }
                //KeyBindings.instance.getKeyMappingByIndex(0).setKey(InputConstants.getKey("key.keyboard.backslash"));
                KeyBindings.INSTANCE.assign(0, toggleEspName);
            } catch (Throwable e) {
                ScriptRuntimeFailureHandler.instance.reportInitializationFailure(new ScriptRef(ScriptType.KEYBINDING, toggleEspName), e);
            }

            final String toggleFreeCamName = "Toggle FreeCam";
            try {
                List<DiagnosticMessage> diagnostics = KeyBindings.INSTANCE.add(toggleFreeCamName, "freeCam.toggle();", false);
                if (!diagnostics.isEmpty()) {
                    ScriptRuntimeFailureHandler.instance.reportCompilationFailure(new ScriptRef(ScriptType.KEYBINDING, toggleFreeCamName), diagnostics);
                }
                KeyBindings.INSTANCE.getKeyMappingByIndex(1).setKeyCode(Keyboard.KEY_F6);
                KeyBindings.INSTANCE.assign(1, toggleFreeCamName);
            } catch (Throwable e) {
                ScriptRuntimeFailureHandler.instance.reportInitializationFailure(new ScriptRef(ScriptType.KEYBINDING, toggleFreeCamName), e);
            }
        } else {
            ArrayList<KeyBindingScriptsConfig.ScriptEntry> copy = new ArrayList<>(config.keyBindingScriptsConfig.scripts);
            config.keyBindingScriptsConfig.scripts.clear();
            copy.forEach(s -> {
                try {
                    List<DiagnosticMessage> diagnostics = KeyBindings.INSTANCE.add(s.name, s.code, true);
                    if (!diagnostics.isEmpty()) {
                        ScriptRuntimeFailureHandler.instance.reportCompilationFailure(new ScriptRef(ScriptType.KEYBINDING, s.name), diagnostics);
                    }
                } catch (Throwable e) {
                    ScriptRuntimeFailureHandler.instance.reportInitializationFailure(new ScriptRef(ScriptType.KEYBINDING, s.name), e);
                }
            });

            String[] bindings = config.keyBindingsConfig.bindings;
            for (int i = 0; i < KeyBindingsConfig.KEYS_COUNT; i++) {
                if (bindings[i] != null) {
                    KeyBindings.INSTANCE.assign(i, bindings[i]);
                }
            }
        }
    }

    // only this method should update this.config
    private void setConfig(Config config) {
        config.blocks.refreshMap();
        this.config = config;
    }
}