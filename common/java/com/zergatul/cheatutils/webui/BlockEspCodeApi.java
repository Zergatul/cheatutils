package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public class BlockEspCodeApi extends ApiBase {

    @Override
    public String getRoute() {
        return "block-esp-code";
    }

    @Override
    public String post(String json) throws ApiException {
        Request request = gson.fromJson(json, Request.class);
        if (request.block == null) {
            return "{ \"error\": \"block is null\" }";
        }

        BlockEspConfig config = ConfigStore.instance.getConfig().blocks.find(request.block);
        if (config == null) {
            throw new ApiException("Cannot find block config.", HttpResponseCodes.NOT_FOUND);
        }

        String identifier = BuiltInRegistries.BLOCK.getKey(request.block).toString();
        ScriptSaveResult result = ScriptWorkspace.INSTANCE.get(ScriptType.BLOCK_ESP).save(identifier, request.code);
        if (result.isSuccess()) {
            return "{ \"ok\": true }";
        } else {
            return gson.toJson(result.getDiagnostics());
        }
    }

    public record Request(Block block, String code) {}
}