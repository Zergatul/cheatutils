package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;

public record AimAssistConfig(
        boolean bowAssist,
        String aimAssistMode
) implements ImmutableConfig, ModuleStateProvider {

    public static final String AIM_ASSIST_CENTER = "AIM_ASSIST_CENTER";
    public static final String AIM_ASSIST_HEAD = "AIM_ASSIST_HEAD";

    public static AimAssistConfig defaults() {
        return new Builder()
                .bowAssist(false)
                .aimAssistMode(AIM_ASSIST_CENTER)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return bowAssist;
    }

    // <generated-code>

    public AimAssistConfig withBowAssist(boolean bowAssist) {
        return Builder.from(this).bowAssist(bowAssist).build();
    }

    public AimAssistConfig withAimAssistMode(String aimAssistMode) {
        return Builder.from(this).aimAssistMode(aimAssistMode).build();
    }

    public static class Builder {

        private boolean bowAssist;
        private String aimAssistMode;

        private Builder() {}

        public static Builder from(AimAssistConfig config) {
            return new Builder()
                    .bowAssist(config.bowAssist)
                    .aimAssistMode(config.aimAssistMode);
        }

        public Builder bowAssist(boolean bowAssist) {
            this.bowAssist = bowAssist;
            return this;
        }

        public Builder aimAssistMode(String aimAssistMode) {
            this.aimAssistMode = aimAssistMode;
            return this;
        }

        public AimAssistConfig build() {
            return new AimAssistConfig(bowAssist, aimAssistMode);
        }
    }

    // </generated-code>
}