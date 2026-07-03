package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.services.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.services.ScriptWorkspaceService;

public abstract class CodeApiBase extends ApiBase {

    @Override
    public String post(String code) {
        code = gson.fromJson(code, String.class);

        ScriptSaveResult result = ScriptWorkspaceService.INSTANCE.get(getScriptType()).save(code);
        if (result.isSuccess()) {
            return "{ \"ok\": true }";
        } else {
            return gson.toJson(result.getDiagnostics());
        }
    }

    protected abstract ScriptType getScriptType();
}