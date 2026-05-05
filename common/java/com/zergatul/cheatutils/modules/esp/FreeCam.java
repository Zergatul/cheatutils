package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ModifyFieldOfViewEvent;
import com.zergatul.cheatutils.common.events.PlayerTurnByMouseEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FreeCamConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.render.LineRenderer;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.utils.FreeCamPath;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FreeCam implements Module {

    public static final FreeCam instance = new FreeCam();

    private final static int REMEMBER_STATE_DELAY_MS = 400;

    private final Minecraft mc = Minecraft.getInstance();
    private final Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
    private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
    private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
    private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
    private final FreeCamPath path = new FreeCamPath(this);
    private boolean active;
    private CameraType oldCameraType;
    private ClientInput playerInput;
    private ClientInput freecamInput;
    private double x, y, z;
    private float yRot, xRot;
    private double forwardVelocity;
    private double leftVelocity;
    private double upVelocity;
    private long lastTime;
    private boolean freecamHitResultPicking;
    private boolean cameraLock;
    private boolean eyeLock;
    private boolean followCamera;
    private double followDeltaX, followDeltaY, followDeltaZ;
    private boolean picking;
    private boolean moveAlongPath;
    private long pathStartTime;
    private long doNotMoveFreeCamBefore;

    private FreeCam() {
        Events.PlayerTurnByMouse.add(this::onPlayerTurnByMouse);
        Events.ClientTickStart.add(this::onClientTickStart);
        Events.ModifyFieldOfView.add(this::onModifyFieldOfView, 0);
        Events.RenderTickStart.add(this::onRenderTickStart);
        Events.OnBeforePick.add(this::onBeforePick);
        Events.OnAfterPick.add(this::onAfterPick);
        Events.AfterRenderWorld.add(this::onRenderWorldLast);
        Events.LevelUnload.add(this::onWorldUnload);
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

    public FreeCamPath getPath() {
        return path;
    }

    public void toggle() {
        if (active) {
            disable();
        } else {
            enable();
        }
    }

    public void toggleCameraLock() {
        if (mc.player == null) {
            return;
        }

        if (active && !followCamera) {
            cameraLock = !cameraLock;
            if (cameraLock) {
                mc.player.input = playerInput;
            } else {
                mc.player.input = freecamInput;
            }
        }
    }

    public void toggleEyeLock() {
        if (active && !followCamera) {
            eyeLock = !eyeLock;
        }
    }

    public void toggleFollowCamera() {
        assert mc.player != null;

        if (active) {
            followCamera = !followCamera;
            if (followCamera) {
                mc.player.input = playerInput;
                cameraLock = false;
                eyeLock = false;

                Entity entity = mc.getCameraEntity();
                if (entity == null) {
                    return;
                }

                Vec3 pos = entity.getEyePosition();
                followDeltaX = x - pos.x;
                followDeltaY = y - pos.y;
                followDeltaZ = z - pos.z;
            } else {
                mc.player.input = freecamInput;
            }
        }
    }

    public void enable() {
        if (active) {
            return;
        }

        Entity entity = mc.getCameraEntity();
        if (mc.player == null || entity == null) {
            return;
        }

        active = true;
        cameraLock = false;
        eyeLock = false;
        followCamera = false;
        oldCameraType = mc.options.getCameraType();
        playerInput = new KeyboardInput(mc.options); //mc.player.input; // changed for baritone compat
        mc.player.input = freecamInput = createFreeCamInput(mc.player.input);
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        if (oldCameraType.isFirstPerson() != mc.options.getCameraType().isFirstPerson()) {
            mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
        }

        if (getConfig().rememberInputState) {
            doNotMoveFreeCamBefore = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(REMEMBER_STATE_DELAY_MS);
        }

        float frameTime = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 pos = entity.getEyePosition(frameTime);
        x = pos.x;
        y = pos.y;
        z = pos.z;
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

    public void disable() {
        assert mc.player != null;

        if (!active) {
            return;
        }

        active = false;
        moveAlongPath = false;
        CameraType cameraType = mc.options.getCameraType();
        mc.options.setCameraType(oldCameraType);
        mc.player.input = playerInput;
        if (cameraType.isFirstPerson() != mc.options.getCameraType().isFirstPerson()) {
            mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
        }
        oldCameraType = null;
    }

    public boolean onRenderCrosshairIsFirstPerson(boolean isFirstPerson) {
        FreeCamConfig config = getConfig();
        if (active) {
            return !cameraLock && !eyeLock && !followCamera && config.target;
        } else {
            return isFirstPerson;
        }
    }

    public boolean onRenderItemInHandIsFirstPerson(CameraType cameraType) {
        FreeCamConfig config = getConfig();
        if (active && config.renderHands && !cameraLock && !eyeLock && !followCamera) {
            return true;
        } else {
            return cameraType.isFirstPerson();
        }
    }

    public boolean isCameraDetached(boolean value) {
        FreeCamConfig config = getConfig();
        if (active && config.renderHands && !cameraLock && !eyeLock && !followCamera) {
            return false;
        } else {
            return value;
        }
    }

    private void onPlayerTurnByMouse(PlayerTurnByMouseEvent event) {
        if (active && !cameraLock && !followCamera) {
            if (!eyeLock && !moveAlongPath) {
                this.xRot += (float) event.getXRot() * 0.15F;
                this.yRot += (float) event.getYRot() * 0.15F;
                this.xRot = Mth.clamp(this.xRot, -90, 90);
                calculateVectors();
            }
            event.cancel();
        }
    }

    private void onModifyFieldOfView(ModifyFieldOfViewEvent event) {
        if (active) {
            event.fov = mc.options.fov().get();
        }
    }

    private void onRenderTickStart(DeltaTracker delta) {
        if (!active) {
            return;
        }

        if (lastTime == 0) {
            lastTime = System.nanoTime();
            return;
        }

        long currTime = System.nanoTime();
        float frameTime = (currTime - lastTime) / 1e9f;
        lastTime = currTime;

        FreeCamConfig config = getConfig();

        if (moveAlongPath) {
            FreeCamPath.Entry entry = path.interpolate((currTime - pathStartTime) / 1e6);
            if (entry == null) {
                moveAlongPath = false;
            } else {
                x = entry.position().x;
                y = entry.position().y;
                z = entry.position().z;
                xRot = (float) entry.xRot();
                yRot = (float) entry.yRot();
            }
        } else if (followCamera) {
            Entity entity = mc.getCameraEntity();
            if (entity != null) {
                Vec3 pos = entity.getEyePosition(delta.getGameTimeDeltaPartialTick(true));
                x = pos.x + followDeltaX;
                y = pos.y + followDeltaY;
                z = pos.z + followDeltaZ;
            }
        } else {
            ClientInput input = playerInput;
            float forwardImpulse = !cameraLock ? (input.keyPresses.forward() ? 1 : 0) + (input.keyPresses.backward() ? -1 : 0) : 0;
            float leftImpulse = !cameraLock ? (input.keyPresses.left() ? 1 : 0) + (input.keyPresses.right() ? -1 : 0) : 0;
            float upImpulse = !cameraLock ? ((input.keyPresses.jump() ? 1 : 0) + (input.keyPresses.shift() ? -1 : 0)) : 0;
            double slowdown = Math.pow(config.slowdownFactor, frameTime);
            forwardVelocity = combineMovement(forwardVelocity, forwardImpulse, frameTime, config.acceleration, slowdown);
            leftVelocity = combineMovement(leftVelocity, leftImpulse, frameTime, config.acceleration, slowdown);
            upVelocity = combineMovement(upVelocity, upImpulse, frameTime, config.acceleration, slowdown);

            double dx = (double) this.forwards.x() * forwardVelocity + (double) this.left.x() * leftVelocity;
            double dy = (double) this.forwards.y() * forwardVelocity + upVelocity + (double) this.left.y() * leftVelocity;
            double dz = (double) this.forwards.z() * forwardVelocity + (double) this.left.z() * leftVelocity;
            dx *= frameTime;
            dy *= frameTime;
            dz *= frameTime;
            double speed = new Vec3(dx, dy, dz).length() / frameTime;
            if (speed > config.maxSpeed) {
                double factor = config.maxSpeed / speed;
                forwardVelocity *= factor;
                leftVelocity *= factor;
                upVelocity *= factor;
                dx *= factor;
                dy *= factor;
                dz *= factor;
            }
            if (!config.rememberInputState || doNotMoveFreeCamBefore < currTime) {
                x += dx;
                y += dy;
                z += dz;
            }
        }

        applyEyeLock(delta.getGameTimeDeltaPartialTick(true));
    }

    private void onClientTickStart() {
        if (active) {
            while (mc.options.keyTogglePerspective.consumeClick()) {
                // consume clicks
            }
            if (mc.player != null && mc.player.input != playerInput) {
                // don't tick here, since vanilla code will run tick during LocalPlayer.aiStep
                playerInput.tick();
            }
        }
    }

    private void onBeforePick() {
        picking = true;
    }

    private void onAfterPick() {
        picking = false;
    }

    private void onWorldUnload() {
        disable();
    }

    private void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!active || moveAlongPath) {
            return;
        }

        List<FreeCamPath.Entry> path = getPath().get();
        if (path.size() < 2) {
            return;
        }

        LineRenderer renderer = LineRenderer.getInstance();
        renderer.begin();

        for (int i = 1; i < path.size(); i++) {
            FreeCamPath.Entry e1 = path.get(i - 1);
            FreeCamPath.Entry e2 = path.get(i);

            renderer.line(
                    event.getCameraPos(),
                    e1.position().x, e1.position().y, e1.position().z,
                    e2.position().x, e2.position().y, e2.position().z,
                    Color.WHITE.getRGB(), 1f);
        }

        renderer.end(event.getMvp());
    }

    public void startPath() {
        if (!active) {
            return;
        }

        moveAlongPath = true;
        pathStartTime = System.nanoTime();
    }

    public boolean shouldOverrideCameraEntityPosition(Entity entity) {
        FreeCamConfig config = getConfig();
        if (active && !cameraLock && !eyeLock && !followCamera && config.target) {
            return entity == mc.getCameraEntity() && picking || freecamHitResultPicking;
        } else {
            return false;
        }
    }

    public HitResult getHitResult() {
        if (!active || mc.player == null) {
            return null;
        }
        if (cameraLock || eyeLock || followCamera) {
            return null;
        }
        if (!getConfig().target) {
            return null;
        }

        freecamHitResultPicking = true;
        try {
            return mc.player.pick(20.0D, 0.0F, false);
        }
        finally {
            freecamHitResultPicking = false;
        }
    }

    private ClientInput createFreeCamInput(ClientInput playerInput) {
        if (getConfig().rememberInputState) {
            return new FreeCamInput(new Input(
                    playerInput.keyPresses.forward(),
                    playerInput.keyPresses.backward(),
                    playerInput.keyPresses.left(),
                    playerInput.keyPresses.right(),
                    playerInput.keyPresses.jump(),
                    playerInput.keyPresses.shift(),
                    playerInput.keyPresses.sprint()));
        } else {
            return new ClientInput();
        }
    }

    private void applyEyeLock(float partialTicks) {
        if (!eyeLock) {
            return;
        }

        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return;
        }

        Vec3 pos = entity.getEyePosition(partialTicks);
        double dx = x - pos.x;
        double dy = y - pos.y;
        double dz = z - pos.z;
        this.xRot = (float) (Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) / Math.PI * 180);
        this.yRot = (float) (Math.atan2(dz, dx) / Math.PI * 180 + 90);
        this.xRot = Mth.clamp(this.xRot, -90, 90);
        calculateVectors();
    }

    private void calculateVectors() {
        rotation.rotationYXZ(
                -yRot * ((float)Math.PI / 180F),
                (getConfig().spectatorFlight ? 0 : xRot) * ((float)Math.PI / 180F),
                0.0F);
        forwards.set(0.0F, 0.0F, 1.0F).rotate(rotation);
        up.set(0.0F, 1.0F, 0.0F).rotate(rotation);
        left.set(1.0F, 0.0F, 0.0F).rotate(rotation);
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

    private FreeCamConfig getConfig() {
        return ConfigStore.instance.getConfig().freeCamConfig;
    }

    private static class FreeCamInput extends ClientInput {

        public FreeCamInput(Input input) {
            this.keyPresses = input;
            this.moveVector = new Vec2(
                    calcImpulse(input.left(), input.right()),
                    calcImpulse(input.forward(), input.backward())).normalized();
        }

        private static float calcImpulse(boolean b1, boolean b2) {
            if (b1 == b2) {
                return 0.0F;
            } else {
                return b1 ? 1.0F : -1.0F;
            }
        }
    }
}