package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.HelpText;
import net.minecraft.network.chat.Component;

public class CurrentChatMessageApi {

    @HelpText("Use from onChatMessage event.")
    @ApiVisibility(ApiType.EVENTS)
    public String get() {
        Component message = EventsScripting.instance.getCurrentChatMessage();
        return message == null ? "" : message.getString();
    }
}