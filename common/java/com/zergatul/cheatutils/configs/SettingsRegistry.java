package com.zergatul.cheatutils.configs;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Owns the active profile: one immutable value per registered setting type, stored in a {@link SettingsSnapshot}.
 * Snapshots are replaced, never changed, so reads take no lock and a captured snapshot can be saved as is.
 * Writers hold the store lock, which keeps updates in order with their notifications.
 * <p>
 * Settings are registered while the store is constructed, before any profile is loaded.
 * The registered list never changes after that.
 */
public final class SettingsRegistry {

    final Object lock;
    final Runnable requestWrite;
    private final List<Entry<?>> entries = new ArrayList<>();
    private final List<SettingHandle<?>> handles = new ArrayList<>();
    private final Map<Class<?>, SettingHandle<?>> handlesByType = new HashMap<>();
    private volatile SettingsSnapshot snapshot = new SettingsSnapshot(new Object[0]);

    public SettingsRegistry(Object lock, Runnable requestWrite) {
        this.lock = lock;
        this.requestWrite = requestWrite;
    }

    /**
     * Registers a setting. It is saved under the type name in camelCase: {@code FogConfig} is stored as
     * {@code "fogConfig"}. Setup only.
     */
    @SuppressWarnings("unchecked")
    public <T extends ImmutableConfig> SettingHandle<T> register(Supplier<T> defaults) {
        synchronized (lock) {
            T value = Objects.requireNonNull(defaults.get());
            // records are final, so the class of the default value is the setting type
            Class<T> type = (Class<T>) value.getClass();
            if (!type.isRecord()) {
                throw new IllegalArgumentException("Setting type must be a record: " + type.getName());
            }
            if (handlesByType.containsKey(type)) {
                throw new IllegalArgumentException("Setting is already registered: " + type.getName());
            }

            String name = type.getSimpleName();
            String key = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            if (entries.stream().anyMatch(e -> e.key().equals(key))) {
                throw new IllegalArgumentException("Setting key is already registered: " + key);
            }

            Entry<T> entry = new Entry<>(entries.size(), key, type, defaults);
            SettingHandle<T> handle = new SettingHandle<>(this, entry);
            entries.add(entry);
            handles.add(handle);
            handlesByType.put(type, handle);
            snapshot = snapshot.append(value);

            return handle;
        }
    }

    public List<Entry<?>> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<SettingHandle<?>> getHandles() {
        return Collections.unmodifiableList(handles);
    }

    @SuppressWarnings("unchecked")
    public <T extends ImmutableConfig> SettingHandle<T> find(Class<T> type) {
        SettingHandle<?> handle = handlesByType.get(type);
        if (handle == null) {
            throw new IllegalArgumentException("Settings are not registered for " + type.getName());
        }
        return (SettingHandle<T>) handle;
    }

    /** The active profile. Never changes, later updates publish a new snapshot. */
    public SettingsSnapshot getSnapshot() {
        return snapshot;
    }

    /** Builds a profile from one value per setting. Any thread. */
    public SettingsSnapshot createSnapshot(Function<Entry<?>, ? extends ImmutableConfig> read) {
        Object[] values = new Object[entries.size()];
        for (Entry<?> entry : entries) {
            values[entry.index()] = entry.type().cast(Objects.requireNonNull(read.apply(entry)));
        }
        return new SettingsSnapshot(values);
    }

    public SettingsSnapshot createDefaults() {
        return createSnapshot(Entry::createDefaults);
    }

    /**
     * Publishes the entire profile before notifying any listener; never requests a save.
     * Listeners run under the store lock, so they are ordered with concurrent updates of the same setting.
     */
    public void replace(SettingNotificationReason reason, SettingsSnapshot next) {
        checkSize(next);
        synchronized (lock) {
            SettingsSnapshot previous = snapshot;
            List<Runnable> notifications = new ArrayList<>(handles.size());
            for (SettingHandle<?> handle : handles) {
                notifications.add(handle.prepareNotification(reason, previous, next));
            }

            snapshot = next;

            for (Runnable notification : notifications) {
                notification.run();
            }
        }
    }

    public void replaceNoNotify(SettingsSnapshot next) {
        checkSize(next);
        synchronized (lock) {
            snapshot = next;
        }
    }

    public void initialNotify() {
        synchronized (lock) {
            SettingsSnapshot current = snapshot;
            for (SettingHandle<?> handle : handles) {
                handle.initialNotify(current);
            }
        }
    }

    // under the lock
    <T extends ImmutableConfig> void publish(Entry<T> entry, T value) {
        snapshot = snapshot.with(entry.index(), value);
    }

    private void checkSize(SettingsSnapshot next) {
        if (next.size() != entries.size()) {
            throw new IllegalArgumentException("Snapshot doesn't match registered settings.");
        }
    }

    public record Entry<T extends ImmutableConfig>(int index, String key, Class<T> type, Supplier<T> defaults) {
        public T createDefaults() {
            return Objects.requireNonNull(defaults.get());
        }
    }
}