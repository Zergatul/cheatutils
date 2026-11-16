package com.zergatul.cheatutils.configs;

import com.google.gson.*;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.adapters.*;
import com.zergatul.cheatutils.configs.modules.BlockEspEntry;
import com.zergatul.cheatutils.configs.modules.EntityEspEntry;
import com.zergatul.cheatutils.modules.esp.block.BlockGroupKey;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.MultiScriptSlot;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

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
        settingsRegistry = new SettingsRegistry<>(this, new Config(), this::requestWrite);
        settings = new Settings(settingsRegistry);
    }

    public void initialNotify() {
        settingsRegistry.initialNotify();
    }

    Settings getSettings() {
        return settings;
    }

    public synchronized void read(boolean isInitial, File file) {
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
        onConfigLoaded();
    }

    public synchronized void switchFile(File file) {
        currentFile = file;
        requestWrite();
    }

    public synchronized void createNew(File file) {
        ScriptExecutionManager.INSTANCE.cancelAll();
        currentFile = file;
        settingsRegistry.replace(SettingNotificationReason.PROFILE_LOAD, new Config());
        onConfigLoaded();
        requestWrite();
    }

    public void requestWrite() {
        ConfigWriterQueue.INSTANCE.queue(this.currentFile, WRITE_FILE_DELAY, getWriteToFileTask());
    }

    public Runnable getWriteToFileTask() {
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

    private void onConfigLoaded() {
        Config config = settingsRegistry.getConfig();

        initializeScript(ScriptType.OVERLAY, null, config.statusOverlayConfig.code());
        initializeScript(ScriptType.BLOCK_AUTOMATION, null, config.blockAutomationConfig.code());
        initializeScript(ScriptType.VILLAGER_ROLLER, null, config.villagerRollerConfig.code());
        initializeScript(ScriptType.EVENTS, null, config.eventsScriptingConfig.code());

        ((MultiScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.BLOCK_ESP)).clear();
        if (config.blockEspConfig != null && config.blockEspConfig.entries() != null) {
            for (BlockEspEntry entry : config.blockEspConfig.entries()) {
                if (entry.code() != null) {
                    try {
                        String identifier = new BlockGroupKey(entry.blocks()).toString();
                        initializeScript(ScriptType.BLOCK_ESP, identifier, entry.code());
                    } catch (Throwable e) {
                        ScriptRuntimeFailureHandler.INSTANCE.reportInitializationFailure(new ScriptRef(ScriptType.BLOCK_ESP), e);
                    }
                }
            }
        }

        ((MultiScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.ENTITY_ESP)).clear();
        if (config.entityEspConfig != null && config.entityEspConfig.entries() != null) {
            for (EntityEspEntry entry : config.entityEspConfig.entries()) {
                if (entry.code() != null) {
                    try {
                        initializeScript(ScriptType.ENTITY_ESP, entry.type().getName(), entry.code());
                    } catch (Throwable e) {
                        ScriptRuntimeFailureHandler.INSTANCE.reportInitializationFailure(new ScriptRef(ScriptType.ENTITY_ESP), e);
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
                ScriptRuntimeFailureHandler.INSTANCE.reportCompilationFailure(ref, result.getDiagnostics());
            }
        } catch (Throwable e) {
            ScriptRuntimeFailureHandler.INSTANCE.reportInitializationFailure(ref, e);
        }
    }
}