package com.zergatul.cheatutils.web;

import com.zergatul.cheatutils.configs.SettingHandle;

public class SettingWebApi<T> extends WebApiBase {

    private final String route;
    private final SettingHandle<T> handle;
    private final Class<T> clazz;

    public SettingWebApi(String route, SettingHandle<T> handle, Class<T> clazz) {
        this.route = route;
        this.handle = handle;
        this.clazz = clazz;
    }

    @Override
    public String getRoute() {
        return route;
    }

    @Override
    public String get() {
        return gson.toJson(handle.get());
    }

    @Override
    public String post(String body) {
        T config = gson.fromJson(body, clazz);
        if (config != null) {
            handle.set(config);
        }
        return get();
    }
}