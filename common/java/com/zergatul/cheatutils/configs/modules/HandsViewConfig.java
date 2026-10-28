package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.world.phys.Vec3;

public record HandsViewConfig(
        boolean enabled,
        boolean renderArms,
        Vec3 armsScale,
        Vec3 armsShift,
        boolean renderItems,
        Vec3 itemsScale,
        Vec3 itemsShift
) implements ImmutableConfig, ToggleableConfig<HandsViewConfig> {

    public HandsViewConfig {
        if (armsScale == null) {
            armsScale = defaultScale();
        }
        if (armsShift == null) {
            armsShift = defaultShift();
        }
        if (itemsScale == null) {
            itemsScale = defaultScale();
        }
        if (itemsShift == null) {
            itemsShift = defaultShift();
        }

        armsScale = sanitizeScale(armsScale);
        armsShift = sanitizeShift(armsShift);
        itemsScale = sanitizeScale(itemsScale);
        itemsShift = sanitizeShift(itemsShift);
    }

    public static HandsViewConfig defaults() {
        return new Builder()
                .enabled(false)
                .renderArms(true)
                .armsScale(defaultScale())
                .armsShift(defaultShift())
                .renderItems(true)
                .itemsScale(defaultScale())
                .itemsShift(defaultShift())
                .build();
    }

    private static Vec3 sanitizeScale(Vec3 scale) {
        return new Vec3(
                MathUtils.clamp(scale.x, 0, 100),
                MathUtils.clamp(scale.y, 0, 100),
                MathUtils.clamp(scale.z, 0, 100));
    }

    private static Vec3 sanitizeShift(Vec3 shift) {
        return new Vec3(
                MathUtils.clamp(shift.x, -10, 10),
                MathUtils.clamp(shift.y, -10, 10),
                MathUtils.clamp(shift.z, -10, 10));
    }

    private static Vec3 defaultScale() {
        return new Vec3(1, 1, 1);
    }

    private static Vec3 defaultShift() {
        return Vec3.ZERO;
    }

    // <generated-code>

    public HandsViewConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public HandsViewConfig withRenderArms(boolean renderArms) {
        return Builder.from(this).renderArms(renderArms).build();
    }

    public HandsViewConfig withArmsScale(Vec3 armsScale) {
        return Builder.from(this).armsScale(armsScale).build();
    }

    public HandsViewConfig withArmsShift(Vec3 armsShift) {
        return Builder.from(this).armsShift(armsShift).build();
    }

    public HandsViewConfig withRenderItems(boolean renderItems) {
        return Builder.from(this).renderItems(renderItems).build();
    }

    public HandsViewConfig withItemsScale(Vec3 itemsScale) {
        return Builder.from(this).itemsScale(itemsScale).build();
    }

    public HandsViewConfig withItemsShift(Vec3 itemsShift) {
        return Builder.from(this).itemsShift(itemsShift).build();
    }

    public static class Builder {

        private boolean enabled;
        private boolean renderArms;
        private Vec3 armsScale;
        private Vec3 armsShift;
        private boolean renderItems;
        private Vec3 itemsScale;
        private Vec3 itemsShift;

        private Builder() {}

        public static Builder from(HandsViewConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .renderArms(config.renderArms)
                    .armsScale(config.armsScale)
                    .armsShift(config.armsShift)
                    .renderItems(config.renderItems)
                    .itemsScale(config.itemsScale)
                    .itemsShift(config.itemsShift);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder renderArms(boolean renderArms) {
            this.renderArms = renderArms;
            return this;
        }

        public Builder armsScale(Vec3 armsScale) {
            this.armsScale = armsScale;
            return this;
        }

        public Builder armsShift(Vec3 armsShift) {
            this.armsShift = armsShift;
            return this;
        }

        public Builder renderItems(boolean renderItems) {
            this.renderItems = renderItems;
            return this;
        }

        public Builder itemsScale(Vec3 itemsScale) {
            this.itemsScale = itemsScale;
            return this;
        }

        public Builder itemsShift(Vec3 itemsShift) {
            this.itemsShift = itemsShift;
            return this;
        }

        public HandsViewConfig build() {
            return new HandsViewConfig(enabled, renderArms, armsScale, armsShift, renderItems, itemsScale, itemsShift);
        }
    }

    // </generated-code>
}