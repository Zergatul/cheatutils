package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LockInputsConfig;
import net.minecraft.client.Minecraft;

public class LockInputsController {

    public static final LockInputsController instance = new LockInputsController();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean lastHoldForward;
    private boolean lastHoldAttack;
    private boolean lastHoldUse;

    private LockInputsController() {
        Events.ClientTickStart.add(this::onClientTickStart);
    }

    private void onClientTickStart() {
        LockInputsConfig config = ConfigStore.instance.getConfig().lockInputsConfig;

        if (config.holdForward) {
            mc.options.keyUp.setDown(true);
        } else {
            if (lastHoldForward) {
                mc.options.keyUp.setDown(false);
            }
        }

        if (config.holdAttack) {
            mc.options.keyAttack.setDown(true);
        } else {
            if (lastHoldAttack) {
                mc.options.keyAttack.setDown(false);
            }
        }

        if (config.holdUse) {
            mc.options.keyUse.setDown(true);
        } else {
            if (lastHoldUse) {
                mc.options.keyUse.setDown(false);
            }
        }

        lastHoldForward = config.holdForward;
        lastHoldAttack = config.holdAttack;
        lastHoldUse = config.holdUse;
    }
}
