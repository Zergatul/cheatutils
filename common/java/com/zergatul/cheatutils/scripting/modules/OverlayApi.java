package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.ui.HorizontalAlign;
import com.zergatul.cheatutils.ui.VerticalAlign;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class OverlayApi {

    @MethodDescription("""
            Sets current horizontal align to left
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void left() {
        StatusOverlay.instance.setHorizontalAlign(HorizontalAlign.LEFT);
    }

    @MethodDescription("""
            Sets current horizontal align to center
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void center() {
        StatusOverlay.instance.setHorizontalAlign(HorizontalAlign.CENTER);
    }

    @MethodDescription("""
            Sets current horizontal align to right
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void right() {
        StatusOverlay.instance.setHorizontalAlign(HorizontalAlign.RIGHT);
    }

    @MethodDescription("""
            Sets current vertical align to top
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void top() {
        StatusOverlay.instance.setVerticalAlign(VerticalAlign.TOP);
    }

    @MethodDescription("""
            Sets current vertical align to middle
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void middle() {
        StatusOverlay.instance.setVerticalAlign(VerticalAlign.MIDDLE);
    }

    @MethodDescription("""
            Sets current vertical align to bottom
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void bottom() {
        StatusOverlay.instance.setVerticalAlign(VerticalAlign.BOTTOM);
    }

    @MethodDescription("""
            Sets default background color to be used when method does not have background color as parameter
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void backgroundColor(String color) {
        Integer colorInt = ColorUtils.parseColor(color);
        if (colorInt != null) {
            StatusOverlay.instance.setDefaultBackgroundColor(colorInt);
        }
    }

    @MethodDescription("""
            Adds text to Status Overlay
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void add(String text) {
        add("#FFFFFF", text);
    }

    @MethodDescription("""
            Adds text to Status Overlay
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void add(String color, String text) {
        StatusOverlay.instance.addText(StylizedText.createSafe(text, color));
    }

    @MethodDescription("""
            Adds text to Status Overlay
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void add(String color1, String text1, String color2, String text2) {
        StatusOverlay.instance.addText(StylizedText.createSafe(new String[] {
                color1, text1,
                "#FFFFFF", " ",
                color2, text2
        }));
    }

    @MethodDescription("""
            Adds text to Status Overlay.
            Parameters array should have length dividable by 2, and look like this: [color1, text1, color2, text2] and so on.
            No space will be added in between, unlike with other add(...) methods.
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void add(String backgroundColor, String[] parameters) {
        Integer background = ColorUtils.parseColor(backgroundColor);
        if (background != null) {
            StatusOverlay.instance.addText(background, StylizedText.createSafe(parameters));
        } else {
            StatusOverlay.instance.addText(StylizedText.createSafe(parameters));
        }
    }

    @MethodDescription("""
            Adds text to Status Overlay at screen 2D coordinates. Ignores current alignment settings.
            Use window.getWidth() and window.getHeight() to implement custom alignment.
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void addAtPosition(int x, int y, String color, String text) {
        StatusOverlay.instance.addFreeText(x, y, StylizedText.createSafe(text, color));
    }

    @MethodDescription("""
            Adds text to Status Overlay at screen 2D coordinates. Ignores current alignment settings.
            Use window.getWidth() and window.getHeight() to implement custom alignment.
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void addAtPosition(int x, int y, String color1, String text1, String color2, String text2) {
        StatusOverlay.instance.addFreeText(x, y, StylizedText.createSafe(new String[] {
                color1, text1,
                "#FFFFFF", " ",
                color2, text2
        }));
    }

    @MethodDescription("""
            Adds text to Status Overlay at screen 2D coordinates. Ignores current alignment settings.
            Use window.getWidth() and window.getHeight() to implement custom alignment.
            Parameters array should have length dividable by 2, and look like this: [color1, text1, color2, text2] and so on.
            No space will be added in between, unlike with other add(...) methods.
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void addAtPosition(int x, int y, String backgroundColor, String[] parameters) {
        Integer background = ColorUtils.parseColor(backgroundColor);
        if (background != null) {
            StatusOverlay.instance.addFreeText(x, y, background, StylizedText.createSafe(parameters));
        } else {
            StatusOverlay.instance.addFreeText(x, y, StylizedText.createSafe(parameters));
        }
    }
}