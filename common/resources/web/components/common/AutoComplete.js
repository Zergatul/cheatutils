import * as FallbackLoader from '/fallback-loader.js'
import { withCss } from '/components/Loader.js'

const { ref, toRefs, onMounted, onUnmounted } = await FallbackLoader.vue();

const DefaultMaxItems = 50;

export function createComponent(template) {
    const args = {
        template: template,
        props: {
            value: {
                required: true
            },
            list: {
                required: true
            },
            autofocus: {
                type: Boolean,
                required: false
            },
            cancelOnBlur: {
                type: Boolean,
                required: false
            },
            applyOnClick: {
                type: Boolean,
                required: false
            }
        },
        emits: ['apply', 'cancel'],
        setup(props, { emit }) {
            const input = ref(null);
            const ul = ref(null);
            const { value, list, autofocus, cancelOnBlur, applyOnClick } = toRefs(props);

            let currentValue;
            let maxItems = DefaultMaxItems;

            const onDocumentClick = event => {
                if (!ul.value.checkVisibility()) {
                    return;
                }

                let isInnerClick = false;
                let element = event.target;
                while (element) {
                    if (element == input.value || element == ul.value) {
                        isInnerClick = true;
                    }
                    element = element.parentElement;
                }

                if (!isInnerClick) {
                    input.value.value = currentValue;
                    ul.value.style.display = 'none';
                }
            };

            const onInput = () => {
                const text = input.value.value.toLowerCase();
                const parts = text.split(/\s+/);
                const filtered = [];
                let i = 0;
                while (i < list.value.length && filtered.length <= maxItems) {
                    const item = list.value[i++];
                    const itemLower = item.toLowerCase();
                    if (parts.every(p => itemLower.includes(p))) {
                        filtered.push(item);
                    }
                }

                ul.value.innerHTML = '';

                const hasNone = filtered.length == 0;
                const hasMore = filtered.length > maxItems;

                if (hasNone) {
                    const li = document.createElement('li');
                    li.textContent = 'Nothing found';
                    ul.value.appendChild(li);
                }

                filtered.splice(maxItems);
                filtered.forEach((item) => {
                    const li = document.createElement('li');
                    li.classList.add('item');
                    li.textContent = item;
                    li.addEventListener('mousedown', event => {
                        event.preventDefault();
                    });
                    li.addEventListener('click', () => {
                        currentValue = item;
                        input.value.value = currentValue;
                        ul.value.style.display = 'none';
                        if (applyOnClick.value) {
                            emit('apply', currentValue);
                        } else {
                            input.value.focus();
                        }
                    });
                    ul.value.appendChild(li);
                });

                if (hasMore) {
                    const li = document.createElement('li');
                    li.classList.add('more');
                    li.textContent = '...';
                    li.title = 'Show more';
                    li.addEventListener('mousedown', event => {
                        event.preventDefault();
                    });
                    li.addEventListener('click', event => {
                        event.stopPropagation(); // don't propagate to document.click
                        maxItems *= 2;
                        onInput();
                    });
                    ul.value.appendChild(li);
                }

                ul.value.style.display = 'block';
            };

            const onKeyDown = event => {
                if (event.key == 'Enter') {
                    emit('apply', currentValue);
                }
                if (event.key == 'Escape') {
                    emit('cancel');
                }
            };

            const onBlur = () => {
                if (cancelOnBlur.value) {
                    emit('cancel');
                }
            };

            onMounted(() => {
                currentValue = value.value || '';
                input.value.value = currentValue;

                input.value.addEventListener('input', onInput);
                input.value.addEventListener('keydown', onKeyDown);
                input.value.addEventListener('blur', onBlur);
                document.addEventListener('click', onDocumentClick);

                if (autofocus.value) {
                    input.value.focus();
                }
            });

            onUnmounted(() => {
                input.value.removeEventListener('input', onInput);
                input.value.removeEventListener('keydown', onKeyDown);
                input.value.removeEventListener('blur', onBlur);
                document.removeEventListener('click', onDocumentClick);
            });

            return {
                input,
                ul
            };
        }
    };
    return withCss(import.meta.url, args);
};