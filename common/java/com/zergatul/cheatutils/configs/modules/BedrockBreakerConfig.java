package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import net.minecraft.resources.Identifier;

public record BedrockBreakerConfig(
        boolean replace,
        String replaceBlockId,
        boolean allBlocks
) implements ImmutableConfig {

    private static final String DEFAULT_REPLACE_BLOCK = "minecraft:netherrack";

    public BedrockBreakerConfig {
        // read comment in defaults() method to see why we can't perform full validation
        if (replaceBlockId == null || Identifier.tryParse(replaceBlockId) == null) {
            replaceBlockId = DEFAULT_REPLACE_BLOCK;
        }
    }

    public static BedrockBreakerConfig defaults() {
        // it is better to not access game registries at this point
        // in some versions it may work, in some others it may not
        // because of this block id is hardcoded
        return new Builder()
                .replace(false)
                .replaceBlockId(DEFAULT_REPLACE_BLOCK)
                .allBlocks(false)
                .build();
    }

    // <generated-code>

    public BedrockBreakerConfig withReplace(boolean replace) {
        return Builder.from(this).replace(replace).build();
    }

    public BedrockBreakerConfig withReplaceBlockId(String replaceBlockId) {
        return Builder.from(this).replaceBlockId(replaceBlockId).build();
    }

    public BedrockBreakerConfig withAllBlocks(boolean allBlocks) {
        return Builder.from(this).allBlocks(allBlocks).build();
    }

    public static class Builder {

        private boolean replace;
        private String replaceBlockId;
        private boolean allBlocks;

        private Builder() {}

        public static Builder from(BedrockBreakerConfig config) {
            return new Builder()
                    .replace(config.replace)
                    .replaceBlockId(config.replaceBlockId)
                    .allBlocks(config.allBlocks);
        }

        public Builder replace(boolean replace) {
            this.replace = replace;
            return this;
        }

        public Builder replaceBlockId(String replaceBlockId) {
            this.replaceBlockId = replaceBlockId;
            return this;
        }

        public Builder allBlocks(boolean allBlocks) {
            this.allBlocks = allBlocks;
            return this;
        }

        public BedrockBreakerConfig build() {
            return new BedrockBreakerConfig(replace, replaceBlockId, allBlocks);
        }
    }

    // </generated-code>
}