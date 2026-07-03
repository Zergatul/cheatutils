package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;

public abstract class CodeApiBase extends ApiBase {

    @Override
    public String post(String code) {
        code = gson.fromJson(code, String.class);

        ScriptSaveResult result = ScriptWorkspace.INSTANCE.get(getScriptType()).save(code);
        if (result.isSuccess()) {
            return "{ \"ok\": true }";
        } else {
            return gson.toJson(result.getDiagnostics());
        }
    }

    protected abstract ScriptType getScriptType();
}