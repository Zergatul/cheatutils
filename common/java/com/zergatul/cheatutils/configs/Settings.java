package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.configs.modules.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Typed settings catalog: every setting of a profile, in the order they are saved.
 * Each setting is saved under its type name in camelCase, for example {@code fogConfig}.
 */
public final class Settings {

    private final SettingsRegistry registry;

    public Settings(SettingsRegistry registry) {
        this.registry = registry;

        registry.register(AdvancedTooltipsConfig::defaults);
        registry.register(AimAssistConfig::defaults);
        registry.register(AirPlaceConfig::defaults);
        registry.register(AntiHungerConfig::defaults);
        registry.register(AntiRespawnResetConfig::defaults);
        registry.register(AreaMineConfig::defaults);
        registry.register(ArmorOverlayConfig::defaults);
        registry.register(AutoAttackConfig::defaults);
        registry.register(AutoBucketConfig::defaults);
        registry.register(AutoCraftConfig::defaults);
        registry.register(AutoCriticalsConfig::defaults);
        registry.register(AutoDropConfig::defaults);
        registry.register(AutoEatConfig::defaults);
        registry.register(AutoFishConfig::defaults);
        registry.register(AutoHotbarConfig::defaults);
        registry.register(AutoStunnerConfig::defaults);
        registry.register(AutoToolConfig::defaults);
        registry.register(AutoTotemConfig::defaults);
        registry.register(BedrockBreakerConfig::defaults);
        registry.register(BlockAutomationConfig::defaults);
        registry.register(BlockEntityDistanceConfig::defaults);
        registry.register(BlockEspConfig::defaults);
        registry.register(BoatHackConfig::defaults);
        registry.register(BobHurtConfig::defaults);
        registry.register(BreachSwapConfig::defaults);
        registry.register(ChatUtilitiesConfig::defaults);
        registry.register(ChunksConfig::defaults);
        registry.register(ContainerButtonsConfig::defaults);
        registry.register(ContainerSummaryConfig::defaults);
        registry.register(CoreConfig::defaults);
        registry.register(CrystalAuraConfig::defaults);
        registry.register(DeathCoordinatesConfig::defaults);
        registry.register(ElytraBounceConfig::defaults);
        registry.register(ElytraHackConfig::defaults);
        registry.register(ElytraTunnelConfig::defaults);
        registry.register(EndCityChunksConfig::defaults);
        registry.register(EntityEspConfig::defaults);
        registry.register(EntityTitleConfig::defaults);
        registry.register(EventsScriptingConfig::defaults);
        registry.register(ExecConfig::defaults);
        registry.register(ExplorationMiniMapConfig::defaults);
        registry.register(FastBreakConfig::defaults);
        registry.register(FlyHackConfig::defaults);
        registry.register(FogConfig::defaults);
        registry.register(FreeCamConfig::defaults);
        registry.register(FullBrightConfig::defaults);
        registry.register(HandsViewConfig::defaults);
        registry.register(HitboxSizeConfig::defaults);
        registry.register(InvMoveConfig::defaults);
        registry.register(KeyBindingsConfig::defaults);
        registry.register(KillAuraConfig::defaults);
        registry.register(LightLevelConfig::defaults);
        registry.register(LockInputsConfig::defaults);
        registry.register(LogoutSpotsConfig::defaults);
        registry.register(McpServerConfig::defaults);
        registry.register(MonacoEditorConfig::defaults);
        registry.register(MovementHackConfig::defaults);
        registry.register(NewChunksConfig::defaults);
        registry.register(NoFallConfig::defaults);
        registry.register(ParkourAssistConfig::defaults);
        registry.register(PerformanceConfig::defaults);
        registry.register(PigHackConfig::defaults);
        registry.register(PrivacyConfig::defaults);
        registry.register(ProjectilePathConfig::defaults);
        registry.register(ReachConfig::defaults);
        registry.register(ScaffoldConfig::defaults);
        registry.register(SchematicaConfig::defaults);
        registry.register(ServerPluginsConfig::defaults);
        registry.register(ShulkerTooltipConfig::defaults);
        registry.register(SpearRangeConfig::defaults);
        registry.register(StatusEffectsConfig::defaults);
        registry.register(StatusOverlayConfig::defaults);
        registry.register(StepUpConfig::defaults);
        registry.register(TridentHackConfig::defaults);
        registry.register(UserNameConfig::defaults);
        registry.register(VillagerRollerConfig::defaults);
        registry.register(WorldMarkersConfig::defaults);
    }

    public static <T extends ImmutableConfig> SettingHandle<T> get(Class<T> type) {
        // TODO: why config store own settings, and not singleton?
        return ConfigStore.INSTANCE.getSettings().registry.find(type);
    }

    public static Map<Class<? extends ImmutableConfig>, Boolean> getModuleStates() {
        Map<Class<? extends ImmutableConfig>, Boolean> states = new HashMap<>();
        for (SettingHandle<?> handle : ConfigStore.INSTANCE.getSettings().registry.getHandles()) {
            if (handle.get() instanceof ModuleStateProvider config) {
                states.put(handle.getType(), config.isEnabled());
            }
        }
        return states;
    }
}