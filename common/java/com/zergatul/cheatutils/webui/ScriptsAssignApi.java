package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.KeyBindingsController;
import org.apache.http.HttpException;

public class ScriptsAssignApi extends ApiBase {

    @Override
    public String getRoute() {
        return "scripts-assign";
    }

    @Override
    public String put(String id, String body) throws HttpException {
        int index = gson.fromJson(body, int.class);
        KeyBindingsController.instance.assign(index, id);
        ConfigStore.instance.requestWrite();
        return "true";
    }
}