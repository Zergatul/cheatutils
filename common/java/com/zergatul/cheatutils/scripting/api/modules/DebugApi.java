package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.modules.scripting.Debugging;
import com.zergatul.scripting.MethodDescription;

public class DebugApi {

    @MethodDescription("Writes a message to the scripting debugger.")
    public void write(String message) {
        Debugging.instance.addMessage(message);
    }
}