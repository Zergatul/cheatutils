package com.zergatul.cheatutils.modules.esp.entity.web;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.web.ApiException;
import com.zergatul.cheatutils.web.CodeWebApiBase;
import com.zergatul.cheatutils.web.HttpResponseCodes;
import com.zergatul.cheatutils.web.WebApiBase;

public class CodeWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "entity-esp-code";
    }

    @Override
    public String post(String json) {
        Request request = gson.fromJson(json, Request.class);
        if (request.type == null) {
            throw new ApiException("Entity type is required.", HttpResponseCodes.BAD_REQUEST);
        }

        return CodeWebApiBase.saveScript(gson, new ScriptRef(ScriptType.ENTITY_ESP, request.type.getName()), request.code);
    }

    public record Request(Class<?> type, String code) {}
}
