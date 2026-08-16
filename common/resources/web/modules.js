import { getComponent } from '/components/Loader.js'

const modules = {
    all: [],
    automation: {},
    esp: {},
    hacks: {},
    visuals: {},
    scripting: {},
    utility: {}
};

const module = params => {
    modules.all.push(params);

    params.componentRef = getComponent(params.legacyComponent || `${params.group}/${params.component}`);
    if (!params.hidden) {
        modules[params.group][params.component] = params;
    }
};

// Automation Modules =================

module({
    group: 'automation',
    name: 'Auto Disconnect',
    component: 'AutoDisconnect',
    legacyComponent: 'AutoDisconnectConfig',
    path: 'auto-disconnect',
    tags: ['auto', 'disconnect']
});
module({
    group: 'automation',
    name: 'Auto Bucket',
    component: 'AutoBucket',
    legacyComponent: 'AutoBucketConfig',
    path: 'auto-bucket',
    tags: ['auto', 'bucket', 'mlg', 'nofall', 'no', 'fall']
});
module({
    group: 'automation',
    name: 'Auto Fish',
    component: 'AutoFish',
    legacyComponent: 'AutoFishConfig',
    path: 'auto-fish',
    tags: ['auto', 'fish', 'fishing']
});
module({
    group: 'automation',
    name: 'Auto Totem',
    component: 'AutoTotem',
    legacyComponent: 'AutoTotemConfig',
    path: 'auto-totem',
    tags: ['auto', 'totem']
});
module({
    group: 'automation',
    name: 'Auto Drop',
    component: 'AutoDrop',
    legacyComponent: 'AutoDropConfig',
    path: 'auto-drop',
    tags: ['auto', 'drop', 'inventory']
});
module({
    group: 'automation',
    name: 'Container Buttons',
    component: 'ContainerButtons',
    legacyComponent: 'ContainerButtonsConfig',
    path: 'container-buttons',
    tags: ['container', 'buttons']
});
module({
    group: 'automation',
    name: 'Auto Eat',
    component: 'AutoEat',
    legacyComponent: 'AutoEatConfig',
    path: 'auto-eat',
    tags: ['auto', 'eat']
});
module({
    group: 'automation',
    name: 'Anti Respawn Reset',
    component: 'AntiRespawnReset',
    legacyComponent: 'AntiRespawnResetConfig',
    path: 'anti-respawn-reset',
    tags: ['anti', 'respawn', 'bed', 'anchor']
});
module({
    group: 'automation',
    name: 'Auto Craft',
    component: 'AutoCraft',
    legacyComponent: 'AutoCraftConfig',
    path: 'auto-craft',
    tags: ['auto', 'craft']
});
module({
    group: 'automation',
    name: 'Auto Attack',
    component: 'AutoAttack',
    legacyComponent: 'AutoAttackConfig',
    path: 'auto-attack',
    tags: ['auto', 'attack']
});
module({
    group: 'automation',
    name: 'Schematica',
    component: 'Schematica',
    legacyComponent: 'SchematicaConfig',
    path: 'schematica',
    tags: ['schematica']
});
module({
    group: 'automation',
    name: 'Auto Hotbar',
    component: 'AutoHotbar',
    legacyComponent: 'AutoHotbarConfig',
    path: 'auto-hotbar',
    tags: ['auto', 'hotbar']
});

// ESP Modules ======================

module({
    group: 'esp',
    name: 'Block ESP',
    component: 'BlockESP',
    legacyComponent: 'BlocksConfig',
    path: 'block-esp',
    oldPaths: ['blocks'],
    tags: ['blocks', 'esp', 'xray']
});
module({
    group: 'esp',
    name: 'Entity ESP',
    component: 'EntityESP',
    legacyComponent: 'EntitiesConfig',
    path: 'entity-esp',
    oldPaths: ['entities'],
    tags: ['entity', 'entities', 'esp']
});
module({
    group: 'esp',
    name: 'Projectile Path',
    component: 'ProjectilePath',
    legacyComponent: 'ProjectilePathConfig',
    path: 'projectile-path',
    tags: ['projectile', 'path', 'ender', 'pearl']
});
module({
    group: 'esp',
    name: 'Light Level',
    component: 'LightLevel',
    legacyComponent: 'LightLevelConfig',
    path: 'light-level',
    tags: ['light', 'level', 'mob', 'spawn']
});
module({
    group: 'esp',
    name: 'End City Chunks',
    component: 'EndCityChunks',
    legacyComponent: 'EndCityChunksConfig',
    path: 'end-city-chunks',
    tags: ['end', 'city', 'cities', 'chunks']
});
module({
    group: 'esp',
    name: 'Entity Owner',
    component: 'EntityOwner',
    legacyComponent: 'EntityOwnerConfig',
    path: 'entity-owner',
    tags: ['entity', 'owner']
});
module({
    group: 'esp',
    name: 'Free Cam',
    component: 'FreeCam',
    legacyComponent: 'FreeCamConfig',
    path: 'freecam',
    oldPaths: ['free-cam'],
    tags: ['freecam', 'camera']
});
module({
    group: 'esp',
    name: 'New Chunks',
    component: 'NewChunks',
    legacyComponent: 'NewChunksConfig',
    path: 'new-chunks',
    tags: ['new', 'chunks']
});
module({
    group: 'esp',
    name: 'Entity Titles',
    component: 'EntityTitle',
    legacyComponent: 'EntityTitleConfig',
    path: 'entity-titles',
    oldPaths: ['entity-title'],
    tags: ['entity', 'title', 'health']
});

// Hacks Modules ==========================

module({
    group: 'hacks',
    name: 'Kill Aura',
    component: 'KillAura',
    legacyComponent: 'KillAuraConfig',
    path: 'kill-aura',
    tags: ['kill', 'aura', 'auto', 'attack']
});
module({
    group: 'hacks',
    name: 'Elytra Fly',
    component: 'ElytraHack',
    legacyComponent: 'ElytraHackConfig',
    path: 'elytra-fly',
    oldPaths: ['elytra-hack'],
    tags: ['elytra', 'hack', 'fly']
});
module({
    group: 'hacks',
    name: 'Pig',
    component: 'PigHack',
    legacyComponent: 'PigHackConfig',
    path: 'pig',
    oldPaths: ['pig-hack'],
    tags: ['pig', 'hack']
});
module({
    group: 'hacks',
    name: 'Auto Criticals',
    component: 'AutoCriticals',
    legacyComponent: 'AutoCriticalsConfig',
    path: 'auto-criticals',
    tags: ['auto', 'criticals']
});
module({
    group: 'hacks',
    name: 'Fly',
    component: 'FlyHack',
    legacyComponent: 'FlyHackConfig',
    path: 'fly',
    oldPaths: ['fly-hack'],
    tags: ['fly', 'hack']
});
module({
    group: 'hacks',
    name: 'Elytra Tunnel',
    component: 'ElytraTunnel',
    legacyComponent: 'ElytraTunnelConfig',
    path: 'elytra-tunnel',
    tags: ['elytra', 'tunnel']
});
module({
    group: 'hacks',
    name: 'Movement',
    component: 'MovementHack',
    legacyComponent: 'MovementHackConfig',
    path: 'movement',
    oldPaths: ['movement-hack'],
    tags: ['movement', 'hack']
});
module({
    group: 'hacks',
    name: 'Scaffold',
    component: 'Scaffold',
    legacyComponent: 'ScaffoldConfig',
    path: 'scaffold',
    tags: ['scaffold']
});
module({
    group: 'hacks',
    name: 'No Fall',
    component: 'NoFall',
    legacyComponent: 'NoFallConfig',
    path: 'no-fall',
    tags: ['nofall', 'no', 'fall']
});
module({
    group: 'hacks',
    name: 'Fast Break',
    component: 'FastBreak',
    legacyComponent: 'FastBreakConfig',
    path: 'fast-break',
    tags: ['fast', 'break']
});
module({
    group: 'hacks',
    name: 'Reach',
    component: 'Reach',
    legacyComponent: 'ReachConfig',
    path: 'reach',
    tags: ['reach']
});
module({
    group: 'hacks',
    name: 'Teleport',
    component: 'TeleportHack',
    legacyComponent: 'TeleportHackConfig',
    path: 'teleport',
    oldPaths: ['teleport-hack'],
    tags: ['teleport', 'hack']
});
module({
    group: 'hacks',
    name: 'Fake Lag',
    component: 'FakeLag',
    legacyComponent: 'FakeLagConfig',
    path: 'fake-lag',
    tags: ['fake', 'lag']
});
module({
    group: 'hacks',
    name: 'Blink',
    component: 'Blink',
    legacyComponent: 'BlinkConfig',
    path: 'blink',
    tags: ['blink']
});
module({
    group: 'hacks',
    name: 'Boat',
    component: 'BoatHack',
    legacyComponent: 'BoatHackConfig',
    path: 'boat',
    oldPaths: ['boat-hack'],
    tags: ['boat', 'hack', 'fly']
});
module({
    group: 'hacks',
    name: 'Inv Move',
    component: 'InvMove',
    legacyComponent: 'InvMoveConfig',
    path: 'inv-move',
    tags: ['inventory', 'move', 'keys']
});
module({
    group: 'hacks',
    name: 'Area Mine',
    component: 'AreaMine',
    legacyComponent: 'AreaMineConfig',
    path: 'area-mine',
    tags: ['area', 'mine']
});
module({
    group: 'hacks',
    name: 'Server Plugins',
    component: 'ServerPlugins',
    legacyComponent: 'ServerPluginsConfig',
    path: 'server-plugins',
    tags: ['server', 'plugins']
});
module({
    group: 'hacks',
    name: 'Hitbox Size',
    component: 'HitboxSize',
    legacyComponent: 'HitboxSizeConfig',
    path: 'hitbox-size',
    tags: ['hitbox', 'size']
});
module({
    group: 'hacks',
    name: 'Bedrock Breaker',
    component: 'BedrockBreaker',
    legacyComponent: 'BedrockBreakerConfig',
    path: 'bedrock-breaker',
    tags: ['bedrock', 'breaker']
});

// Visuals Modules ==========================

module({
    group: 'visuals',
    name: 'Full Bright',
    component: 'FullBright',
    legacyComponent: 'FullBrightConfig',
    path: 'full-bright',
    tags: ['full', 'bright', 'night', 'vision']
});
module({
    group: 'visuals',
    name: 'Armor Overlay',
    component: 'ArmorOverlay',
    legacyComponent: 'ArmorOverlayConfig',
    path: 'armor-overlay',
    tags: ['armor', 'overlay']
});
module({
    group: 'visuals',
    name: 'Shulker Tooltip',
    component: 'ShulkerTooltip',
    legacyComponent: 'ShulkerTooltipConfig',
    path: 'shulker-tooltip',
    tags: ['shulker', 'tooltip']
});
module({
    group: 'visuals',
    name: 'Advanced Tooltips',
    component: 'AdvancedTooltips',
    legacyComponent: 'AdvancedTooltipsConfig',
    path: 'adv-tooltips',
    oldPaths: ['advanced-tooltips'],
    tags: ['advanced', 'tooltips']
});
module({
    group: 'visuals',
    name: 'Exploration Mini Map',
    component: 'ExplorationMiniMap',
    legacyComponent: 'ExplorationMiniMapConfig',
    path: 'exploration-mini-map',
    tags: ['exploration', 'minimap']
});
module({
    group: 'visuals',
    name: 'Death Coordinates',
    component: 'DeathCoordinates',
    legacyComponent: 'DeathCoordinatesConfig',
    path: 'death-coordinates',
    tags: ['death', 'coordinates']
});
module({
    group: 'visuals',
    name: 'No Fog',
    component: 'Fog',
    legacyComponent: 'FogConfig',
    path: 'no-fog',
    oldPaths: ['fog'],
    tags: ['fog']
});
module({
    group: 'visuals',
    name: 'Chunks',
    component: 'Chunks',
    legacyComponent: 'ChunksConfig',
    path: 'chunks',
    tags: ['chunks', 'distance']
});
module({
    group: 'visuals',
    name: 'Status Effects',
    component: 'StatusEffects',
    legacyComponent: 'StatusEffectsConfig',
    path: 'status-effects',
    tags: ['status', 'effects']
});
module({
    group: 'visuals',
    name: 'Particles',
    component: 'Particles',
    legacyComponent: 'ParticlesConfig',
    path: 'particles',
    tags: ['particles']
});
module({
    group: 'visuals',
    name: 'Zoom',
    component: 'Zoom',
    legacyComponent: 'ZoomConfig',
    path: 'zoom',
    tags: ['zoom']
});
module({
    group: 'visuals',
    name: 'Performance',
    component: 'Performance',
    legacyComponent: 'PerformanceConfig',
    path: 'performance',
    tags: ['performance']
});
module({
    group: 'visuals',
    name: 'World Markers',
    component: 'WorldMarkers',
    legacyComponent: 'WorldMarkersConfig',
    path: 'world-markers',
    tags: ['world', 'markers']
});
module({
    group: 'visuals',
    name: 'Hurt Bobbing',
    component: 'BobHurt',
    legacyComponent: 'BobHurtConfig',
    path: 'bob-hurt',
    tags: ['nohurtcam', 'bobhurt']
});
module({
    group: 'visuals',
    name: 'No Weather',
    component: 'NoWeather',
    legacyComponent: 'NoWeatherConfig',
    path: 'no-weather',
    tags: ['no', 'weather']
});
module({
    group: 'visuals',
    name: 'Fake Weather',
    component: 'FakeWeather',
    legacyComponent: 'FakeWeatherConfig',
    path: 'fake-weather',
    tags: ['fake', 'weather']
});

// Scripting Modules ==========================

module({
    group: 'scripting',
    name: 'Key Bindings',
    component: 'KeyBindingScripts',
    path: 'key-bindings',
    oldPaths: ['scripts'],
    tags: ['key', 'bindings', 'scripting']
});
module({
    group: 'scripting',
    name: 'Status Overlay',
    component: 'StatusOverlay',
    path: 'status-overlay',
    tags: ['status', 'overlay', 'f3']
});
module({
    group: 'scripting',
    name: 'Game Tick Scripting',
    component: 'GameTickScripting',
    legacyComponent: 'GameTickScriptingConfig',
    path: 'game-tick-scripting',
    tags: ['game', 'tick', 'scripting']
});
module({
    group: 'scripting',
    name: 'Block Automation',
    component: 'BlockAutomation',
    path: 'block-automation',
    oldPaths: ['scripted-block-placer'],
    tags: ['scripted', 'block', 'placer', 'automation']
});
module({
    group: 'scripting',
    name: 'Exec',
    component: 'Exec',
    legacyComponent: 'ExecConfig',
    path: 'exec',
    tags: ['exec']
});
module({
    group: 'scripting',
    name: 'Villager Roller',
    component: 'VillagerRoller',
    path: 'villager-roller',
    tags: ['villager', 'roller']
});
module({
    group: 'scripting',
    name: 'TPS',
    component: 'Tps',
    legacyComponent: 'TpsConfig',
    path: 'tps',
    tags: ['tps', 'tick', 'rate']
});
module({
    group: 'scripting',
    name: 'Editor Config',
    component: 'MonacoEditorConfig',
    path: 'editor-config',
    tags: ['script', 'editor', 'config', 'monaco']
});

// Utility Modules ==========================

module({
    group: 'utility',
    name: 'User Name',
    component: 'UserName',
    legacyComponent: 'UserNameConfig',
    path: 'user-name',
    tags: ['user', 'name']
});
module({
    group: 'utility',
    name: 'Container Summary',
    component: 'ContainerSummary',
    legacyComponent: 'ContainerSummaryConfig',
    path: 'container-summary',
    tags: ['container', 'summary']
});
module({
    group: 'utility',
    name: 'Chat',
    component: 'ChatUtilities',
    legacyComponent: 'ChatUtilitiesConfig',
    path: 'chat',
    oldPaths: ['chat-utilities'],
    tags: ['chat']
});
module({
    group: 'utility',
    name: 'Lock Inputs',
    component: 'LockInputs',
    legacyComponent: 'LockInputsConfig',
    path: 'lock-inputs',
    tags: ['lock', 'inputs']
});
module({
    group: 'utility',
    name: 'World Download',
    component: 'WorldDownload',
    legacyComponent: 'WorldDownloadConfig',
    path: 'world-download',
    tags: ['world', 'download']
});

// Previously hidden routes ==========================

module({
    group: 'utility',
    name: 'Coordinate Leak Protection',
    component: 'CoordinateLeakProtection',
    legacyComponent: 'CoordinateLeakProtectionConfig',
    path: 'coordinate-leak-protection',
    tags: ['coordinate', 'leak'],
    hidden: true
});
module({
    group: 'utility',
    name: 'Instant Disconnect',
    component: 'InstantDisconnect',
    legacyComponent: 'InstantDisconnectConfig',
    path: 'instant-disconnect',
    tags: ['instant', 'disconnect'],
    hidden: true
});

export { modules }