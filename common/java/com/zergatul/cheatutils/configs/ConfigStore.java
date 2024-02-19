package com.zergatul.cheatutils.configs;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.adapters.*;
import com.zergatul.cheatutils.controllers.*;
import com.zergatul.cheatutils.modules.automation.AutoDisconnect;
import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import com.zergatul.cheatutils.modules.esp.LightLevel;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.modules.scripting.BlockAutomation;
import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.scripting.compiler.ScriptCompileException;
import com.zergatul.scripting.generated.ParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class ConfigStore {

    public static final ConfigStore instance = new ConfigStore();

    private static final String FILE = "zergatulcheatutils.json";
    private static long WRITE_FILE_DELAY = 15 * 1000000000L;

    public final Gson gson = new GsonBuilder()
            .setExclusionStrategies(new GsonSkipExcludeStrategy())
            .registerTypeAdapterFactory(new BlockTypeAdapterFactory())
            .registerTypeAdapterFactory(new ItemTypeAdapterFactory())
            .registerTypeAdapterFactory(new KillAuraConfig$PriorityEntryTypeAdapterFactory())
            .registerTypeAdapterFactory(new ClassTypeAdapterFactory())
            .registerTypeAdapter(BlockState.class, new BlockStateTypeAdapter())
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(ImmutableList.class, new ImmutableListSerializer())
            .setPrettyPrinting()
            .create();

    private Config config;
    private final Logger logger = LogManager.getLogger(ConfigStore.class);
    private final Thread thread;
    private final Object writeEvent = new Object();
    private volatile long lastWriteRequest = 0;

    private ConfigStore() {
        config = new Config();
        thread = new Thread(this::delayedWriteThreadFunc);
        thread.start();
    }

    public Config getConfig() {
        return config;
    }

    public void read() {
        File file = getFile();
        if (file.exists()) {
            Config readCfg = null;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                JsonElement element = JsonParser.parseReader(reader);
                migration1(element);
                migration2(element);
                readCfg = gson.fromJson(element, Config.class);
                reader.close();
            } catch (Exception e) {
                logger.warn("Cannot read config");
                e.printStackTrace();
            }

            if (readCfg != null) {
                config = readCfg;
            }
        }

        onConfigLoaded();
    }

    public void requestWrite() {
        lastWriteRequest = System.nanoTime();
        synchronized (writeEvent) {
            writeEvent.notify();
        }
    }

    public void onClose() {
        thread.interrupt();
    }

    private void delayedWriteThreadFunc() {
        boolean writeQeued = false;
        try {
            while (true) {
                writeQeued = false;
                synchronized (writeEvent) {
                    writeEvent.wait();
                }
                writeQeued = true;
                long lastValue = lastWriteRequest;
                Thread.sleep(WRITE_FILE_DELAY / 1000000);
                while (lastWriteRequest != lastValue) {
                    lastValue = lastWriteRequest;
                    long waitNs = lastWriteRequest + WRITE_FILE_DELAY - System.nanoTime();
                    Thread.sleep(waitNs / 1000000);
                }
                write();
            }
        }
        catch (InterruptedException e) {
            if (writeQeued) {
                write();
            }
        }
    }

    private void write() {
        logger.debug("Saving config to file");
        File file = getFile();
        try {
            synchronized (this) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                gson.toJson(config, writer);
                writer.close();
            }
        }
        catch (Exception e) {
            logger.warn("Cannot write config");
            e.printStackTrace();
        }
    }

    private File getFile() {
        File configDir = new File(Minecraft.getInstance().gameDirectory, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        return new File(configDir.getPath(), FILE);
    }

    private void onConfigLoaded() {
        LightLevel.instance.onChanged();
        config.blocks.apply();

        // clazz==null can occur after removing mod with custom entities
        config.entities.configs = config.entities.configs.removeIf(c -> c.clazz == null);

        // TODO: use reflection to automatically find ValidatableConfig's?
        config.killAuraConfig.validate();
        config.movementHackConfig.validate();
        config.fastBreakConfig.validate();
        config.elytraHackConfig.validate();
        config.freeCamConfig.validate();
        config.flyHackConfig.validate();
        config.boatHackConfig.validate();
        config.explorationMiniMapConfig.validate();
        config.reachConfig.validate();
        config.lightLevelConfig.validate();
        config.schematicaConfig.validate();
        config.autoBucketConfig.validate();
        config.performanceConfig.validate();
        config.entityTitleConfig.validate();
        config.keyBindingsConfig.validate();
        config.worldMarkersConfig.validate();
        config.autoAttackConfig.validate();
        config.projectilePathConfig.validate();
        config.chatUtilitiesConfig.validate();
        config.areaMineConfig.validate();
        config.hitboxSizeConfig.validate();

        config.blocks.refreshMap();

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

        if (config.blockAutomationConfig.code != null) {
            try {
                Runnable script = ScriptController.instance.compileBlockPlacer(config.blockAutomationConfig.code);
                BlockAutomation.instance.setScript(script);
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

        if (config.eventsScriptingConfig.code != null) {
            try {
                Runnable script = ScriptController.instance.compileEvents(config.eventsScriptingConfig.code);
                EventsScripting.instance.setScript(script);
            } catch (ParseException | ScriptCompileException e) {
                e.printStackTrace();
            }
        }

        if (config.entities != null && config.entities.configs != null) {
            for (EntityEspConfig config : config.entities.configs) {
                if (config.code != null && config.code.isBlank()) {
                    config.code = null;
                }

                if (config.code != null) {
                    try {
                        config.script = ScriptController.instance.compileEntityEsp(config.code);
                    } catch (ParseException | ScriptCompileException e) {
                        e.printStackTrace();
                    }
                }
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
}