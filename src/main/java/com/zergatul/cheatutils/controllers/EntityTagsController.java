package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.gl.GlyphPageFontRenderer;
import com.zergatul.cheatutils.utils.GuiUtils;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.PostRenderGuiEvent;
import com.zergatul.cheatutils.wrappers.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.ArrayList;

public class EntityTagsController {

    public static final EntityTagsController instance = new EntityTagsController();

    private static Minecraft mc = Minecraft.getInstance();
    private GlyphPageFontRenderer font = GlyphPageFontRenderer.create(
            "Consolas", //"Tahoma",
            12,
            false,
            false,
            false);
    private java.util.List<EntityEntry> entities = new ArrayList<>();

    /*temp*/
    private Matrix4f projMatrix;
    private Matrix4f poseMatrix;
    private Vec3 view;

    private EntityTagsController() {
        ModApiWrapper.RenderWorldLast.add(this::onRenderWorld);
        ModApiWrapper.PostRenderGui.add(this::onRenderGui);
    }

    private void onRenderWorld(RenderWorldLastEvent event) {
        Vec3 view = event.getCamera().getPosition();
        entities.clear();
        for (Entity entity: mc.level.entitiesForRendering()) {
            Vec3 pos = entity.getPosition(event.getTickDelta());
            if (pos.distanceToSqr(view) < 32 * 32) {
                pos = pos.add(-view.x, -view.y + entity.getBbHeight(), -view.z);
                entities.add(new EntityEntry(entity, pos));
            }
        }

        try {
            poseMatrix = (Matrix4f) event.getMatrixStack().last().pose().clone();
            projMatrix = (Matrix4f) event.getProjectionMatrix().clone();

        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void onRenderGui(PostRenderGuiEvent event) {
        /*for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                font.drawString(event.getMatrixStack(), String.format("%.3f", poseMatrix.get(x, y)), 20 + x * 30, 20 + y * 15, Color.WHITE.getRGB());
            }
        }

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                font.drawString(event.getMatrixStack(), String.format("%.3f", projMatrix.get(x, y)), 20 + x * 30, 120 + y * 15, Color.WHITE.getRGB());
            }
        }*/

        //Vector4f trans = poseMatrix.transform(new Vector4f((float)(100 - view.x), (float)(100 - view.y), (float)(100 - view.z), 1));
        /*font.drawString(event.getMatrixStack(), String.format("%.3f", trans.x), 20 + 0 * 30, 220, Color.WHITE.getRGB());
        font.drawString(event.getMatrixStack(), String.format("%.3f", trans.y), 20 + 1 * 30, 220, Color.WHITE.getRGB());
        font.drawString(event.getMatrixStack(), String.format("%.3f", trans.z), 20 + 2 * 30, 220, Color.WHITE.getRGB());
        font.drawString(event.getMatrixStack(), String.format("%.3f", trans.w), 20 + 3 * 30, 220, Color.WHITE.getRGB());*/

        //Vector4f trans2 = projMatrix.transform(trans);
        /*font.drawString(event.getMatrixStack(), String.format("%.3f", trans2.x), 20 + 0 * 30, 240, Color.WHITE.getRGB());
        font.drawString(event.getMatrixStack(), String.format("%.3f", trans2.y), 20 + 1 * 30, 240, Color.WHITE.getRGB());
        font.drawString(event.getMatrixStack(), String.format("%.3f", trans2.z), 20 + 2 * 30, 240, Color.WHITE.getRGB());
        font.drawString(event.getMatrixStack(), String.format("%.3f", trans2.w), 20 + 3 * 30, 240, Color.WHITE.getRGB());*/

        double scale = mc.getWindow().getGuiScale();
        double invScale = 1d / scale;
        double scaledHalfWidth = mc.getWindow().getWidth() / scale / 2;
        double scaledHalfHeight = mc.getWindow().getHeight() / scale / 2;

        event.getMatrixStack().pushPose();
        event.getMatrixStack().last().pose().translate((float)scaledHalfWidth, (float)scaledHalfHeight, 0);

        for (EntityEntry entry: entities) {
            Vector4f v1 = poseMatrix.transform(new Vector4f((float)entry.position.x, (float)entry.position.y, (float)entry.position.z, 1));
            Vector4f v2 = projMatrix.transform(v1);
            if (v2.z > 0) {
                //double xp = Math.round(v2.x / v2.w * scaledHalfWidth / invScale) * invScale;
                //double yp = Math.round(-v2.y / v2.w * scaledHalfHeight / invScale) * invScale;
                float height = font.getFontHeight();
                double xp = v2.x / v2.w * scaledHalfWidth;
                double yp = -v2.y / v2.w * scaledHalfHeight - height;
                String text = getEntityTags(entry.entity);
                if (text != null) {
                    float width = font.getStringWidth(text);
                    xp -= width / 2;
                    GuiUtils.fill(event.getMatrixStack(), xp - 2, yp, xp + width + 4, yp + height, Color.BLACK.getRGB() & 0x40000000);
                    font.drawString(event.getMatrixStack(), text, xp, yp, Color.WHITE.getRGB());
                }
            }
        }
        /*if (trans2.z > 0) {
            double xp = Math.round(trans2.x / trans2.w * scaledHalfWidth / invScale) * invScale;
            double yp = Math.round(-trans2.y / trans2.w * scaledHalfHeight / invScale) * invScale;
            font.drawString(event.getMatrixStack(), "CENTER", xp, yp, Color.WHITE.getRGB());
        }*/
        event.getMatrixStack().popPose();
    }

    private String getEntityTags(Entity entity) {
        String result = "";
        if (entity instanceof LivingEntity living) {
            result += "HP=" + living.getHealth();
        }
        if (entity instanceof AgeableMob ageable) {
            result += " Age=" + ageable.getAge();
        }
        return result.length() == 0 ? null : result;
        /*String tags = String.join(";", entity.getTags());
        if (entity instanceof LivingEntity living) {
            for (AttributeInstance attr: living.getAttributes().getSyncableAttributes()) {
                tags += attr.getAttribute().getDescriptionId() + "=" + attr.getValue() + ";";
            }
            tags += "!";
            for (AttributeInstance attr: living.getAttributes().getDirtyAttributes()) {
                tags += attr.getAttribute().getDescriptionId() + "=" + attr.getValue() + ";";
            }
        }
        return tags;*/
    }

    private record EntityEntry(Entity entity, Vec3 position) {
    }
}