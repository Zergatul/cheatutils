import * as Vue from '/vue.esm-browser.js';
import { addComponent } from '/components/Loader.js'

(function () {

    let modules = {};

    const args = {
        data() {
            return {
                newname: null,
                current: 'main',
                filtered: {},
                search: ''
            };
        },
        mounted() {
            this.$refs.search.focus();
        },
        created() {
            this.refresh();
            this.filterModules();
        },
        methods: {
            hardSwitch() {
                var self = this;
                axios.post('/api/hard-switch', true).then(function (response) {
                    alert('ok');
                });
            },
            backToMain() {
                var self = this;
                self.current = 'main';
                this.refresh();
                setTimeout(() => {
                    this.$refs.search.focus();
                }, 100);
                
            },
            filterModules() {
                this.filtered = {};

                let words = this.search.split(/\s+/).filter(w => w).map(w => w.toLowerCase());
                if (words.length == 0) {
                    for (let module in modules) {
                        this.filtered[module] = true;
                    }
                    return;
                }

                for (let module in modules) {
                    let tags = modules[module];
                    if (words.every(w => tags.some(t => t.indexOf(w) >= 0))) {
                        this.filtered[module] = true;
                    }
                }
            },
            onFilterKeyDown(event) {
                if (event.keyCode == 27) {
                    // escape
                    this.search = '';
                    this.filterModules();
                }
            },
            refresh() {
                var self = this;
                axios.get('/api/user').then(function (response) {
                    self.name = response.data;
                    document.title = self.name;
                    self.newname = self.name;
                });
            }
        }
    };

    let loadModule = (name, tags) => {
        addComponent(args, name);
        if (tags) {
            let key = name.replace(/Config$/, '');
            modules[key] = tags;
        }
    };

    loadModule('AutoFishConfig', ['auto', 'fish', 'fishing']);
    loadModule('FullBrightConfig', ['full', 'bright', 'night', 'vision']);
    loadModule('ArmorOverlayConfig', ['armor', 'overlay']);
    loadModule('KillAuraConfig', ['kill', 'aura', 'auto', 'attack']);
    loadModule('BoatHackConfig', ['boat', 'hack', 'fly']);
    loadModule('ElytraHackConfig', ['elytra', 'hack', 'fly']);
    loadModule('PigHackConfig', ['pig', 'hack']);
    loadModule('AutoDisconnectConfig', ['auto', 'disconnect']);
    loadModule('BlocksConfig', ['blocks', 'esp', 'xray']);
    loadModule('EntitiesConfig', ['entity', 'entities', 'esp']);
    loadModule('ProjectilePathConfig', ['projectile', 'path', 'ender', 'pearl']);
    loadModule('ShulkerTooltipConfig', ['shulker', 'tooltip']);
    loadModule('LightLevelConfig', ['light', 'level', 'mob', 'spawn']);
    loadModule('EndCityChunksConfig', ['end', 'city', 'cities', 'chunks']);
    loadModule('EntityOwnerConfig', ['entity', 'owner']);
    loadModule('ExplorationMiniMapConfig', ['exploration', 'minimap']);
    loadModule('AutoCriticalsConfig', ['auto', 'criticals']);
    loadModule('FlyHackConfig', ['fly', 'hack']);
    loadModule('AutoTotemConfig', ['auto', 'totem']);
    loadModule('DeathCoordinatesConfig', ['death', 'coordinates']);
    loadModule('ElytraTunnelConfig', ['elytra', 'tunnel']);
    loadModule('FreeCamConfig', ['freecam', 'camera']);
    loadModule('LockInputsConfig', ['lock', 'inputs']);
    loadModule('MovementHackConfig', ['movement', 'hack']);
    loadModule('ScaffoldConfig', ['scaffold']);
    loadModule('AdvancedTooltipsConfig', ['advanced', 'tooltips']);
    loadModule('FogConfig', ['fog']);
    loadModule('InstantDisconnectConfig', ['instant', 'disconnect']);
    loadModule('ScriptsConfig', ['key', 'bindings', 'scripting']);
    loadModule('WorldMarkersConfig', ['world', 'markers']);
    loadModule('UserNameConfig', ['user', 'name']);
    loadModule('NewChunksConfig', ['new', 'chunks']);
    loadModule('AutoDropConfig', ['auto', 'drop', 'inventory']);
    loadModule('ChunksConfig', ['chunks', 'distance']);
    loadModule('ContainerButtonsConfig', ['container', 'buttons']);
    loadModule('StatusOverlayConfig', ['status', 'overlay', 'f3']);
    loadModule('StatusEffectsConfig', ['status', 'effects']);
    loadModule('AutoEatConfig', ['auto', 'eat']);
    loadModule('NoFallConfig', ['nofall', 'no', 'fall']);
    loadModule('ParticlesConfig', ['particles']);
    loadModule('AntiRespawnResetConfig', ['anti', 'respawn', 'bed', 'anchor']);
    loadModule('FastBreakConfig', ['fast', 'break']);
    loadModule('ContainerSummaryConfig', ['container', 'summary']);
    loadModule('AutoCraftConfig', ['auto', 'craft']);
    loadModule('ReachConfig', ['reach']);
    loadModule('GameTickScriptingConfig', ['game', 'tick', 'scripting']);
    loadModule('ZoomConfig', ['zoom']);
    loadModule('SchematicaConfig', ['schematica']);
    loadModule('AutoBucketConfig', ['auto', 'bucket', 'mlg']);
    loadModule('WorldDownloadConfig', ['world', 'download']);
    loadModule('PerformanceConfig', ['performance']);
    loadModule('EntityTitleConfig', ['entity', 'title', 'health']);
    loadModule('TeleportHackConfig', ['teleport', 'hack']);
    loadModule('FakeLagConfig', ['fake', 'lag']);
    loadModule('CoordinateLeakProtectionConfig', ['coordinate', 'leak']);
    loadModule('TpsConfig', ['tps', 'tick', 'rate']);
    loadModule('ScriptedBlockPlacerConfig', ['scripted', 'block', 'placer']);

    let app = Vue.createApp(args);
    app.mount('#vue-app');

})();