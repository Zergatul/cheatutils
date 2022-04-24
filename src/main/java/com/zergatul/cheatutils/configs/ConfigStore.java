package com.zergatul.cheatutils.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zergatul.cheatutils.controllers.BlockFinderController;
import com.zergatul.cheatutils.controllers.FullBrightController;
import com.zergatul.cheatutils.controllers.LightLevelController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConfigStore {

    public static final ConfigStore instance = new ConfigStore();

    private static final String FILE = "zergatulcheatutils.json";

    public boolean esp;
    public boolean fullBright;
    public boolean autoFish;
    public boolean holdUseKey;
    public final List<BlockTracerConfig> blocks = new ArrayList<>();
    public final List<EntityTracerConfig> entities = new ArrayList<>();
    public LightLevelConfig lightLevelConfig = new LightLevelConfig();

    private final File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private ConfigStore() {

        File configDir = new File(Minecraft.getInstance().gameDirectory, "config");

        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        file = new File(configDir.getPath(), FILE);
        read();
    }

    public void addBlock(BlockTracerConfig config) {
        synchronized (blocks) {
            blocks.add(config);
        }
        synchronized (BlockFinderController.instance.blocks) {
            BlockFinderController.instance.blocks.put(config.block.getRegistryName(), new HashSet<>());
        }
    }

    public void removeBlock(BlockTracerConfig config) {
        synchronized (blocks) {
            blocks.remove(config);
        }
        synchronized (BlockFinderController.instance.blocks) {
            BlockFinderController.instance.blocks.remove(config.block.getRegistryName());
        }
    }

    public void addEntity(EntityTracerConfig config) {
        synchronized (entities) {
            entities.add(config);
        }
    }

    public void removeEntity(EntityTracerConfig config) {
        synchronized (entities) {
            entities.remove(config);
        }
    }

    public void read() {

        if (file.exists()) {

            int size = blocks.size();
            for (int i = 0; i < size; i++) {
                removeBlock(blocks.get(0));
            }

            try {
                Type type = new TypeToken<JsonConfig>() {}.getType();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                JsonConfig jsonConfig = gson.fromJson(reader, type);

                esp = jsonConfig.esp;
                fullBright = jsonConfig.fullBright;
                autoFish = jsonConfig.autoFish;
                holdUseKey = jsonConfig.holdUseKey;
                FullBrightController.instance.apply(fullBright);

                if (jsonConfig.lightLevelConfig != null) {
                    lightLevelConfig = jsonConfig.lightLevelConfig;
                }
                LightLevelController.instance.setActive(lightLevelConfig.active);

                if (jsonConfig.blocks == null) {
                    setDefaultBlocks();
                } else {
                    for (JsonBlockTracerConfig config : jsonConfig.blocks) {
                        addBlock(config.convert());
                    }
                }

                if (jsonConfig.entities == null) {
                    setDefaultEntities();
                } else {
                    for (JsonEntityTracerConfig config : jsonConfig.entities) {
                        addEntity(config.convert());
                    }
                }

                reader.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            esp = true;
            fullBright = false;
            autoFish = false;
            holdUseKey = false;
            setDefaultBlocks();
        }

    }

    public JsonConfig toJson() {
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.esp = esp;
        jsonConfig.fullBright = fullBright;
        jsonConfig.autoFish = autoFish;
        jsonConfig.holdUseKey = holdUseKey;
        jsonConfig.blocks = new ArrayList<>();
        jsonConfig.entities = new ArrayList<>();
        synchronized (blocks) {
            for (BlockTracerConfig blockConfig : blocks) {
                jsonConfig.blocks.add(blockConfig.convert());
            }
        }
        synchronized (entities) {
            for (EntityTracerConfig entityConfig : entities) {
                jsonConfig.entities.add(entityConfig.convert());
            }
        }
        return jsonConfig;
    }

    public void write() {
        JsonConfig jsonConfig = toJson();
        try {
            synchronized (this) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                gson.toJson(jsonConfig, writer);
                writer.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setDefaultBlocks() {
        addBlock(BlockTracerConfig.createDefault(Blocks.CHEST));
    }

    private void setDefaultEntities() {
        addEntity(EntityTracerConfig.createDefault(Player.class));
    }
}
