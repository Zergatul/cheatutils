package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.GameTickScriptingController;
import com.zergatul.cheatutils.controllers.ScriptController;
import org.apache.http.HttpException;

public class GameTickScriptingCodeApi extends ApiBase {

    @Override
    public String getRoute() {
        return "game-tick-scripting-code";
    }

    @Override
    public String post(String code) throws HttpException {
        if (code == null || code.length() == 0) {
            ConfigStore.instance.getConfig().gameTickScriptingConfig.code = null;
            ConfigStore.instance.requestWrite();
            GameTickScriptingController.instance.setScript(() -> {});
            return "{ \"ok\": true }";
        }

        Runnable script;
        try {
            script = ScriptController.instance.compileKeys(code);
        }
        catch (Throwable e) {
            throw new HttpException(e.getMessage());
        }
        if (script != null) {
            ConfigStore.instance.getConfig().gameTickScriptingConfig.code = code;
            ConfigStore.instance.requestWrite();
            GameTickScriptingController.instance.setScript(script);
        }
        return "{ \"ok\": true }";
    }
}
