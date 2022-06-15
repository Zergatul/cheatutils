package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.BlockFinderController;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.apache.http.MethodNotSupportedException;

public class BlocksConfigApi extends ApiBase {

    @Override
    public String getRoute() {
        return "blocks";
    }

    @Override
    public String get() {
        Object[] result;
        var list = ConfigStore.instance.getConfig().blocks.configs;
        synchronized (list) {
            result = list.toArray();
        }
        return gson.toJson(result);
    }

    @Override
    public String post(String body) throws MethodNotSupportedException {

        BlockTracerConfig jsonConfig = gson.fromJson(body, BlockTracerConfig.class);

        BlockTracerConfig config;
        var list = ConfigStore.instance.getConfig().blocks.configs;
        synchronized (list) {

            config = list.stream().filter(c -> c.block == jsonConfig.block).findFirst().orElse(null);
            if (config != null) {
                throw new MethodNotSupportedException("Block config already exists.");
            }

            config = BlockTracerConfig.createDefault(jsonConfig.block);
            ConfigStore.instance.getConfig().blocks.add(config);
        }

        BlockFinderController.instance.scan(config);
        ConfigStore.instance.requestWrite();

        return gson.toJson(config);
    }

    @Override
    public String put(String id, String body) throws MethodNotSupportedException {

        BlockTracerConfig jsonConfig = gson.fromJson(body, BlockTracerConfig.class);
        if (!id.equals(Registry.BLOCK.getKey(jsonConfig.block).toString())) {
            throw new MethodNotSupportedException("Block ids don't match.");
        }

        BlockTracerConfig config;
        var list = ConfigStore.instance.getConfig().blocks.configs;
        synchronized (list) {
            config = list.stream().filter(c -> c.block == jsonConfig.block).findFirst().orElse(null);
        }

        if (config == null) {
            throw new MethodNotSupportedException("Cannot find block config.");
        }

        config.copyFrom(jsonConfig);
        ConfigStore.instance.requestWrite();

        return gson.toJson(config);
    }

    @Override
    public String delete(String id) throws MethodNotSupportedException {

        ResourceLocation loc = new ResourceLocation(id);

        var list = ConfigStore.instance.getConfig().blocks.configs;
        synchronized (list) {
            BlockTracerConfig config = list.stream().filter(c -> Registry.BLOCK.getKey(c.block).equals(loc)).findFirst().orElse(null);
            if (config == null) {
                throw new MethodNotSupportedException("Cannot find block config.");
            }

            ConfigStore.instance.getConfig().blocks.remove(config);
        }

        ConfigStore.instance.requestWrite();

        return "{ ok: true }";
    }

}
