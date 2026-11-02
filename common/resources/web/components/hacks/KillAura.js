import * as http from '/http.js'
import { components } from '../../components.js'
import { withCss } from '/components/Loader.js'

export function createComponent(template) {
    const args = {
        template: template,
        created() {
            Promise.all([
                http.get('/api/kill-aura'),
                http.get('/api/kill-aura-predefined-priorities')
            ]).then(([config, priorities]) => {
                this.predefinedPriorities = Object.fromEntries(priorities.map(priority => [priority.priority, priority]));
                this.config = config;
            }).catch(() => {
                this.loadError = 'Cannot load Kill Aura configuration. Please reload the page to try again.';
            });
        },
        data() {
            return {
                config: null,
                loadError: '',
                predefinedPriorities: {},
                classNames: null,
                classSuggestionsError: '',
                showClassPriorityForm: false,
                newClassPriority: null,
                classPriorityError: '',
                addingClassPriority: false
            };
        },
        methods: {
            priorityName(entry) {
                if (entry.type == 'predefined') {
                    return this.predefinedPriorities[entry.priority]?.name || entry.priority;
                }
                return entry.name;
            },
            priorityDescription(entry) {
                if (entry.type == 'predefined') {
                    return this.predefinedPriorities[entry.priority]?.description;
                }
                return entry.description;
            },
            async openAddNewClassPriority() {
                this.newClassPriority = { name: '', description: '', className: '' };
                this.classPriorityError = '';
                this.classSuggestionsError = '';
                this.showClassPriorityForm = true;
                await this.$nextTick();
                this.$refs.classPriorityDialog.showModal();

                if (this.classNames == null) {
                    try {
                        const entities = await http.get('/api/entity-info');
                        this.classNames = [...new Set(entities.map(entity => entity.type))].sort();
                    } catch (error) {
                        this.classSuggestionsError = 'Cannot load class suggestions. You can still enter a class name manually.';
                    }
                }
            },
            closeAddNewClassPriority() {
                if (!this.addingClassPriority) {
                    this.$refs.classPriorityDialog.close();
                }
            },
            async addClassPriority() {
                if (this.addingClassPriority) {
                    return;
                }

                const name = this.newClassPriority.name.trim();
                const className = this.newClassPriority.className.trim();
                this.classPriorityError = '';
                if (!name || !className) {
                    this.classPriorityError = 'Name and class name are required.';
                    return;
                }

                this.addingClassPriority = true;
                try {
                    await http.get('/api/class-name/' + encodeURIComponent(className));
                } catch (error) {
                    this.classPriorityError = error.code == 404 ?
                        `Class with name "${className}" doesn't exist.` :
                        'Cannot verify the class name. Please try again.';
                    this.addingClassPriority = false;
                    return;
                }

                const entry = {
                    type: 'class',
                    name: name,
                    description: this.newClassPriority.description.trim(),
                    class: className,
                    enabled: true
                };
                try {
                    await this.update({
                        ...this.config,
                        priorities: [...this.config.priorities, entry]
                    });
                    this.$refs.classPriorityDialog.close();
                } catch (error) {
                    this.classPriorityError = 'Cannot save the priority entry. Please try again.';
                } finally {
                    this.addingClassPriority = false;
                }
            },
            moveDown(index) {
                if (index < this.config.priorities.length - 1) {
                    this.swapPriorities(index, index + 1);
                    this.update();
                }
            },
            moveUp(index) {
                if (index > 0) {
                    this.swapPriorities(index, index - 1);
                    this.update();
                }
            },
            removePriorityEntry(index) {
                if (this.config.priorities[index].type == 'predefined') {
                    return;
                }
                this.config.priorities.splice(index, 1);
                this.update();
            },
            swapPriorities(index1, index2) {
                let item = this.config.priorities[index1];
                this.config.priorities[index1] = this.config.priorities[index2];
                this.config.priorities[index2] = item;
            },
            update(config = this.config) {
                if (config.maxHorizontalAngle == '') {
                    config.maxHorizontalAngle = null;
                }
                if (config.maxVerticalAngle == '') {
                    config.maxVerticalAngle = null;
                }
                return http.post('/api/kill-aura', config).then(response => {
                    this.config = response;
                });
            }
        }
    };
    components.add(args, 'Radio');
    components.add(args, 'SwitchCheckbox');
    components.add(args, 'CodeBlock');
    components.add(args, 'AutoComplete');
    return withCss(import.meta.url, args);
}