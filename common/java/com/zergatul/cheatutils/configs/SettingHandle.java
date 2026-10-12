package com.zergatul.cheatutils.configs;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Applies a module's configuration consistently for web, scripting, and profile updates.
 * The supplier resolves the current profile; the handle does not retain a config instance.
 * Runtime callbacks remain responsible for scheduling work on their owning thread.
 */
public class SettingHandle<T> {

    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final Runnable onChanged;
    private final Runnable requestWrite;

    public SettingHandle(Supplier<T> getter, Consumer<T> setter) {
        this(getter, setter, () -> {}, ConfigStore.instance::requestWrite);
    }

    public SettingHandle(Supplier<T> getter, Consumer<T> setter, Runnable onChanged, Runnable requestWrite) {
        this.getter = getter;
        this.setter = setter;
        this.onChanged = onChanged;
        this.requestWrite = requestWrite;

        SettingsRegistry.INSTANCE.register(this);
    }

    public T get() {
        return getter.get();
    }

    public void set(T config) {
        apply(config, true);
    }

    /** Applies the current profile, including its runtime callback, without requesting a save. */
    public void reload() {
        apply(get(), false);
    }

    public void update(Consumer<T> consumer) {
        T config = get();
        consumer.accept(config);
        set(config);
    }

    protected void apply(T config, boolean persist) {
        Objects.requireNonNull(config);
        if (config instanceof Sanitizable sanitizable) {
            sanitizable.sanitize();
        }
        setter.accept(config);
        onChanged.run();
        if (persist) {
            requestWrite.run();
        }
    }
}