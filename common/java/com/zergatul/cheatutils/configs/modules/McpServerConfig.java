package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;

public record McpServerConfig(
        boolean enabled,
        boolean allowSavingScripts,
        boolean allowEvaluatingExpressions,
        boolean allowExecutingCode,
        boolean allowScreenshots
) implements ImmutableConfig, ToggleableConfig<McpServerConfig> {

    public static McpServerConfig defaults() {
        return new Builder()
                .enabled(false)
                .allowSavingScripts(false)
                .allowEvaluatingExpressions(false)
                .allowExecutingCode(false)
                .allowScreenshots(false)
                .build();
    }

    // <generated-code>

    public McpServerConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public McpServerConfig withAllowSavingScripts(boolean allowSavingScripts) {
        return Builder.from(this).allowSavingScripts(allowSavingScripts).build();
    }

    public McpServerConfig withAllowEvaluatingExpressions(boolean allowEvaluatingExpressions) {
        return Builder.from(this).allowEvaluatingExpressions(allowEvaluatingExpressions).build();
    }

    public McpServerConfig withAllowExecutingCode(boolean allowExecutingCode) {
        return Builder.from(this).allowExecutingCode(allowExecutingCode).build();
    }

    public McpServerConfig withAllowScreenshots(boolean allowScreenshots) {
        return Builder.from(this).allowScreenshots(allowScreenshots).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean allowSavingScripts;
        private boolean allowEvaluatingExpressions;
        private boolean allowExecutingCode;
        private boolean allowScreenshots;

        private Builder() {}

        public static Builder from(McpServerConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .allowSavingScripts(config.allowSavingScripts)
                    .allowEvaluatingExpressions(config.allowEvaluatingExpressions)
                    .allowExecutingCode(config.allowExecutingCode)
                    .allowScreenshots(config.allowScreenshots);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder allowSavingScripts(boolean allowSavingScripts) {
            this.allowSavingScripts = allowSavingScripts;
            return this;
        }

        public Builder allowEvaluatingExpressions(boolean allowEvaluatingExpressions) {
            this.allowEvaluatingExpressions = allowEvaluatingExpressions;
            return this;
        }

        public Builder allowExecutingCode(boolean allowExecutingCode) {
            this.allowExecutingCode = allowExecutingCode;
            return this;
        }

        public Builder allowScreenshots(boolean allowScreenshots) {
            this.allowScreenshots = allowScreenshots;
            return this;
        }

        public McpServerConfig build() {
            return new McpServerConfig(enabled, allowSavingScripts, allowEvaluatingExpressions, allowExecutingCode, allowScreenshots);
        }
    }

    // </generated-code>
}