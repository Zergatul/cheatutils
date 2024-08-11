export async function get(url) {
    let response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Response code = ${response.status}`);
    }

    return await response.json();
}

export function post() {

}