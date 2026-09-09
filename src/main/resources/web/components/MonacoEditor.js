import * as http from '/http.js';

function formatCodeResponse(response) {
    return response.map(m => `Ln ${m.range.line || m.range.line1}, Col ${m.range.column || m.range.column1}: ${m.message}`).join('\n');
}

function handleCodeSave(url, code) {
    http.post(url, code).then(response => {
        if (response.ok) {
            alert('Saved');
        } else {
            alert(formatCodeResponse(response));
        }
    }, error => {
        alert(error.response);
    });
}

export { formatCodeResponse, handleCodeSave }