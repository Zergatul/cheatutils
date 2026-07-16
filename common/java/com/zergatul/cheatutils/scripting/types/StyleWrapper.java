package com.zergatul.cheatutils.scripting.types;

import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.PropertyDescription;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.ItemStack;

import java.util.List;

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

        return switch (event.action()) {
            case SHOW_TEXT -> new ComponentWrapper(((HoverEvent.ShowText) event).value());
            default -> ComponentWrapper.EMPTY;
        };
    }

    @Getter(name = "hoverTooltip")
    public ComponentWrapper[] getHoverTooltip() {
        HoverEvent event = inner.getHoverEvent();
        if (event == null) {
            return new ComponentWrapper[0];
        }

        return switch (event.action()) {
            case SHOW_TEXT -> new ComponentWrapper[] { new ComponentWrapper(((HoverEvent.ShowText) event).value()) };
            case SHOW_ITEM -> {
                List<Component> components = Screen.getTooltipFromItem(Minecraft.getInstance(), ((HoverEvent.ShowItem) event).item().create());
                yield components.stream().map(ComponentWrapper::new).toArray(ComponentWrapper[]::new);
            }
            case SHOW_ENTITY -> {
                List<Component> components = ((HoverEvent.ShowEntity) event).entity().getTooltipLines();
                yield components.stream().map(ComponentWrapper::new).toArray(ComponentWrapper[]::new);
            }
        };
    }

    @Getter(name = "hoverItem")
    public ItemStackWrapper getHoverItem() {
        HoverEvent event = inner.getHoverEvent();
        if (event == null) {
            return new ItemStackWrapper(ItemStack.EMPTY);
        }

        return switch (event.action()) {
            case SHOW_ITEM -> new ItemStackWrapper(((HoverEvent.ShowItem) event).item().create());
            default -> new ItemStackWrapper(ItemStack.EMPTY);
        };
    }

    @PropertyDescription("May return null")
    @Getter(name = "hoverEntity")
    public UUIDWrapper getHoverEntity() {
        HoverEvent event = inner.getHoverEvent();
        if (event == null) {
            return null;
        }

        return switch (event.action()) {
            case SHOW_ENTITY -> new UUIDWrapper(((HoverEvent.ShowEntity) event).entity().uuid);
            default -> null;
        };
    }

    @PropertyDescription("Click event payload: URL, file path, command, or clipboard text; empty when absent.")
    @Getter(name = "clickCommand")
    public String getClickCommand() {
        ClickEvent event = inner.getClickEvent();
        if (event == null) {
            return "";
        }

        return switch (event.action()) {
            case OPEN_URL -> ((ClickEvent.OpenUrl) event).uri().toString();
            case OPEN_FILE -> ((ClickEvent.OpenFile) event).path();
            case RUN_COMMAND -> ((ClickEvent.RunCommand) event).command();
            case SUGGEST_COMMAND -> ((ClickEvent.SuggestCommand) event).command();
            case COPY_TO_CLIPBOARD -> ((ClickEvent.CopyToClipboard) event).value();
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