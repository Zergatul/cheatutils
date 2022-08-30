package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.apache.http.MethodNotSupportedException;

public class BlockColorApi extends ApiBase {

    @Override
    public String getRoute() {
        return "block-color";
    }

    @Override
    public String get(String id) throws MethodNotSupportedException {
        Block block = ModApiWrapper.BLOCKS.getValue(new Identifier(id));
        int color = MinecraftClient.getInstance().getBlockColors().getColor(block.getDefaultState(), null, null, 0);
        return Integer.toString(color);
    }
}