package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.chunkoverlays.ExplorationMiniMapChunkOverlay;
import com.zergatul.cheatutils.chunkoverlays.NewChunksOverlay;
import net.minecraft.client.Minecraft;
import org.slf4j.helpers.MessageFormatter;

import java.text.DecimalFormat;
import java.util.List;

public class DebugScreenController {

    public static final DebugScreenController instance = new DebugScreenController();

    private final Minecraft mc = Minecraft.getInstance();
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

        ExplorationMiniMapChunkOverlay miniMapChunkOverlay = ChunkOverlayController.instance.ofType(ExplorationMiniMapChunkOverlay.class);
        list.add(String.format("ExplMiniMap scan thread: queue size=%s; state=%s;",
                miniMapChunkOverlay.getScanningQueueCount(),
                miniMapChunkOverlay.getThreadState()));

        NewChunksOverlay newChunksOverlay = ChunkOverlayController.instance.ofType(NewChunksOverlay.class);
        list.add(String.format("NewChunks scan thread: queue size=%s; state=%s;",
                newChunksOverlay.getScanningQueueCount(),
                newChunksOverlay.getThreadState()));

        FreeCamController.instance.onDebugScreenGetGameInformation(list);
    }

    public void onGetSystemInformation(List<String> list) {
        FreeCamController.instance.onDebugScreenGetSystemInformation(list);
    }
}