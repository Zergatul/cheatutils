package com.zergatul.cheatutils.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.adapters.*;
import com.zergatul.cheatutils.configs.adapters.KillAuraConfig$PriorityEntryTypeAdapter;
import com.zergatul.cheatutils.controllers.KeyBindingsController;
import com.zergatul.cheatutils.controllers.ScriptController;
import com.zergatul.cheatutils.controllers.StatusOverlayController;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import com.zergatul.cheatutils.scripting.generated.ParseException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
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
            .registerTypeAdapterFactory(new BlockTypeAdapterFactory())
            .registerTypeAdapterFactory(new ItemTypeAdapterFactory())
            .registerTypeAdapter(Class.class, new ClassTypeAdapter())
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .registerTypeAdapter(ImmutableList.class, new ImmutableListSerializer())
            .registerTypeAdapter(KillAuraConfig.PriorityEntry.class, new KillAuraConfig$PriorityEntryTypeAdapter())
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
                readCfg = gson.fromJson(reader, Config.class);
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
        File configDir = new File(MinecraftClient.getInstance().runDirectory, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        return new File(configDir.getPath(), FILE);
    }

    private void onConfigLoaded() {
        //LightLevelController.instance.onChanged();
        config.blocks.apply();

        // TODO: use reflection to automatically find ValidatableConfig's?
        config.killAuraConfig.validate();
        config.movementHackConfig.validate();
        config.elytraHackConfig.validate();
        config.freeCamConfig.validate();
        config.flyHackConfig.validate();

        if (config.scriptsConfig.scripts.size() == 0) {
            final String toggleEspName = "Toggle ESP";
            try {
                ScriptController.instance.add(toggleEspName, "main.toggleEsp();");
                KeyBindingsController.instance.keys[0].setBoundKey(InputUtil.fromTranslationKey("key.keyboard.backslash"));
                KeyBindingsController.instance.assign(0, toggleEspName);
            } catch (ParseException | ScriptCompileException e) {
                e.printStackTrace();
            }

            final String toggleFreeCamName = "Toggle FreeCam";
            try {
                ScriptController.instance.add(toggleFreeCamName, "freeCam.toggle();");
                KeyBindingsController.instance.keys[1].setBoundKey(InputUtil.fromTranslationKey("key.keyboard.f6"));
                KeyBindingsController.instance.assign(1, toggleFreeCamName);
            } catch (ParseException | ScriptCompileException e) {
                e.printStackTrace();
            }
        } else {
            ArrayList<ScriptsConfig.ScriptEntry> copy = new ArrayList<>(config.scriptsConfig.scripts);
            config.scriptsConfig.scripts.clear();
            copy.forEach(s -> {
                try {
                    ScriptController.instance.add(s.name, s.code);
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
                StatusOverlayController.instance.setScript(script);
            } catch (ParseException | ScriptCompileException e) {
                e.printStackTrace();
            }
        }
    }
}