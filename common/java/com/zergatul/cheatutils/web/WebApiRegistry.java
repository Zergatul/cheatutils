package com.zergatul.cheatutils.web;

import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.ImmutableConfig;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public final class WebApiRegistry {

    public static final WebApiRegistry INSTANCE = new WebApiRegistry();

    private final Map<String, WebApiBase> apis;

    private WebApiRegistry() {
        this.apis = new HashMap<>();
    }

    public synchronized @Nullable WebApiBase get(String route) {
        return apis.get(route);
    }

    public <T> void register(String route, SettingHandle<T> settings, Class<T> type) {
        SettingWebApi<T> api = new SettingWebApi<>(route, settings, type);
        register(api);
    }

    public <T extends ImmutableConfig> void register(String route, SettingHandle2<T> settings, Class<T> type) {
        register(SettingWebApi.immutable(route, settings, type));
    }

    public synchronized void register(WebApiBase api) {
        if (apis.containsKey(api.getRoute())) {
            throw new IllegalArgumentException(api.getRoute() + " is already registered.");
        }

        apis.put(api.getRoute(), api);
    }
}