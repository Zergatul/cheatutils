package com.zergatul.cheatutils.ui;

import java.util.ArrayList;
import java.util.List;

public class FlexColumnElement implements Element {

    private List<Element> items;
    private HorizontalAlign align;
    private int gap;
    private int measuredWidth, measuredHeight;
    private int x, y;

    public FlexColumnElement() {
        this.align = HorizontalAlign.CENTER;
    }

    @Override
    public void measure(RenderingContext context) {
        if (items == null || items.isEmpty()) {
            measuredWidth = 0;
            measuredHeight = 0;
        } else {
            int width = 0;
            int height = 0;
            for (Element item : items) {
                item.measure(context);
                width = Math.max(width, item.getMeasuredWidth());
                height += item.getMeasuredHeight();
            }
            height += (items.size() - 1) * gap;

            measuredWidth = width;
            measuredHeight = height;
        }
    }

    @Override
    public int getMeasuredWidth() {
        return measuredWidth;
    }

    @Override
    public int getMeasuredHeight() {
        return measuredHeight;
    }

    @Override
    public void layout(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;

        if (items == null || items.isEmpty()) {
            return;
        }

        int yOffset = y;
        for (Element item : items) {
            int xItem = switch (align) {
                case LEFT -> x;
                case CENTER -> x + (measuredWidth - item.getMeasuredWidth()) / 2;
                case RIGHT -> x + measuredWidth - item.getMeasuredWidth();
            };
            item.layout(xItem, yOffset, item.getMeasuredWidth(), item.getMeasuredHeight());
            yOffset += item.getMeasuredHeight() + gap;
        }
    }

    @Override
    public void render(RenderingContext context) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (Element item : items) {
            item.render(context);
        }
    }

    public FlexColumnElement setAlign(HorizontalAlign align) {
        this.align = align;
        return this;
    }

    public FlexColumnElement setGap(int gap) {
        this.gap = gap;
        return this;
    }

    public FlexColumnElement append(Element element) {
        ensureListCreated();
        items.add(element);
        return this;
    }

    public FlexColumnElement insertAt(int index, Element element) {
        ensureListCreated();
        items.add(index, element);
        return this;
    }

    private void ensureListCreated() {
        if (items == null) {
            items = new ArrayList<>();
        }
    }
}