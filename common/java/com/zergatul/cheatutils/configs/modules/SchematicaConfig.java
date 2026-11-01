package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ToggleableConfig;
import com.zergatul.cheatutils.configs.utility.BlockPlacerConfig;
import com.zergatul.cheatutils.utils.MathUtils;

public record SchematicaConfig(
        boolean enabled,
        double maxRange,
        ImmutableList<Integer> autoSelectSlots,
        boolean attachToAir,
        boolean useShift,
        double placementRate,
        boolean autoRotate,
        boolean renderBlocks,
        boolean shadeBlocks,
        boolean showMissingBlockTracers,
        double missingBlockTracersMaxDistance,
        boolean showMissingBlockCubes,
        double missingBlockCubesMaxDistance,
        boolean showWrongBlockTracers,
        double wrongBlockTracersMaxDistance,
        boolean showWrongBlockCubes,
        double wrongBlockCubesMaxDistance,
        boolean replaceableAsAir,
        boolean airAlwaysValid,
        boolean autoBuild,
        SchematicaWorldSlice create
) implements ImmutableConfig, ToggleableConfig<SchematicaConfig>, BlockPlacerConfig {

    public SchematicaConfig {
        maxRange = MathUtils.clamp(maxRange, 1, 10);
        autoSelectSlots = BlockPlacerConfig.sanitizeAutoSelectSlots(autoSelectSlots);
        placementRate = MathUtils.clamp(placementRate, 1, 100);

        missingBlockTracersMaxDistance = MathUtils.clamp(missingBlockTracersMaxDistance, 1, 1000);
        missingBlockCubesMaxDistance = MathUtils.clamp(missingBlockCubesMaxDistance, 1, 1000);
        wrongBlockTracersMaxDistance = MathUtils.clamp(wrongBlockTracersMaxDistance, 1, 1000);
        wrongBlockCubesMaxDistance = MathUtils.clamp(wrongBlockCubesMaxDistance, 1, 1000);

        if (create == null) {
            create = SchematicaWorldSlice.defaults();
        }
    }

    public static SchematicaConfig defaults() {
        return new Builder()
                .enabled(false)
                .maxRange(5)
                .autoSelectSlots(ImmutableList.<Integer>empty().add(9))
                .attachToAir(false)
                .useShift(false)
                .placementRate(1)
                .autoRotate(false)
                .renderBlocks(true)
                .shadeBlocks(true)
                .showMissingBlockTracers(false)
                .missingBlockTracersMaxDistance(30)
                .showMissingBlockCubes(true)
                .missingBlockCubesMaxDistance(100)
                .showWrongBlockTracers(false)
                .wrongBlockTracersMaxDistance(10)
                .showWrongBlockCubes(false)
                .wrongBlockCubesMaxDistance(10)
                .replaceableAsAir(false)
                .airAlwaysValid(false)
                .autoBuild(false)
                .create(SchematicaWorldSlice.defaults())
                .build();
    }

    @Override
    public ImmutableList<Integer> getAutoSelectSlots() {
        return autoSelectSlots;
    }

    @Override
    public boolean shouldAttachToAir() {
        return attachToAir;
    }

    @Override
    public boolean shouldUseShift() {
        return useShift;
    }

    @Override
    public double getPlacementRate() {
        return placementRate;
    }

    @Override
    public double getMaxRange() {
        return maxRange;
    }

    @Override
    public boolean shouldAutoRotate() {
        return autoRotate;
    }

    // <generated-code>

    public SchematicaConfig withEnabled(boolean enabled) {
        return Builder.from(this).enabled(enabled).build();
    }

    public SchematicaConfig withMaxRange(double maxRange) {
        return Builder.from(this).maxRange(maxRange).build();
    }

    public SchematicaConfig withAutoSelectSlots(ImmutableList<Integer> autoSelectSlots) {
        return Builder.from(this).autoSelectSlots(autoSelectSlots).build();
    }

    public SchematicaConfig withAttachToAir(boolean attachToAir) {
        return Builder.from(this).attachToAir(attachToAir).build();
    }

    public SchematicaConfig withUseShift(boolean useShift) {
        return Builder.from(this).useShift(useShift).build();
    }

    public SchematicaConfig withPlacementRate(double placementRate) {
        return Builder.from(this).placementRate(placementRate).build();
    }

    public SchematicaConfig withAutoRotate(boolean autoRotate) {
        return Builder.from(this).autoRotate(autoRotate).build();
    }

    public SchematicaConfig withRenderBlocks(boolean renderBlocks) {
        return Builder.from(this).renderBlocks(renderBlocks).build();
    }

    public SchematicaConfig withShadeBlocks(boolean shadeBlocks) {
        return Builder.from(this).shadeBlocks(shadeBlocks).build();
    }

    public SchematicaConfig withShowMissingBlockTracers(boolean showMissingBlockTracers) {
        return Builder.from(this).showMissingBlockTracers(showMissingBlockTracers).build();
    }

    public SchematicaConfig withMissingBlockTracersMaxDistance(double missingBlockTracersMaxDistance) {
        return Builder.from(this).missingBlockTracersMaxDistance(missingBlockTracersMaxDistance).build();
    }

    public SchematicaConfig withShowMissingBlockCubes(boolean showMissingBlockCubes) {
        return Builder.from(this).showMissingBlockCubes(showMissingBlockCubes).build();
    }

    public SchematicaConfig withMissingBlockCubesMaxDistance(double missingBlockCubesMaxDistance) {
        return Builder.from(this).missingBlockCubesMaxDistance(missingBlockCubesMaxDistance).build();
    }

    public SchematicaConfig withShowWrongBlockTracers(boolean showWrongBlockTracers) {
        return Builder.from(this).showWrongBlockTracers(showWrongBlockTracers).build();
    }

    public SchematicaConfig withWrongBlockTracersMaxDistance(double wrongBlockTracersMaxDistance) {
        return Builder.from(this).wrongBlockTracersMaxDistance(wrongBlockTracersMaxDistance).build();
    }

    public SchematicaConfig withShowWrongBlockCubes(boolean showWrongBlockCubes) {
        return Builder.from(this).showWrongBlockCubes(showWrongBlockCubes).build();
    }

    public SchematicaConfig withWrongBlockCubesMaxDistance(double wrongBlockCubesMaxDistance) {
        return Builder.from(this).wrongBlockCubesMaxDistance(wrongBlockCubesMaxDistance).build();
    }

    public SchematicaConfig withReplaceableAsAir(boolean replaceableAsAir) {
        return Builder.from(this).replaceableAsAir(replaceableAsAir).build();
    }

    public SchematicaConfig withAirAlwaysValid(boolean airAlwaysValid) {
        return Builder.from(this).airAlwaysValid(airAlwaysValid).build();
    }

    public SchematicaConfig withAutoBuild(boolean autoBuild) {
        return Builder.from(this).autoBuild(autoBuild).build();
    }

    public SchematicaConfig withCreate(SchematicaWorldSlice create) {
        return Builder.from(this).create(create).build();
    }

    public static class Builder {

        private boolean enabled;
        private double maxRange;
        private ImmutableList<Integer> autoSelectSlots;
        private boolean attachToAir;
        private boolean useShift;
        private double placementRate;
        private boolean autoRotate;
        private boolean renderBlocks;
        private boolean shadeBlocks;
        private boolean showMissingBlockTracers;
        private double missingBlockTracersMaxDistance;
        private boolean showMissingBlockCubes;
        private double missingBlockCubesMaxDistance;
        private boolean showWrongBlockTracers;
        private double wrongBlockTracersMaxDistance;
        private boolean showWrongBlockCubes;
        private double wrongBlockCubesMaxDistance;
        private boolean replaceableAsAir;
        private boolean airAlwaysValid;
        private boolean autoBuild;
        private SchematicaWorldSlice create;

        private Builder() {}

        public static Builder from(SchematicaConfig config) {
            return new Builder()
                    .enabled(config.enabled)
                    .maxRange(config.maxRange)
                    .autoSelectSlots(config.autoSelectSlots)
                    .attachToAir(config.attachToAir)
                    .useShift(config.useShift)
                    .placementRate(config.placementRate)
                    .autoRotate(config.autoRotate)
                    .renderBlocks(config.renderBlocks)
                    .shadeBlocks(config.shadeBlocks)
                    .showMissingBlockTracers(config.showMissingBlockTracers)
                    .missingBlockTracersMaxDistance(config.missingBlockTracersMaxDistance)
                    .showMissingBlockCubes(config.showMissingBlockCubes)
                    .missingBlockCubesMaxDistance(config.missingBlockCubesMaxDistance)
                    .showWrongBlockTracers(config.showWrongBlockTracers)
                    .wrongBlockTracersMaxDistance(config.wrongBlockTracersMaxDistance)
                    .showWrongBlockCubes(config.showWrongBlockCubes)
                    .wrongBlockCubesMaxDistance(config.wrongBlockCubesMaxDistance)
                    .replaceableAsAir(config.replaceableAsAir)
                    .airAlwaysValid(config.airAlwaysValid)
                    .autoBuild(config.autoBuild)
                    .create(config.create);
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder maxRange(double maxRange) {
            this.maxRange = maxRange;
            return this;
        }

        public Builder autoSelectSlots(ImmutableList<Integer> autoSelectSlots) {
            this.autoSelectSlots = autoSelectSlots;
            return this;
        }

        public Builder attachToAir(boolean attachToAir) {
            this.attachToAir = attachToAir;
            return this;
        }

        public Builder useShift(boolean useShift) {
            this.useShift = useShift;
            return this;
        }

        public Builder placementRate(double placementRate) {
            this.placementRate = placementRate;
            return this;
        }

        public Builder autoRotate(boolean autoRotate) {
            this.autoRotate = autoRotate;
            return this;
        }

        public Builder renderBlocks(boolean renderBlocks) {
            this.renderBlocks = renderBlocks;
            return this;
        }

        public Builder shadeBlocks(boolean shadeBlocks) {
            this.shadeBlocks = shadeBlocks;
            return this;
        }

        public Builder showMissingBlockTracers(boolean showMissingBlockTracers) {
            this.showMissingBlockTracers = showMissingBlockTracers;
            return this;
        }

        public Builder missingBlockTracersMaxDistance(double missingBlockTracersMaxDistance) {
            this.missingBlockTracersMaxDistance = missingBlockTracersMaxDistance;
            return this;
        }

        public Builder showMissingBlockCubes(boolean showMissingBlockCubes) {
            this.showMissingBlockCubes = showMissingBlockCubes;
            return this;
        }

        public Builder missingBlockCubesMaxDistance(double missingBlockCubesMaxDistance) {
            this.missingBlockCubesMaxDistance = missingBlockCubesMaxDistance;
            return this;
        }

        public Builder showWrongBlockTracers(boolean showWrongBlockTracers) {
            this.showWrongBlockTracers = showWrongBlockTracers;
            return this;
        }

        public Builder wrongBlockTracersMaxDistance(double wrongBlockTracersMaxDistance) {
            this.wrongBlockTracersMaxDistance = wrongBlockTracersMaxDistance;
            return this;
        }

        public Builder showWrongBlockCubes(boolean showWrongBlockCubes) {
            this.showWrongBlockCubes = showWrongBlockCubes;
            return this;
        }

        public Builder wrongBlockCubesMaxDistance(double wrongBlockCubesMaxDistance) {
            this.wrongBlockCubesMaxDistance = wrongBlockCubesMaxDistance;
            return this;
        }

        public Builder replaceableAsAir(boolean replaceableAsAir) {
            this.replaceableAsAir = replaceableAsAir;
            return this;
        }

        public Builder airAlwaysValid(boolean airAlwaysValid) {
            this.airAlwaysValid = airAlwaysValid;
            return this;
        }

        public Builder autoBuild(boolean autoBuild) {
            this.autoBuild = autoBuild;
            return this;
        }

        public Builder create(SchematicaWorldSlice create) {
            this.create = create;
            return this;
        }

        public SchematicaConfig build() {
            return new SchematicaConfig(enabled, maxRange, autoSelectSlots, attachToAir, useShift, placementRate, autoRotate, renderBlocks, shadeBlocks, showMissingBlockTracers, missingBlockTracersMaxDistance, showMissingBlockCubes, missingBlockCubesMaxDistance, showWrongBlockTracers, wrongBlockTracersMaxDistance, showWrongBlockCubes, wrongBlockCubesMaxDistance, replaceableAsAir, airAlwaysValid, autoBuild, create);
        }
    }

    // </generated-code>
}