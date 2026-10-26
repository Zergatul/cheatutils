package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.configs.modules.*;

public class Config implements Sanitizable {

    public FullBrightConfig fullBrightConfig = FullBrightConfig.defaults();
    public AutoFishConfig autoFishConfig = AutoFishConfig.defaults();
    public ArmorOverlayConfig armorOverlayConfig = ArmorOverlayConfig.defaults();
    public BlocksConfig blocks = new BlocksConfig();
    public EntitiesConfig entities = new EntitiesConfig();
    public LightLevelConfig lightLevelConfig = LightLevelConfig.defaults();
    public KillAuraConfig killAuraConfig = new KillAuraConfig();
    public BoatHackConfig boatHackConfig = BoatHackConfig.defaults();
    public ShulkerTooltipConfig shulkerTooltipConfig = ShulkerTooltipConfig.defaults();
    public ProjectilePathConfig projectilePathConfig = ProjectilePathConfig.defaults();
    public ElytraHackConfig elytraHackConfig = ElytraHackConfig.defaults();
    public PigHackConfig pigHackConfig = PigHackConfig.defaults();
    public EndCityChunksConfig endCityChunksConfig = EndCityChunksConfig.defaults();
    public ExplorationMiniMapConfig explorationMiniMapConfig = ExplorationMiniMapConfig.defaults();
    public AutoCriticalsConfig autoCriticalsConfig = AutoCriticalsConfig.defaults();
    public FlyHackConfig flyHackConfig = FlyHackConfig.defaults();
    public AutoTotemConfig autoTotemConfig = AutoTotemConfig.defaults();
    public DeathCoordinatesConfig deathCoordinatesConfig = DeathCoordinatesConfig.defaults();
    public ElytraTunnelConfig elytraTunnelConfig = ElytraTunnelConfig.defaults();
    public FreeCamConfig freeCamConfig = FreeCamConfig.defaults();
    public MovementHackConfig movementHackConfig = MovementHackConfig.defaults();
    public LockInputsConfig lockInputsConfig = LockInputsConfig.defaults();
    public ScaffoldConfig scaffoldConfig = ScaffoldConfig.defaults();
    public AdvancedTooltipsConfig advancedTooltipsConfig = AdvancedTooltipsConfig.defaults();
    public FogConfig fogConfig = FogConfig.defaults();
    public KeyBindingScriptsConfig keyBindingScriptsConfig = new KeyBindingScriptsConfig();
    public KeyBindingsConfig keyBindingsConfig = new KeyBindingsConfig();
    public WorldMarkersConfig worldMarkersConfig = new WorldMarkersConfig();
    public UserNameConfig userNameConfig = UserNameConfig.defaults();
    public NewChunksConfig newChunksConfig = NewChunksConfig.defaults();
    public AutoDropConfig autoDropConfig = AutoDropConfig.defaults();
    public ContainerButtonsConfig containerButtonsConfig = new ContainerButtonsConfig();
    public ChunksConfig chunksConfig = ChunksConfig.defaults();
    public StatusOverlayConfig statusOverlayConfig = StatusOverlayConfig.defaults();
    public StatusEffectsConfig statusEffectsConfig = StatusEffectsConfig.defaults();
    public AutoEatConfig autoEatConfig = AutoEatConfig.defaults();
    public NoFallConfig noFallConfig = NoFallConfig.defaults();
    public AntiRespawnResetConfig antiRespawnResetConfig = AntiRespawnResetConfig.defaults();
    public FastBreakConfig fastBreakConfig = FastBreakConfig.defaults();
    public ContainerSummaryConfig containerSummaryConfig = ContainerSummaryConfig.defaults();
    public AutoCraftConfig autoCraftConfig = AutoCraftConfig.defaults();
    public ReachConfig reachConfig = ReachConfig.defaults();
    public AutoBucketConfig autoBucketConfig = AutoBucketConfig.defaults();
    public SchematicaConfig schematicaConfig = new SchematicaConfig();
    public PerformanceConfig performanceConfig = PerformanceConfig.defaults();
    public EntityTitleConfig entityTitleConfig = EntityTitleConfig.defaults();
    public BlockAutomationConfig blockAutomationConfig = new BlockAutomationConfig();
    public BobHurtConfig bobHurtConfig = BobHurtConfig.defaults();
    public AutoAttackConfig autoAttackConfig = AutoAttackConfig.defaults();
    public BreachSwapConfig breachSwapConfig = BreachSwapConfig.defaults();
    public SpearRangeConfig spearRangeConfig = SpearRangeConfig.defaults();
    public AutoStunnerConfig autoStunnerConfig = AutoStunnerConfig.defaults();
    public ChatUtilitiesConfig chatUtilitiesConfig = new ChatUtilitiesConfig();
    public ExecConfig execConfig = ExecConfig.defaults();
    public VillagerRollerConfig villagerRollerConfig = VillagerRollerConfig.defaults();
    public AutoHotbarConfig autoHotbarConfig = AutoHotbarConfig.defaults();
    public InvMoveConfig invMoveConfig = InvMoveConfig.defaults();
    public AreaMineConfig areaMineConfig = AreaMineConfig.defaults();
    public ServerPluginsConfig serverPluginsConfig = ServerPluginsConfig.defaults();
    public HitboxSizeConfig hitboxSizeConfig = HitboxSizeConfig.defaults();
    public EventsScriptingConfig eventsScriptingConfig = EventsScriptingConfig.defaults();
    public BedrockBreakerConfig bedrockBreakerConfig = BedrockBreakerConfig.defaults();
    public CoreConfig coreConfig = CoreConfig.defaults();
    public AntiHungerConfig antiHungerConfig = AntiHungerConfig.defaults();
    public MonacoEditorConfig monacoEditor = new MonacoEditorConfig();
    public StepUpConfig stepUp = StepUpConfig.defaults();
    public AimAssistConfig aimAssist = AimAssistConfig.defaults();
    public LogoutSpotsConfig logoutSpots = LogoutSpotsConfig.defaults();
    public AutoToolConfig autoTool = AutoToolConfig.defaults();
    public BlockEntityDistanceConfig blockEntityDistance = BlockEntityDistanceConfig.defaults();
    public AirPlaceConfig airPlaceConfig = AirPlaceConfig.defaults();
    public ElytraBounceConfig elytraBounceConfig = ElytraBounceConfig.defaults();
    public ParkourAssistConfig parkourAssistConfig = ParkourAssistConfig.defaults();
    public HandsViewConfig handsViewConfig = new HandsViewConfig();
    public PrivacyConfig privacyConfig = PrivacyConfig.defaults();
    public CrystalAuraConfig crystalAuraConfig = new CrystalAuraConfig();
    public McpServerConfig mcpServerConfig = McpServerConfig.defaults();

    @Override
    public void sanitize() {
        blocks.sanitize();
        entities.sanitize();
        killAuraConfig.sanitize();
        schematicaConfig.sanitize();
        keyBindingsConfig.sanitize();
        worldMarkersConfig.sanitize();
        chatUtilitiesConfig.sanitize();
        handsViewConfig.sanitize();
        crystalAuraConfig.sanitize();
        containerButtonsConfig.sanitize();
    }
}