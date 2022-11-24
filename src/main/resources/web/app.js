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
            changeName() {
                let self = this;
                axios.post('/api/username', self.newname).then(function (response) {
                    self.name = response.data;
                    document.title = self.name;
                    self.newname = self.name;
                });
            },
            hardSwitch() {
                var self = this;
                axios.post('/api/hard-switch', true).then(function (response) {
                    alert('ok');
                });
            },
            backToMain() {
                var self = this;
                self.current = 'main';
            }
        },

        created() {
            var self = this;
            axios.get('/api/username').then(function (response) {
                self.name = response.data;
                document.title = self.name;
                self.newname = self.name;
            });
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
    addComponent(args, 'EnderPearlPathConfig');
    addComponent(args, 'ShulkerTooltipConfig');
    addComponent(args, 'BeeContainerTooltipConfig');
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
    addComponent(args, 'ScriptsConfig');
    addComponent(args, 'AutoDropConfig');
    addComponent(args, 'ContainerButtonsConfig');
    addComponent(args, 'AutoEatConfig');
    addComponent(args, 'NoFallConfig');
    addComponent(args, 'FogConfig');
    addComponent(args, 'ParticlesConfig');

    let app = Vue.createApp(args);
    app.mount('#vue-app');

})();