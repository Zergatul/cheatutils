package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class AutoFishConfig extends ModuleConfig implements Sanitizable {

    public boolean autoRestartOnIdle;
    public int idleTimeout;

    public AutoFishConfig() {
        idleTimeout = 60;
    }

    @Override
    public void sanitize() {
        idleTimeout = MathUtils.clamp(idleTimeout, 5, 3600);
    }
}