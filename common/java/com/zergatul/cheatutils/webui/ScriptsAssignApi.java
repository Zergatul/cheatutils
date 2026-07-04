package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;

public class ScriptsAssignApi extends ApiBase {

    @Override
    public String getRoute() {
        return "keybinding-scripts-assign";
    }

    @Override
    public String put(String id, String body) {
        int index = gson.fromJson(body, int.class);
        KeyBindings.instance.assign(index, id);
        ConfigStore.instance.requestWrite();
        return "true";
    }
}