package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;
import com.zergatul.cheatutils.utils.MathUtils;

public record MovementHackConfig(
        boolean disableSlowdownOnUseItem,
        boolean scaleInputVector,
        double inputVectorFactor,
        boolean disableCrouchingSlowdown,
        boolean antiKnockback,
        boolean antiPush,
        boolean scaleJumpHeight,
        double jumpHeightFactor,
        boolean disableWaterPush
) implements ImmutableConfig, ModuleStateProvider {

    public MovementHackConfig {
        inputVectorFactor = MathUtils.clamp(inputVectorFactor, 0.01, 1000);
        jumpHeightFactor = MathUtils.clamp(jumpHeightFactor, 0.01, 1000);
    }

    public static MovementHackConfig defaults() {
        return new Builder()
                .disableSlowdownOnUseItem(false)
                .scaleInputVector(false)
                .inputVectorFactor(1)
                .disableCrouchingSlowdown(false)
                .antiKnockback(false)
                .antiPush(false)
                .scaleJumpHeight(false)
                .jumpHeightFactor(1)
                .disableWaterPush(false)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return disableSlowdownOnUseItem || scaleInputVector || disableCrouchingSlowdown || antiKnockback || antiPush || scaleJumpHeight || disableWaterPush;
    }

    // <generated-code>

    public MovementHackConfig withDisableSlowdownOnUseItem(boolean disableSlowdownOnUseItem) {
        return Builder.from(this).disableSlowdownOnUseItem(disableSlowdownOnUseItem).build();
    }

    public MovementHackConfig withScaleInputVector(boolean scaleInputVector) {
        return Builder.from(this).scaleInputVector(scaleInputVector).build();
    }

    public MovementHackConfig withInputVectorFactor(double inputVectorFactor) {
        return Builder.from(this).inputVectorFactor(inputVectorFactor).build();
    }

    public MovementHackConfig withDisableCrouchingSlowdown(boolean disableCrouchingSlowdown) {
        return Builder.from(this).disableCrouchingSlowdown(disableCrouchingSlowdown).build();
    }

    public MovementHackConfig withAntiKnockback(boolean antiKnockback) {
        return Builder.from(this).antiKnockback(antiKnockback).build();
    }

    public MovementHackConfig withAntiPush(boolean antiPush) {
        return Builder.from(this).antiPush(antiPush).build();
    }

    public MovementHackConfig withScaleJumpHeight(boolean scaleJumpHeight) {
        return Builder.from(this).scaleJumpHeight(scaleJumpHeight).build();
    }

    public MovementHackConfig withJumpHeightFactor(double jumpHeightFactor) {
        return Builder.from(this).jumpHeightFactor(jumpHeightFactor).build();
    }

    public MovementHackConfig withDisableWaterPush(boolean disableWaterPush) {
        return Builder.from(this).disableWaterPush(disableWaterPush).build();
    }

    public static class Builder {

        private boolean disableSlowdownOnUseItem;
        private boolean scaleInputVector;
        private double inputVectorFactor;
        private boolean disableCrouchingSlowdown;
        private boolean antiKnockback;
        private boolean antiPush;
        private boolean scaleJumpHeight;
        private double jumpHeightFactor;
        private boolean disableWaterPush;

        private Builder() {}

        public static Builder from(MovementHackConfig config) {
            return new Builder()
                    .disableSlowdownOnUseItem(config.disableSlowdownOnUseItem)
                    .scaleInputVector(config.scaleInputVector)
                    .inputVectorFactor(config.inputVectorFactor)
                    .disableCrouchingSlowdown(config.disableCrouchingSlowdown)
                    .antiKnockback(config.antiKnockback)
                    .antiPush(config.antiPush)
                    .scaleJumpHeight(config.scaleJumpHeight)
                    .jumpHeightFactor(config.jumpHeightFactor)
                    .disableWaterPush(config.disableWaterPush);
        }

        public Builder disableSlowdownOnUseItem(boolean disableSlowdownOnUseItem) {
            this.disableSlowdownOnUseItem = disableSlowdownOnUseItem;
            return this;
        }

        public Builder scaleInputVector(boolean scaleInputVector) {
            this.scaleInputVector = scaleInputVector;
            return this;
        }

        public Builder inputVectorFactor(double inputVectorFactor) {
            this.inputVectorFactor = inputVectorFactor;
            return this;
        }

        public Builder disableCrouchingSlowdown(boolean disableCrouchingSlowdown) {
            this.disableCrouchingSlowdown = disableCrouchingSlowdown;
            return this;
        }

        public Builder antiKnockback(boolean antiKnockback) {
            this.antiKnockback = antiKnockback;
            return this;
        }

        public Builder antiPush(boolean antiPush) {
            this.antiPush = antiPush;
            return this;
        }

        public Builder scaleJumpHeight(boolean scaleJumpHeight) {
            this.scaleJumpHeight = scaleJumpHeight;
            return this;
        }

        public Builder jumpHeightFactor(double jumpHeightFactor) {
            this.jumpHeightFactor = jumpHeightFactor;
            return this;
        }

        public Builder disableWaterPush(boolean disableWaterPush) {
            this.disableWaterPush = disableWaterPush;
            return this;
        }

        public MovementHackConfig build() {
            return new MovementHackConfig(disableSlowdownOnUseItem, scaleInputVector, inputVectorFactor, disableCrouchingSlowdown, antiKnockback, antiPush, scaleJumpHeight, jumpHeightFactor, disableWaterPush);
        }
    }

    // </generated-code>
}