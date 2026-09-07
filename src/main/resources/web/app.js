import * as FallbackLoader from '/fallback-loader.js'
import { getComponent } from '/components/Loader.js'
import * as events from '/events-service.js'
import { modules } from '/modules.js'

const { createApp, defineComponent, ref, computed, onMounted, onUnmounted, watch, nextTick } = await FallbackLoader.vue();
const main = getComponent('Main');

const App = defineComponent({
    setup() {
        const routes = {};
        for (let module of modules.all) {
            if (module.path) routes['/' + module.path] = module;
        }
        const search = ref('');
        const searchInput = ref(null);
        const path = ref(window.location.hash);
        const module = computed(() => routes[path.value.slice(1) || '/'] || null);
        const view = computed(() => module.value ? module.value.componentRef : main);
        const onHashChange = () => { path.value = window.location.hash; };
        const isMain = () => view.value == main;
        const onFilterInput = () => events.trigger({ type: 'filter', value: search.value });
        const onFilterKeyDown = event => {
            if (event.key == 'Escape') {
                search.value = '';
                onFilterInput();
            }
        };
        const onEvent = event => {
            if (event.type == 'focus-filter') {
                onFilterInput();
                searchInput.value?.focus();
            }
        };
        watch(view, async current => {
            if (current == main) {
                await nextTick();
                onFilterInput();
            }
        });
        onMounted(() => {
            events.subscribe(onEvent);
            window.addEventListener('hashchange', onHashChange);
        });
        onUnmounted(() => {
            events.unsubscribe(onEvent);
            window.removeEventListener('hashchange', onHashChange);
        });
        return { search, searchInput, module, view, isMain, onFilterInput, onFilterKeyDown };
    }
});

createApp(App).mount('#vue-app');
