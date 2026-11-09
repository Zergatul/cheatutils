package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.common.RegistryExtensions;
import com.zergatul.cheatutils.configs.ImmutableConfigWithUpdateOptions;
import com.zergatul.cheatutils.utils.MathUtils;
import net.minecraft.world.level.block.Block;

import java.awt.*;
import java.util.Objects;

public record BlockEspEntry(
        boolean enabled,
        ImmutableList<Block> blocks,
        double maxDistance,
        boolean drawTracers,
        double tracerWidth,
        Color tracerColor,
        Double tracerMaxDistance,
        boolean drawBoundingBox,
        double boundingBoxWidth,
        Color boundingBoxColor,
        Double boundingBoxMaxDistance,
        boolean drawOverlay,
        Color overlayColor,
        boolean scriptEnabled,
        String code
) implements ImmutableConfigWithUpdateOptions<BlockEspEntry.UpdateOptions> {

    public BlockEspEntry {
        if (blocks == null) {
            blocks = ImmutableList.empty();
        }

        blocks = blocks
                .removeAll(Objects::isNull)
                .removeAll(b -> b == RegistryExtensions.DEFAULT_BLOCK)
                .distinct();

        tracerWidth = MathUtils.clamp(tracerWidth, 0.5, 100);
        if (tracerMaxDistance != null) {
            tracerMaxDistance = MathUtils.clamp(tracerMaxDistance, 0, 1e9);
        }

        boundingBoxWidth = MathUtils.clamp(boundingBoxWidth, 0.5, 100);
        if (boundingBoxMaxDistance != null) {
            boundingBoxMaxDistance = MathUtils.clamp(boundingBoxMaxDistance, 0, 1e9);
        }

        if (code != null && code.isBlank()) {
            code = null;
        }
    }

    public static BlockEspEntry defaults() {
        return new Builder()
                .enabled(false)
                .blocks(ImmutableList.empty())
                .maxDistance(64 * 16)
                .drawTracers(true)
                .tracerWidth(1)
                .tracerColor(Color.WHITE)
                .tracerMaxDistance(null)
                .drawBoundingBox(false)
                .boundingBoxWidth(1)
                .boundingBoxColor(Color.WHITE)
                .boundingBoxMaxDistance(null)
                .drawOverlay(false)
                .overlayColor(new Color(0x80FFFFFF, true))
                .scriptEnabled(false)
                .code(null)
                .build();
    }

    public double getTracerMaxDistanceSqr() {
        if (tracerMaxDistance != null) {
            return tracerMaxDistance * tracerMaxDistance;
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

    // <generated-code>

    public BlockEspEntry withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public BlockEspEntry withBlocks(ImmutableList<Block> blocks) {
        return Builder.from(this).blocks(blocks).build();
    }

    public BlockEspEntry withMaxDistance(double maxDistance) {
        return Builder.from(this).maxDistance(maxDistance).build();
    }

    public BlockEspEntry withDrawTracers(boolean drawTracers) {
        return Builder.from(this).drawTracers(drawTracers).build();
    }

    public BlockEspEntry withTracerWidth(double tracerWidth) {
        return Builder.from(this).tracerWidth(tracerWidth).build();
    }

    public BlockEspEntry withTracerColor(Color tracerColor) {
        return Builder.from(this).tracerColor(tracerColor).build();
    }

    public BlockEspEntry withTracerMaxDistance(Double tracerMaxDistance) {
        return Builder.from(this).tracerMaxDistance(tracerMaxDistance).build();
    }

    public BlockEspEntry withDrawBoundingBox(boolean drawBoundingBox) {
        return Builder.from(this).drawBoundingBox(drawBoundingBox).build();
    }

    public BlockEspEntry withBoundingBoxWidth(double boundingBoxWidth) {
        return Builder.from(this).boundingBoxWidth(boundingBoxWidth).build();
    }

    public BlockEspEntry withBoundingBoxColor(Color boundingBoxColor) {
        return Builder.from(this).boundingBoxColor(boundingBoxColor).build();
    }

    public BlockEspEntry withBoundingBoxMaxDistance(Double boundingBoxMaxDistance) {
        return Builder.from(this).boundingBoxMaxDistance(boundingBoxMaxDistance).build();
    }

    public BlockEspEntry withDrawOverlay(boolean drawOverlay) {
        return Builder.from(this).drawOverlay(drawOverlay).build();
    }

    public BlockEspEntry withOverlayColor(Color overlayColor) {
        return Builder.from(this).overlayColor(overlayColor).build();
    }

    public BlockEspEntry withScriptEnabled(boolean scriptEnabled) {
        return Builder.from(this).scriptEnabled(scriptEnabled).build();
    }

    public BlockEspEntry withCode(String code) {
        return Builder.from(this).code(code).build();
    }

    @Override
    public BlockEspEntry withOptions(UpdateOptions options) {
        return Builder.from(this)
                .enabled(options.enabled())
                .blocks(options.blocks())
                .maxDistance(options.maxDistance())
                .drawTracers(options.drawTracers())
                .tracerWidth(options.tracerWidth())
                .tracerColor(options.tracerColor())
                .tracerMaxDistance(options.tracerMaxDistance())
                .drawBoundingBox(options.drawBoundingBox())
                .boundingBoxWidth(options.boundingBoxWidth())
                .boundingBoxColor(options.boundingBoxColor())
                .boundingBoxMaxDistance(options.boundingBoxMaxDistance())
                .drawOverlay(options.drawOverlay())
                .overlayColor(options.overlayColor())
                .scriptEnabled(options.scriptEnabled())
                .build();
    }

    public record UpdateOptions(
            boolean enabled,
            ImmutableList<Block> blocks,
            double maxDistance,
            boolean drawTracers,
            double tracerWidth,
            Color tracerColor,
            Double tracerMaxDistance,
            boolean drawBoundingBox,
            double boundingBoxWidth,
            Color boundingBoxColor,
            Double boundingBoxMaxDistance,
            boolean drawOverlay,
            Color overlayColor,
            boolean scriptEnabled
    ) {}

    public static class Builder {

        private boolean enabled;
        private ImmutableList<Block> blocks;
        private double maxDistance;
        private boolean drawTracers;
        private double tracerWidth;
        private Color tracerColor;
        private Double tracerMaxDistance;
        private boolean drawBoundingBox;
        private double boundingBoxWidth;
        private Color boundingBoxColor;
        private Double boundingBoxMaxDistance;
        private boolean drawOverlay;
        private Color overlayColor;
        private boolean scriptEnabled;
        private String code;

        private Builder() {}

        public static Builder from(BlockEspEntry config) {
            return new Builder()
                    .enabled(config.enabled)
                    .blocks(config.blocks)
                    .maxDistance(config.maxDistance)
                    .drawTracers(config.drawTracers)
                    .tracerWidth(config.tracerWidth)
                    .tracerColor(config.tracerColor)
                    .tracerMaxDistance(config.tracerMaxDistance)
                    .drawBoundingBox(config.drawBoundingBox)
                    .boundingBoxWidth(config.boundingBoxWidth)
                    .boundingBoxColor(config.boundingBoxColor)
                    .boundingBoxMaxDistance(config.boundingBoxMaxDistance)
                    .drawOverlay(config.drawOverlay)
                    .overlayColor(config.overlayColor)
                    .scriptEnabled(config.scriptEnabled)
                    .code(config.code);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder blocks(ImmutableList<Block> blocks) {
            this.blocks = blocks;
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

        public Builder scriptEnabled(boolean scriptEnabled) {
            this.scriptEnabled = scriptEnabled;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public BlockEspEntry build() {
            return new BlockEspEntry(enabled, blocks, maxDistance, drawTracers, tracerWidth, tracerColor, tracerMaxDistance, drawBoundingBox, boundingBoxWidth, boundingBoxColor, boundingBoxMaxDistance, drawOverlay, overlayColor, scriptEnabled, code);
        }
    }

    // </generated-code>
}