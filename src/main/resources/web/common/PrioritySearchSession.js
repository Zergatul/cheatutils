export class PrioritySearchSession {

    constructor(itemsRef, predicates) {
        this.itemsRef = itemsRef;
        this.predicates = predicates;
        this.query = '';
        this.predicateCursor = 0;
        this.itemsCursor = 0;
        this.hasMore = true;
    }

    reset(query) {
        this.query = (query ?? '').toLowerCase();
        this.predicateCursor = 0;
        this.itemsCursor = 0;
        this.hasMore = true;
        this.seen = new Set();
    }

    nextBatch(limit) {
        const predicates = this.predicates;
        const items = this.itemsRef.value ?? [];
        const filtered = [];
        while (this.predicateCursor < predicates.length) {
            const predicate = predicates[this.predicateCursor];
            while (this.itemsCursor < items.length) {
                const item = items[this.itemsCursor++];
                if (!this.seen.has(item) && predicate(item, this.query)) {
                    this.seen.add(item);
                    filtered.push(item);
                }
                if (filtered.length >= limit) {
                    break;
                }
            }

            if (filtered.length >= limit) {
                break;
            } else {
                this.predicateCursor++;
                this.itemsCursor = 0;
            }
        }

        this.hasMore = this.predicateCursor < predicates.length;

        return filtered;
    }
}