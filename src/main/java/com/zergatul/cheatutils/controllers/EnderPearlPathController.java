package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.SharedVertexBuffer;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

public class EnderPearlPathController {

    public static final EnderPearlPathController instance = new EnderPearlPathController();

    private final Minecraft mc = Minecraft.getInstance();

    // TODO: highlight block?

    private EnderPearlPathController() {
        ModApiWrapper.RenderWorldLast.add(this::render);
    }

    private void render(RenderWorldLastEvent event) {
        if (!shouldDrawPath()) {
            return;
        }

        Vec3 view = event.getCamera().getPosition();
        Vec3 playerPos = event.getPlayerPos();
        float partialTick = event.getTickDelta();

        double x = playerPos.x;
        double y = playerPos.y + mc.player.getEyeHeight() - 0.1;
        double z = playerPos.z;
        float xRot = mc.player.getViewXRot(partialTick);
        float yRot = mc.player.getViewYRot(partialTick);

        double shiftX = -Mth.sin((yRot + 90) * ((float)Math.PI / 180F));
        double shiftZ = Mth.cos((yRot + 90) * ((float)Math.PI / 180F));

        float speedX = -Mth.sin(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));
        float speedY = -Mth.sin(xRot * ((float)Math.PI / 180F));
        float speedZ = Mth.cos(yRot * ((float)Math.PI / 180F)) * Mth.cos(xRot * ((float)Math.PI / 180F));

        Vec3 movement = new Vec3(speedX, speedY, speedZ).normalize().scale(1.5d);
        /*Vec3 vec = mc.player.getDeltaMovement();
        movement = movement.add(vec.x, mc.player.isOnGround() ? 0.0D : vec.y, vec.z);*/

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        float stepSize = 0.1f;
        int steps = 1000;
        for (int i = 0; i <= steps; i++) {
            x += movement.x * stepSize;
            y += movement.y * stepSize;
            z += movement.z * stepSize;
            movement = new Vec3(movement.x, movement.y - 0.03F * stepSize, movement.z);
            double px = x + shiftX / (20 + i);
            double py = y;
            double pz = z + shiftZ / (20 + i);

            if (i > 0) {
                buffer.vertex(px - view.x, py - view.y, pz - view.z).color(1f, 1f, 1f, 1f).endVertex();
            }
            if (i < steps) {
                buffer.vertex(px - view.x, py - view.y, pz - view.z).color(1f, 1f, 1f, 1f).endVertex();
            }
        }

        SharedVertexBuffer.instance.bind();
        SharedVertexBuffer.instance.upload(buffer.end());

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
    }

    private boolean shouldDrawPath() {
        if (!ConfigStore.instance.getConfig().enderPearlPathConfig.enabled) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return false;
        }

        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        return itemStack.getItem() == Items.ENDER_PEARL;
    }
}
