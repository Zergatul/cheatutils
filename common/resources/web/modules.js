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

const module = (params) => {
    params.componentRef = getComponent(`${params.group}/${params.component}`);

    modules.all.push(params);
    modules[params.group][params.component] = params;
};

module({
    group: 'automation',
    name: 'Auto Disconnect',
    component: 'AutoDisconnect',
    path: 'auto-disconnect',
    tags: ['auto', 'disconnect']
});
module({
    group: 'automation',
    name: 'Auto Bucket',
    component: 'AutoBucket',
    path: 'auto-bucket',
    tags: ['auto', 'bucket', 'mlg']
});
module({
    group: 'automation',
    name: 'Auto Fish',
    component: 'AutoFish',
    path: 'auto-fish',
    tags: ['auto', 'fish', 'fishing']
});
module({
    group: 'automation',
    name: 'Auto Totem',
    component: 'AutoTotem',
    path: 'auto-totem',
    tags: ['auto', 'totem']
});
module({
    group: 'automation',
    name: 'Auto Drop',
    component: 'AutoDrop',
    path: 'auto-drop',
    tags: ['auto', 'drop', 'inventory']
});
module({
    group: 'automation',
    name: 'Container Buttons',
    component: 'ContainerButtons',
    path: 'container-buttons',
    tags: ['container', 'buttons']
});
module({
    group: 'automation',
    name: 'Auto Eat',
    component: 'AutoEat',
    path: 'auto-eat',
    tags: ['auto', 'eat']
});
module({
    group: 'automation',
    name: 'Anti Respawn Reset',
    component: 'AntiRespawnReset',
    path: 'anti-respawn-reset',
    tags: ['anti', 'respawn', 'bed', 'anchor']
});
module({
    group: 'automation',
    name: 'Auto Craft',
    component: 'AutoCraft',
    path: 'auto-craft',
    tags: ['auto', 'craft']
});
module({
    group: 'automation',
    name: 'Auto Attack',
    component: 'AutoAttack',
    path: 'auto-attack',
    tags: ['auto', 'attack']
});
module({
    group: 'automation',
    name: 'Schematica (ALPHA)',
    component: 'Schematica',
    path: 'schematica',
    tags: ['schematica']
});
module({
    group: 'automation',
    name: 'Auto Hotbar',
    component: 'AutoHotbar',
    path: 'auto-hotbar',
    tags: ['auto', 'hotbar']
});

module({
    group: 'esp',
    name: 'Block ESP',
    component: 'BlockESP',
    path: 'block-esp',
    tags: ['blocks', 'esp', 'xray']
});
module({
    group: 'esp',
    name: 'Entity ESP',
    component: 'Entities',
    path: 'entity-esp',
    tags: ['entity', 'entities', 'esp']
});
module({
    group: 'esp',
    name: 'Projectile Path',
    component: 'ProjectilePath',
    tags: ['projectile', 'path', 'ender', 'pearl']
});
module({
    group: 'esp',
    name: 'Light Level',
    component: 'LightLevel',
    tags: ['light', 'level', 'mob', 'spawn']
});
module({
    group: 'esp',
    name: 'End City Chunks',
    component: 'EndCityChunks',
    tags: ['end', 'city', 'cities', 'chunks']
});
module({
    group: 'esp',
    name: 'Entity Owner',
    component: 'EntityOwner',
    tags: ['entity', 'owner']
});
module({
    group: 'esp',
    name: 'Free Cam',
    component: 'FreeCam',
    tags: ['freecam', 'camera']
});
module({
    group: 'esp',
    name: 'New Chunks',
    component: 'NewChunks',
    tags: ['new', 'chunks']
});
module({
    group: 'esp',
    name: 'Entity Titles',
    component: 'EntityTitle',
    tags: ['entity', 'title', 'health']
});

module({
    group: 'hacks',
    name: 'Kill Aura',
    component: 'KillAura',
    tags: ['kill', 'aura', 'auto', 'attack']
});
module({
    group: 'hacks',
    name: 'Elytra Fly',
    component: 'ElytraHack',
    tags: ['elytra', 'hack', 'fly']
});
module({
    group: 'hacks',
    name: 'Pig',
    component: 'PigHack',
    tags: ['pig', 'hack']
});
module({
    group: 'hacks',
    name: 'Auto Criticals',
    component: 'AutoCriticals',
    tags: ['auto', 'criticals']
});
module({
    group: 'hacks',
    name: 'Fly',
    component: 'FlyHack',
    tags: ['fly', 'hack']
});
module({
    group: 'hacks',
    name: 'Elytra Tunnel',
    component: 'ElytraTunnel',
    tags: ['elytra', 'tunnel']
});
module({
    group: 'hacks',
    name: 'Movement',
    component: 'MovementHack',
    tags: ['movement', 'hack']
});
module({
    group: 'hacks',
    name: 'Scaffold',
    component: 'Scaffold',
    tags: ['scaffold']
});
module({
    group: 'hacks',
    name: 'No Fall',
    component: 'NoFall',
    tags: ['nofall', 'no', 'fall']
});
module({
    group: 'hacks',
    name: 'Fast Break',
    component: 'FastBreak',
    tags: ['fast', 'break']
});
module({
    group: 'hacks',
    name: 'Reach',
    component: 'Reach',
    tags: ['reach']
});
module({
    group: 'hacks',
    name: 'Teleport',
    component: 'TeleportHack',
    tags: ['teleport', 'hack']
});
module({
    group: 'hacks',
    name: 'Fake Lag',
    component: 'FakeLag',
    tags: ['fake', 'lag']
});
module({
    group: 'hacks',
    name: 'Blink',
    component: 'Blink',
    tags: ['blink']
});
module({
    group: 'hacks',
    name: 'Boat',
    component: 'BoatHack',
    tags: ['boat', 'hack', 'fly']
});
module({
    group: 'hacks',
    name: 'Inv Move',
    component: 'InvMove',
    tags: ['inventory', 'move', 'keys']
});
module({
    group: 'hacks',
    name: 'Area Mine',
    component: 'AreaMine',
    tags: ['area', 'mine']
});
module({
    group: 'hacks',
    name: 'Server Plugins',
    component: 'ServerPlugins',
    tags: ['server', 'plugins']
});
module({
    group: 'hacks',
    name: 'Hitbox Size',
    component: 'HitboxSize',
    tags: ['hitbox', 'size']
});
module({
    group: 'hacks',
    name: 'Bedrock Breaker',
    component: 'BedrockBreaker',
    tags: ['bedrock', 'breaker']
});
module({
    group: 'hacks',
    name: 'Anti Hunger',
    component: 'AntiHunger',
    tags: ['anti', 'hunger']
});

module({
    group: 'visuals',
    name: 'Full Bright',
    component: 'FullBright',
    tags: ['full', 'bright', 'night', 'vision']
});
module({
    group: 'visuals',
    name: 'Armor Overlay',
    component: 'ArmorOverlay',
    tags: ['armor', 'overlay']
});
module({
    group: 'visuals',
    name: 'Shulker Tooltip',
    component: 'ShulkerTooltip',
    tags: ['shulker', 'tooltip']
});
module({
    group: 'visuals',
    name: 'Advanced Tooltips',
    component: 'AdvancedTooltips',
    tags: ['advanced', 'tooltips']
});
module({
    group: 'visuals',
    name: 'Exploration Mini Map',
    component: 'ExplorationMiniMap',
    tags: ['exploration', 'minimap']
});
module({
    group: 'visuals',
    name: 'Death Coordinates',
    component: 'DeathCoordinates',
    tags: ['death', 'coordinates']
});
module({
    group: 'visuals',
    name: 'No Fog',
    component: 'Fog',
    tags: ['fog']
});
module({
    group: 'visuals',
    name: 'Chunks',
    component: 'Chunks',
    tags: ['chunks', 'distance']
});
module({
    group: 'visuals',
    name: 'Status Effects',
    component: 'StatusEffects',
    tags: ['status', 'effects']
});
module({
    group: 'visuals',
    name: 'Particles',
    component: 'Particles',
    tags: ['particles']
});
module({
    group: 'visuals',
    name: 'Zoom',
    component: 'Zoom',
    tags: ['zoom']
});
module({
    group: 'visuals',
    name: 'Performance',
    component: 'Performance',
    tags: ['performance']
});
module({
    group: 'visuals',
    name: 'World Markers',
    component: 'WorldMarkers',
    tags: ['world', 'markers']
});
module({
    group: 'visuals',
    name: 'Hurt Bobbing',
    component: 'BobHurt',
    tags: ['nohurtcam', 'bobhurt']
});
module({
    group: 'visuals',
    name: 'No Weather',
    component: 'NoWeather',
    tags: ['no', 'weather']
});
module({
    group: 'visuals',
    name: 'Fake Weather',
    component: 'FakeWeather',
    tags: ['fake', 'weather']
});

module({
    group: 'scripting',
    name: 'Key Bindings',
    component: 'KeyBindingScripts',
    tags: ['key', 'bindings', 'scripting']
});
module({
    group: 'scripting',
    name: 'Status Overlay',
    component: 'StatusOverlay',
    tags: ['status', 'overlay', 'f3']
});
module({
    group: 'scripting',
    name: 'Events Scripting',
    component: 'EventsScripting',
    tags: ['events', 'tick', 'scripting']
});
module({
    group: 'scripting',
    name: 'Block Automation',
    component: 'BlockAutomation',
    tags: ['scripted', 'block', 'placer', 'automation']
});
module({
    group: 'scripting',
    name: 'Exec',
    component: 'Exec',
    tags: ['exec']
});
module({
    group: 'scripting',
    name: 'Villager Roller',
    component: 'VillagerRoller',
    tags: ['villager', 'roller']
});
module({
    group: 'scripting',
    name: 'TPS',
    component: 'Tps',
    tags: ['tps', 'tick', 'rate']
});
module({
    group: 'scripting',
    name: 'Debug',
    component: 'Debugging',
    tags: ['script', 'debug']
});
module({
    group: 'scripting',
    name: 'Editor Config',
    component: 'MonacoEditor',
    tags: ['script', 'editor', 'config', 'monaco']
});

module({
    group: 'utility',
    name: 'Core Config',
    component: 'Core',
    tags: ['core', 'port']
});
module({
    group: 'utility',
    name: 'Profiles',
    component: 'Profiles',
    tags: ['profiles']
});
module({
    group: 'utility',
    name: 'User Name',
    component: 'UserName',
    tags: ['user', 'name']
});
module({
    group: 'utility',
    name: 'Container Summary',
    component: 'ContainerSummary',
    tags: ['container', 'summary']
});
module({
    group: 'utility',
    name: 'Chat',
    component: 'ChatUtilities',
    tags: ['chat']
});
module({
    group: 'utility',
    name: 'Lock Inputs',
    component: 'LockInputs',
    tags: ['lock', 'inputs']
});
module({
    group: 'utility',
    name: 'World Download',
    component: 'WorldDownload',
    tags: ['world', 'download']
});

export { modules }