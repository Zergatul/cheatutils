package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.HoldKeyConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HoldKeyController {

    public static final HoldKeyController instance = new HoldKeyController();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean lastHoldUp;
    private boolean lastHoldUse;

    private HoldKeyController() {

    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            HoldKeyConfig config = ConfigStore.instance.getConfig().holdKeyConfig;
            if (!config.enabled) {
                lastHoldUp = false;
                lastHoldUse = false;
                return;
            }

            if (config.holdUp) {
                mc.options.keyUp.setDown(true);
            } else {
                if (lastHoldUp) {
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

            lastHoldUp = config.holdUp;
            lastHoldUse = config.holdUse;
        }
    }
}
