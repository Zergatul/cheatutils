package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptController;
import com.zergatul.cheatutils.controllers.ScriptedBlockPlacerController;
import org.apache.http.HttpException;

public class ScriptedBlockPlacerCodeApi extends ApiBase {

    @Override
    public String getRoute() {
        return "scripted-block-placer-code";
    }

    @Override
    public String post(String code) throws HttpException {
        if (code == null || code.length() == 0) {
            ConfigStore.instance.getConfig().scriptedBlockPlacerConfig.code = null;
            ConfigStore.instance.requestWrite();
            ScriptedBlockPlacerController.instance.setScript(null);
            return "{ \"ok\": true }";
        }

        Runnable script;
        try {
            script = ScriptController.instance.compileBlockPlacer(code);
        }
        catch (Throwable e) {
            throw new HttpException(e.getMessage());
        }
        if (script != null) {
            ConfigStore.instance.getConfig().scriptedBlockPlacerConfig.code = code;
            ConfigStore.instance.requestWrite();
            ScriptedBlockPlacerController.instance.setScript(script);
        }
        return "{ \"ok\": true }";
    }
}