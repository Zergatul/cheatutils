import * as FallbackLoader from '/fallback-loader.js'
import * as http from '/http.js'
import { getComponent } from '/components/Loader.js'
import * as events from '/events-service.js'
import { modules } from '/modules.js'

const { createApp, defineComponent, ref, computed, onMounted, onUnmounted, watch, nextTick } = await FallbackLoader.vue();

const main = getComponent('Main');

const App = defineComponent({
    components: {
        SwitchCheckbox: getComponent('common/SwitchCheckbox')
    },
    setup() {
        const routes = {};
        for (let module of modules.all) {
            if (module.path) {
                routes['/' + module.path] = module;
            }
        }

        const search = ref('');
        const searchInput = ref(null);
        const notificationsRoot = ref(null);
        const notificationDialog = ref(null);
        const selectedNotification = ref(null);
        const notificationsOpen = ref(false);
        const notifications = ref([]);
        const unreadNotificationsCount = ref(0);
        let latestNotificationId = 0;
        let lastReadNotificationId = 0;
        let notificationsInterval = null;
        let notificationsRequestPending = false;
        const path = ref(window.location.hash);
        const module = computed(() => {
            const route = routes[path.value.slice(1) || '/'];
            if (route != null) {
                return route;
            } else {
                return null;
            }
        });
        const view = computed(() => {
            if (module.value != null) {
                return module.value.componentRef;
            } else {
                return main;
            }
        });

        window.addEventListener('hashchange', () => {
            path.value = window.location.hash;
        });

        const onBellClick = () => {
            notificationsOpen.value = !notificationsOpen.value;
            if (notificationsOpen.value) {
                lastReadNotificationId = latestNotificationId;
                unreadNotificationsCount.value = 0;
            }
        };

        const refreshNotifications = async () => {
            if (notificationsRequestPending) {
                return;
            }

            notificationsRequestPending = true;
            try {
                const response = await http.get('/api/notifications');
                if (response.latestId < latestNotificationId) {
                    lastReadNotificationId = 0;
                }
                latestNotificationId = response.latestId;
                notifications.value = response.notifications;
                if (notificationsOpen.value) {
                    lastReadNotificationId = latestNotificationId;
                    unreadNotificationsCount.value = 0;
                } else {
                    unreadNotificationsCount.value = response.notifications.filter(n => n.id > lastReadNotificationId).length;
                }
            } catch (_) {
                // The game or its HTTP server may be restarting. The next poll will retry.
            } finally {
                notificationsRequestPending = false;
            }
        };

        const showNotificationDetails = async notification => {
            selectedNotification.value = notification;
            await nextTick();
            notificationDialog.value.showModal();
        };

        const closeNotificationDetails = () => {
            notificationDialog.value.close();
        };

        const formatNotificationTime = timestamp => {
            const elapsed = Date.now() - timestamp;
            if (elapsed < 60_000) {
                return 'Just now';
            }
            if (elapsed < 3_600_000) {
                const minutes = Math.floor(elapsed / 60_000);
                return `${minutes} minute${minutes == 1 ? '' : 's'} ago`;
            }
            if (elapsed < 86_400_000) {
                const hours = Math.floor(elapsed / 3_600_000);
                return `${hours} hour${hours == 1 ? '' : 's'} ago`;
            }
            if (elapsed < 604_800_000) {
                const days = Math.floor(elapsed / 86_400_000);
                return `${days} day${days == 1 ? '' : 's'} ago`;
            }
            return new Date(timestamp).toLocaleString();
        };

        const onDocumentClick = event => {
            if (notificationsRoot.value != null && !notificationsRoot.value.contains(event.target)) {
                notificationsOpen.value = false;
            }
        };

        const onDocumentKeyDown = event => {
            if (event.key == 'Escape') {
                notificationsOpen.value = false;
            }
        };

        const onFilterKeyDown = event => {
            // escape
            if (event.keyCode == 27) {
                search.value = '';
                onFilterInput();
            }
        };

        const onFilterInput = () => {
            events.trigger({
                type: 'filter',
                value: search.value
            });
        };

        const isMain = () => {
            return view.value == main;
        };

        const onEvent = event => {
            if (event.type == 'focus-filter') {
                setTimeout(() => {
                    if (searchInput.value != null) {
                        searchInput.value.focus();
                    }
                }, 100);
            }
        };

        watch(view, async (current) => {
            if (current == main) {
                await nextTick();
                onFilterInput();
            }
        });

        onMounted(() => {
            events.subscribe(onEvent);
            document.addEventListener('click', onDocumentClick);
            document.addEventListener('keydown', onDocumentKeyDown);
            refreshNotifications();
            notificationsInterval = setInterval(refreshNotifications, 5000);
            http.getText('/api/user').then(response => {
                document.title = response;
            });
        });

        onUnmounted(() => {
            events.unsubscribe(onEvent);
            document.removeEventListener('click', onDocumentClick);
            document.removeEventListener('keydown', onDocumentKeyDown);
            clearInterval(notificationsInterval);
        });

        return {
            search,
            searchInput,
            notifications,
            notificationDialog,
            notificationsOpen,
            notificationsRoot,
            selectedNotification,
            unreadNotificationsCount,
            path,
            module,
            view,

            isMain,
            closeNotificationDetails,
            formatNotificationTime,
            onBellClick,
            onFilterKeyDown,
            onFilterInput,
            showNotificationDetails
        };
    }
});

const app = createApp(App);
app.mount('#vue-app');