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
    public String post(String json) throws ApiException {
        Request request = gson.fromJson(json, Request.class);
        if (request.blocks == null) {
            return "{ \"error\": \"blocks is null\" }";
        }

        String[] errorHolder = new String[1];
        String[] resultHolder = new String[1];

        BlockEsp.INSTANCE.getSettings().update(config -> {
            BlockGroupKey key = new BlockGroupKey(request.blocks);
            BlockEspEntry entry = key.findIn(config.entries());
            if (entry == null) {
                errorHolder[0] = "Cannot find block ESP entry.";
                return config;
            }

            ScriptSaveResult result = ScriptWorkspace.INSTANCE.get(ScriptType.BLOCK_ESP).save(key.toString(), request.code);
            if (result.isSuccess()) {
                resultHolder[0] = "{ \"ok\": true }";
            } else {
                resultHolder[0] = gson.toJson(result.getDiagnostics());
            }

            return config;
        });

        if (errorHolder[0] != null) {
            throw new ApiException(errorHolder[0], HttpResponseCodes.BAD_REQUEST);
        }

        return resultHolder[0];
    }

    public record Request(List<Block> blocks, String code) {}
}