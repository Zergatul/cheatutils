export async function get(url) {
    let response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Response code = ${response.status}`);
    }

    return await response.json();
}

export async function post(url, body) {
    let response = await fetch(url, {
        method: 'POST',
        body: JSON.stringify(body)
    });
    if (!response.ok) {
        throw new Error(`Response code = ${response.status}`);
    }

    return await response.json();
}