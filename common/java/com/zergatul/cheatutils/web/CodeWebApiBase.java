package com.zergatul.cheatutils.web;

import com.google.gson.Gson;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveRejectedException;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import org.jspecify.annotations.Nullable;

public abstract class CodeWebApiBase extends WebApiBase {

    @Override
    public String post(String code) {
        code = gson.fromJson(code, String.class);
        return saveScript(gson, new ScriptRef(getScriptType()), code);
    }

    protected abstract ScriptType getScriptType();

    /** Saves a script and formats the response shared by all code save endpoints. */
    public static String saveScript(Gson gson, ScriptRef ref, @Nullable String code) {
        ScriptSaveResult result;
        try {
            result = ScriptWorkspace.INSTANCE.save(ref, code);
        } catch (ScriptSaveRejectedException e) {
            throw new ApiException(e.getMessage(), HttpResponseCodes.BAD_REQUEST);
        }

        if (result.isSuccess()) {
            return "{ \"ok\": true }";
        } else {
            return gson.toJson(result.getDiagnostics());
        }
    }
}
