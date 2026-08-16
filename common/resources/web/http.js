async function throwIfFailed(response) {
    if (!response.ok) {
        throw {
            code: response.status,
            response: await response.text()
        };
    }
}

export async function get(url) {
    let response = await fetch(url);
    await throwIfFailed(response);
    return await response.json();
}

export async function getText(url) {
    let response = await fetch(url);
    await throwIfFailed(response);
    return await response.text();
}

export async function post(url, body) {
    let response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify(body)
    });
    await throwIfFailed(response);
    return await response.json();
}

export async function put(url, body) {
    let response = await fetch(url, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify(body)
    });
    await throwIfFailed(response);
    return await response.json();
}

async function delete$(url, body) {
    let response = await fetch(url, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify(body)
    });
    await throwIfFailed(response);
    return await response.json();
}

export { delete$ as delete }