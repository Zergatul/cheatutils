package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PlayerTurnByMouseEvent;
import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.LockInputsConfig;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;

public class LockInputs {

    public static final LockInputs INSTANCE = new LockInputs();

    private final SettingHandle2<LockInputsConfig> settings = Settings.get(LockInputsConfig.class);
    private final Minecraft mc = Minecraft.getInstance();
    private boolean lastHoldForward;
    private boolean lastHoldAttack;
    private boolean lastHoldUse;

    private LockInputs() {
        Events.InGameTickStart.add(this::onClientTickStart);
        Events.PlayerTurnByMouse.add(this::onPlayerTurnByMouse);

        WebApiRegistry.INSTANCE.register("lock-inputs", settings, LockInputsConfig.class);
    }

    public SettingHandle2<LockInputsConfig> getSettings() {
        return settings;
    }

    private void onClientTickStart() {
        if (mc.player == null) {
            return;
        }

        LockInputsConfig config = settings.get();

        if (config.holdForward()) {
            mc.options.keyUp.setDown(true);
        } else {
            if (lastHoldForward) {
                mc.options.keyUp.setDown(false);
            }
        }

        if (config.holdAttack()) {
            mc.options.keyAttack.setDown(true);
        } else {
            if (lastHoldAttack) {
                mc.options.keyAttack.setDown(false);
            }
        }

        if (config.holdUse()) {
            mc.options.keyUse.setDown(true);
        } else {
            if (lastHoldUse) {
                mc.options.keyUse.setDown(false);
            }
        }

        lastHoldForward = config.holdForward();
        lastHoldAttack = config.holdAttack();
        lastHoldUse = config.holdUse();
    }

    private void onPlayerTurnByMouse(PlayerTurnByMouseEvent event) {
        if (settings.get().mouseInputDisabled()) {
            event.cancel();
        }
    }
}