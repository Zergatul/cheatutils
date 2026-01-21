package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.esp.BlockEsp;
import com.zergatul.scripting.type.CustomType;

@SuppressWarnings("unused")
@CustomType(name = "BlockEspEvent")
public class BlockEspEvent {

    private final BlockEsp.BlockScriptResult result;

    public BlockEspEvent(BlockEsp.BlockScriptResult result) {
        this.result = result;
    }

    public void setTracerStatus(boolean status) {
        result.tracer = status ? 1 : 0;
    }

    public void setOutlineStatus(boolean status) {
        result.outline = status ? 1 : 0;
    }

    public void setOverlayStatus(boolean status) {
        result.overlay = status ? 1 : 0;
    }
}