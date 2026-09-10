package com.zergatul.cheatutils.modules;

import com.zergatul.cheatutils.modules.esp.BlockEsp;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import org.apache.logging.log4j.LogManager;

public class Modules {

    public static void register() {
        register(BlockEsp.INSTANCE);
        register(FreeCam.INSTANCE);
        register(KeyBindings.INSTANCE);
    }

    private static void register(Module module) {
        LogManager.getLogger(Modules.class).debug("Registered module {}", module.getClass().getName());
    }
}