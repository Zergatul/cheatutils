package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptSlot;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.MultiScriptSlot;

public class ScriptTypesApi extends ApiBase {

    @Override
    public String getRoute() {
        return "script-types";
    }

    @Override
    public String get() {
        ScriptWorkspace workspace = ScriptWorkspace.INSTANCE;
        return gson.toJson(workspace.getSupportedTypes().stream()
                .map(type -> describe(workspace, type))
                .toList());
    }

    private static Response describe(ScriptWorkspace workspace, ScriptType type) {
        ScriptSlot slot = workspace.get(type);
        return new Response(type.name(), type.getModuleName(), slot instanceof MultiScriptSlot);
    }

    public record Response(String type, String moduleName, boolean multiple) {}
}