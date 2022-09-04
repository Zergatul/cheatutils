package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.StatusOverlayConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class StatusOverlayController {

    public static final StatusOverlayController instance = new StatusOverlayController();

    private static final int TranslateZ = 200;

    private static final Minecraft mc = Minecraft.getInstance();
    private Runnable script;
    private Map<Align, List<MutableComponent>> texts = new HashMap<>();
    private HorizontalAlign hAlign;
    private VerticalAlign vAlign;

    private StatusOverlayController() {
        for (Align align: Align.values()) {
            texts.put(align, new ArrayList<>());
        }
    }

    public void setScript(Runnable script) {
        this.script = script;
    }

    public void addText(MutableComponent message) {
        texts.get(Align.get(vAlign, hAlign)).add(message);
    }

    public void setHorizontalAlign(HorizontalAlign align) {
        hAlign = align;
    }

    public void setVerticalAlign(VerticalAlign align) {
        vAlign = align;
    }

    @SubscribeEvent
    public void render(RenderGuiEvent.Post event) {
        if (mc.player == null) {
            return;
        }

        StatusOverlayConfig config = ConfigStore.instance.getConfig().statusOverlayConfig;
        if (!config.enabled) {
            return;
        }

        for (Align align: Align.values()) {
            texts.get(align).clear();
        }

        if (script != null) {
            hAlign = HorizontalAlign.RIGHT;
            vAlign = VerticalAlign.BOTTOM;
            script.run();
        }

        event.getPoseStack().pushPose();
        event.getPoseStack().setIdentity();
        event.getPoseStack().translate(0, 0, TranslateZ);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        for (Align align: Align.values()) {
            List<MutableComponent> list = texts.get(align);
            if (list.size() == 0) {
                continue;
            }
            for (int i = 0; i < list.size(); i++) {
                MutableComponent text = list.get(i);
                int width = mc.font.width(text);
                int x = getLeft(align.hAlign, mc.getWindow().getGuiScaledWidth(), width);
                int y = getTop(align.vAlign, mc.getWindow().getGuiScaledHeight(), mc.font.lineHeight, i, list.size());
                fill(event.getPoseStack(), x, y, x + width, y + mc.font.lineHeight, -1873784752);
                mc.font.draw(event.getPoseStack(), text, x, y, 16777215);
            }
        }

        event.getPoseStack().popPose();
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

    private void fill(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {
        if (x1 < x2) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            int j = y1;
            y1 = y2;
            y2 = j;
        }

        Matrix4f matrix = poseStack.last().pose();

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(matrix, (float)x1, (float)y2, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(matrix, (float)x2, (float)y1, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(matrix, (float)x1, (float)y1, 0.0F).color(f, f1, f2, f3).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
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