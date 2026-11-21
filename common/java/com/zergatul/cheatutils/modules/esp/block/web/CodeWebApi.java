package com.zergatul.cheatutils.modules.esp.block.web;

import com.zergatul.cheatutils.modules.esp.block.BlockGroupKey;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.web.CodeWebApiBase;
import com.zergatul.cheatutils.web.WebApiBase;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class CodeWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "block-esp-code";
    }

    @Override
    public String post(String json) {
        Request request = gson.fromJson(json, Request.class);
        if (request.blocks == null) {
            return "{ \"error\": \"blocks is null\" }";
        }

        BlockGroupKey key = new BlockGroupKey(request.blocks);
        return CodeWebApiBase.saveScript(gson, new ScriptRef(ScriptType.BLOCK_ESP, key.toString()), request.code);
    }

    public record Request(List<Block> blocks, String code) {}
}
