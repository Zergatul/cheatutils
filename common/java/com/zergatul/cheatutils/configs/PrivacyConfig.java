package com.zergatul.cheatutils.configs;

public class PrivacyConfig implements ModuleStateProvider {

    public boolean hideFromModVersions;
    public boolean disconnectOnTranslationExploit;

    public PrivacyConfig() {
        hideFromModVersions = true;
        disconnectOnTranslationExploit = true;
    }

    @Override
    public boolean isEnabled() {
        return hideFromModVersions || disconnectOnTranslationExploit;
    }
}