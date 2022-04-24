package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.JsonBlockTracerConfig;
import com.zergatul.cheatutils.controllers.BlockFinderController;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.http.MethodNotSupportedException;

public class BlocksConfigApi extends ApiBase {

    @Override
    public String getRoute() {
        return "blocks";
    }

    @Override
    public String get() {
        Object[] result;
        synchronized (ConfigStore.instance.blocks) {
            result = ConfigStore.instance.blocks.stream().map(BlockTracerConfig::convert).toArray();
        }
        return gson.toJson(result);
    }

    @Override
    public String post(String body) throws MethodNotSupportedException {

        JsonBlockTracerConfig jsonConfig = gson.fromJson(body, JsonBlockTracerConfig.class);

        ResourceLocation loc = new ResourceLocation(jsonConfig.blockId);
        Block block = ForgeRegistries.BLOCKS.getValue(loc);
        if (block == null) {
            throw new MethodNotSupportedException("Cannot find block.");
        }

        BlockTracerConfig config;
        synchronized (ConfigStore.instance.blocks) {

            config = ConfigStore.instance.blocks.stream().filter(c -> c.block.getRegistryName().equals(loc)).findFirst().orElse(null);
            if (config != null) {
                throw new MethodNotSupportedException("Block config already exists.");
            }

            config = BlockTracerConfig.createDefault(block);
            ConfigStore.instance.addBlock(config);
        }

        BlockFinderController.instance.scan(config);
        ConfigStore.instance.write();

        return gson.toJson(config.convert());
    }

    @Override
    public String put(String id, String body) throws MethodNotSupportedException {

        JsonBlockTracerConfig jsonConfig = gson.fromJson(body, JsonBlockTracerConfig.class);
        if (!id.equals(jsonConfig.blockId)) {
            throw new MethodNotSupportedException("Block ids don't match.");
        }

        ResourceLocation loc = new ResourceLocation(id);

        BlockTracerConfig config;
        synchronized (ConfigStore.instance.blocks) {
            config = ConfigStore.instance.blocks.stream().filter(c -> c.block.getRegistryName().equals(loc)).findFirst().orElse(null);
        }

        if (config == null) {
            throw new MethodNotSupportedException("Cannot find block config.");
        }

        config.copyFrom(jsonConfig);
        ConfigStore.instance.write();

        return gson.toJson(config.convert());
    }

    @Override
    public String delete(String id) throws MethodNotSupportedException {

        ResourceLocation loc = new ResourceLocation(id);

        synchronized (ConfigStore.instance.blocks) {
            BlockTracerConfig config = ConfigStore.instance.blocks.stream().filter(c -> c.block.getRegistryName().equals(loc)).findFirst().orElse(null);
            if (config == null) {
                throw new MethodNotSupportedException("Cannot find block config.");
            }

            ConfigStore.instance.removeBlock(config);
        }

        ConfigStore.instance.write();

        return "{ ok: true }";
    }

}
