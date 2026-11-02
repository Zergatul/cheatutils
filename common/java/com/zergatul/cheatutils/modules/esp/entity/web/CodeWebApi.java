package com.zergatul.cheatutils.modules.esp.entity.web;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.modules.EntityEspConfig;
import com.zergatul.cheatutils.modules.esp.entity.EntityEsp;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.web.ApiException;
import com.zergatul.cheatutils.web.HttpResponseCodes;
import com.zergatul.cheatutils.web.WebApiBase;

public class CodeWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "entity-esp-code";
    }

    @Override
    public String post(String json) throws ApiException {
        Request request = gson.fromJson(json, Request.class);

        SettingHandle2<EntityEspConfig> handle = EntityEsp.INSTANCE.getSettings();
        if (!handle.get().entries().contains(e -> e.type() == request.type)) {
            throw new ApiException("Cannot find entity config.", HttpResponseCodes.NOT_FOUND);
        }

        ScriptSaveResult result = ScriptWorkspace.INSTANCE.get(ScriptType.ENTITY_ESP).save(request.type.getName(), request.code);
        if (result.isSuccess()) {
            return "{ \"ok\": true }";
        } else {
            return gson.toJson(result.getDiagnostics());
        }
    }

    public record Request(Class<?> type, String code) {}
}