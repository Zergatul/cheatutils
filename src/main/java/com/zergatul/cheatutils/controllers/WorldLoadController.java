package com.zergatul.cheatutils.controllers;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldLoadController {

    public static final WorldLoadController instance = new WorldLoadController();

    private final Logger logger = LogManager.getLogger(WorldLoadController.class);

    //private Script script;

    private WorldLoadController() {

    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        /*if (script == null) {
            script = new Scaffold();
            script.start();
        }*/
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        //script.stop();
    }

}
