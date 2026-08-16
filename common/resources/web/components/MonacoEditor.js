import * as http from '/http.js';

function formatCodeResponse(response) {
    return response
        .map(message => `Ln ${message.range.line || message.range.line1}, Col ${message.range.column || message.range.column1}: ${message.message}`)
        .join('\n');
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