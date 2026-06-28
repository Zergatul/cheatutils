package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;

public class StatusOverlayCodeApi extends ApiBase {

    @Override
    public String getRoute() {
        return "status-overlay-code";
    }

    @Override
    public String post(String body) throws Throwable {
        String code = gson.fromJson(body, String.class);
        ScriptSaveResult result = ScriptWorkspace.INSTANCE.get(ScriptType.OVERLAY).save(code);
        if (result.isSuccess()) {
            return "{ \"ok\": true }";
        } else {
            return gson.toJson(result.getDiagnostics());
        }
    }
}