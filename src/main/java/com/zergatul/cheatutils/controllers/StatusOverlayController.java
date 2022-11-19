package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.StatusOverlayConfig;
import com.zergatul.cheatutils.utils.GuiUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.MutableText;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusOverlayController {

    public static final StatusOverlayController instance = new StatusOverlayController();

    private static final int TranslateZ = 200;

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private Runnable script;
    private Map<Align, List<MutableText>> texts = new HashMap<>();
    private HorizontalAlign hAlign;
    private VerticalAlign vAlign;

    private StatusOverlayController() {
        for (Align align: Align.values()) {
            texts.put(align, new ArrayList<>());
        }

        ModApiWrapper.addOnPostRenderGui(this::render);
    }

    public void setScript(Runnable script) {
        this.script = script;
    }

    public void addText(MutableText message) {
        texts.get(Align.get(vAlign, hAlign)).add(message);
    }

    public void setHorizontalAlign(HorizontalAlign align) {
        hAlign = align;
    }

    public void setVerticalAlign(VerticalAlign align) {
        vAlign = align;
    }

    private void render(ModApiWrapper.PostRenderGuiEvent event) {
        if (mc.player == null) {
            return;
        }

        StatusOverlayConfig config = ConfigStore.instance.getConfig().statusOverlayConfig;
        if (!config.enabled || script == null) {
            return;
        }

        for (Align align: Align.values()) {
            texts.get(align).clear();
        }

        hAlign = HorizontalAlign.RIGHT;
        vAlign = VerticalAlign.BOTTOM;
        script.run();

        event.getMatrixStack().push();
        event.getMatrixStack().loadIdentity();
        event.getMatrixStack().translate(0, 0, TranslateZ);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        for (Align align: Align.values()) {
            List<MutableText> list = texts.get(align);
            if (list.size() == 0) {
                continue;
            }
            for (int i = 0; i < list.size(); i++) {
                MutableText text = list.get(i);
                int width = mc.textRenderer.getWidth(text);
                int x = getLeft(align.hAlign, mc.getWindow().getScaledWidth(), width);
                int y = getTop(align.vAlign, mc.getWindow().getScaledHeight(), mc.textRenderer.fontHeight, i, list.size());
                GuiUtils.fill(event.getMatrixStack(), x, y, x + width, y + mc.textRenderer.fontHeight, -1873784752);
                mc.textRenderer.draw(event.getMatrixStack(), text, x, y, 16777215);
            }
        }

        event.getMatrixStack().pop();
        RenderSystem.applyModelViewMatrix();
    }

    private int getLeft(HorizontalAlign align, int screenWidth, int textWidth) {
        return switch (align) {
            case LEFT -> 2;
            case CENTER -> (screenWidth - textWidth) / 2;
            case RIGHT -> screenWidth - 2 - textWidth;
        };
    }

    private int getTop(VerticalAlign align, int screenHeight, int textHeight, int index, int count) {
        return switch (align) {
            case TOP -> 2 + index * textHeight;
            case MIDDLE -> (screenHeight - textHeight * count) / 2 + index * textHeight;
            case BOTTOM -> screenHeight - 2 - textHeight * (count - index);
        };
    }

    public enum HorizontalAlign {
        LEFT,
        CENTER,
        RIGHT
    }

    public enum VerticalAlign {
        TOP,
        MIDDLE,
        BOTTOM
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
}