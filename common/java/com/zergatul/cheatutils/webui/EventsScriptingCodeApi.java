package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptController;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import org.apache.http.HttpException;

public class EventsScriptingCodeApi extends ApiBase {

    @Override
    public String getRoute() {
        return "events-scripting-code";
    }

    @Override
    public String post(String code) throws HttpException {
        if (code == null || code.isEmpty()) {
            ConfigStore.instance.getConfig().eventsScriptingConfig.code = null;
            ConfigStore.instance.requestWrite();
            EventsScripting.instance.setScript(null);
            return "{ \"ok\": true }";
        }

        Runnable script;
        try {
            script = ScriptController.instance.compileEvents(code);
        } catch (Throwable e) {
            throw new HttpException(e.getMessage());
        }
        if (script != null) {
            ConfigStore.instance.getConfig().eventsScriptingConfig.code = code;
            ConfigStore.instance.requestWrite();
            EventsScripting.instance.setScript(script);
        }
        return "{ \"ok\": true }";
    }
}