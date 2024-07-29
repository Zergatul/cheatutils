function formatCodeResponse(response) {
    return response.data.map(m => `Ln ${m.range.line || m.range.line1}, Col ${m.range.column || m.range.column1}: ${m.message}`).join('\n');
}

function handleCodeSave(url, code) {
    axios.post(url, code).then(response => {
        if (response.data.ok) {
            alert('Saved');
        } else {
            alert(formatCodeResponse(response));
        }
    }, error => {
        alert(error.response.data);
    });
}

export { formatCodeResponse, handleCodeSave }