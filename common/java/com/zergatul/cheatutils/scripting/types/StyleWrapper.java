package com.zergatul.cheatutils.scripting.types;

import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

@CustomType(name = "TextStyle")
public class StyleWrapper {

    private final Style inner;

    public StyleWrapper(Style style) {
        this.inner = style;
    }

    @Getter(name = "isBold")
    public boolean isBold() {
        return inner.isBold();
    }

    @Getter(name = "isItalic")
    public boolean isItalic() {
        return inner.isItalic();
    }

    @Getter(name = "isStrikethrough")
    public boolean isStrikethrough() {
        return inner.isStrikethrough();
    }

    @Getter(name = "isUnderlined")
    public boolean isUnderlined() {
        return inner.isUnderlined();
    }

    @Getter(name = "isObfuscated")
    public boolean isObfuscated() {
        return inner.isObfuscated();
    }

    @Getter(name = "hasClickEvent")
    public boolean hasClickEvent() {
        return inner.getClickEvent() != null;
    }

    @Getter(name = "hasColor")
    public boolean hasColor() {
        return inner.getColor() != null;
    }

    @Getter(name = "hasHoverEvent")
    public boolean hasHoverEvent() {
        return inner.getHoverEvent() != null;
    }

    @Getter(name = "clickEventType")
    public String getClickEventType() {
        ClickEvent event = inner.getClickEvent();
        if (event == null) {
            return "";
        }
        return event.action().toString();
    }

    @Getter(name = "color")
    public String getColor() {
        TextColor color = inner.getColor();
        if (color == null) {
            return "";
        }
        return ColorUtils.asHexRGB(color.getValue());
    }

    @Getter(name = "hoverEventType")
    public String getHoverEventType() {
        HoverEvent event = inner.getHoverEvent();
        if (event == null) {
            return "";
        }

        return event.action().toString();
    }

    @Getter(name = "hoverText")
    public ComponentWrapper getHoverText() {
        HoverEvent event = inner.getHoverEvent();
        if (event == null) {
            return ComponentWrapper.EMPTY;
        }
        if (event.action() == HoverEvent.Action.SHOW_TEXT) {
            HoverEvent.ShowText showText = (HoverEvent.ShowText) event;
            return new ComponentWrapper(showText.value());
        } else {
            return ComponentWrapper.EMPTY;
        }
    }

    @Getter(name = "clickCommand")
    public String getClickCommand() {
        ClickEvent event = inner.getClickEvent();
        if (event == null) {
            return "";
        }

        return switch (event.action()) {
            case RUN_COMMAND -> ((ClickEvent.RunCommand) event).command();
            case SUGGEST_COMMAND -> ((ClickEvent.SuggestCommand) event).command();
            default -> "";
        };
    }

    @Getter(name = "insertion")
    public String getInsertion() {
        String insertion = inner.getInsertion();
        return insertion != null ? insertion : "";
    }

    @MethodDescription("""
            If click event type is RUN_COMMAND or SUGGEST_COMMAND, sends this command to server.
            Other click event types are ignored.
            Returns true on success.
            """)
    public boolean click() {
        ClickEvent event = inner.getClickEvent();
        if (event == null) {
            return false;
        }

        return switch (event.action()) {
            case RUN_COMMAND -> executeCommand(((ClickEvent.RunCommand) event).command());
            case SUGGEST_COMMAND -> executeCommand(((ClickEvent.SuggestCommand) event).command());
            default -> false;
        };
    }

    private boolean executeCommand(String command) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        mc.player.connection.sendUnattendedCommand(Commands.trimOptionalPrefix(command), null);
        return true;
    }
}