package com.zergatul.cheatutils.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** Internal root ownership and bindings; modules only receive SettingHandle2 instances. */
final class SettingsRegistry2<R> {

    private final Object lock;
    private final Runnable requestWrite;
    private final List<Function<R, Runnable>> bindings = new ArrayList<>();
    private R config;

    SettingsRegistry2(Object lock, R config, Runnable requestWrite) {
        this.lock = lock;
        this.config = Objects.requireNonNull(config);
        this.requestWrite = requestWrite;
    }

    <T extends ImmutableConfig> SettingHandle2<T> register(Function<R, T> getter, BiConsumer<R, T> setter) {
        synchronized (lock) {
            SettingHandle2<T> handle = new SettingHandle2<>(
                    lock,
                    () -> getter.apply(config),
                    value -> setter.accept(config, value),
                    requestWrite);
            bindings.add(next -> handle.prepareNotification(getter.apply(config), getter.apply(next)));
            return handle;
        }
    }

    // Compatibility access for ConfigStore until the mutable modules are migrated.
    R getConfig() {
        synchronized (lock) {
            return config;
        }
    }

    /** Publishes the entire profile before notifying any listener; never requests a save. */
    void replace(R next) {
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
