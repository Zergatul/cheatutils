package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.common.RegistryExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockColorApi extends ApiBase {

    @Override
    public String getRoute() {
        return "block-color";
    }

    @Override
    public String get(String id) throws ApiException {
        Block block = RegistryExtensions.safeParse(BuiltInRegistries.BLOCK, id);
        if (block == null) {
            throw new ApiException("Cannot find block by id.", HttpResponseCodes.NOT_FOUND);
        }

        BlockState state = block.defaultBlockState();
        BlockTintSource tintSource = Minecraft.getInstance().getBlockColors().getTintSource(block.defaultBlockState(), 0);
        int color = tintSource != null ? tintSource.color(state) : -1;
        return Integer.toString(color);
    }
}