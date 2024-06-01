package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderGuiEvent;

public class PreRenderGuiExecutor extends EventExecutor {

    public static final PreRenderGuiExecutor instance = new PreRenderGuiExecutor();

    private PreRenderGuiExecutor() {
        super(1000);
        Events.PreRenderGui.add(this::onPreRenderGui);
    }

    private void onPreRenderGui(RenderGuiEvent event) {
        processQueue();
    }
}