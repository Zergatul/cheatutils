package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;

public record MonacoEditorConfig(
        String json
) implements ImmutableConfig {

    public static MonacoEditorConfig defaults() {
        return new Builder()
                .json("")
                .build();
    }

    // <generated-code>

    public MonacoEditorConfig withJson(String json) {
        return Builder.from(this).json(json).build();
    }

    public static class Builder {

        private String json;

        private Builder() {}

        public static Builder from(MonacoEditorConfig config) {
            return new Builder()
                    .json(config.json);
        }

        public Builder json(String json) {
            this.json = json;
            return this;
        }

        public MonacoEditorConfig build() {
            return new MonacoEditorConfig(json);
        }
    }

    // </generated-code>
}