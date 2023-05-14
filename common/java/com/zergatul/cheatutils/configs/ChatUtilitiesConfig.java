package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.configs.adapters.GsonSkip;
import com.zergatul.cheatutils.utils.MathUtils;

import java.time.format.DateTimeFormatter;

public class ChatUtilitiesConfig implements ValidatableConfig {

    public boolean dontCloseChatOnEnter;
    public boolean overrideMessageLimit;
    public int messageLimit;
    public boolean showTime;
    public String timeFormat;

    @GsonSkip
    private DateTimeFormatter formatter;

    public ChatUtilitiesConfig() {
        messageLimit = 100;
        timeFormat = "HH:mm:ss";
    }

    public DateTimeFormatter getFormatter() {
        if (formatter == null) {
            try {
                formatter = DateTimeFormatter.ofPattern(timeFormat);
            }
            catch (IllegalArgumentException e) {
                formatter = null;
            }
        }

        return formatter;
    }

    @Override
    public void validate() {
        messageLimit = MathUtils.clamp(messageLimit, 20, 1000000);
        if (getFormatter() == null) {
            showTime = false;
        }
    }
}