package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptController;
import com.zergatul.cheatutils.controllers.StatusOverlayController;
import org.apache.http.HttpException;

public class StatusOverlayApi extends ApiBase {

    @Override
    public String getRoute() {
        return "status-overlay-code";
    }

    @Override
    public String post(String code) throws HttpException {
        if (code == null || code.length() == 0) {
            ConfigStore.instance.getConfig().statusOverlayConfig.code = null;
            ConfigStore.instance.requestWrite();
            StatusOverlayController.instance.setScript(() -> {});
            return "{ \"ok\": true }";
        }

        Runnable script;
        try {
            script = ScriptController.instance.compileOverlay(code);
        }
        catch (Throwable e) {
            throw new HttpException(e.getMessage());
        }
        if (script != null) {
            ConfigStore.instance.getConfig().statusOverlayConfig.code = code;
            ConfigStore.instance.requestWrite();
            StatusOverlayController.instance.setScript(script);
        }
        return "{ \"ok\": true }";
    }
}