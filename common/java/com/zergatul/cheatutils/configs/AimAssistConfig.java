package com.zergatul.cheatutils.configs;

public class AimAssistConfig implements ModuleStateProvider {

    public static final String AIM_ASSIST_CENTER = "AIM_ASSIST_CENTER";
    public static final String AIM_ASSIST_HEAD = "AIM_ASSIST_HEAD";

    public boolean bowAssist;
    public String aimAssistMode;

    public AimAssistConfig() {
        this.aimAssistMode = AIM_ASSIST_CENTER;
    }

    @Override
    public boolean isEnabled() {
        return bowAssist;
    }
}