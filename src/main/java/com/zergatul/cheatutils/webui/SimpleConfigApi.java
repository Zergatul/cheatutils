package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import org.apache.http.MethodNotSupportedException;

public abstract class SimpleConfigApi<T> extends ApiBase {

    private final String route;
    private final Class<T> clazz;

    public SimpleConfigApi(String route, Class<T> clazz) {
        this.route = route;
        this.clazz = clazz;
    }

    @Override
    public String getRoute() {
        return route;
    }

    @Override
    public String get() throws MethodNotSupportedException {
        return gson.toJson(getConfig());
    }

    @Override
    public String post(String body) throws MethodNotSupportedException {
        T config = gson.fromJson(body, clazz);
        if (config != null) {
            setConfig(config);
            ConfigStore.instance.requestWrite();
        }
        return get();
    }

    protected abstract T getConfig();
    protected abstract void setConfig(T config);
}
