package com.zergatul.cheatutils.controllers;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ProjectilePathConfig;
import com.zergatul.cheatutils.utils.SharedVertexBuffer;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.RenderWorldLastEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectilePathController {

    public static final ProjectilePathController instance = new ProjectilePathController();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final List<ThrowableItemEntry> entries = Lists.newArrayList(
            new EnderPearlEntry(),
            new SnowballEntry(),
            new EggEntry(),
            new PotionEntry(),
            new ExpBottleEntry(),
            new BowItemEntry(),
            new CrossbowItemEntry(),
            new TridentItemEntry());

    // TODO: highlight block?

    private ProjectilePathController() {
        ModApiWrapper.RenderWorldLast.add(this::render);
    }

    private void render(RenderWorldLastEvent event) {
        if (!ConfigStore.instance.getConfig().esp) {
            return;
        }

        ThrowableItemEntry entry = null;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).shouldDraw()) {
                entry = entries.get(i);
                break;
            }
        }

        if (entry == null) {
            return;
        }

        if (mc.player == null) {
            return;
        }

        Vec3d view = event.getCamera().getPos();
        Vec3d playerPos = event.getPlayerPos();
        float partialTick = event.getTickDelta();

        double x = playerPos.x;
        double y = playerPos.y + mc.player.getEyeHeight(mc.player.getPose());
        double z = playerPos.z;
        Vec3d from = new Vec3d(x, y, z);
        float xRot = mc.player.getPitch(partialTick);
        float yRot = mc.player.getYaw(partialTick);

        double shiftX = -MathHelper.sin((yRot + 90) * ((float)Math.PI / 180F));
        double shiftY = -0.5;
        double shiftZ = MathHelper.cos((yRot + 90) * ((float)Math.PI / 180F));
        Vec3d shift = new Vec3d(shiftX, shiftY, shiftZ);

        float speedX = -MathHelper.sin(yRot * ((float)Math.PI / 180F)) * MathHelper.cos(xRot * ((float)Math.PI / 180F));
        float speedY = -MathHelper.sin((xRot + entry.getXRotDelta()) * ((float)Math.PI / 180F));
        float speedZ = MathHelper.cos(yRot * ((float)Math.PI / 180F)) * MathHelper.cos(xRot * ((float)Math.PI / 180F));
        Vec3d movement = new Vec3d(speedX, speedY, speedZ).normalize().multiply(entry.getSpeed());

        /*final double deviationBase = 0.0172275D;
        double dev = deviationBase * entry.getDeviation();
        List<Vec3> deviations = new ArrayList<>(8);
        deviations.add(movement.add(dev, dev, dev));
        deviations.add(movement.subtract(dev, dev, dev));
        deviations.add(movement.add(dev, dev, 0));
        deviations.add(movement.subtract(dev, dev, 0));
        deviations.add(movement.add(dev, 0, dev));
        deviations.add(movement.subtract(dev, 0, dev));
        deviations.add(movement.add(0, dev, dev));
        deviations.add(movement.subtract(0, dev, dev));*/

        /*Vec3 vec = mc.player.getDeltaMovement();
        movement = movement.add(vec.x, mc.player.isOnGround() ? 0.0D : vec.y, vec.z);*/

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        List<Vec3d> mainPath = calculatePath(entry, from, movement, shift, view);
        drawPath(mainPath, buffer, Color.WHITE.getRGB());

        /*for (Vec3 devSpeed : deviations) {
            List<Vec3> devPath = calculatePath(entry, from, devSpeed, shift, view);
            drawPath(devPath, buffer, Color.LIGHT_GRAY.getRGB());
        }*/

        SharedVertexBuffer.instance.bind();
        SharedVertexBuffer.instance.upload(buffer.end());

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        SharedVertexBuffer.instance.draw(event.getMatrixStack().peek().getPositionMatrix(), event.getProjectionMatrix(), GameRenderer.getPositionColorProgram());
        VertexBuffer.unbind();

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableTexture();
    }

    private ProjectilePathConfig getConfig() {
        return ConfigStore.instance.getConfig().projectilePathConfig;
    }

    private List<Vec3d> calculatePath(ThrowableItemEntry entry, Vec3d from, Vec3d speed, Vec3d shift, Vec3d view) {
        List<Vec3d> list = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            list.add(from.add(shift.multiply(getShiftFactor(i))).subtract(view));
            from = from.add(speed);
            speed = speed.multiply(entry.getResistance()).add(0, -entry.getGravity(), 0);
        }
        return list;
    }

    private void drawPath(List<Vec3d> path, BufferBuilder buffer, int color) {
        for (int i = 0; i < path.size(); i++) {
            Vec3d point = path.get(i);
            if (i < path.size() - 1) {
                buffer.vertex(point.x, point.y, point.z).color(color).next();
            }
            if (i > 0) {
                buffer.vertex(point.x, point.y, point.z).color(color).next();
            }
        }
    }

    private double getShiftFactor(int step) {
        return 1d / (4 + 1d * step * step / 20);
    }

    private ItemStack getItemStackInHand() {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return null;
        }

        return player.getStackInHand(Hand.MAIN_HAND);
    }

    private static abstract class ThrowableItemEntry {
        public abstract boolean shouldDraw();
        public abstract float getXRotDelta();
        public abstract float getSpeed();
        public abstract float getGravity();
        public float getResistance() {
            return 0.99F;
        }
        public abstract float getDeviation();
    }

    private abstract class SimpleThrowableItemEntry extends ThrowableItemEntry {

        private final Item item;
        private final float xRotDelta;
        private final float speed;
        private final float deviation;

        public SimpleThrowableItemEntry(Item item, float xRotDelta, float speed, float deviation) {
            this.item = item;
            this.xRotDelta = xRotDelta;
            this.speed = speed;
            this.deviation = deviation;
        }

        @Override
        public boolean shouldDraw() {
            ItemStack itemStack = getItemStackInHand();
            if (itemStack == null) {
                return false;
            }

            return itemStack.getItem() == item;
        }

        @Override
        public float getXRotDelta() {
            return xRotDelta;
        }

        @Override
        public float getSpeed() {
            return speed;
        }

        @Override
        public float getGravity() {
            return 0.03F;
        }

        @Override
        public float getDeviation() {
            return deviation;
        }
    }

    private class EnderPearlEntry extends SimpleThrowableItemEntry {

        public EnderPearlEntry() {
            super(Items.ENDER_PEARL, 0, 1.5f, 1);
        }

        @Override
        public boolean shouldDraw() {
            return getConfig().enderPearls && super.shouldDraw();
        }
    }

    private class SnowballEntry extends SimpleThrowableItemEntry {

        public SnowballEntry() {
            super(Items.SNOWBALL, 0, 1.5f, 1);
        }

        @Override
        public boolean shouldDraw() {
            return getConfig().snowballs && super.shouldDraw();
        }
    }

    private class EggEntry extends SimpleThrowableItemEntry {

        public EggEntry() {
            super(Items.EGG, 0, 1.5f, 1);
        }

        @Override
        public boolean shouldDraw() {
            return getConfig().eggs && super.shouldDraw();
        }
    }

    private class PotionEntry extends SimpleThrowableItemEntry {
        public PotionEntry() {
            super(Items.SPLASH_POTION, -20, 0.5f, 1);
        }

        @Override
        public boolean shouldDraw() {
            return getConfig().potions && super.shouldDraw();
        }

        @Override
        public float getGravity() {
            return 0.05F;
        }
    }

    private class ExpBottleEntry extends SimpleThrowableItemEntry {
        public ExpBottleEntry() {
            super(Items.EXPERIENCE_BOTTLE, -20, 0.7f, 1);
        }

        @Override
        public boolean shouldDraw() {
            return getConfig().expBottles && super.shouldDraw();
        }

        @Override
        public float getGravity() {
            return 0.07F;
        }
    }

    private class BowItemEntry extends ThrowableItemEntry {

        @Override
        public boolean shouldDraw() {
            if (!getConfig().bows) {
                return false;
            }

            ClientPlayerEntity player = mc.player;
            if (player == null) {
                return false;
            }

            ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
            if (itemStack.getItem() != Items.BOW) {
                return false;
            }

            return player.isUsingItem();
        }

        @Override
        public float getXRotDelta() {
            return 0;
        }

        @Override
        public float getSpeed() {
            ClientPlayerEntity player = mc.player;
            if (player == null) {
                return 0;
            }

            int ticks = player.getItemUseTime();
            float power = BowItem.getPullProgress(ticks);
            return power * 3;
        }

        @Override
        public float getGravity() {
            return 0.05F;
        }

        @Override
        public float getDeviation() {
            return 1;
        }
    }

    private class TridentItemEntry extends BowItemEntry {

        @Override
        public boolean shouldDraw() {
            if (!getConfig().tridents) {
                return false;
            }

            ClientPlayerEntity player = mc.player;
            if (player == null) {
                return false;
            }

            ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
            return itemStack.getItem() == Items.TRIDENT;
        }

        @Override
        public float getXRotDelta() {
            return 0;
        }

        @Override
        public float getSpeed() {
            return 2.5f;
        }

        @Override
        public float getDeviation() {
            return 1;
        }
    }

    private class CrossbowItemEntry extends BowItemEntry {

        @Override
        public boolean shouldDraw() {
            if (!getConfig().crossbows) {
                return false;
            }

            ItemStack itemStack = getItemStackInHand();
            if (itemStack == null) {
                return false;
            }

            if (itemStack.getItem() != Items.CROSSBOW) {
                return false;
            }

            return CrossbowItem.isCharged(itemStack);
        }

        @Override
        public float getSpeed() {
            ItemStack itemStack = getItemStackInHand();
            if (itemStack == null) {
                return 0;
            }

            return CrossbowItem.hasProjectile(itemStack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
        }
    }
}