package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.scripting.api.HelpText;
import net.minecraft.world.entity.Entity;

public class CurrentPlayerApi {

    @HelpText("Use from onPlayerAdded/onPlayerRemoved events.")
    @ApiVisibility(ApiType.EVENTS)
    public String getName() {
        Entity entity = EventsScripting.instance.getCurrentEntity();
        if (entity != null) {
            return entity.getName().getString();
        } else {
            return "";
        }
    }
}