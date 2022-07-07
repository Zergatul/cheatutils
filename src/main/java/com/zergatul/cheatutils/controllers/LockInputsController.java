package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LockInputsConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LockInputsController {

    public static final LockInputsController instance = new LockInputsController();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean lastHoldForward;
    private boolean lastHoldUse;

    private LockInputsController() {

    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            LockInputsConfig config = ConfigStore.instance.getConfig().lockInputsConfig;

            if (config.holdForward) {
                mc.options.keyUp.setDown(true);
            } else {
                if (lastHoldForward) {
                    mc.options.keyUp.setDown(false);
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
            lastHoldUse = config.holdUse;
        }
    }
}
