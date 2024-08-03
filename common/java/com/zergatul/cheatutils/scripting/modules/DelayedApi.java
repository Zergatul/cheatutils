package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.utilities.DelayedRun;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.HelpText;
import com.zergatul.cheatutils.scripting.MethodDescription;

public class DelayedApi {

    @MethodDescription("""
            Runs actions after some amount of ticks passed
            """)
    @ApiVisibility(ApiType.ACTION)
    public void run(int ticks, Runnable action) {
        if (ticks <= 0) {
            return;
        }

        DelayedRun.instance.add(ticks, action);
    }
}