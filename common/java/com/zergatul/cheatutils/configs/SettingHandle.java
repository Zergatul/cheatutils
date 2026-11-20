package com.zergatul.cheatutils.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.*;

/**
 * A stable handle to an immutable value in the active profile.
 * Callbacks run synchronously under the store lock and must schedule thread-owned work without waiting for it.
 */
public final class SettingHandle<T extends ImmutableConfig> {

    private final Object lock;
    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final Runnable requestWrite;
    private final List<BiConsumer<T, T>> listeners = new ArrayList<>();
    private final List<SettingObserver<T>> observers = new ArrayList<>();

    public SettingHandle(Object lock, Supplier<T> getter, Consumer<T> setter, Runnable requestWrite) {
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

    public void update(UnaryOperator<T> operation) {
        synchronized (lock) {
            T previous = getter.get();
            T current = Objects.requireNonNull(operation.apply(previous));
            if (previous.equals(current)) {
                return;
            }

            Runnable notification = prepareNotification(SettingNotificationReason.UPDATE, previous, current);
            setter.accept(current);

            try {
                notification.run();
            } finally {
                requestWrite.run();
            }
        }
    }

    public <R> R updateWithResult(Function<T, SettingUpdateResult<T, R>> operation) {
        synchronized (lock) {
            T previous = getter.get();
            SettingUpdateResult<T, R> result = operation.apply(previous);
            T current = result.config();
            if (previous.equals(current)) {
                return result.result();
            }

            Runnable notification = prepareNotification(SettingNotificationReason.UPDATE, previous, current);
            setter.accept(current);

            try {
                notification.run();
            } finally {
                requestWrite.run();
            }

            return result.result();
        }
    }

    /** Runs operation under shared store lock */
    public <R> R withLockedConfig(Function<T, R> operation) {
        synchronized (lock) {
            return operation.apply(getter.get());
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

    // Called under the shared store lock, before publishing a replacement profile.
    public Runnable prepareNotification(SettingNotificationReason reason, T previous, T current) {
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

    public void initialNotify(T current) {
        synchronized (lock) {
            for (SettingObserver<T> observer : observers) {
                observer.notify(SettingNotificationReason.INITIAL, current, current);
            }
        }
    }
}