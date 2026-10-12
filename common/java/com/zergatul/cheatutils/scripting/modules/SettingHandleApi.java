package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ModuleConfig;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;

import java.util.function.Consumer;

public abstract class SettingHandleApi<T extends ModuleConfig> extends ModuleApi<T> {

    private final ModuleConfigSettingHandle<T> handle;

    protected SettingHandleApi(ModuleConfigSettingHandle<T> handle) {
        this.handle = handle;
    }

    @Override
    protected T getConfig() {
        return handle.get();
    }

    @Override
    protected void updateEnabled(boolean value) {
        handle.setEnabled(value);
    }

    protected void update(Consumer<T> consumer) {
        handle.update(consumer);
    }
}