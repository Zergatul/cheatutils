package com.zergatul.cheatutils.controllers;

import net.minecraft.client.MinecraftClient;

import java.text.DecimalFormat;
import java.util.List;

public class DebugScreenController {

    public static final DebugScreenController instance = new DebugScreenController();

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final DecimalFormat format = new DecimalFormat("0.00");

    private DebugScreenController() {

    }

    public void onGetGameInformation(List<String> list) {
        list.add("");
        list.add("Zergatul Cheat Utils");
        list.add("Loaded chunks: " + ChunkController.instance.getLoadedChunksCount());
        list.add(String.format("BlockFinder scan thread: queue size=%s; load=%s; state=%s;",
                BlockFinderController.instance.getScanningQueueCount(),
                format.format(BlockFinderController.instance.getScanningThreadLoadPercent()) + "%",
                BlockFinderController.instance.getThreadState()));
        //list.add("Horizontal speed: " + format.format(SpeedCounterController.instance.getSpeed()));

        FreeCamController.instance.onDebugScreenGetGameInformation(list);
    }

    public void onGetSystemInformation(List<String> list) {
        FreeCamController.instance.onDebugScreenGetSystemInformation(list);
    }
}