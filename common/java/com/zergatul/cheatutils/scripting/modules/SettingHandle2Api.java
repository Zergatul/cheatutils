package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

import java.util.function.UnaryOperator;

public abstract class SettingHandle2Api<T extends ToggleableConfig<T> & ImmutableConfig> {

    private final SettingHandle2<T> handle;

    protected SettingHandle2Api(SettingHandle2<T> handle) {
        this.handle = handle;
    }

    @MethodDescription("""
            Checks if module is enabled
            """)
    public boolean isEnabled() {
        return handle.get().enabled();
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
        handle.update(config -> config.withEnabled(!config.enabled()));
    }

    private void updateEnabled(boolean value) {
        if (handle.get().enabled() != value) {
            handle.update(config -> config.withEnabled(value));
        }
    }

    protected final T getConfig() {
        return handle.get();
    }

    protected final void update(UnaryOperator<T> consumer) {
        handle.update(consumer);
    }
}