package com.zergatul.cheatutils.web;

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

    public synchronized void register(WebApiBase api) {
        if (apis.containsKey(api.getRoute())) {
            throw new IllegalArgumentException(api.getRoute() + " is already registered.");
        }

        apis.put(api.getRoute(), api);
    }
}