package com.zergatul.cheatutils.configs;

public class CoreConfig implements Sanitizable {
    public int port = 5005;
    public boolean advancedScripting;

    @Override
    public void sanitize() {
        port = Math.max(1, Math.min(65535, port));
    }
}
