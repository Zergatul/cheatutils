package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;
import com.zergatul.cheatutils.utils.MathUtils;

import java.time.format.DateTimeFormatter;

public record ChatUtilitiesConfig(
        boolean dontCloseChatOnEnter,
        boolean overrideMessageLimit,
        int messageLimit,
        boolean showTime,
        String timeFormat
) implements ImmutableConfig, ModuleStateProvider {

    public ChatUtilitiesConfig {
        messageLimit = MathUtils.clamp(messageLimit, 20, 1000000);

        if (timeFormat == null) {
            timeFormat = "HH:mm:ss";
        }

        try {
            DateTimeFormatter.ofPattern(timeFormat);
        } catch (IllegalArgumentException e) {
            showTime = false;
        }
    }

    public static ChatUtilitiesConfig defaults() {
        return new Builder()
                .dontCloseChatOnEnter(false)
                .overrideMessageLimit(false)
                .messageLimit(100)
                .showTime(false)
                .timeFormat("HH:mm:ss")
                .build();
    }

    @Override
    public boolean isEnabled() {
        return dontCloseChatOnEnter || overrideMessageLimit || showTime;
    }

    // <generated-code>

    public ChatUtilitiesConfig withDontCloseChatOnEnter(boolean dontCloseChatOnEnter) {
        return Builder.from(this).dontCloseChatOnEnter(dontCloseChatOnEnter).build();
    }

    public ChatUtilitiesConfig withOverrideMessageLimit(boolean overrideMessageLimit) {
        return Builder.from(this).overrideMessageLimit(overrideMessageLimit).build();
    }

    public ChatUtilitiesConfig withMessageLimit(int messageLimit) {
        return Builder.from(this).messageLimit(messageLimit).build();
    }

    public ChatUtilitiesConfig withShowTime(boolean showTime) {
        return Builder.from(this).showTime(showTime).build();
    }

    public ChatUtilitiesConfig withTimeFormat(String timeFormat) {
        return Builder.from(this).timeFormat(timeFormat).build();
    }

    public static class Builder {

        private boolean dontCloseChatOnEnter;
        private boolean overrideMessageLimit;
        private int messageLimit;
        private boolean showTime;
        private String timeFormat;

        private Builder() {}

        public static Builder from(ChatUtilitiesConfig config) {
            return new Builder()
                    .dontCloseChatOnEnter(config.dontCloseChatOnEnter)
                    .overrideMessageLimit(config.overrideMessageLimit)
                    .messageLimit(config.messageLimit)
                    .showTime(config.showTime)
                    .timeFormat(config.timeFormat);
        }

        public Builder dontCloseChatOnEnter(boolean dontCloseChatOnEnter) {
            this.dontCloseChatOnEnter = dontCloseChatOnEnter;
            return this;
        }

        public Builder overrideMessageLimit(boolean overrideMessageLimit) {
            this.overrideMessageLimit = overrideMessageLimit;
            return this;
        }

        public Builder messageLimit(int messageLimit) {
            this.messageLimit = messageLimit;
            return this;
        }

        public Builder showTime(boolean showTime) {
            this.showTime = showTime;
            return this;
        }

        public Builder timeFormat(String timeFormat) {
            this.timeFormat = timeFormat;
            return this;
        }

        public ChatUtilitiesConfig build() {
            return new ChatUtilitiesConfig(dontCloseChatOnEnter, overrideMessageLimit, messageLimit, showTime, timeFormat);
        }
    }

    // </generated-code>
}