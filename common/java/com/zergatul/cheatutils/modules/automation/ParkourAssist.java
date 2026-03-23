package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ParkourAssist implements Module {

    public static final ParkourAssist instance = new ParkourAssist();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean enabled;
    private Vec3 playerPos;
    private Vec3 lastTickSpeed;

    private ParkourAssist() {
        Events.BeforePlayerAiStep.add(this::onBeforePlayerAiStep);
        Events.ModifyPlayerInput.add(this::onModifyPlayerInput);
        Events.AfterPlayerAiStep.add(this::onAfterPlayerAiStep);
        Events.ClientPlayerLoggingOut.add(this::onLoggingOut);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    private void onBeforePlayerAiStep() {
        assert mc.player != null;

        playerPos = mc.player.position();
    }

    private void onModifyPlayerInput() {
        assert mc.level != null && mc.player != null;

        if (!enabled || lastTickSpeed == null) {
            return;
        }

        if (!mc.player.onGround() || lastTickSpeed.y > 0) {
            // jumping or falling
            return;
        }

        // copied from Entity.checkSupportingBlock
        double threshold = ConfigStore.instance.getConfig().parkourAssistConfig.threshold;
        AABB boundingBox = mc.player.getBoundingBox().move(lastTickSpeed);
        AABB testArea = new AABB(boundingBox.minX, boundingBox.minY - threshold, boundingBox.minZ, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        Optional<BlockPos> supportingBlock = mc.level.findSupportingBlock(mc.player, testArea);
        if (supportingBlock.isEmpty()) {
            mc.player.input.makeJump();
        }
    }

    private void onAfterPlayerAiStep() {
        assert mc.player != null;

        lastTickSpeed = mc.player.position().subtract(playerPos);
    }

    private void onLoggingOut() {
        lastTickSpeed = null;
    }
}