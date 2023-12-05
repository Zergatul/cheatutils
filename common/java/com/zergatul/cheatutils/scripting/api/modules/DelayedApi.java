package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.modules.utilities.DelayedRun;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.scripting.api.HelpText;

public class DelayedApi {

    @HelpText("Runs action after some amount of ticks passed")
    @ApiVisibility(ApiType.ACTION)
    public void run(int ticks, Runnable action) {
        if (ticks <= 0) {
            return;
        }

        DelayedRun.instance.add(ticks, action);
    }
}