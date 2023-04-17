package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.GameTickScriptingConfig;

public class GameTickScriptingController {

    public static final GameTickScriptingController instance = new GameTickScriptingController();

    private Runnable script;

    private GameTickScriptingController() {
        Events.BeforeHandleKeyBindings.add(this::onHandleKeyBindings);
    }

    public void setScript(Runnable script) {
        this.script = script;
    }

    private void onHandleKeyBindings() {
        GameTickScriptingConfig config = ConfigStore.instance.getConfig().gameTickScriptingConfig;
        if (config.enabled && script != null) {
            script.run();
        }
    }
}