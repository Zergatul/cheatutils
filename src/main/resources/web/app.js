import * as Vue from '/vue.esm-browser.js';
import { addComponent } from '/components/Loader.js'

(function () {

    const args = {

        data() {
            return {
                newname: null,
                current: 'main'
            }
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
            },
            refresh() {
                var self = this;
                axios.get('/api/user').then(function (response) {
                    self.name = response.data;
                    document.title = self.name;
                    self.newname = self.name;
                });
            }
        },

        created() {
            this.refresh();
        },

        watch: {

        }
    };

    addComponent(args, 'AutoFishConfig');
    addComponent(args, 'FullBrightConfig');
    addComponent(args, 'ArmorOverlayConfig');
    addComponent(args, 'KillAuraConfig');
    addComponent(args, 'BoatHackConfig');
    addComponent(args, 'ElytraHackConfig');
    addComponent(args, 'PigHackConfig');
    addComponent(args, 'AutoDisconnectConfig');
    addComponent(args, 'BlocksConfig');
    addComponent(args, 'EntitiesConfig');
    addComponent(args, 'ProjectilePathConfig');
    addComponent(args, 'ShulkerTooltipConfig');
    addComponent(args, 'LightLevelConfig');
    addComponent(args, 'EndCityChunksConfig');
    addComponent(args, 'EntityOwnerConfig');
    addComponent(args, 'ExplorationMiniMapConfig');
    addComponent(args, 'AutoCriticalsConfig');
    addComponent(args, 'FlyHackConfig');
    addComponent(args, 'AutoTotemConfig');
    addComponent(args, 'DeathCoordinatesConfig');
    addComponent(args, 'ElytraTunnelConfig');
    addComponent(args, 'FreeCamConfig');
    addComponent(args, 'LockInputsConfig');
    addComponent(args, 'MovementHackConfig');
    addComponent(args, 'ScaffoldConfig');
    addComponent(args, 'AdvancedTooltipsConfig');
    addComponent(args, 'FogConfig');
    addComponent(args, 'InstantDisconnectConfig');
    addComponent(args, 'ScriptsConfig');
    addComponent(args, 'BeaconsConfig');
    addComponent(args, 'UserNameConfig');
    addComponent(args, 'NewChunksConfig');
    addComponent(args, 'AutoDropConfig');
    addComponent(args, 'ChunksConfig');
    addComponent(args, 'ContainerButtonsConfig');
    addComponent(args, 'StatusOverlayConfig');
    addComponent(args, 'StatusEffectsConfig');
    addComponent(args, 'AutoEatConfig');
    addComponent(args, 'NoFallConfig');
    addComponent(args, 'ParticlesConfig');
    addComponent(args, 'AntiRespawnResetConfig');
    addComponent(args, 'FastBreakConfig');
    addComponent(args, 'ContainerSummaryConfig');
    addComponent(args, 'AutoCraftConfig');
    addComponent(args, 'ReachConfig');
    addComponent(args, 'GameTickScriptingConfig');
    addComponent(args, 'ZoomConfig');
    addComponent(args, 'SchematicaConfig');
    addComponent(args, 'AutoBucketConfig');
    addComponent(args, 'WorldDownloadConfig');
    addComponent(args, 'PerformanceConfig');
    addComponent(args, 'EntityTitleConfig');
    addComponent(args, 'TeleportHackConfig');
    addComponent(args, 'FakeLagConfig');

    let app = Vue.createApp(args);
    app.mount('#vue-app');

})();