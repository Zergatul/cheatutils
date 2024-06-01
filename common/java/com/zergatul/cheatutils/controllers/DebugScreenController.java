package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.concurrent.ProfilerSingleThreadExecutor;

import java.text.DecimalFormat;
import java.util.List;

public class DebugScreenController {

    public static final DebugScreenController instance = new DebugScreenController();

    private final DecimalFormat format = new DecimalFormat("0.00");

    private DebugScreenController() {

    }

    public void onGetGameInformation(List<String> list) {
        ProfilerSingleThreadExecutor executor = BlockEventsProcessor.instance.getExecutor();

        list.add("");
        list.add("Cheat Utils");
        list.add(String.format("BlockEvents thread: queue size=%s; successful=%d; failed=%d; rejected=%d; busy=%s;",
                executor.getQueueSize(),
                executor.getSuccessful(),
                executor.getFailed(),
                executor.getRejected(),
                format.format(executor.getBusyPercentage()) + "%"));

        FreeCam.instance.onRenderDebugScreenLeft(list);
    }

    public void onGetSystemInformation(List<String> list) {
        FreeCam.instance.onDebugScreenGetSystemInformation(list);
    }
}