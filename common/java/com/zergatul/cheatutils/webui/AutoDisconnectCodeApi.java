package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptController;
import com.zergatul.cheatutils.modules.automation.AutoDisconnect;
import org.apache.http.HttpException;

public class AutoDisconnectCodeApi extends ApiBase {

    @Override
    public String getRoute() {
        return "auto-disconnect-code";
    }

    @Override
    public String post(String code) throws HttpException {
        if (code == null || code.length() == 0) {
            ConfigStore.instance.getConfig().autoDisconnectConfig.code = null;
            ConfigStore.instance.requestWrite();
            AutoDisconnect.instance.setScript(null);
            return "{ \"ok\": true }";
        }

        Runnable script;
        try {
            script = ScriptController.instance.compileAutoDisconnect(code);
        }
        catch (Throwable e) {
            throw new HttpException(e.getMessage());
        }
        if (script != null) {
            ConfigStore.instance.getConfig().autoDisconnectConfig.code = code;
            ConfigStore.instance.requestWrite();
            AutoDisconnect.instance.setScript(script);
        }
        return "{ \"ok\": true }";
    }
}