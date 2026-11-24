package com.zergatul.cheatutils.configs;

import java.util.Arrays;

/**
 * The values of all settings in one profile, one slot per registered setting. Never changes after construction,
 * so any thread can read or serialize a snapshot without locking. Updates create a new snapshot.
 */
public final class SettingsSnapshot {

    private final Object[] values;

    // takes ownership of the array
    SettingsSnapshot(Object[] values) {
        this.values = values;
    }

    @SuppressWarnings("unchecked")
    public <T extends ImmutableConfig> T get(SettingsRegistry.Entry<T> entry) {
        return (T) values[entry.index()];
    }

    SettingsSnapshot with(int index, Object value) {
        Object[] copy = values.clone();
        copy[index] = value;
        return new SettingsSnapshot(copy);
    }

    SettingsSnapshot append(Object value) {
        Object[] copy = Arrays.copyOf(values, values.length + 1);
        copy[values.length] = value;
        return new SettingsSnapshot(copy);
    }

    int size() {
        return values.length;
    }
}