package com.zergatul.cheatutils.web;

import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.ImmutableConfig;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingWebApi<T> extends WebApiBase {

    private final String route;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final Class<T> clazz;

    public SettingWebApi(String route, SettingHandle<T> handle, Class<T> clazz) {
        this(route, handle::get, handle::set, clazz);
    }

    public static <T extends ImmutableConfig> SettingWebApi<T> immutable(String route, SettingHandle2<T> handle, Class<T> clazz) {
        return new SettingWebApi<>(route, handle::get, handle::set, clazz);
    }

    private SettingWebApi(String route, Supplier<T> getter, Consumer<T> setter, Class<T> clazz) {
        this.route = route;
        this.getter = getter;
        this.setter = setter;
        this.clazz = clazz;
    }

    @Override
    public String getRoute() {
        return route;
    }

    @Override
    public String get() {
        return gson.toJson(getter.get());
    }

    @Override
    public String post(String body) {
        T config = gson.fromJson(body, clazz);
        if (config != null) {
            setter.accept(config);
        }
        return get();
    }
}