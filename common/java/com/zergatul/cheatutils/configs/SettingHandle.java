package com.zergatul.cheatutils.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.*;

/**
 * A stable handle to an immutable value in the active profile. Any thread can read and update it.
 * Reads take no lock. Update operations and callbacks run synchronously under the store lock: keep them short,
 * never run user script code or wait for another thread inside them.
 */
public final class SettingHandle<T extends ImmutableConfig> {

    private final SettingsRegistry registry;
    private final SettingsRegistry.Entry<T> entry;
    private final Object lock;
    private final List<BiConsumer<T, T>> listeners = new ArrayList<>();
    private final List<SettingObserver<T>> observers = new ArrayList<>();

    SettingHandle(SettingsRegistry registry, SettingsRegistry.Entry<T> entry) {
        this.registry = registry;
        this.entry = entry;
        this.lock = registry.lock;
    }

    public Class<T> getType() {
        return entry.type();
    }

    public T get() {
        return registry.getSnapshot().get(entry);
    }

    public void set(T config) {
        Objects.requireNonNull(config);
        update(_ -> config);
    }

    public void update(UnaryOperator<T> operation) {
        synchronized (lock) {
            T previous = get();
            T current = Objects.requireNonNull(operation.apply(previous));
            if (previous.equals(current)) {
                return;
            }

            Runnable notification = prepareNotification(SettingNotificationReason.UPDATE, previous, current);
            registry.publish(entry, current);

            try {
                notification.run();
            } finally {
                registry.requestWrite.run();
            }
        }
    }

    public <R> R updateWithResult(Function<T, SettingUpdateResult<T, R>> operation) {
        synchronized (lock) {
            T previous = get();
            SettingUpdateResult<T, R> result = operation.apply(previous);
            T current = Objects.requireNonNull(result.config());
            if (previous.equals(current)) {
                return result.result();
            }

            Runnable notification = prepareNotification(SettingNotificationReason.UPDATE, previous, current);
            registry.publish(entry, current);

            try {
                notification.run();
            } finally {
                registry.requestWrite.run();
            }

            return result.result();
        }
    }

    /** Registers a change listener without invoking it. Initial runtime setup belongs to the module. */
    public void onChanged(BiConsumer<T, T> listener) {
        synchronized (lock) {
            listeners.add(Objects.requireNonNull(listener));
        }
    }

    public void observe(SettingObserver<T> observer) {
        synchronized (lock) {
            observers.add(Objects.requireNonNull(observer));
        }
    }

    // under the lock, before the registry publishes a replacement profile
    Runnable prepareNotification(SettingNotificationReason reason, SettingsSnapshot previous, SettingsSnapshot next) {
        return prepareNotification(reason, previous.get(entry), next.get(entry));
    }

    // under the lock
    void initialNotify(SettingsSnapshot snapshot) {
        T current = snapshot.get(entry);
        for (SettingObserver<T> observer : observers) {
            observer.notify(SettingNotificationReason.INITIAL, current, current);
        }
    }

    private Runnable prepareNotification(SettingNotificationReason reason, T previous, T current) {
        Objects.requireNonNull(current);
        if (previous.equals(current)) {
            if (observers.isEmpty() || reason == SettingNotificationReason.UPDATE) {
                return () -> {};
            }

            List<SettingObserver<T>> observersSnapshot = List.copyOf(observers);
            return () -> {
                for (SettingObserver<T> observer : observersSnapshot) {
                    observer.notify(reason, previous, current);
                }
            };
        }

        List<BiConsumer<T, T>> listenersSnapshot = List.copyOf(listeners);
        List<SettingObserver<T>> observersSnapshot = List.copyOf(observers);
        return () -> {
            for (BiConsumer<T, T> listener : listenersSnapshot) {
                listener.accept(previous, current);
            }
            for (SettingObserver<T> observer : observersSnapshot) {
                observer.notify(reason, previous, current);
            }
        };
    }
}