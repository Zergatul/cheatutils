package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.HelpText;
import net.minecraft.network.Connection;

public class CurrentServerApi {

    @HelpText("Use from onJoinServer event.")
    @ApiVisibility(ApiType.EVENTS)
    public String getAddress() {
        Connection connection = EventsScripting.instance.getCurrentConnection();
        return connection == null ? "" : connection.getRemoteAddress().toString();
    }
}