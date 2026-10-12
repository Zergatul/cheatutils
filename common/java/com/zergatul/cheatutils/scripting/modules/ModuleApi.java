package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ModuleConfig;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.scripting.MethodDescription;

public abstract class ModuleApi<T extends ModuleConfig> {

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

    protected void updateEnabled(boolean value) {
        T config = getConfig();
        if (config.enabled != value) {
            config.enabled = value;
            onEnableChanged();
            ConfigStore.instance.requestWrite();
        }
    }

    protected void onEnableChanged() {}

    protected abstract T getConfig();
}