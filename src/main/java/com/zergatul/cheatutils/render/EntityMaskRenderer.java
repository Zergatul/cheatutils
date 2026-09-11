package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.mixins.accessors.RenderAccessor;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

public final class EntityMaskRenderer implements AutoCloseable {

    public static final EntityMaskRenderer INSTANCE = new EntityMaskRenderer();

    private static boolean renderingMask;
    private final EntityMaskRenderState state = new EntityMaskRenderState();
    private final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
    private int framebuffer;
    private int texture;
    private int width;
    private int height;
    private int compositeVao;
    private ShaderProgram overlayProgram;
    private ShaderProgram outlineProgram;
    private int overlayTextureUniform;
    private int overlayColorUniform;
    private int outlineTextureUniform;
    private int outlineColorUniform;
    private int outlineTexelSizeUniform;

    private EntityMaskRenderer() {}

    public static boolean isRenderingMask() {
        return renderingMask;
    }

    public void render(List<Entity> entities, RenderWorldLastEvent event, Color color, boolean outline) {
        if (entities.isEmpty() || renderingMask) {
            return;
        }

        state.capture();
        try {
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
            ensureFramebuffer();
            ensurePrograms();
            drawMask(entities, event);
            state.bindDestination();
            drawComposite(color, outline);
        } finally {
            state.close();
        }
    }

    private void drawMask(List<Entity> entities, RenderWorldLastEvent event) {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer);
        GlStateManager.viewport(0, 0, width, height);
        GlStateManager.clearColor(0, 0, 0, 0);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
        GL20.glUseProgram(0);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        int previousPass = MinecraftForgeClient.getRenderPass();
        renderingMask = true;
        try {
            ForgeHooksClient.setRenderPass(0);
            for (Entity entity : entities) {
                drawEntity(entity, event);
            }
        } finally {
            renderingMask = false;
            ForgeHooksClient.setRenderPass(previousPass);
        }
    }

    private void drawEntity(Entity entity, RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        FreeCam freeCam = FreeCam.INSTANCE;
        boolean sleeping = entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping();
        if (entity == mc.getRenderViewEntity() && mc.gameSettings.thirdPersonView == 0 && !freeCam.isActive() && !sleeping) {
            // Like vanilla, omit the body surrounding the first-person camera.
            return;
        }

        // Restore the body before calculating coordinates; the event retains the FreeCam render origin.
        freeCam.withRealEntityPosition(entity, () -> drawEntityAtRealPosition(entity, event));
    }

    private void drawEntityAtRealPosition(Entity entity, RenderWorldLastEvent event) {
        Render<Entity> renderer = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);
        if (renderer == null) {
            return;
        }

        // Reload per entity because vanilla renderers/layers modify fixed-function state.
        loadMatrix(GL11.GL_PROJECTION, event.getProjection());
        loadMatrix(GL11.GL_MODELVIEW, event.getModelView());
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        GlStateManager.depthFunc(GL11.GL_ALWAYS);
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

        float partialTicks = event.getPartialTicks();
        Vec3d camera = event.getCameraPos();
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - camera.x;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - camera.y;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - camera.z;
        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        boolean previousOutlines = ((RenderAccessor) renderer).getRenderOutlines_CU();
        renderer.setRenderOutlines(true);
        try {
            // Direct doRender omits RenderManager's shadows, fire and debug boxes.
            renderer.doRender(entity, x, y, z, yaw, partialTicks);
        } finally {
            renderer.setRenderOutlines(previousOutlines);
        }
    }

    private void loadMatrix(int mode, Matrix4f value) {
        matrix.clear();
        value.store(matrix);
        matrix.flip();
        GlStateManager.matrixMode(mode);
        GL11.glLoadMatrix(matrix);
    }

    private void drawComposite(Color color, boolean outline) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD, GL14.GL_FUNC_ADD);
        GlStateManager.disableDepth();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(texture);
        GL20.glUseProgram((outline ? outlineProgram : overlayProgram).getId());
        GL20.glUniform1i(outline ? outlineTextureUniform : overlayTextureUniform, 0);
        GL20.glUniform4f(outline ? outlineColorUniform : overlayColorUniform,
                color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
        if (outline) {
            GL20.glUniform2f(outlineTexelSizeUniform, 1F / width, 1F / height);
        }
        GL30.glBindVertexArray(compositeVao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
        GlStateManager.bindTexture(0);
    }

    private void ensurePrograms() {
        if (overlayProgram == null) {
            overlayProgram = new ShaderProgram(Constants.SHADER_ROOT + "overlay-composite.vsh",
                    Constants.SHADER_ROOT + "entity-overlay-composite.fsh");
            overlayTextureUniform = overlayProgram.getUniform("BufferTexture");
            overlayColorUniform = overlayProgram.getUniform("OverlayColor");
        }
        if (outlineProgram == null) {
            outlineProgram = new ShaderProgram(Constants.SHADER_ROOT + "overlay-composite.vsh",
                    Constants.SHADER_ROOT + "entity-outline-composite.fsh");
            outlineTextureUniform = outlineProgram.getUniform("BufferTexture");
            outlineColorUniform = outlineProgram.getUniform("OutlineColor");
            outlineTexelSizeUniform = outlineProgram.getUniform("TexelSize");
        }
        if (compositeVao == 0) {
            compositeVao = GL30.glGenVertexArrays();
        }
    }

    private void ensureFramebuffer() {
        Minecraft mc = Minecraft.getMinecraft();
        if (framebuffer != 0 && width == mc.displayWidth && height == mc.displayHeight) {
            return;
        }
        deleteFramebuffer();
        width = mc.displayWidth;
        height = mc.displayHeight;
        framebuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffer);
        texture = GL11.glGenTextures();
        GlStateManager.bindTexture(texture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL30.glFramebufferTexture2D(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texture, 0);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GlStateManager.bindTexture(0);
        if (GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            deleteFramebuffer();
            throw new IllegalStateException("Entity ESP mask framebuffer is not complete");
        }
    }

    @Override
    public void close() {
        if (overlayProgram != null) {
            overlayProgram.close();
            overlayProgram = null;
        }
        if (outlineProgram != null) {
            outlineProgram.close();
            outlineProgram = null;
        }
        if (compositeVao != 0) {
            GL30.glDeleteVertexArrays(compositeVao);
            compositeVao = 0;
        }
        deleteFramebuffer();
    }

    private void deleteFramebuffer() {
        if (framebuffer != 0) {
            GL30.glDeleteFramebuffers(framebuffer);
            framebuffer = 0;
        }
        if (texture != 0) {
            GlStateManager.deleteTexture(texture);
            texture = 0;
        }
        width = height = 0;
    }
}
