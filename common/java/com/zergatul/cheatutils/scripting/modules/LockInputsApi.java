package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.utilities.LockInputs;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.configs.LockInputsConfig;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class LockInputsApi {

    public boolean isHoldForwardEnabled() {
        return getConfig().holdForward;
    }

    public boolean isHoldUseEnabled() {
        return getConfig().holdUse;
    }

    public boolean isHoldAttackEnabled() {
        return getConfig().holdAttack;
    }

    public boolean isMouseInputDisabled() {
        return getConfig().mouseInputDisabled;
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleHoldForward() {
        update(c -> c.holdForward = !c.holdForward);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleHoldUse() {
        update(c -> c.holdUse = !c.holdUse);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleHoldAttack() {
        update(c -> c.holdAttack = !c.holdAttack);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void toggleMouseInputDisabled() {
        update(c -> c.mouseInputDisabled = !c.mouseInputDisabled);
    }

    private void update(Consumer<LockInputsConfig> consumer) {
        LockInputs.instance.getSettings().update(consumer);
    }

    private LockInputsConfig getConfig() {
        return LockInputs.instance.getSettings().get();
    }
}