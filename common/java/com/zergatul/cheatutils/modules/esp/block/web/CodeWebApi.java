package com.zergatul.cheatutils.modules.esp.block.web;

import com.zergatul.cheatutils.configs.modules.BlockEspEntry;
import com.zergatul.cheatutils.modules.esp.block.BlockEsp;
import com.zergatul.cheatutils.modules.esp.block.BlockGroupKey;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.web.ApiException;
import com.zergatul.cheatutils.web.HttpResponseCodes;
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

        return BlockEsp.INSTANCE.getSettings().withLockedConfig(config -> {
            BlockGroupKey key = new BlockGroupKey(request.blocks);
            BlockEspEntry entry = key.findIn(config.entries());
            if (entry == null) {
                throw new ApiException("Cannot find block ESP entry.", HttpResponseCodes.BAD_REQUEST);
            }

            ScriptSaveResult result = ScriptWorkspace.INSTANCE.get(ScriptType.BLOCK_ESP).save(key.toString(), request.code);
            if (result.isSuccess()) {
                return "{ \"ok\": true }";
            } else {
                return gson.toJson(result.getDiagnostics());
            }
        });
    }

    public record Request(List<Block> blocks, String code) {}
}