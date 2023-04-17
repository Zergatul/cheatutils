package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.resources.ResourceLocation;
import org.apache.http.MethodNotSupportedException;

public class BlocksConfigApi extends ApiBase {

    @Override
    public String getRoute() {
        return "blocks";
    }

    @Override
    public synchronized String get() {
        Object[] result;
        var list = ConfigStore.instance.getConfig().blocks.configs;
        result = list.stream().toArray();
        return gson.toJson(result);
    }

    @Override
    public synchronized String post(String body) throws MethodNotSupportedException {
        BlockTracerConfig jsonConfig = gson.fromJson(body, BlockTracerConfig.class);

        BlockTracerConfig config = ConfigStore.instance.getConfig().blocks.configs.stream()
                .filter(c -> c.block == jsonConfig.block)
                .findFirst()
                .orElse(null);
        if (config != null) {
            throw new MethodNotSupportedException("Block config already exists.");
        }

        config = BlockTracerConfig.createDefault(jsonConfig.block);
        ConfigStore.instance.getConfig().blocks.add(config);
        ConfigStore.instance.requestWrite();

        return gson.toJson(config);
    }

    @Override
    public synchronized String put(String id, String body) throws MethodNotSupportedException {
        BlockTracerConfig jsonConfig = gson.fromJson(body, BlockTracerConfig.class);
        if (!id.equals(Registries.BLOCKS.getKey(jsonConfig.block).toString())) {
            throw new MethodNotSupportedException("Block ids don't match.");
        }

        BlockTracerConfig config = ConfigStore.instance.getConfig().blocks.configs.stream()
                .filter(c -> c.block == jsonConfig.block)
                .findFirst()
                .orElse(null);

        if (config == null) {
            throw new MethodNotSupportedException("Cannot find block config.");
        }

        config.copyFrom(jsonConfig);
        ConfigStore.instance.requestWrite();

        return gson.toJson(config);
    }

    @Override
    public synchronized String delete(String id) throws MethodNotSupportedException {
        ResourceLocation loc = new ResourceLocation(id);

        BlockTracerConfig config = ConfigStore.instance.getConfig().blocks.configs.stream()
                .filter(c -> Registries.BLOCKS.getKey(c.block).equals(loc))
                .findFirst()
                .orElse(null);
        if (config == null) {
            throw new MethodNotSupportedException("Cannot find block config.");
        }

        ConfigStore.instance.getConfig().blocks.remove(config);
        ConfigStore.instance.requestWrite();

        return "{ ok: true }";
    }
}