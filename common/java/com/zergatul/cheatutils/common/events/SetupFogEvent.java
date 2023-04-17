package com.zergatul.cheatutils.common.events;

public class SetupFogEvent {

    private float start;
    private float end;

    public SetupFogEvent(float start, float end) {
        this.start = start;
        this.end = end;
    }

    public float getFogStart() {
        return start;
    }

    public float getFogEnd() {
        return end;
    }

    public void setFogStart(float value) {
        start = value;
    }

    public void setFogEnd(float value) {
        end = value;
    }
}