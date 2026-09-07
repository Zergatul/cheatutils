package com.zergatul.cheatutils.modules;

import com.zergatul.cheatutils.modules.esp.BlockEsp;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.modules.esp.blocks.BlockEventsProcessor;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import org.apache.logging.log4j.LogManager;

public class Modules {

    public static void register() {
        register(BlockEventsProcessor.instance);

        register(BlockEsp.INSTANCE);
        register(EntityEsp.INSTANCE);
        register(FreeCam.INSTANCE);
        register(KeyBindings.INSTANCE);
    }

    private static void register(Object instance) {
        LogManager.getLogger(Modules.class).trace("Registered module {}", instance.getClass().getName());
    }
}