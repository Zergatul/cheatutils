package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.MethodDescription;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;

import static com.zergatul.cheatutils.utils.ComponentUtils.constructMessage;

@SuppressWarnings("unused")
public class OverlayApi {

    @MethodDescription("""
            Sets current horizontal align to left
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void left() {
        StatusOverlay.instance.setHorizontalAlign(StatusOverlay.HorizontalAlign.LEFT);
    }

    @MethodDescription("""
            Sets current horizontal align to center
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void center() {
        StatusOverlay.instance.setHorizontalAlign(StatusOverlay.HorizontalAlign.CENTER);
    }

    @MethodDescription("""
            Sets current horizontal align to right
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void right() {
        StatusOverlay.instance.setHorizontalAlign(StatusOverlay.HorizontalAlign.RIGHT);
    }

    @MethodDescription("""
            Sets current vertical align to top
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void top() {
        StatusOverlay.instance.setVerticalAlign(StatusOverlay.VerticalAlign.TOP);
    }

    @MethodDescription("""
            Sets current vertical align to middle
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void middle() {
        StatusOverlay.instance.setVerticalAlign(StatusOverlay.VerticalAlign.MIDDLE);
    }

    @MethodDescription("""
            Sets current vertical align to bottom
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void bottom() {
        StatusOverlay.instance.setVerticalAlign(StatusOverlay.VerticalAlign.BOTTOM);
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
        Integer colorInt = ColorUtils.parseColor(color);
        MutableComponent component = MutableComponent.create(new PlainTextContents.LiteralContents(text));
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        StatusOverlay.instance.addText(component);
    }

    @MethodDescription("""
            Adds text to Status Overlay
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void add(String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableComponent component1 = MutableComponent.create(new PlainTextContents.LiteralContents(text1));
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = MutableComponent.create(new PlainTextContents.LiteralContents(text2));
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        StatusOverlay.instance.addText(component1.append(" ").append(component2));
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
            StatusOverlay.instance.addText(background, constructMessage(parameters));
        } else {
            StatusOverlay.instance.addText(constructMessage(parameters));
        }
    }

    @MethodDescription("""
            Adds text to Status Overlay at screen 2D coordinates. Ignores current alignment settings.
            Use window.getGuiWidth() and window.getGuiHeight() to implement custom alignment.
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void addAtPosition(int x, int y, String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        MutableComponent component = MutableComponent.create(new PlainTextContents.LiteralContents(text));
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        StatusOverlay.instance.addFreeText(x, y, component);
    }

    @MethodDescription("""
            Adds text to Status Overlay at screen 2D coordinates. Ignores current alignment settings.
            Use window.getGuiWidth() and window.getGuiHeight() to implement custom alignment.
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void addAtPosition(int x, int y, String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableComponent component1 = MutableComponent.create(new PlainTextContents.LiteralContents(text1));
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = MutableComponent.create(new PlainTextContents.LiteralContents(text2));
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        StatusOverlay.instance.addFreeText(x, y, component1.append(" ").append(component2));
    }

    @MethodDescription("""
            Adds text to Status Overlay at screen 2D coordinates. Ignores current alignment settings.
            Use window.getGuiWidth() and window.getGuiHeight() to implement custom alignment.
            Parameters array should have length dividable by 2, and look like this: [color1, text1, color2, text2] and so on.
            No space will be added in between, unlike with other add(...) methods.
            """)
    @ApiVisibility(ApiType.OVERLAY)
    public void addAtPosition(int x, int y, String backgroundColor, String[] parameters) {
        Integer background = ColorUtils.parseColor(backgroundColor);
        if (background != null) {
            StatusOverlay.instance.addFreeText(x, y, background, constructMessage(parameters));
        } else {
            StatusOverlay.instance.addFreeText(x, y, constructMessage(parameters));
        }
    }
}