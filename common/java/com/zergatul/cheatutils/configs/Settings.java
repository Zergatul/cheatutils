package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.configs.modules.AirPlaceConfig;
import com.zergatul.cheatutils.configs.modules.EntityTitleConfig;
import com.zergatul.cheatutils.configs.modules.FlyHackConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** Typed settings catalog. Only this class knows where module values live in the root config. */
public final class Settings {

    private final Map<Class<? extends ImmutableConfig>, SettingHandle2<?>> handles;

    Settings(SettingsRegistry2<Config> registry) {
        Map<Class<? extends ImmutableConfig>, SettingHandle2<?>> handles = new HashMap<>();
        Registrar registrar = new Registrar(registry, handles);

        registrar.register(
                AirPlaceConfig.class,
                config -> config.airPlaceConfig,
                (config, value) -> config.airPlaceConfig = value);

        registrar.register(
                EntityTitleConfig.class,
                config -> config.entityTitleConfig,
                (config, value) -> config.entityTitleConfig = value);

        registrar.register(
                FlyHackConfig.class,
                config -> config.flyHackConfig,
                (config, value) -> config.flyHackConfig = value);

        this.handles = Collections.unmodifiableMap(handles);
    }

    public static <T extends ImmutableConfig> SettingHandle2<T> get(Class<T> type) {
        return ConfigStore.instance.getSettings().find(type);
    }

    @SuppressWarnings("unchecked")
    private <T extends ImmutableConfig> SettingHandle2<T> find(Class<T> type) {
        SettingHandle2<?> handle = handles.get(type);
        if (handle == null) {
            throw new IllegalArgumentException("Settings are not registered for " + type.getName());
        }
        return (SettingHandle2<T>) handle;
    }

    private static final class Registrar {

        private final SettingsRegistry2<Config> registry;
        private final Map<Class<? extends ImmutableConfig>, SettingHandle2<?>> handles;

        public Registrar(
                SettingsRegistry2<Config> registry,
                Map<Class<? extends ImmutableConfig>, SettingHandle2<?>> handles
        ) {
            this.registry = registry;
            this.handles = handles;
        }

        public <T extends ImmutableConfig> void register(
                Class<T> type,
                Function<Config, T> getter,
                BiConsumer<Config, T> setter
        ) {
            if (handles.containsKey(type)) {
                throw new IllegalArgumentException("Map already contains key of " + type.getName() + ".");
            }

            SettingHandle2<?> handle = registry.register(getter, setter);
            handles.put(type, handle);
        }
    }
}