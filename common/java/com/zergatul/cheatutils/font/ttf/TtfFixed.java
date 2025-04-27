package com.zergatul.cheatutils.font.ttf;

public class TtfFixed {

    private final int raw;

    public TtfFixed(int raw) {
        this.raw = raw;
    }

    public double asDouble() {
        return 1d * raw / 65536;
    }
}