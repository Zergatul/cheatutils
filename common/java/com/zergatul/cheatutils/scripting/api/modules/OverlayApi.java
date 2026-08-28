package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;

@SuppressWarnings("unused")
public class OverlayApi {

    @MethodDescription("Sets current horizontal align to left")
    @ApiVisibility(ApiType.OVERLAY)
    public void left() {
        StatusOverlay.instance.setHorizontalAlign(StatusOverlay.HorizontalAlign.LEFT);
    }

    @MethodDescription("Sets current horizontal align to center")
    @ApiVisibility(ApiType.OVERLAY)
    public void center() {
        StatusOverlay.instance.setHorizontalAlign(StatusOverlay.HorizontalAlign.CENTER);
    }

    @MethodDescription("Sets current horizontal align to right")
    @ApiVisibility(ApiType.OVERLAY)
    public void right() {
        StatusOverlay.instance.setHorizontalAlign(StatusOverlay.HorizontalAlign.RIGHT);
    }

    @MethodDescription("Sets current vertical align to top")
    @ApiVisibility(ApiType.OVERLAY)
    public void top() {
        StatusOverlay.instance.setVerticalAlign(StatusOverlay.VerticalAlign.TOP);
    }

    @MethodDescription("Sets current vertical align to middle")
    @ApiVisibility(ApiType.OVERLAY)
    public void middle() {
        StatusOverlay.instance.setVerticalAlign(StatusOverlay.VerticalAlign.MIDDLE);
    }

    @MethodDescription("Sets current vertical align to bottom")
    @ApiVisibility(ApiType.OVERLAY)
    public void bottom() {
        StatusOverlay.instance.setVerticalAlign(StatusOverlay.VerticalAlign.BOTTOM);
    }

    @MethodDescription("Sets default background color to be used when method does not have background color as parameter")
    @ApiVisibility(ApiType.OVERLAY)
    public void backgroundColor(String color) {
        Integer value = ColorUtils.parseColor(color);
        if (value != null) {
            StatusOverlay.instance.setDefaultBackgroundColor(value);
        }
    }

    @MethodDescription("Adds text to Status Overlay")
    @ApiVisibility(ApiType.OVERLAY)
    public void add(String text) {
        add("#FFFFFF", text);
    }

    @MethodDescription("Adds text to Status Overlay")
    @ApiVisibility(ApiType.OVERLAY)
    public void add(String color, String text) {
        StatusOverlay.instance.addText(createText(color, text));
    }

    @MethodDescription("Adds text to Status Overlay")
    @ApiVisibility(ApiType.OVERLAY)
    public void add(String color1, String text1, String color2, String text2) {
        StatusOverlay.instance.addText(createText(new String[] {
                color1, text1,
                "#FFFFFF", " ",
                color2, text2
        }));
    }

    @MethodDescription("Adds text to Status Overlay. Parameters are alternating color and text values; no spaces are inserted.")
    @ApiVisibility(ApiType.OVERLAY)
    public void add(String backgroundColor, String[] parameters) {
        MutableComponent text = createText(parameters);
        Integer background = ColorUtils.parseColor(backgroundColor);
        if (background != null) {
            StatusOverlay.instance.addText(background, text);
        } else {
            StatusOverlay.instance.addText(text);
        }
    }

    @MethodDescription("Adds text to Status Overlay at screen coordinates and ignores current alignment settings")
    @ApiVisibility(ApiType.OVERLAY)
    public void addAtPosition(int x, int y, String color, String text) {
        StatusOverlay.instance.addFreeText(x, y, createText(color, text));
    }

    @MethodDescription("Adds text to Status Overlay at screen coordinates and ignores current alignment settings")
    @ApiVisibility(ApiType.OVERLAY)
    public void addAtPosition(int x, int y, String color1, String text1, String color2, String text2) {
        StatusOverlay.instance.addFreeText(x, y, createText(new String[] {
                color1, text1,
                "#FFFFFF", " ",
                color2, text2
        }));
    }

    @MethodDescription("Adds text to Status Overlay at screen coordinates. Parameters are alternating color and text values; no spaces are inserted.")
    @ApiVisibility(ApiType.OVERLAY)
    public void addAtPosition(int x, int y, String backgroundColor, String[] parameters) {
        MutableComponent text = createText(parameters);
        Integer background = ColorUtils.parseColor(backgroundColor);
        if (background != null) {
            StatusOverlay.instance.addFreeText(x, y, background, text);
        } else {
            StatusOverlay.instance.addFreeText(x, y, text);
        }
    }

    private MutableComponent createText(String color, String text) {
        MutableComponent component = MutableComponent.create(new LiteralContents(text));
        Integer value = ColorUtils.parseColor(color);
        return value == null ? component : component.withStyle(Style.EMPTY.withColor(value));
    }

    private MutableComponent createText(String[] parameters) {
        MutableComponent component = MutableComponent.create(new LiteralContents(""));
        if (parameters.length == 0 || parameters.length % 2 != 0) {
            return component;
        }
        for (int i = 0; i < parameters.length; i += 2) {
            component.append(createText(parameters[i], parameters[i + 1]));
        }
        return component;
    }
}