package com.zergatul.cheatutils.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zergatul.cheatutils.configs.adapters.*;
import com.zergatul.cheatutils.configs.adapters.KillAuraConfig$PriorityEntryTypeAdapter;
import com.zergatul.cheatutils.controllers.LightLevelController;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.*;

public class ConfigStore {

    public static final ConfigStore instance = new ConfigStore();

    private static final String FILE = "zergatulcheatutils.json";
    private static long WRITE_FILE_DELAY = 15 * 1000000000L;

    public final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new BlockTypeAdapterFactory())
            .registerTypeAdapter(Class.class, new ClassTypeAdapter())
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
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

        onConfigChanged();
    }

    public void requestWrite() {
        lastWriteRequest = System.nanoTime();
        synchronized (writeEvent) {
            writeEvent.notify();
        }
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

    private void onConfigChanged() {
        LightLevelController.instance.onChanged();
        config.blocks.apply();
    }
}
