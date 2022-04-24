package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import org.apache.http.MethodNotSupportedException;

public class AutoFishApi extends ApiBase {

    @Override
    public String getRoute() {
        return "auto-fish";
    }

    @Override
    public String get() throws MethodNotSupportedException {
        return Boolean.toString(ConfigStore.instance.autoFish);
    }

    @Override
    public String post(String body) throws MethodNotSupportedException {
        ConfigStore.instance.autoFish = Boolean.parseBoolean(body);
        ConfigStore.instance.write();
        return get();
    }

}
