package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class ServerPluginsConfig implements Sanitizable {

    public int waitTicks;
    public boolean autoPrint;

    public ServerPluginsConfig() {
        waitTicks = 20;
    }

    @Override
    public void sanitize() {
        waitTicks = MathUtils.clamp(waitTicks, 0, 60 * 20);
    }
}