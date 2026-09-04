package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class CoreConfig implements Sanitizable {

    public int port;

    public CoreConfig() {
        port = 5005;
    }

    @Override
    public void sanitize() {
        port = MathUtils.clamp(port, 1, 65535);
    }
}