package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;

public record LockInputsConfig(
        boolean mouseInputDisabled,
        boolean holdForward,
        boolean holdAttack,
        boolean holdUse
) implements ImmutableConfig, ModuleStateProvider {

    public static LockInputsConfig defaults() {
        return new Builder()
                .mouseInputDisabled(false)
                .holdForward(false)
                .holdAttack(false)
                .holdUse(false)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return mouseInputDisabled || holdForward || holdAttack || holdUse;
    }

    // <generated-code>

    public LockInputsConfig withMouseInputDisabled(boolean mouseInputDisabled) {
        return Builder.from(this).mouseInputDisabled(mouseInputDisabled).build();
    }

    public LockInputsConfig withHoldForward(boolean holdForward) {
        return Builder.from(this).holdForward(holdForward).build();
    }

    public LockInputsConfig withHoldAttack(boolean holdAttack) {
        return Builder.from(this).holdAttack(holdAttack).build();
    }

    public LockInputsConfig withHoldUse(boolean holdUse) {
        return Builder.from(this).holdUse(holdUse).build();
    }

    public static class Builder {

        private boolean mouseInputDisabled;
        private boolean holdForward;
        private boolean holdAttack;
        private boolean holdUse;

        private Builder() {}

        public static Builder from(LockInputsConfig config) {
            return new Builder()
                    .mouseInputDisabled(config.mouseInputDisabled)
                    .holdForward(config.holdForward)
                    .holdAttack(config.holdAttack)
                    .holdUse(config.holdUse);
        }

        public Builder mouseInputDisabled(boolean mouseInputDisabled) {
            this.mouseInputDisabled = mouseInputDisabled;
            return this;
        }

        public Builder holdForward(boolean holdForward) {
            this.holdForward = holdForward;
            return this;
        }

        public Builder holdAttack(boolean holdAttack) {
            this.holdAttack = holdAttack;
            return this;
        }

        public Builder holdUse(boolean holdUse) {
            this.holdUse = holdUse;
            return this;
        }

        public LockInputsConfig build() {
            return new LockInputsConfig(mouseInputDisabled, holdForward, holdAttack, holdUse);
        }
    }

    // </generated-code>
}
