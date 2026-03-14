import * as FallbackLoader from '/fallback-loader.js'

const { watch, nextTick } = await FallbackLoader.vue();

function debounce(fn, ms) {
    let timeoutId = null;
    return (...args) => {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn(...args), ms);
    };
}

export function useIncrementalSearch({
    queryRef,
    itemsRef,
    session,
    onNewItemsAdded,
    scrollRootRef,
    batchSize
}) {
    const pushItems = async (batch) => {
        if (batch.length > 0) {
            itemsRef.value.push(...batch);
            onNewItemsAdded && onNewItemsAdded();
            await nextTick();
            setupObserver();
        }
    };

    const restartSearch = () => {
        // if we don't reset scroll position here
        // it will continue load batches in loop until it gets to the position of previous scroll
        if (scrollRootRef.value) {
            scrollRootRef.value.scrollTop = 0;
        }

        itemsRef.value = [];
        session.reset(queryRef.value);
        pushItems(session.nextBatch(batchSize));
    };

    let observer = null;

    const setupObserver = () => {
        removeObserver();

        if (scrollRootRef.value == null) {
            return;
        }

        if (!session.hasMore) {
            return;
        }

        observer = new IntersectionObserver(entries => {
            if (entries[0].isIntersecting) {
                pushItems(session.nextBatch(batchSize));
            }
        }, {
            root: scrollRootRef.value,
            rootMargin: '0% 0% 10% 0%',
            threshold: 0
        });

        const row = scrollRootRef.value.querySelector(':scope > table > tr:last-child');
        observer.observe(row);
    };

    const removeObserver = () => {
        if (observer) {
            observer.disconnect();
            observer = null;
        }
    }

    const debouncedRestartSearch = debounce(restartSearch, 200);
    watch(queryRef, () => debouncedRestartSearch(), { immediate: false });

    watch(scrollRootRef, (value) => {
        if (value) {
            setupObserver();
        } else {
            removeObserver();
        }
    });

    return restartSearch;
}