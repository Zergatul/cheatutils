package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.controllers.StatusOverlayController;
import com.zergatul.cheatutils.scripting.api.HelpText;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;

import java.util.Locale;

public class MainApi {

    private final Minecraft mc = Minecraft.getInstance();

    public void addText(String text) {
        addText("#FFFFFF", text);
    }

    public void addText(String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        MutableComponent component = MutableComponent.create(new LiteralContents(text));
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        StatusOverlayController.instance.addText(component);
    }

    public void addText(String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableComponent component1 = MutableComponent.create(new LiteralContents(text1));
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = MutableComponent.create(new LiteralContents(text2));
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        StatusOverlayController.instance.addText(component1.append(" ").append(component2));
    }

    public String getCoordinates() {
        return String.format(Locale.ROOT, "%.3f / %.5f / %.3f", mc.getCameraEntity().getX(), mc.getCameraEntity().getY(), mc.getCameraEntity().getZ());
    }

    public boolean isDebugScreenEnabled() {
        return Minecraft.getInstance().options.renderDebug;
    }

    @HelpText("Allowed values: \"left\", \"center\", \"right\".")
    public void setOverlayHorizontalPosition(String position) {
        if (position != null) {
            position = position.toLowerCase(Locale.ROOT);
        }
        switch (position) {
            case "left" -> StatusOverlayController.instance.setHorizontalAlign(StatusOverlayController.HorizontalAlign.LEFT);
            case "center" -> StatusOverlayController.instance.setHorizontalAlign(StatusOverlayController.HorizontalAlign.CENTER);
            default -> StatusOverlayController.instance.setHorizontalAlign(StatusOverlayController.HorizontalAlign.RIGHT);
        }
    }

    @HelpText("Allowed values: \"top\", \"middle\", \"bottom\".")
    public void setOverlayVerticalPosition(String position) {
        if (position != null) {
            position = position.toLowerCase(Locale.ROOT);
        }
        switch (position) {
            case "top" -> StatusOverlayController.instance.setVerticalAlign(StatusOverlayController.VerticalAlign.TOP);
            case "middle" -> StatusOverlayController.instance.setVerticalAlign(StatusOverlayController.VerticalAlign.MIDDLE);
            default -> StatusOverlayController.instance.setVerticalAlign(StatusOverlayController.VerticalAlign.BOTTOM);
        }
    }
}