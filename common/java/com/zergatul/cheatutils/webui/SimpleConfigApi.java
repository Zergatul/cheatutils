package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
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
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String get() {
        return gson.toJson(getConfig());
    }

    @Override
    public String post(String body) throws ApiException {
        T config = WebHelper.parseJson(gson, body, clazz);
        ConfigStore.replaceFromApi(config, this::setConfig);
        return get();
    }

    protected abstract T getConfig();
    protected abstract void setConfig(T config);
}