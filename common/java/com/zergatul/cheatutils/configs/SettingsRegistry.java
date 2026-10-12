package com.zergatul.cheatutils.configs;

import java.util.ArrayList;
import java.util.List;

public class SettingsRegistry {

    public static final SettingsRegistry INSTANCE = new SettingsRegistry();

    private final List<SettingHandle<?>> handles;

    private SettingsRegistry() {
        this.handles = new ArrayList<>();
    }

    public void reload() {
        for (SettingHandle<?> handle : handles) {
            handle.reload();
        }
    }

    public void register(SettingHandle<?> handle) {
        handles.add(handle);
    }
}