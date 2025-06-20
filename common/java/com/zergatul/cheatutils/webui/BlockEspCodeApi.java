package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.BlockEspConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.scripting.compiler.CompilationResult;
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

        if (request.code == null || request.code.isBlank()) {
            TickEndExecutor.instance.execute(() -> {
                config.code = null;
                config.script = null;
                ConfigStore.instance.requestWrite();
            });
            return "{ \"ok\": true }";
        }

        CompilationResult result = ScriptsController.instance.compileBlockEsp(request.code);
        if (result.getProgram() != null) {
            TickEndExecutor.instance.execute(() -> {
                config.code = request.code;
                config.script = result.getProgram();
                ConfigStore.instance.requestWrite();
            });
            return "{ \"ok\": true }";
        } else {
            return gson.toJson(result.getDiagnostics());
        }
    }

    public record Request(Block block, String code) {}
}