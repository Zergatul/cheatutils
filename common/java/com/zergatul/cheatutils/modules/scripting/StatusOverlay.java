package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.StatusOverlayConfig;
import com.zergatul.cheatutils.font.*;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.common.events.RenderGuiEvent;
import com.zergatul.cheatutils.ui.*;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class StatusOverlay implements Module, FontBackendHolder {

    public static final StatusOverlay instance = new StatusOverlay();

    private static final int DEFAULT_BACKGROUND = 0x90505050;

    private static final Minecraft mc = Minecraft.getInstance();
    private final Map<Align, List<AlignedText>> texts = new HashMap<>();
    private final List<FreeText> freeTexts = new ArrayList<>();
    private Runnable script;
    private HorizontalAlign hAlign;
    private VerticalAlign vAlign;
    private int backgroundColor;

    private boolean fontChanged;
    private CompletableFuture<FontBackend> fontBackendFuture;
    private FontRenderer fontRenderer;

    private StatusOverlay() {
        for (Align align: Align.values()) {
            texts.put(align, new ArrayList<>());
        }

        Events.PostRenderGui.add(this::render);
    }

    @Override
    public boolean uses(FontBackend backend) {
        return fontRenderer != null && fontRenderer.uses(backend);
    }

    public void onFontChange() {
        TickEndExecutor.instance.execute(() -> fontChanged = true);
    }

    public void setScript(Runnable script) {
        this.script = script;
    }

    public void addText(StylizedText message) {
        addText(backgroundColor, message);
    }

    public void addText(int background, StylizedText message) {
        texts.get(Align.get(vAlign, hAlign)).add(new AlignedText(background, message));
    }

    public void addFreeText(int x, int y, StylizedText message) {
        addFreeText(x, y, backgroundColor, message);
    }

    public void addFreeText(int x, int y, int background, StylizedText message) {
        freeTexts.add(new FreeText(x, y, background, message));
    }

    public void setHorizontalAlign(HorizontalAlign align) {
        hAlign = align;
    }

    public void setVerticalAlign(VerticalAlign align) {
        vAlign = align;
    }

    public void setDefaultBackgroundColor(int color) {
        backgroundColor = color;
    }

    private void render(RenderGuiEvent event) {
        if (mc.player == null) {
            return;
        }

        StatusOverlayConfig config = ConfigStore.instance.getConfig().statusOverlayConfig;
        if (!config.enabled || script == null) {
            return;
        }

        if (fontChanged) {
            fontBackendFuture = FontLibrary.instance.createBackend(config.font.asFontParameters());
            // fontRenderer = null; // more smooth transition, but shows prev font for few frames?
            fontChanged = false;
        }

        if (fontBackendFuture != null) {
            if (fontBackendFuture.isDone()) {
                fontRenderer = fontBackendFuture.join().createFontRenderer(config.font.asFontRenderDetails());
                fontBackendFuture = null;
            }
        }

        if (fontRenderer == null) {
            return;
        }

        for (Align align: Align.values()) {
            texts.get(align).clear();
        }

        freeTexts.clear();

        hAlign = HorizontalAlign.RIGHT;
        vAlign = VerticalAlign.BOTTOM;
        backgroundColor = DEFAULT_BACKGROUND;
        script.run();

        int scale = (int) mc.getWindow().getGuiScale(); // currently it is always integer
        int scrWidth = mc.getWindow().getWidth();
        int scrHeight = mc.getWindow().getHeight();
        int halfScrWidth = scrWidth / 2;
        int halfScrHeight = scrHeight / 2;

        Matrix4f matrix = new Matrix4f();
        matrix.ortho(0, scrWidth, scrHeight, 0, -1, 1);

        RenderingContext context = new RenderingContext(event.graphics(), matrix, halfScrWidth, halfScrHeight);

        for (Align align : Align.values()) {
            List<AlignedText> list = texts.get(align);
            if (list.isEmpty()) {
                continue;
            }

            FlexColumnElement flex = new FlexColumnElement().setAlign(align.hAlign);
            for (AlignedText item : list) {
                flex.append(new TextElement(fontRenderer, item.text).setBackgroundColor(item.background));
            }

            int x = switch (align.hAlign) {
                case LEFT -> 2 * scale;
                case CENTER -> halfScrWidth;
                case RIGHT -> scrWidth - 2 * scale;
            };
            int y = switch (align.vAlign) {
                case TOP -> 2 * scale;
                case MIDDLE -> halfScrHeight;
                case BOTTOM -> scrHeight - 2 * scale;
            };

            context.render(flex, x, y, align.hAlign, align.vAlign);
        }

        for (FreeText item : freeTexts) {
            context.render(
                    new TextElement(fontRenderer, item.text).setBackgroundColor(item.background),
                    item.x, item.y, HorizontalAlign.LEFT, VerticalAlign.TOP);
        }
    }

    private enum Align {
        TOP_LEFT(VerticalAlign.TOP, HorizontalAlign.LEFT),
        TOP_CENTER(VerticalAlign.TOP, HorizontalAlign.CENTER),
        TOP_RIGHT(VerticalAlign.TOP, HorizontalAlign.RIGHT),
        MIDDLE_LEFT(VerticalAlign.MIDDLE, HorizontalAlign.LEFT),
        MIDDLE_CENTER(VerticalAlign.MIDDLE, HorizontalAlign.CENTER),
        MIDDLE_RIGHT(VerticalAlign.MIDDLE, HorizontalAlign.RIGHT),
        BOTTOM_LEFT(VerticalAlign.BOTTOM, HorizontalAlign.LEFT),
        BOTTOM_CENTER(VerticalAlign.BOTTOM, HorizontalAlign.CENTER),
        BOTTOM_RIGHT(VerticalAlign.BOTTOM, HorizontalAlign.RIGHT);

        private final VerticalAlign vAlign;
        private final HorizontalAlign hAlign;

        Align(VerticalAlign vAlign, HorizontalAlign hAlign) {
            this.vAlign = vAlign;
            this.hAlign = hAlign;
        }

        public static Align get(VerticalAlign vAlign, HorizontalAlign hAlign) {
            return switch (vAlign) {
                case TOP -> switch (hAlign) {
                    case LEFT -> TOP_LEFT;
                    case CENTER -> TOP_CENTER;
                    case RIGHT -> TOP_RIGHT;
                };
                case MIDDLE -> switch (hAlign) {
                    case LEFT -> MIDDLE_LEFT;
                    case CENTER -> MIDDLE_CENTER;
                    case RIGHT -> MIDDLE_RIGHT;
                };
                case BOTTOM -> switch (hAlign) {
                    case LEFT -> BOTTOM_LEFT;
                    case CENTER -> BOTTOM_CENTER;
                    default -> BOTTOM_RIGHT;
                };
            };
        }
    }

    private record AlignedText(int background, StylizedText text) {}

    private record FreeText(int x, int y, int background, StylizedText text) {}
}