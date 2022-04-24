package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.FullBrightController;
import org.apache.http.MethodNotSupportedException;

public class FullBrightApi extends ApiBase {

    @Override
    public String getRoute() {
        return "full-bright";
    }

    @Override
    public String get() throws MethodNotSupportedException {
        return Boolean.toString(ConfigStore.instance.fullBright);
    }

    @Override
    public String post(String body) throws MethodNotSupportedException {
        ConfigStore.instance.fullBright = Boolean.parseBoolean(body);
        FullBrightController.instance.apply();
        ConfigStore.instance.write();
        return get();
    }
}
