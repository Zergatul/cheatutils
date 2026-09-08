package com.zergatul.cheatutils.configs;

public class Config implements Sanitizable {
    public CoreConfig coreConfig = new CoreConfig();
    public FreeCamConfig freeCamConfig = new FreeCamConfig();

    @Override
    public void sanitize() {
        if (coreConfig == null) {
            coreConfig = new CoreConfig();
        }
        coreConfig.sanitize();
        if (freeCamConfig == null) {
            freeCamConfig = new FreeCamConfig();
        }
        freeCamConfig.sanitize();
    }
}
