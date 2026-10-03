package com.zergatul.cheatutils.web;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.Sanitizable;

public abstract class SimpleModuleConfigWebApi<T> extends WebApiBase {

    private final String route;
    private final Class<T> clazz;

    public SimpleModuleConfigWebApi(String route, Class<T> clazz) {
        this.route = route;
        this.clazz = clazz;
    }

    @Override
    public String getRoute() {
        return route;
    }

    @Override
    public String get() {
        return gson.toJson(getConfig());
    }

    @Override
    public String post(String body) {
        T config = gson.fromJson(body, clazz);
        if (config != null) {
            if (config instanceof Sanitizable sanitizable) {
                sanitizable.sanitize();
            }
            setConfig(config);
            ConfigStore.instance.requestWrite();
        }
        return get();
    }

    protected abstract T getConfig();
    protected abstract void setConfig(T config);
}