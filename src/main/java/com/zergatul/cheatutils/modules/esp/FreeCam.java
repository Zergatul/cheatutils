package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PlayerTurnByMouseEvent;
import com.zergatul.cheatutils.common.events.SimpleCancellableEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.FreeCamConfig;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.entity.EntityPlayerSP;
import com.zergatul.cheatutils.math.Quaternion;
import com.zergatul.cheatutils.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class FreeCam implements Module {

    public static final FreeCam INSTANCE = new FreeCam();

    private static final int REMEMBER_STATE_DELAY_MS = 400;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
    private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
    private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
    private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
    private boolean active;
    private int oldCameraType;
    private MovementInput playerInput;
    private MovementInput freecamInput;
    private Entity viewEntity;
    private EntityPlayerSP inputPlayer;
    private double x, y, z;
    private float yRot, xRot;
    private double forwardVelocity;
    private double leftVelocity;
    private double upVelocity;
    private long lastTime;
    private long dontMoveFreeCamBefore;
    private boolean picking;
    private boolean cameraRestoredForPicking;

    private FreeCam() {
        Events.ClientTickStart.add(this::onClientTickStart);
        Events.RenderTickStart.add(this::onRenderTickStart);
        Events.LevelUnload.add(this::onWorldUnload);
        Events.OnBeforePick.add(this::onBeforePick);
        Events.OnAfterPick.add(this::onAfterPick);
        Events.BeforeRenderWorld.add(this::onBeforeRenderWorld);
        Events.AfterRenderWorld.add(this::onAfterRenderWorld);
        Events.BeforeRenderEntities.add(this::onBeforeRenderEntities);
        Events.AfterRenderEntities.add(this::onAfterRenderEntities);
        Events.BeforeRenderEntity.add(this::onBeforeRenderEntity);
        Events.AfterRenderEntity.add(this::onAfterRenderEntity);
        Events.DebugInfoLeft.add(this::onGetDebugInfoLeft);
        Events.PlayerTurnByMouse.add(this::onPlayerTurnByMouse);
        Events.RenderHand.add(this::onRenderHand);
        Events.ConfigLoaded.add(this::disable);
        Events.Close.add(this::disable, -1);
    }

    public boolean isActive() {
        return active;
    }

    public FreeCamConfig getConfig() {
        return ConfigStore.instance.getConfig().freeCamConfig;
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

    public boolean shouldOverrideSpectator(AbstractClientPlayer player) {
        return override == player && !entitiesRendering;
    }

    public double getViewFrustumEntityPosX(double viewEntityX) {
        return override != null ? px : viewEntityX;
    }

    public double getViewFrustumEntityPosZ(double viewEntityZ) {
        return override != null ? pz : viewEntityZ;
    }

    public boolean shouldDisableBobbing() {
        return active;
    }

    public void enable() {
        if (active) {
            return;
        }

        Entity entity = mc.getRenderViewEntity();
        if (entity == null || mc.player == null || mc.world == null || mc.player.isDead) {
            return;
        }

        active = true;
        oldCameraType = mc.gameSettings.thirdPersonView;
        playerInput = mc.player.movementInput;
        playerInput.updatePlayerMoveState();
        mc.player.movementInput = freecamInput = createFreeCamInput(playerInput);
        mc.gameSettings.thirdPersonView = 0;

        if (getConfig().rememberInputState) {
            dontMoveFreeCamBefore = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(REMEMBER_STATE_DELAY_MS);
        }

        viewEntity = entity;
        inputPlayer = mc.player;
        Vec3d pos = entity.getPositionEyes(1);
        x = pos.x;
        y = pos.y;
        z = pos.z;
        yRot = entity.rotationYaw;
        xRot = entity.rotationPitch;

        calculateVectors();

        double distance = -2;
        x += (double) forwards.x * distance;
        y += (double) forwards.y * distance;
        z += (double) forwards.z * distance;

        forwardVelocity = 0;
        leftVelocity = 0;
        upVelocity = 0;
        lastTime = System.nanoTime();
    }

    public void disable() {
        if (!active) {
            return;
        }

        active = false;
        mc.gameSettings.thirdPersonView = oldCameraType;
        onAfterRenderWorld();
        if (inputPlayer != null && inputPlayer.movementInput == freecamInput) {
            inputPlayer.movementInput = playerInput;
        }
        inputPlayer = null;
        viewEntity = null;
        playerInput = null;
        freecamInput = null;
        picking = false;
        cameraRestoredForPicking = false;
        entitiesRendering = false;
    }

    public boolean shouldRenderTarget() {
        return !active || getConfig().target;
    }

    public boolean shouldRenderHands() {
        return !active || getConfig().renderHands;
    }

    public boolean shouldOverrideCameraEntityForPicking(Entity entity) {
        return active && getConfig().target &&
                picking && entity == mc.getRenderViewEntity();
    }

    public Vec3d getTargetLookVector() {
        float yawCos = MathHelper.cos(-yRot * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-yRot * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-xRot * 0.017453292F);
        float pitchSin = MathHelper.sin(-xRot * 0.017453292F);
        return new Vec3d(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    public AxisAlignedBB getTargetSearchBox(Entity entity, AxisAlignedBB box) {
        if (!shouldOverrideCameraEntityForPicking(entity)) {
            return box;
        }

        double dx = x - entity.posX;
        double dy = y - (entity.posY + entity.getEyeHeight());
        double dz = z - entity.posZ;
        return box.offset(dx, dy, dz);
    }

    private void onPlayerTurnByMouse(PlayerTurnByMouseEvent event) {
        if (!active) return;
        double xRot = event.getXRot();
        double yRot = event.getYRot();
        this.xRot = MathHelper.clamp(this.xRot + (float) xRot * 0.15F, -90, 90);
        this.yRot += (float) yRot * 0.15F;
        calculateVectors();
        event.cancel();
    }

    private void onClientTickStart() {
        validatePlayer();
        if (active) {
            while (mc.gameSettings.keyBindTogglePerspective.isPressed()) {
                // consume clicks
            }
            if (mc.player != null && mc.player.movementInput != playerInput) {
                playerInput.updatePlayerMoveState();
            }
        }
    }

    private void onRenderTickStart(float partialTicks) {
        validatePlayer();
        if (!active) {
            return;
        }

        long currTime = System.nanoTime();
        float frameTime = Math.min((currTime - lastTime) / 1e9f, 0.1F);
        lastTime = currTime;
        if (mc.isGamePaused() || mc.currentScreen != null || !mc.inGameHasFocus || frameTime <= 0) {
            forwardVelocity = leftVelocity = upVelocity = 0;
            return;
        }
        calculateVectors();

        MovementInput input = playerInput;
        float forwardImpulse = input.moveForward;
        float leftImpulse = input.moveStrafe;
        float upImpulse = (input.jump ? 1 : 0) + (input.sneak ? -1 : 0);
        double slowdown = Math.pow(getConfig().slowdownFactor, frameTime);
        forwardVelocity = combineMovement(forwardVelocity, forwardImpulse, frameTime, getConfig().acceleration, slowdown);
        leftVelocity = combineMovement(leftVelocity, leftImpulse, frameTime, getConfig().acceleration, slowdown);
        upVelocity = combineMovement(upVelocity, upImpulse, frameTime, getConfig().acceleration, slowdown);

        double dx = (double) forwards.x * forwardVelocity + (double) left.x * leftVelocity;
        double dy = (double) forwards.y * forwardVelocity + upVelocity + (double) left.y * leftVelocity;
        double dz = (double) forwards.z * forwardVelocity + (double) left.z * leftVelocity;
        dx *= frameTime;
        dy *= frameTime;
        dz *= frameTime;
        double speed = Math.sqrt(dx * dx + dy * dy + dz * dz) / frameTime;
        if (speed > getConfig().maxSpeed) {
            double factor = getConfig().maxSpeed / speed;
            forwardVelocity *= factor;
            leftVelocity *= factor;
            upVelocity *= factor;
            dx *= factor;
            dy *= factor;
            dz *= factor;
        }
        if (!getConfig().rememberInputState || currTime >= dontMoveFreeCamBefore) {
            x += dx;
            y += dy;
            z += dz;
        }

    }

    private void onBeforePick() {
        picking = true;
        cameraRestoredForPicking = false;

        if (override != null) {
            restoreCameraEntityPosition();
            cameraRestoredForPicking = true;
        }
    }

    private void onAfterPick() {
        if (cameraRestoredForPicking && override != null) {
            moveCameraEntityToFreeCamPosition();
        }

        cameraRestoredForPicking = false;
        picking = false;
    }

    private void onRenderHand(SimpleCancellableEvent event) {
        if (!shouldRenderHands()) event.cancel();
    }

    private void onWorldUnload() {
        disable();
    }

    private void onGetDebugInfoLeft(List<String> list) {
        if (active) {
            list.add("");
            list.add("FreeCam");
            list.add(String.format("XYZ: %.3f / %.5f / %.3f", x, y, z));
            list.add(String.format("Facing: (%.1f / %.1f)",
                    MathHelper.wrapDegrees(yRot),
                    MathHelper.wrapDegrees(xRot)));
        }
    }

    private double px, py, pz, lastX, lastY, lastZ, llX, llY, llZ;
    private float eXRot, eYRot, lastXRot, lastYRot;
    private boolean pNoClip;
    private Entity override;
    private boolean entitiesRendering;

    private void onBeforeRenderWorld() {
        onAfterRenderWorld();

        if (!active) {
            return;
        }

        Entity cameraEntity = mc.getRenderViewEntity();
        if (cameraEntity == null) {
            return;
        }

        override = cameraEntity;
        saveCameraEntityPosition();
        moveCameraEntityToFreeCamPosition();
        pNoClip = override.noClip;
        override.noClip = true;
    }

    private void onAfterRenderWorld() {
        if (override == null) {
            return;
        }

        restoreCameraEntityPosition();
        override.noClip = pNoClip;
        override = null;
    }

    private void onBeforeRenderEntity(Entity entity) {
        if (override == entity) {
            restoreCameraEntityPosition();
        }
    }

    private void onAfterRenderEntity(Entity entity) {
        if (override == entity) {
            moveCameraEntityToFreeCamPosition();
        }
    }

    private void onBeforeRenderEntities() {
        entitiesRendering = true;
        if (override != null) {
            mc.gameSettings.thirdPersonView = 1;
        }
    }

    private void onAfterRenderEntities() {
        entitiesRendering = false;
        if (override != null) {
            mc.gameSettings.thirdPersonView = 0;
        }
    }

    private MovementInput createFreeCamInput(MovementInput input) {
        MovementInput result = new MovementInput();
        if (getConfig().rememberInputState) {
            result.moveForward = input.moveForward;
            result.moveStrafe = input.moveStrafe;
            result.jump = input.jump;
            result.sneak = input.sneak;
        }
        return result;
    }

    private void validatePlayer() {
        if (active && (mc.world == null || mc.player != inputPlayer || inputPlayer.isDead ||
                inputPlayer.world != mc.world || mc.getRenderViewEntity() != viewEntity)) {
            disable();
        }
    }

    private void calculateVectors() {
        rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
        rotation.mul(Vector3f.YP.rotationDegrees(-yRot));
        if (!getConfig().spectatorFlight) {
            rotation.mul(Vector3f.XP.rotationDegrees(xRot));
        }
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

    private void saveCameraEntityPosition() {
        px = override.posX;
        py = override.posY;
        pz = override.posZ;
        lastX = override.lastTickPosX;
        lastY = override.lastTickPosY;
        lastZ = override.lastTickPosZ;
        llX = override.prevPosX;
        llY = override.prevPosY;
        llZ = override.prevPosZ;
        eXRot = override.rotationPitch;
        eYRot = override.rotationYaw;
        lastXRot = override.prevRotationPitch;
        lastYRot = override.prevRotationYaw;
    }

    private void restoreCameraEntityPosition() {
        override.posX = px;
        override.posY = py;
        override.posZ = pz;
        override.lastTickPosX = lastX;
        override.lastTickPosY = lastY;
        override.lastTickPosZ = lastZ;
        override.prevPosX = llX;
        override.prevPosY = llY;
        override.prevPosZ = llZ;
        override.rotationPitch = eXRot;
        override.rotationYaw = eYRot;
        override.prevRotationPitch = lastXRot;
        override.prevRotationYaw = lastYRot;
    }

    private void moveCameraEntityToFreeCamPosition() {
        override.posX = override.lastTickPosX = override.prevPosX = x;
        override.posY = override.lastTickPosY = override.prevPosY = y - override.getEyeHeight();
        override.posZ = override.lastTickPosZ = override.prevPosZ = z;
        override.rotationPitch = override.prevRotationPitch = xRot;
        override.rotationYaw = override.prevRotationYaw = yRot;
    }
}