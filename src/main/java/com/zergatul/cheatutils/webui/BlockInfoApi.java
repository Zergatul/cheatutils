package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
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

            IFormattableTextComponent text = block.getName();
            if (text instanceof TranslationTextComponent) {
                name = I18n.get(((TranslationTextComponent) text).getKey());
            } else {
                name = id;
            }
        }
    }
}