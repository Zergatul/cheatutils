package com.zergatul.cheatutils.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A stable handle to an immutable value in the active profile.
 * Callbacks run synchronously under the store lock and must schedule thread-owned work without waiting for it.
 */
public final class SettingHandle2<T extends ImmutableConfig> {

    private final Object lock;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final Runnable requestWrite;
    private final List<BiConsumer<T, T>> listeners = new ArrayList<>();

    SettingHandle2(Object lock, Supplier<T> getter, Consumer<T> setter, Runnable requestWrite) {
        this.lock = lock;
        this.getter = getter;
        this.setter = setter;
        this.requestWrite = requestWrite;
    }

    public T get() {
        synchronized (lock) {
            return getter.get();
        }
    }

    public void set(T config) {
        Objects.requireNonNull(config);
        update(_ -> config);
    }

    public void update(UnaryOperator<T> update) {
        synchronized (lock) {
            T previous = getter.get();
            T current = Objects.requireNonNull(update.apply(previous));
            if (previous.equals(current)) {
                return;
            }

            Runnable notification = prepareNotification(previous, current);
            setter.accept(current);
            try {
                notification.run();
            } finally {
                requestWrite.run();
            }
        }
    }

    /** Registers a change listener without invoking it. Initial runtime setup belongs to the module. */
    public void onChanged(BiConsumer<T, T> listener) {
        synchronized (lock) {
            listeners.add(Objects.requireNonNull(listener));
        }
    }

    // Called under the shared store lock, before publishing a replacement profile.
    Runnable prepareNotification(T previous, T current) {
        Objects.requireNonNull(current);
        if (previous.equals(current)) {
            return () -> {};
        }

        List<BiConsumer<T, T>> snapshot = List.copyOf(listeners);
        return () -> {
            for (BiConsumer<T, T> listener : snapshot) {
                listener.accept(previous, current);
            }
        };
    }
}