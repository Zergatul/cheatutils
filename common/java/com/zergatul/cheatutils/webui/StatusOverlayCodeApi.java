package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import org.apache.http.HttpException;

public class StatusOverlayCodeApi extends ApiBase {

    @Override
    public String getRoute() {
        return "status-overlay-code";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String post(String body) throws HttpException {
        String code = WebHelper.parseJson(gson, body, String.class);
        ScriptSaveResult result = ClientThreadDispatcher.call(
                () -> ScriptWorkspace.INSTANCE.get(ScriptType.OVERLAY).save(code));
        if (result.isSuccess()) {
            ConfigStore.instance.requestWrite();
            return "{ \"ok\": true }";
        }
        return gson.toJson(result.getDiagnostics());
    }
}