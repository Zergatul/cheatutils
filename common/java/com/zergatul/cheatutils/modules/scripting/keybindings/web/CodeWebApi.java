package com.zergatul.cheatutils.modules.scripting.keybindings.web;

import com.zergatul.cheatutils.modules.scripting.keybindings.KeyBindings;
import com.zergatul.cheatutils.web.WebApiBase;

public class CodeWebApi extends WebApiBase {

    @Override
    public String getRoute() {
        return "keybindings-code";
    }

    @Override
    public String put(String id, String body) throws Throwable {
        String code = gson.fromJson(body, String.class);
        return KeyBindings.INSTANCE.saveCode(id, code).match(
                _ -> "{ \"ok\": true }",
                diagnostics -> gson.toJson(diagnostics),
                error -> gson.toJson(new ErrorResponse(error.error())));
    }

    public record ErrorResponse(String error) {}
}