package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfigWithUpdateOptions;
import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.world.entity.Entity;

import java.awt.*;

public record EntityEspEntry(
        boolean enabled,
        Class<?> type,
        double maxDistance,
        boolean drawTracers,
        double tracerWidth,
        Color tracerColor,
        Double tracerMaxDistance,
        boolean drawOutline,
        Color outlineColor,
        Double outlineMaxDistance,
        int outlineMethod,
        boolean drawBoundingBox,
        double boundingBoxWidth,
        Color boundingBoxColor,
        Double boundingBoxMaxDistance,
        boolean drawOverlay,
        Color overlayColor,
        boolean drawTitles,
        boolean showDefaultNames,
        boolean useRawNames,
        boolean showHp,
        boolean showEquippedItems,
        boolean showOwner,
        boolean scriptEnabled,
        String code
) implements ImmutableConfigWithUpdateOptions<EntityEspEntry.UpdateOptions> {

    public EntityEspEntry {
        tracerWidth = MathUtils.clamp(tracerWidth, 0.5, 100);
        if (tracerMaxDistance != null) {
            tracerMaxDistance = MathUtils.clamp(tracerMaxDistance, 0, 1e9);
        }

        if (outlineMaxDistance != null) {
            outlineMaxDistance = MathUtils.clamp(outlineMaxDistance, 0, 1e9);
        }

        boundingBoxWidth = MathUtils.clamp(boundingBoxWidth, 0.5, 100);
        if (boundingBoxMaxDistance != null) {
            boundingBoxMaxDistance = MathUtils.clamp(boundingBoxMaxDistance, 0, 1e9);
        }

        if (code != null && code.isBlank()) {
            code = null;
        }
    }

    public static EntityEspEntry defaults() {
        return defaults(null);
    }

    public static EntityEspEntry defaults(Class<?> type) {
        return new Builder()
                .enabled(false)
                .type(type)
                .maxDistance(64 * 16)
                .drawTracers(true)
                .tracerWidth(1)
                .tracerColor(Color.WHITE)
                .tracerMaxDistance(null)
                .drawOutline(true)
                .outlineColor(Color.WHITE)
                .outlineMaxDistance(null)
                .outlineMethod(0)
                .drawBoundingBox(false)
                .boundingBoxWidth(1)
                .boundingBoxColor(Color.WHITE)
                .boundingBoxMaxDistance(null)
                .drawOverlay(false)
                .overlayColor(new Color(0x80FFFFFF, true))
                .drawTitles(false)
                .showDefaultNames(false)
                .useRawNames(false)
                .showHp(false)
                .showEquippedItems(false)
                .showOwner(false)
                .scriptEnabled(false)
                .code(null)
                .build();
    }

    public boolean isValidEntity(Entity entity) {
        return type.isInstance(entity);
    }

    public double getTracerMaxDistanceSqr() {
        if (tracerMaxDistance != null) {
            return tracerMaxDistance * tracerMaxDistance;
        } else {
            return maxDistance * maxDistance;
        }
    }

    public double getOutlineMaxDistanceSqr() {
        if (outlineMaxDistance != null) {
            return outlineMaxDistance * outlineMaxDistance;
        } else {
            return maxDistance * maxDistance;
        }
    }

    public double getBoundingBoxMaxDistanceSqr() {
        if (boundingBoxMaxDistance != null) {
            return boundingBoxMaxDistance * boundingBoxMaxDistance;
        } else {
            return maxDistance * maxDistance;
        }
    }

    public double getOverlayMaxDistanceSqr() {
        return maxDistance * maxDistance;
    }

    public boolean useMinecraftOutline() {
        return this.enabled && this.drawOutline && this.outlineMethod == 0;
    }

    public boolean useModOutline() {
        return this.drawOutline && this.outlineMethod == 1;
    }

    // <generated-code>

    public EntityEspEntry withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public EntityEspEntry withType(Class<?> type) {
        return Builder.from(this).type(type).build();
    }

    public EntityEspEntry withMaxDistance(double maxDistance) {
        return Builder.from(this).maxDistance(maxDistance).build();
    }

    public EntityEspEntry withDrawTracers(boolean drawTracers) {
        return Builder.from(this).drawTracers(drawTracers).build();
    }

    public EntityEspEntry withTracerWidth(double tracerWidth) {
        return Builder.from(this).tracerWidth(tracerWidth).build();
    }

    public EntityEspEntry withTracerColor(Color tracerColor) {
        return Builder.from(this).tracerColor(tracerColor).build();
    }

    public EntityEspEntry withTracerMaxDistance(Double tracerMaxDistance) {
        return Builder.from(this).tracerMaxDistance(tracerMaxDistance).build();
    }

    public EntityEspEntry withDrawOutline(boolean drawOutline) {
        return Builder.from(this).drawOutline(drawOutline).build();
    }

    public EntityEspEntry withOutlineColor(Color outlineColor) {
        return Builder.from(this).outlineColor(outlineColor).build();
    }

    public EntityEspEntry withOutlineMaxDistance(Double outlineMaxDistance) {
        return Builder.from(this).outlineMaxDistance(outlineMaxDistance).build();
    }

    public EntityEspEntry withOutlineMethod(int outlineMethod) {
        return Builder.from(this).outlineMethod(outlineMethod).build();
    }

    public EntityEspEntry withDrawBoundingBox(boolean drawBoundingBox) {
        return Builder.from(this).drawBoundingBox(drawBoundingBox).build();
    }

    public EntityEspEntry withBoundingBoxWidth(double boundingBoxWidth) {
        return Builder.from(this).boundingBoxWidth(boundingBoxWidth).build();
    }

    public EntityEspEntry withBoundingBoxColor(Color boundingBoxColor) {
        return Builder.from(this).boundingBoxColor(boundingBoxColor).build();
    }

    public EntityEspEntry withBoundingBoxMaxDistance(Double boundingBoxMaxDistance) {
        return Builder.from(this).boundingBoxMaxDistance(boundingBoxMaxDistance).build();
    }

    public EntityEspEntry withDrawOverlay(boolean drawOverlay) {
        return Builder.from(this).drawOverlay(drawOverlay).build();
    }

    public EntityEspEntry withOverlayColor(Color overlayColor) {
        return Builder.from(this).overlayColor(overlayColor).build();
    }

    public EntityEspEntry withDrawTitles(boolean drawTitles) {
        return Builder.from(this).drawTitles(drawTitles).build();
    }

    public EntityEspEntry withShowDefaultNames(boolean showDefaultNames) {
        return Builder.from(this).showDefaultNames(showDefaultNames).build();
    }

    public EntityEspEntry withUseRawNames(boolean useRawNames) {
        return Builder.from(this).useRawNames(useRawNames).build();
    }

    public EntityEspEntry withShowHp(boolean showHp) {
        return Builder.from(this).showHp(showHp).build();
    }

    public EntityEspEntry withShowEquippedItems(boolean showEquippedItems) {
        return Builder.from(this).showEquippedItems(showEquippedItems).build();
    }

    public EntityEspEntry withShowOwner(boolean showOwner) {
        return Builder.from(this).showOwner(showOwner).build();
    }

    public EntityEspEntry withScriptEnabled(boolean scriptEnabled) {
        return Builder.from(this).scriptEnabled(scriptEnabled).build();
    }

    public EntityEspEntry withCode(String code) {
        return Builder.from(this).code(code).build();
    }

    @Override
    public EntityEspEntry withOptions(UpdateOptions options) {
        return Builder.from(this)
                .enabled(options.enabled())
                .type(options.type())
                .maxDistance(options.maxDistance())
                .drawTracers(options.drawTracers())
                .tracerWidth(options.tracerWidth())
                .tracerColor(options.tracerColor())
                .tracerMaxDistance(options.tracerMaxDistance())
                .drawOutline(options.drawOutline())
                .outlineColor(options.outlineColor())
                .outlineMaxDistance(options.outlineMaxDistance())
                .outlineMethod(options.outlineMethod())
                .drawBoundingBox(options.drawBoundingBox())
                .boundingBoxWidth(options.boundingBoxWidth())
                .boundingBoxColor(options.boundingBoxColor())
                .boundingBoxMaxDistance(options.boundingBoxMaxDistance())
                .drawOverlay(options.drawOverlay())
                .overlayColor(options.overlayColor())
                .drawTitles(options.drawTitles())
                .showDefaultNames(options.showDefaultNames())
                .useRawNames(options.useRawNames())
                .showHp(options.showHp())
                .showEquippedItems(options.showEquippedItems())
                .showOwner(options.showOwner())
                .scriptEnabled(options.scriptEnabled())
                .build();
    }

    public record UpdateOptions(
            boolean enabled,
            Class<?> type,
            double maxDistance,
            boolean drawTracers,
            double tracerWidth,
            Color tracerColor,
            Double tracerMaxDistance,
            boolean drawOutline,
            Color outlineColor,
            Double outlineMaxDistance,
            int outlineMethod,
            boolean drawBoundingBox,
            double boundingBoxWidth,
            Color boundingBoxColor,
            Double boundingBoxMaxDistance,
            boolean drawOverlay,
            Color overlayColor,
            boolean drawTitles,
            boolean showDefaultNames,
            boolean useRawNames,
            boolean showHp,
            boolean showEquippedItems,
            boolean showOwner,
            boolean scriptEnabled
    ) {}

    public static class Builder {

        private boolean enabled;
        private Class<?> type;
        private double maxDistance;
        private boolean drawTracers;
        private double tracerWidth;
        private Color tracerColor;
        private Double tracerMaxDistance;
        private boolean drawOutline;
        private Color outlineColor;
        private Double outlineMaxDistance;
        private int outlineMethod;
        private boolean drawBoundingBox;
        private double boundingBoxWidth;
        private Color boundingBoxColor;
        private Double boundingBoxMaxDistance;
        private boolean drawOverlay;
        private Color overlayColor;
        private boolean drawTitles;
        private boolean showDefaultNames;
        private boolean useRawNames;
        private boolean showHp;
        private boolean showEquippedItems;
        private boolean showOwner;
        private boolean scriptEnabled;
        private String code;

        private Builder() {}

        public static Builder from(EntityEspEntry config) {
            return new Builder()
                    .enabled(config.enabled)
                    .type(config.type)
                    .maxDistance(config.maxDistance)
                    .drawTracers(config.drawTracers)
                    .tracerWidth(config.tracerWidth)
                    .tracerColor(config.tracerColor)
                    .tracerMaxDistance(config.tracerMaxDistance)
                    .drawOutline(config.drawOutline)
                    .outlineColor(config.outlineColor)
                    .outlineMaxDistance(config.outlineMaxDistance)
                    .outlineMethod(config.outlineMethod)
                    .drawBoundingBox(config.drawBoundingBox)
                    .boundingBoxWidth(config.boundingBoxWidth)
                    .boundingBoxColor(config.boundingBoxColor)
                    .boundingBoxMaxDistance(config.boundingBoxMaxDistance)
                    .drawOverlay(config.drawOverlay)
                    .overlayColor(config.overlayColor)
                    .drawTitles(config.drawTitles)
                    .showDefaultNames(config.showDefaultNames)
                    .useRawNames(config.useRawNames)
                    .showHp(config.showHp)
                    .showEquippedItems(config.showEquippedItems)
                    .showOwner(config.showOwner)
                    .scriptEnabled(config.scriptEnabled)
                    .code(config.code);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder type(Class<?> type) {
            this.type = type;
            return this;
        }

        public Builder maxDistance(double maxDistance) {
            this.maxDistance = maxDistance;
            return this;
        }

        public Builder drawTracers(boolean drawTracers) {
            this.drawTracers = drawTracers;
            return this;
        }

        public Builder tracerWidth(double tracerWidth) {
            this.tracerWidth = tracerWidth;
            return this;
        }

        public Builder tracerColor(Color tracerColor) {
            this.tracerColor = tracerColor;
            return this;
        }

        public Builder tracerMaxDistance(Double tracerMaxDistance) {
            this.tracerMaxDistance = tracerMaxDistance;
            return this;
        }

        public Builder drawOutline(boolean drawOutline) {
            this.drawOutline = drawOutline;
            return this;
        }

        public Builder outlineColor(Color outlineColor) {
            this.outlineColor = outlineColor;
            return this;
        }

        public Builder outlineMaxDistance(Double outlineMaxDistance) {
            this.outlineMaxDistance = outlineMaxDistance;
            return this;
        }

        public Builder outlineMethod(int outlineMethod) {
            this.outlineMethod = outlineMethod;
            return this;
        }

        public Builder drawBoundingBox(boolean drawBoundingBox) {
            this.drawBoundingBox = drawBoundingBox;
            return this;
        }

        public Builder boundingBoxWidth(double boundingBoxWidth) {
            this.boundingBoxWidth = boundingBoxWidth;
            return this;
        }

        public Builder boundingBoxColor(Color boundingBoxColor) {
            this.boundingBoxColor = boundingBoxColor;
            return this;
        }

        public Builder boundingBoxMaxDistance(Double boundingBoxMaxDistance) {
            this.boundingBoxMaxDistance = boundingBoxMaxDistance;
            return this;
        }

        public Builder drawOverlay(boolean drawOverlay) {
            this.drawOverlay = drawOverlay;
            return this;
        }

        public Builder overlayColor(Color overlayColor) {
            this.overlayColor = overlayColor;
            return this;
        }

        public Builder drawTitles(boolean drawTitles) {
            this.drawTitles = drawTitles;
            return this;
        }

        public Builder showDefaultNames(boolean showDefaultNames) {
            this.showDefaultNames = showDefaultNames;
            return this;
        }

        public Builder useRawNames(boolean useRawNames) {
            this.useRawNames = useRawNames;
            return this;
        }

        public Builder showHp(boolean showHp) {
            this.showHp = showHp;
            return this;
        }

        public Builder showEquippedItems(boolean showEquippedItems) {
            this.showEquippedItems = showEquippedItems;
            return this;
        }

        public Builder showOwner(boolean showOwner) {
            this.showOwner = showOwner;
            return this;
        }

        public Builder scriptEnabled(boolean scriptEnabled) {
            this.scriptEnabled = scriptEnabled;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public EntityEspEntry build() {
            return new EntityEspEntry(enabled, type, maxDistance, drawTracers, tracerWidth, tracerColor, tracerMaxDistance, drawOutline, outlineColor, outlineMaxDistance, outlineMethod, drawBoundingBox, boundingBoxWidth, boundingBoxColor, boundingBoxMaxDistance, drawOverlay, overlayColor, drawTitles, showDefaultNames, useRawNames, showHp, showEquippedItems, showOwner, scriptEnabled, code);
        }
    }

    // </generated-code>
}