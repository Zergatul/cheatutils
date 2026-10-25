package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.utilities.LockInputs;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.configs.modules.LockInputsConfig;

import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
public class LockInputsApi {

    public boolean isHoldForwardEnabled() {
        return getConfig().holdForward();
    }

    public boolean isHoldUseEnabled() {
        return getConfig().holdUse();
    }

    public boolean isHoldAttackEnabled() {
        return getConfig().holdAttack();
    }

    public boolean isMouseInputDisabled() {
        return getConfig().mouseInputDisabled();
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleHoldForward() {
        update(c -> c.withHoldForward(!c.holdForward()));
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleHoldUse() {
        update(c -> c.withHoldUse(!c.holdUse()));
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleHoldAttack() {
        update(c -> c.withHoldAttack(!c.holdAttack()));
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleMouseInputDisabled() {
        update(c -> c.withMouseInputDisabled(!c.mouseInputDisabled()));
    }

    private void update(UnaryOperator<LockInputsConfig> consumer) {
        LockInputs.INSTANCE.getSettings().update(consumer);
    }

    private LockInputsConfig getConfig() {
        return LockInputs.INSTANCE.getSettings().get();
    }
}