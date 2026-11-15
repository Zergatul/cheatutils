package com.zergatul.cheatutils.modules.scripting.keybindings.web;

import com.zergatul.cheatutils.modules.scripting.keybindings.KeyBindings;
import com.zergatul.cheatutils.web.ApiException;
import com.zergatul.cheatutils.web.HttpResponseCodes;
import com.zergatul.cheatutils.web.WebApiBase;

public class CodeWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "keybindings-code";
    }

    @Override
    public String put(String id, String body) throws Throwable {
        if (!id.startsWith("~")) {
            throw new ApiException("Invalid identifier.", HttpResponseCodes.BAD_REQUEST);
        }

        String name = id.substring(1);
        String code = gson.fromJson(body, String.class);
        return KeyBindings.INSTANCE.saveCode(name, code).match(
                _ -> "{ \"ok\": true }",
                diagnostics -> gson.toJson(diagnostics),
                error -> gson.toJson(new ErrorResponse(error.error())));
    }

    public record ErrorResponse(String error) {}
}