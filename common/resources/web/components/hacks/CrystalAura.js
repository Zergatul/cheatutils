import { components } from '/components.js'
import * as http from '/http.js'
import { withCss } from '/components/Loader.js'

let entityTypesPromise = null;

function getEntityTypes() {
    if (entityTypesPromise == null) {
        entityTypesPromise = http.get('/api/entity-types')
            .then(types => types.toSorted());
    }
    return entityTypesPromise;
}

export function createComponent(template) {
    const url = '/api/crystal-aura';
    const args = {
        template: template,
        created() {
            http.get(url).then(response => {
                this.config = response;
            });
        },
        data() {
            return {
                config: null,
                showAddNew: false,
                entityTypes: null
            };
        },
        methods: {
            async showAutoComplete() {
                if (this.entityTypes == null) {
                    this.entityTypes = await getEntityTypes();
                }
                this.showAddNew = true;
            },
            addTarget(type) {
                if (!this.entityTypes.includes(type)) {
                    alert('Cannot find matching entity type');
                    this.showAddNew = false;
                    return;
                }
                if (!this.config.targets.includes(type)) {
                    this.config.targets.push(type);
                    this.config.targets.sort();
                    this.update();
                }
                this.showAddNew = false;
            },
            cancelAddTarget() {
                this.showAddNew = false;
            },
            removeTarget(index) {
                this.config.targets.splice(index, 1);
                this.update();
            },
            update() {
                http.post(url, this.config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    components.add(args, 'SwitchCheckbox');
    components.add(args, 'AutoComplete');
    components.add(args, 'Description');
    return withCss(import.meta.url, args);
}