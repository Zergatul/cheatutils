package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FreeCamConfig;
import com.zergatul.cheatutils.helpers.MixinGameRendererHelper;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.state.property.Property;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FreeCamController {

    public static final FreeCamController instance = new FreeCamController();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
    private final Vec3f forwards = new Vec3f(0.0F, 0.0F, 1.0F);
    private final Vec3f up = new Vec3f(0.0F, 1.0F, 0.0F);
    private final Vec3f left = new Vec3f(1.0F, 0.0F, 0.0F);
    private boolean active;
    private Perspective oldCameraType;
    private Input oldInput;
    private double x, y, z;
    private float yRot, xRot;
    private double forwardVelocity;
    private double leftVelocity;
    private double upVelocity;
    private long lastTime;
    private boolean insideRenderDebug;

    private FreeCamController() {
        ModApiWrapper.addOnClientTickStart(this::onClientTickStart);
        ModApiWrapper.addOnRenderTickStart(this::onRenderTickStart);
        ModApiWrapper.addOnWorldUnload(this::onWorldUnload);
    }

    public boolean isActive() {
        return active;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getXRot() {
        return xRot;
    }

    public float getYRot() {
        return yRot;
    }

    public void toggle() {
        if (active) {
            disable();
        } else {
            enable();
        }
    }

    public void enable() {
        if (!active) {
            active = true;
            oldCameraType = mc.options.getPerspective();
            oldInput = mc.player.input;
            mc.player.input = new Input();
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            if (oldCameraType.isFirstPerson() != mc.options.getPerspective().isFirstPerson()) {
                mc.gameRenderer.onCameraEntitySet(mc.options.getPerspective().isFirstPerson() ? mc.getCameraEntity() : null);
            }

            float frameTime = mc.getLastFrameDuration();
            Entity entity = mc.getCameraEntity();
            x = MathHelper.lerp(frameTime, entity.lastRenderX, entity.getX());
            y = MathHelper.lerp(frameTime, entity.lastRenderY, entity.getY()) + entity.getStandingEyeHeight();
            z = MathHelper.lerp(frameTime, entity.lastRenderZ, entity.getZ());
            yRot = entity.getYaw(frameTime);
            xRot = entity.getPitch(frameTime);

            calculateVectors();

            double distance = -2;
            x += (double)this.forwards.getX() * distance;
            y += (double)this.forwards.getY() * distance;
            z += (double)this.forwards.getZ() * distance;

            forwardVelocity = 0;
            leftVelocity = 0;
            upVelocity = 0;
            lastTime = 0;
        }
    }

    public void disable() {
        if (active) {
            active = false;
            Perspective cameraType = mc.options.getPerspective();
            mc.options.setPerspective(oldCameraType);
            mc.player.input = oldInput;
            if (cameraType.isFirstPerson() != mc.options.getPerspective().isFirstPerson()) {
                mc.gameRenderer.onCameraEntitySet(mc.options.getPerspective().isFirstPerson() ? mc.getCameraEntity() : null);
            }
            oldCameraType = null;
        }
    }

    public void onMouseTurn(double yRot, double xRot) {
        this.xRot += (float)xRot * 0.15F;
        this.yRot += (float)yRot * 0.15F;
        this.xRot = MathHelper.clamp(this.xRot, -90, 90);
        calculateVectors();
    }

    private void onRenderTickStart() {
        if (active) {
            if (lastTime == 0) {
                lastTime = System.nanoTime();
                return;
            }

            long currTime = System.nanoTime();
            float frameTime = (currTime - lastTime) / 1e9f;
            lastTime = currTime;

            FreeCamConfig config = ConfigStore.instance.getConfig().freeCamConfig;

            Input input = oldInput;
            float forwardImpulse = (input.pressingForward ? 1 : 0) + (input.pressingBack ? -1 : 0);
            float leftImpulse = (input.pressingLeft ? 1 : 0) + (input.pressingRight ? -1 : 0);
            float upImpulse = ((input.jumping ? 1 : 0) + (input.sneaking ? -1 : 0));
            double slowdown = Math.pow(config.slowdownFactor, frameTime);
            forwardVelocity = combineMovement(forwardVelocity, forwardImpulse, frameTime, config.acceleration, slowdown);
            leftVelocity = combineMovement(leftVelocity, leftImpulse, frameTime, config.acceleration, slowdown);
            upVelocity = combineMovement(upVelocity, upImpulse, frameTime, config.acceleration, slowdown);

            double dx = (double) this.forwards.getX() * forwardVelocity + (double) this.left.getX() * leftVelocity;
            double dy = (double) this.forwards.getY() * forwardVelocity + upVelocity + (double) this.left.getY() * leftVelocity;
            double dz = (double) this.forwards.getZ() * forwardVelocity + (double) this.left.getZ() * leftVelocity;
            dx *= frameTime;
            dy *= frameTime;
            dz *= frameTime;
            double speed = new Vec3d(dx, dy, dz).length() / frameTime;
            if (speed > config.maxSpeed) {
                double factor = config.maxSpeed / speed;
                forwardVelocity *= factor;
                leftVelocity *= factor;
                upVelocity *= factor;
                dx *= factor;
                dy *= factor;
                dz *= factor;
            }
            x += dx;
            y += dy;
            z += dz;
        }
    }

    private void onClientTickStart() {
        if (active) {
            while (mc.options.togglePerspectiveKey.wasPressed()) {
                // consume clicks
            }
            oldInput.tick(false, 0);
        }
    }

    private void onWorldUnload() {
        disable();
    }

    public boolean shouldOverridePlayerPosition() {
        return MixinGameRendererHelper.insideUpdateTargetedEntity || insideRenderDebug;
    }

    public void onDebugScreenGetGameInformation(List<String> list) {
        if (active) {
            String coordinates = String.format(Locale.ROOT, "Free Cam XYZ: %.3f / %.5f / %.3f", x, y, z);
            list.add(coordinates);
        }
    }

    public HitResult getHitResult() {
        if (active) {
            insideRenderDebug = true;
            try {
                return mc.player.raycast(20.0D, 0.0F, false);
            }
            finally {
                insideRenderDebug = false;
            }
        } else {
            return null;
        }
    }

    public void onDebugScreenGetSystemInformation(List<String> list) {
        if (active) {
            HitResult hit = getHitResult();
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult)hit).getBlockPos();
                BlockState state = mc.world.getBlockState(pos);
                list.add("");
                list.add(Formatting.UNDERLINE + "Free Cam Targeted Block: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                list.add(String.valueOf(Registry.BLOCK.getId(state.getBlock())));

                for (var entry: state.getEntries().entrySet()) {
                    list.add(propertyToString(entry));
                }

                state.streamTags().map(tag -> "#" + tag.id()).forEach(list::add);
            }
        }
    }

    private void calculateVectors() {
        rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
        rotation.hamiltonProduct(Vec3f.POSITIVE_Y.getDegreesQuaternion(-yRot));
        rotation.hamiltonProduct(Vec3f.POSITIVE_X.getDegreesQuaternion(xRot));
        forwards.set(0.0F, 0.0F, 1.0F);
        forwards.rotate(rotation);
        up.set(0.0F, 1.0F, 0.0F);
        up.rotate(rotation);
        left.set(1.0F, 0.0F, 0.0F);
        left.rotate(rotation);
    }

    private double combineMovement(double velocity, double impulse, double frameTime, double acceleration, double slowdown) {
        if (impulse != 0) {
            if (impulse > 0 && velocity < 0) {
                velocity = 0;
            }
            if (impulse < 0 && velocity > 0) {
                velocity = 0;
            }
            velocity += acceleration * impulse * frameTime;
        } else {
            velocity *= slowdown;
        }
        return velocity;
    }

    private String propertyToString(Map.Entry<Property<?>, Comparable<?>> propEntry) {
        Property<?> property = propEntry.getKey();
        Comparable<?> comparable = propEntry.getValue();
        String string = Util.getValueAsString(property, comparable);
        if (Boolean.TRUE.equals(comparable)) {
            string = Formatting.GREEN + string;
        } else if (Boolean.FALSE.equals(comparable)) {
            string = Formatting.RED + string;
        }

        return property.getName() + ": " + string;
    }
}