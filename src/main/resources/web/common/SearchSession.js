export class SearchSession {

    constructor(itemsRef, searchCallback) {
        this.itemsRef = itemsRef;
        this.searchCallback = searchCallback;
        this.query = '';
        this.cursor = 0;
        this.hasMore = true;
    }

    reset(query) {
        this.query = (query ?? '').toLowerCase();
        this.cursor = 0;
        this.hasMore = true;
    }

    nextBatch(limit) {
        const items = this.itemsRef.value ?? [];
        const filtered = [];
        for (; this.cursor < items.length; this.cursor++) {
            if (this.searchCallback(items[this.cursor], this.query)) {
                filtered.push(items[this.cursor]);
            }
            if (filtered.length >= limit) {
                this.cursor++;
                break;
            }
        }

        if (this.cursor >= items.length) {
            this.hasMore = false;
        }

        return filtered;
    }
}