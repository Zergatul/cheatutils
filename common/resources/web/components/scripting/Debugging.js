import * as http from '/http.js'

const delays = [1000, 200, 15];

export function createComponent(template) {
    return {
        template: template,
        mounted() {
            this.restartInterval();
        },
        unmounted() {
            clearInterval(this.interval);
        },
        data() {
            return {
                autoRefresh: true,
                autoScroll: true,
                interval: null,
                lastId: 0,
                busy: false,
                delay: 0
            };
        },
        methods: {
            getDelay() {
                return delays[this.delay];
            },
            clear() {
                while (this.$refs.messages.lastChild) {
                    this.$refs.messages.removeChild(this.$refs.messages.lastChild);
                }
            },
            restartInterval() {
                if (this.interval != null) {
                    clearInterval(this.interval);
                }

                if (!this.autoRefresh) {
                    return;
                }

                this.interval = setInterval(async () => {
                    if (this.busy) {
                        return;
                    }
    
                    this.busy = true;
                    let response = await http.get(`/api/debugging/${this.lastId}`);
                    this.lastId = response.lastId;
                    for (let entry of response.entries) {
                        let div = document.createElement('div');
                        let span1 = document.createElement('span');
                        span1.classList.add('time');
                        span1.innerText = `[${entry.time}] `;
                        let span2 = document.createElement('span');
                        span2.classList.add('msg');
                        span2.innerText = entry.message;
                        div.appendChild(span1);
                        div.appendChild(span2);
                        this.$refs.messages.appendChild(div);
                    }
                    if (this.autoScroll && this.$refs.messages.lastChild && response.entries.length > 0) {
                        this.$refs.messages.lastChild.scrollIntoView(false);
                    }
                    this.busy = false;
                }, this.getDelay());
            },
            toggleAutoRefresh() {
                this.autoRefresh = !this.autoRefresh;
                this.restartInterval();
            },
            toggleAutoScroll() {
                this.autoScroll = !this.autoScroll;
            },
            toggleDelay() {
                this.delay = (this.delay + 1) % delays.length;
                this.restartInterval();
            }
        }
    }
}