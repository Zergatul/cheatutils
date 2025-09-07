export function createComponent(template) {
    const args = {
        template: template,
        methods: {
            reset() {
                fetch('/api/reset-config', {
                    method: 'POST'
                }).then(async response => {
                    const errors = await response.json();
                    if (errors.length == 0) {
                        alert('OK');
                    } else {
                        alert(errors.join('\n'));
                    }
                });
            }
        }
    };
    return args;
}