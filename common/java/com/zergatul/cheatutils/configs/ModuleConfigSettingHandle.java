package com.zergatul.cheatutils.configs;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModuleConfigSettingHandle<T extends ModuleConfig> extends SettingHandle<T> {

    public ModuleConfigSettingHandle(Supplier<T> getter, Consumer<T> setter) {
        super(getter, setter);
    }

    public ModuleConfigSettingHandle(Supplier<T> getter, Consumer<T> setter, BiConsumer<T, T> onChanged) {
        this(getter, setter, onChanged, ConfigStore.instance::requestWrite);
    }

    public ModuleConfigSettingHandle(Supplier<T> getter, Consumer<T> setter, BiConsumer<T, T> onChanged, Runnable requestWrite) {
        super(getter, setter, onChanged, requestWrite);
    }

    public void setEnabled(boolean enabled) {
        T config = get();
        if (config.enabled != enabled) {
            config.enabled = enabled;
            apply(config, true);
        }
    }
}