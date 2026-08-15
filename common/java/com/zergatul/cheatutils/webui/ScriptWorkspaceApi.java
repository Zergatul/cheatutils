package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.monaco.MonacoJson;
import com.zergatul.cheatutils.scripting.workspace.ScriptDocument;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.scripting.DiagnosticMessage;

import java.util.List;

public class ScriptWorkspaceApi extends ApiBase {

    @Override
    public String getRoute() {
        return "script-workspace";
    }

    @Override
    public String get() {
        return MonacoJson.toJson(ScriptWorkspace.INSTANCE.getAllInstances().stream()
                .map(ScriptWorkspaceApi::map)
                .toList());
    }

    @Override
    public String get(String id) throws ApiException {
        ScriptType type = parseType(id);
        return MonacoJson.toJson(ScriptWorkspace.INSTANCE.get(type).getInstances().stream()
                .map(ScriptWorkspaceApi::map)
                .toList());
    }

    private static ScriptType parseType(String value) throws ApiException {
        return WebHelper.parseEnum(ScriptType.class, value, "script type");
    }

    private static Response map(ScriptDocument document) {
        return new Response(
                document.ref.type().name(),
                document.ref.identifier(),
                document.code,
                document.lastAttemptCode,
                document.lastAttemptDiagnostics,
                document.lastAttemptAt == null ? null : document.lastAttemptAt.toString());
    }

    public record Response(
            String type,
            String identifier,
            String code,
            String lastAttemptCode,
            List<DiagnosticMessage> lastAttemptDiagnostics,
            String lastAttemptAt
    ) {}
}