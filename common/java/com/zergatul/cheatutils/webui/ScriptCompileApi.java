package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.monaco.MonacoJson;
import com.zergatul.cheatutils.scripting.workspace.ScriptCompileResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.scripting.DiagnosticMessage;

import java.util.List;

public class ScriptCompileApi extends ApiBase {

    @Override
    public String getRoute() {
        return "script-compile";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String post(String body) throws ApiException {
        Request request = WebHelper.parseJson(gson, body, Request.class);
        if (request.code == null) {
            throw new ApiException("Code is required", HttpResponseCodes.BAD_REQUEST);
        }

        ScriptType type = WebHelper.parseEnum(ScriptType.class, request.type, "script type");

        ScriptCompileResult result = ScriptWorkspace.INSTANCE.get(type).compile(request.code);
        return MonacoJson.toJson(new Response(result.isSuccess(), result.getDiagnostics()));
    }

    public static class Request {
        public String type;
        public String code;
    }

    public record Response(boolean ok, List<DiagnosticMessage> diagnostics) {}
}