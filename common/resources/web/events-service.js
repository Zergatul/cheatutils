const listeners = [];

export function subscribe(listener) {
    listeners.push(listener);
}

export function unsubscribe(listener) {
    const index = listeners.indexOf(listener);
    if (index >= 0) {
        listeners.splice(index, 1);
    }
}

export function trigger(event) {
    for (let listener of listeners) {
        listener(event);
    }
}