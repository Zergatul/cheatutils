package com.zergatul.cheatutils.controllers;

public class HoldUseKeyController {

    public static final HoldUseKeyController instance = new HoldUseKeyController();

    private boolean active = false;

    private HoldUseKeyController() {

    }

    /*@SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (ConfigStore.instance.holdUseKey) {
            active = true;
            KeyBinding keyCode = Minecraft.getInstance().options.keyUse.getKeyBinding();
            if (!keyCode.isDown()) {
                keyCode.setDown(true);
            }
        } else {
            if (active) {
                active = false;
                KeyBinding keyCode = Minecraft.getInstance().options.keyUse.getKeyBinding();
                if (keyCode.isDown()) {
                    keyCode.setDown(false);
                }
            }
        }
    }*/
}
