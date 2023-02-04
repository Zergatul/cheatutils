package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.GameTickScriptingConfig;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;

public class GameTickScriptingController {

    public static final GameTickScriptingController instance = new GameTickScriptingController();

    private Runnable script;

    private GameTickScriptingController() {
        ModApiWrapper.HandleKeyBindings.add(this::onHandleKeyBindings);
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