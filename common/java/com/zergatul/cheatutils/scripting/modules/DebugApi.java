package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.scripting.Debugging;

public class DebugApi {
    public void write(String message) {
        Debugging.instance.addMessage(message);
    }
}