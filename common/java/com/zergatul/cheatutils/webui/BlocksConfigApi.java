package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.BlocksConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.esp.BlockFinder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.apache.http.HttpException;

import java.util.ArrayList;
import java.util.List;

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
        return gson.toJson(ConfigStore.instance.getConfig().blocks.getBlockConfigs().stream().toArray());
    }

    @Override
    public synchronized String post(String body) throws HttpException {
        BlockEspConfig jsonConfig = WebHelper.parseJson(gson, body, BlockEspConfig.class);
        sanitizeRequest(jsonConfig);

        BlocksConfig blocksConfig = ConfigStore.instance.getConfig().blocks;
        BlockEspConfig config = blocksConfig.findExact(jsonConfig.blocks);
        if (config != null) {
            config.copyFrom(jsonConfig);
        } else {
            List<BlockEspConfig> overlapping = new ArrayList<>();
            for (Block block : jsonConfig.blocks) {
                config = blocksConfig.find(block);
                if (config != null && !overlapping.contains(config)) {
                    overlapping.add(config);
                }
            }

            if (overlapping.size() > 1) {
                throw new ApiException(
                        "Received BlockEspConfig with blocks from multiple existing groups.",
                        HttpResponseCodes.BAD_REQUEST);
            }

            if (overlapping.size() == 1) {
                config = overlapping.get(0);
                config.blocks = jsonConfig.blocks;
                config.copyFrom(jsonConfig);
                blocksConfig.refreshMap();
                BlockFinder.instance.removeConfig(config);
                BlockFinder.instance.addConfig(config);
            } else {
                config = BlockEspConfig.createDefault(jsonConfig.blocks);
                blocksConfig.add(config);
            }
        }

        ConfigStore.instance.requestWrite();
        return gson.toJson(config);
    }

    @Override
    public synchronized String delete(String id) throws HttpException {
        Block block = parseBlock(id);
        BlocksConfig blocksConfig = ConfigStore.instance.getConfig().blocks;
        BlockEspConfig config = blocksConfig.find(block);
        if (config == null) {
            throw new ApiException("Block ESP group does not exist.", HttpResponseCodes.BAD_REQUEST);
        }

        blocksConfig.remove(config);
        ConfigStore.instance.requestWrite();
        return "{\"ok\":true}";
    }

    private static void sanitizeRequest(BlockEspConfig config) throws ApiException {
        WebHelper.requireField(config.blocks, "blocks");
        config.sanitize();
        ImmutableList<Block> blocks = new ImmutableList<>();
        for (Block block : config.blocks) {
            if (block == null || block == Blocks.AIR) {
                throw new ApiException("Block ESP group contains an invalid block.", HttpResponseCodes.BAD_REQUEST);
            }
            if (!blocks.contains(block)) {
                blocks = blocks.add(block);
            }
        }
        if (blocks.isEmpty()) {
            throw new ApiException("Block ESP group cannot be empty.", HttpResponseCodes.BAD_REQUEST);
        }
        config.blocks = blocks;
    }

    private static Block parseBlock(String id) throws ApiException {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) {
            throw new ApiException("Invalid block id: " + id, HttpResponseCodes.BAD_REQUEST);
        }
        Block block = Registries.BLOCKS.getValue(location);
        if (block == null || block == Blocks.AIR && !location.equals(Registries.BLOCKS.getKey(Blocks.AIR))) {
            throw new ApiException("Cannot find block by id: " + id, HttpResponseCodes.BAD_REQUEST);
        }
        return block;
    }

    public static class Add extends ApiBase {

        @Override
        public String getRoute() {
            return "blocks-add";
        }

        @Override
        public boolean requiresJsonContentType() {
            return true;
        }

        @Override
        public synchronized String post(String body) throws HttpException {
            String id = WebHelper.parseJson(gson, body, String.class);
            Block block = parseBlock(id);
            BlocksConfig blocksConfig = ConfigStore.instance.getConfig().blocks;
            if (blocksConfig.find(block) != null) {
                throw new ApiException("Selected block already belongs to a Block ESP group.", HttpResponseCodes.BAD_REQUEST);
            }

            BlockEspConfig config = BlockEspConfig.createDefault(ImmutableList.from(block));
            blocksConfig.add(config);
            ConfigStore.instance.requestWrite();
            return gson.toJson(config);
        }
    }
}