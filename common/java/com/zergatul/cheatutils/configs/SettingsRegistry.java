package com.zergatul.cheatutils.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** Internal root ownership and bindings; modules only receive SettingHandle2 instances. */
public final class SettingsRegistry<R> {

    private final Object lock;
    private final Runnable requestWrite;
    private final List<Function<R, Runnable>> bindings = new ArrayList<>();
    private R config;

    public SettingsRegistry(Object lock, R config, Runnable requestWrite) {
        this.lock = lock;
        this.config = Objects.requireNonNull(config);
        this.requestWrite = requestWrite;
    }

    public <T extends ImmutableConfig> SettingHandle<T> register(Function<R, T> getter, BiConsumer<R, T> setter) {
        synchronized (lock) {
            SettingHandle<T> handle = new SettingHandle<>(
                    lock,
                    () -> getter.apply(config),
                    value -> setter.accept(config, value),
                    requestWrite);
            bindings.add(next -> handle.prepareNotification(getter.apply(config), getter.apply(next)));
            return handle;
        }
    }

    // Compatibility access for ConfigStore until the mutable modules are migrated.
    public R getConfig() {
        synchronized (lock) {
            return config;
        }
    }

    /** Publishes the entire profile before notifying any listener; never requests a save. */
    public void replace(R next) {
        synchronized (lock) {
            Objects.requireNonNull(next);
            List<Runnable> notifications = new ArrayList<>();
            for (Function<R, Runnable> binding : bindings) {
                notifications.add(binding.apply(next));
            }

            config = next;
            for (Runnable notification : notifications) {
                notification.run();
            }
        }
    }
}