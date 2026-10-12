package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ModuleConfig;
import com.zergatul.cheatutils.configs.ModuleConfigSettingHandle;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

import java.util.function.Consumer;

public abstract class SettingHandleApi<T extends ModuleConfig> {

    private final ModuleConfigSettingHandle<T> handle;

    protected SettingHandleApi(ModuleConfigSettingHandle<T> handle) {
        this.handle = handle;
    }

    @MethodDescription("""
            Checks if module is enabled
            """)
    public boolean isEnabled() {
        return getConfig().enabled;
    }

    @MethodDescription("""
            Enables module
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void enable() {
        updateEnabled(true);
    }

    @MethodDescription("""
            Disables module
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void disable() {
        updateEnabled(false);
    }

    @MethodDescription("""
            Sets module enabled status
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void setEnabled(boolean value) {
        updateEnabled(value);
    }

    @MethodDescription("""
            Toggles module enabled state
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void toggle() {
        updateEnabled(!getConfig().enabled);
    }

    private void updateEnabled(boolean value) {
        handle.setEnabled(value);
    }

    protected final T getConfig() {
        return handle.get();
    }

    protected final void update(Consumer<T> consumer) {
        handle.update(consumer);
    }
}