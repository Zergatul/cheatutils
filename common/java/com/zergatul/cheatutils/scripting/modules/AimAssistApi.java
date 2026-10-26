package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AimAssistConfig;
import com.zergatul.cheatutils.modules.automation.AimAssist;
import net.minecraft.world.entity.Entity;

@SuppressWarnings("unused")
public class AimAssistApi {

    public boolean isBowAssistEnabled() {
        return getConfig().bowAssist();
    }

    public void toggleBowAssist() {
        AimAssist.INSTANCE.getSettings().update(config -> config.withBowAssist(!config.bowAssist()));
    }

    public boolean hasBowAssistTarget() {
        return AimAssist.INSTANCE.getBowAssistTarget() != null;
    }

    public int getBowAssistEntityId() {
        Entity target = AimAssist.INSTANCE.getBowAssistTarget();
        return target != null ? target.getId() : Integer.MIN_VALUE;
    }

    public boolean isTargetLockEnabled() {
        return AimAssist.INSTANCE.isTargetLockEnabled();
    }

    public void enableTargetLock() {
        AimAssist.INSTANCE.enableTargetLock();
    }

    public void disableTargetLock() {
        AimAssist.INSTANCE.disableTargetLock();
    }

    private AimAssistConfig getConfig() {
        return AimAssist.INSTANCE.getSettings().get();
    }
}