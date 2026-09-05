package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.concurrent.InGameTickEndExecutor;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.ScriptActivation;
import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;
import com.zergatul.scripting.MethodDescription;

public class DelayedApi {

    @MethodDescription("""
            Runs actions after some amount of ticks passed.
            Action will not run if you disconnect from the world.
            """)
    @ApiVisibility(ApiType.ACTION)
    public void run(int ticks, Runnable action) {
        if (ticks <= 0) {
            return;
        }

        ScriptActivation<?> owner = ScriptActivation.findOwner(action);
        InGameTickEndExecutor.instance.waitTicks(ticks).thenRun(() -> {
            if (owner == null) {
                action.run();
            } else {
                owner.run("delayed callback", action);
            }
        }).whenComplete((_, throwable) -> ScriptRuntimeFailureHandler.instance.report("Delayed script callback failed.", throwable));
    }
}