package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.utility.FontConfig;
import com.zergatul.cheatutils.utils.MathUtils;

@SuppressWarnings("unused")
public record EntityTitleConfig(
        String hpPrefix,
        FontConfig titleFont,
        FontConfig enchantmentFont,
        int itemScale
) implements ImmutableConfig {

    public EntityTitleConfig {
        if (titleFont == null) {
            titleFont = FontConfig.defaults();
        }
        if (enchantmentFont == null) {
            enchantmentFont = FontConfig.defaults();
        }

        itemScale = MathUtils.clamp(itemScale, 0, 50);
    }

    public static EntityTitleConfig defaults() {
        return new Builder()
                .hpPrefix("\u2665")
                .titleFont(FontConfig.defaults())
                .enchantmentFont(FontConfig.defaults())
                .itemScale(0)
                .build();
    }

    // <generated-code>

    public EntityTitleConfig withHpPrefix(String hpPrefix) {
        return Builder.from(this).hpPrefix(hpPrefix).build();
    }

    public EntityTitleConfig withTitleFont(FontConfig titleFont) {
        return Builder.from(this).titleFont(titleFont).build();
    }

    public EntityTitleConfig withEnchantmentFont(FontConfig enchantmentFont) {
        return Builder.from(this).enchantmentFont(enchantmentFont).build();
    }

    public EntityTitleConfig withItemScale(int itemScale) {
        return Builder.from(this).itemScale(itemScale).build();
    }

    public static class Builder {

        private String hpPrefix;
        private FontConfig titleFont;
        private FontConfig enchantmentFont;
        private int itemScale;

        private Builder() {}

        public static Builder from(EntityTitleConfig config) {
            return new Builder()
                    .hpPrefix(config.hpPrefix)
                    .titleFont(config.titleFont)
                    .enchantmentFont(config.enchantmentFont)
                    .itemScale(config.itemScale);
        }

        public EntityTitleConfig.Builder hpPrefix(String hpPrefix) {
            this.hpPrefix = hpPrefix;
            return this;
        }

        public EntityTitleConfig.Builder titleFont(FontConfig titleFont) {
            this.titleFont = titleFont;
            return this;
        }

        public EntityTitleConfig.Builder enchantmentFont(FontConfig enchantmentFont) {
            this.enchantmentFont = enchantmentFont;
            return this;
        }

        public EntityTitleConfig.Builder itemScale(int itemScale) {
            this.itemScale = itemScale;
            return this;
        }

        public EntityTitleConfig build() {
            return new EntityTitleConfig(hpPrefix, titleFont, enchantmentFont, itemScale);
        }
    }

    // </generated-code>
}