package com.zergatul.cheatutils.configs;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Converts profiles to and from the JSON in profile files: one property per setting, named by its key. */
final class SettingsSerializer {

    private static final Logger LOGGER = LogManager.getLogger(SettingsSerializer.class);

    private final Gson gson;
    private final SettingsRegistry registry;

    SettingsSerializer(Gson gson, SettingsRegistry registry) {
        this.gson = gson;
        this.registry = registry;
    }

    JsonObject toJson(SettingsSnapshot snapshot) {
        JsonObject root = new JsonObject();
        for (SettingsRegistry.Entry<?> entry : registry.getEntries()) {
            root.add(entry.key(), gson.toJsonTree(snapshot.get(entry), entry.type()));
        }
        return root;
    }

    /** Expects migrated JSON. Each setting is read on its own: a broken one falls back to its defaults alone. */
    SettingsSnapshot fromJson(JsonObject root) {
        return registry.createSnapshot(entry -> read(root, entry));
    }

    private ImmutableConfig read(JsonObject root, SettingsRegistry.Entry<?> entry) {
        JsonElement element = root.get(entry.key());
        if (element == null || element.isJsonNull()) {
            return entry.createDefaults();
        }

        try {
            ImmutableConfig value = gson.fromJson(element, entry.type());
            if (value != null) {
                return value;
            }
        } catch (RuntimeException e) {
            LOGGER.error("Cannot read '{}' from config, using defaults", entry.key(), e);
        }

        return entry.createDefaults();
    }
}