package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.services.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.services.ScriptWorkspaceService;

public class StatusOverlayCodeApi extends ApiBase {

    @Override
    public String getRoute() {
        return "status-overlay-code";
    }

    @Override
    public String post(String body) throws Throwable {
        String code = gson.fromJson(body, String.class);
        ScriptSaveResult result = ScriptWorkspaceService.INSTANCE.get(ScriptType.OVERLAY).save(code);
        if (result.isSuccess()) {
            return "{ \"ok\": true }";
        } else {
            return gson.toJson(result.getDiagnostics());
        }
    }
}