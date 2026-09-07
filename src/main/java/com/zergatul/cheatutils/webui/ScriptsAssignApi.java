package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.scripting.KeyBindings;

public class ScriptsAssignApi extends ApiBase {

    public String getRoute() {
        return "keybinding-scripts-assign";
    }

    public String put(String name, String body) {
        com.google.gson.JsonElement value = new com.google.gson.JsonParser().parse(body);
        if (!value.isJsonPrimitive() || !value.getAsString().matches("-?\\d+")) {
            throw new IllegalArgumentException("Key index must be an integer.");
        }
        KeyBindings.INSTANCE.assign(Integer.parseInt(value.getAsString()), name);
        return "true";
    }
}