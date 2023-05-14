package com.zergatul.cheatutils.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.time.LocalDateTime;
import java.util.List;

public class TimeWrappedComponent implements Component {

    private final Component content;
    private final LocalDateTime time;

    public TimeWrappedComponent(Component content) {
        this.content = content;
        this.time = LocalDateTime.now();
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Component unwrap() {
        return content;
    }

    @Override
    public Style getStyle() {
        return content.getStyle();
    }

    @Override
    public ComponentContents getContents() {
        return content.getContents();
    }

    @Override
    public List<Component> getSiblings() {
        return content.getSiblings();
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        return content.getVisualOrderText();
    }
}