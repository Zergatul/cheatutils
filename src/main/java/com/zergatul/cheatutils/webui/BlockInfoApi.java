package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableTextContent;
import org.apache.http.MethodNotSupportedException;

import java.util.Collection;

public class BlockInfoApi extends ApiBase {

    @Override
    public String getRoute() {
        return "block-info";
    }

    @Override
    public String get() throws MethodNotSupportedException {
        Collection<Block> blocks = ModApiWrapper.BLOCKS.getValues();
        Object[] result = blocks.stream().filter(b -> !b.equals(Blocks.AIR)).map(BlockInfo::new).toArray();
        return gson.toJson(result);
    }

    private static class BlockInfo {
        public String id;
        public String name;

        public BlockInfo(Block block) {
            id = ModApiWrapper.BLOCKS.getKey(block).toString();

            MutableText text = block.getName();
            if (text.getContent() instanceof TranslatableTextContent) {
                name = I18n.translate(((TranslatableTextContent) text.getContent()).getKey());
            } else {
                name = id;
            }
        }
    }
}
