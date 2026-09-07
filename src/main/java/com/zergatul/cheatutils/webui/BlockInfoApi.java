package com.zergatul.cheatutils.webui;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Collection;

public class BlockInfoApi extends ApiBase {

    @Override
    public String getRoute() {
        return "block-info";
    }

    @Override
    public String get() {
        Collection<Block> blocks = ForgeRegistries.BLOCKS.getValuesCollection();
        Object[] result = blocks.stream().filter(b -> !b.equals(Blocks.AIR)).map(BlockInfo::new).toArray();
        return gson.toJson(result);
    }

    public static class BlockInfo {

        public String id;
        public String name;

        public BlockInfo(Block block) {
            id = ForgeRegistries.BLOCKS.getKey(block).toString();
            name = block.getLocalizedName();
        }
    }
}