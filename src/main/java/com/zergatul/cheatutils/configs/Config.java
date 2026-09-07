package com.zergatul.cheatutils.configs;

public class Config implements Sanitizable {
    public CoreConfig coreConfig = new CoreConfig();

    @Override
    public void sanitize() {
        if (coreConfig == null) {
            coreConfig = new CoreConfig();
        }
        coreConfig.sanitize();
    }
}
