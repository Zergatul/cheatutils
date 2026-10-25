package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.configs.modules.*;

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
                AntiHungerConfig.class,
                config -> config.antiHungerConfig,
                (config, value) -> config.antiHungerConfig = value);

        registrar.register(
                AntiRespawnResetConfig.class,
                config -> config.antiRespawnResetConfig,
                (config, value) -> config.antiRespawnResetConfig = value);

        registrar.register(
                AreaMineConfig.class,
                config -> config.areaMineConfig,
                (config, value) -> config.areaMineConfig = value);

        registrar.register(
                ArmorOverlayConfig.class,
                config -> config.armorOverlayConfig,
                (config, value) -> config.armorOverlayConfig = value);

        registrar.register(
                AutoCriticalsConfig.class,
                config -> config.autoCriticalsConfig,
                (config, value) -> config.autoCriticalsConfig = value);

        registrar.register(
                AutoFishConfig.class,
                config -> config.autoFishConfig,
                (config, value) -> config.autoFishConfig = value);

        registrar.register(
                AutoStunnerConfig.class,
                config -> config.autoStunnerConfig,
                (config, value) -> config.autoStunnerConfig = value);

        registrar.register(
                BoatHackConfig.class,
                config -> config.boatHackConfig,
                (config, value) -> config.boatHackConfig = value);

        registrar.register(
                BobHurtConfig.class,
                config -> config.bobHurtConfig,
                (config, value) -> config.bobHurtConfig = value);

        registrar.register(
                BreachSwapConfig.class,
                config -> config.breachSwapConfig,
                (config, value) -> config.breachSwapConfig = value);

        registrar.register(
                EntityTitleConfig.class,
                config -> config.entityTitleConfig,
                (config, value) -> config.entityTitleConfig = value);

        registrar.register(
                ExecConfig.class,
                config -> config.execConfig,
                (config, value) -> config.execConfig = value);

        registrar.register(
                FlyHackConfig.class,
                config -> config.flyHackConfig,
                (config, value) -> config.flyHackConfig = value);

        registrar.register(
                FullBrightConfig.class,
                config -> config.fullBrightConfig,
                (config, value) -> config.fullBrightConfig = value);

        registrar.register(
                InvMoveConfig.class,
                config -> config.invMoveConfig,
                (config, value) -> config.invMoveConfig = value);

        this.handles = Collections.unmodifiableMap(handles);
    }

    public static <T extends ImmutableConfig> SettingHandle2<T> get(Class<T> type) {
        // TODO: why config store own settings, and not singleton?
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