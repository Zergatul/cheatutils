package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.hacks.BoatHack;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class BoatHackApi {

    @MethodDescription("""
            If Boat Fly enabled
            """)
    public boolean isFlyEnabled() {
        return BoatHack.INSTANCE.getSettings().get().fly;
    }

    @MethodDescription("""
            Toggles Boat Fly status
            """)
    @ApiVisibility(ApiType.UPDATE)
    public void toggleFly() {
        BoatHack.INSTANCE.getSettings().update(c -> c.fly = !c.fly);
    }
}