package com.zergatul.cheatutils.controllers;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FreeCamConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreeCamController {

    public static final FreeCamController instance = new FreeCamController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
    private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
    private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
    private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
    private boolean active;
    private CameraType oldCameraType;
    private Input oldInput;
    private double x, y, z;
    private float yRot, xRot;
    private double forwardVelocity;
    private double leftVelocity;
    private double upVelocity;
    private long lastTime;

    private FreeCamController() {

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
            oldCameraType = mc.options.getCameraType();
            oldInput = mc.player.input;
            mc.player.input = new Input();
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            if (oldCameraType.isFirstPerson() != mc.options.getCameraType().isFirstPerson()) {
                mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
            }

            float frameTime = mc.getFrameTime();
            Entity entity = mc.getCameraEntity();
            x = Mth.lerp(frameTime, entity.xo, entity.getX());
            y = Mth.lerp(frameTime, entity.yo, entity.getY()) + entity.getEyeHeight();
            z = Mth.lerp(frameTime, entity.zo, entity.getZ());
            yRot = entity.getViewYRot(frameTime);
            xRot = entity.getViewXRot(frameTime);

            calculateVectors();

            double distance = -2;
            x += (double)this.forwards.x() * distance;
            y += (double)this.forwards.y() * distance;
            z += (double)this.forwards.z() * distance;

            forwardVelocity = 0;
            leftVelocity = 0;
            upVelocity = 0;
            lastTime = 0;
        }
    }

    public void disable() {
        if (active) {
            active = false;
            CameraType cameraType = mc.options.getCameraType();
            mc.options.setCameraType(oldCameraType);
            mc.player.input = oldInput;
            if (cameraType.isFirstPerson() != mc.options.getCameraType().isFirstPerson()) {
                mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
            }
            oldCameraType = null;
        }
    }

    public void onMouseTurn(double yRot, double xRot) {
        this.xRot += (float)xRot * 0.15F;
        this.yRot += (float)yRot * 0.15F;
        this.xRot = Mth.clamp(this.xRot, -90, 90);
        calculateVectors();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (active) {
            if (event.phase == TickEvent.Phase.START) {
                if (lastTime == 0) {
                    lastTime = System.nanoTime();
                    return;
                }

                long currTime = System.nanoTime();
                float frameTime = (currTime - lastTime) / 1e9f;
                lastTime = currTime;

                FreeCamConfig config = ConfigStore.instance.getConfig().freeCamConfig;

                Input input = oldInput;
                float forwardImpulse = (input.up ? 1 : 0) + (input.down ? -1 : 0);
                float leftImpulse = (input.left ? 1 : 0) + (input.right ? -1 : 0);
                float upImpulse = ((input.jumping ? 1 : 0) + (input.shiftKeyDown ? -1 : 0));
                double slowdown = Math.pow(config.slowdownFactor, frameTime);
                forwardVelocity = combineMovement(forwardVelocity, forwardImpulse, frameTime, config.acceleration, slowdown);
                leftVelocity = combineMovement(leftVelocity, leftImpulse, frameTime, config.acceleration, slowdown);
                upVelocity = combineMovement(upVelocity, upImpulse, frameTime, config.acceleration, slowdown);

                double dx = (double) this.forwards.x() * forwardVelocity + (double) this.left.x() * leftVelocity;
                double dy = (double) this.forwards.y() * forwardVelocity + upVelocity + (double) this.left.y() * leftVelocity;
                double dz = (double) this.forwards.z() * forwardVelocity + (double) this.left.z() * leftVelocity;
                double speed = new Vec3(dx, dy, dz).length() / frameTime;
                if (speed > config.maxSpeed) {
                    double factor = config.maxSpeed / speed;
                    forwardVelocity *= factor;
                    leftVelocity *= factor;
                    upVelocity *= factor;
                }
                x += dx;
                y += dy;
                z += dz;
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (active) {
            if (event.phase == TickEvent.Phase.START) {
                while (mc.options.keyTogglePerspective.consumeClick()) {
                    // consume clicks
                }
                oldInput.tick(false, 0);
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        disable();
    }

    private void calculateVectors() {
        rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
        rotation.mul(Vector3f.YP.rotationDegrees(-yRot));
        rotation.mul(Vector3f.XP.rotationDegrees(xRot));
        forwards.set(0.0F, 0.0F, 1.0F);
        forwards.transform(rotation);
        up.set(0.0F, 1.0F, 0.0F);
        up.transform(rotation);
        left.set(1.0F, 0.0F, 0.0F);
        left.transform(rotation);
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
}