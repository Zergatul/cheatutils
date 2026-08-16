package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.BlockTracerConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.resources.ResourceLocation;
import org.apache.http.HttpException;
import org.apache.http.MethodNotSupportedException;

public class BlocksConfigApi extends ApiBase {

    @Override
    public String getRoute() {
        return "blocks";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public synchronized String get() {
        Object[] result;
        var list = ConfigStore.instance.getConfig().blocks.configs;
        result = list.stream().toArray();
        return gson.toJson(result);
    }

    @Override
    public synchronized String post(String body) throws HttpException {
        BlockTracerConfig jsonConfig = WebHelper.parseJson(gson, body, BlockTracerConfig.class);
        WebHelper.requireField(jsonConfig.block, "block");

        BlockTracerConfig config = ConfigStore.instance.getConfig().blocks.configs.stream()
                .filter(c -> c.block == jsonConfig.block)
                .findFirst()
                .orElse(null);
        if (config != null) {
            throw new MethodNotSupportedException("Block config already exists.");
        }

        config = BlockTracerConfig.createDefault(jsonConfig.block);
        BlockTracerConfig created = config;
        ConfigStore.updateFromApi(c -> c.blocks, blocks -> blocks.add(created));

        return gson.toJson(config);
    }

    @Override
    public synchronized String put(String id, String body) throws HttpException {
        BlockTracerConfig jsonConfig = WebHelper.parseJson(gson, body, BlockTracerConfig.class);
        WebHelper.requireField(jsonConfig.block, "block");
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

        ConfigStore.updateFromApi(c -> c.blocks, blocks -> config.copyFrom(jsonConfig));

        return gson.toJson(config);
    }

    @Override
    public synchronized String delete(String id) throws HttpException {
        ResourceLocation loc = ResourceLocation.tryParse(id);
        if (loc == null) {
            throw new ApiException("Invalid block id: " + id, HttpResponseCodes.BAD_REQUEST);
        }

        BlockTracerConfig config = ConfigStore.instance.getConfig().blocks.configs.stream()
                .filter(c -> Registries.BLOCKS.getKey(c.block).equals(loc))
                .findFirst()
                .orElse(null);
        if (config == null) {
            throw new MethodNotSupportedException("Cannot find block config.");
        }

        ConfigStore.updateFromApi(c -> c.blocks, blocks -> blocks.remove(config));

        return "{\"ok\":true}";
    }
}