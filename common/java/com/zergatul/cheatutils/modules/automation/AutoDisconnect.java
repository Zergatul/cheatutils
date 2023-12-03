package com.zergatul.cheatutils.modules.automation;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;

public class AutoDisconnect implements Module {

    public static final AutoDisconnect instance = new AutoDisconnect();

    private Runnable script;

    private AutoDisconnect() {
        Events.ClientTickEnd.add(this::onClientTickEnd);
    }

    public void setScript(Runnable script) {
        this.script = script;
    }

    private void onClientTickEnd() {
        if (!ConfigStore.instance.getConfig().autoDisconnectConfig.enabled) {
            return;
        }
        if (script != null) {
            script.run();
        }
    }
}