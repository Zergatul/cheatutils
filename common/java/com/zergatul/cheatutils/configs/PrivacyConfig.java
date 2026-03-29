package com.zergatul.cheatutils.configs;

public class PrivacyConfig implements ModuleStateProvider {

    public boolean hideFromModVersions;
    public boolean disconnectOnTranslationExploit;
    public boolean logTranslationExploitDetails;
    public boolean disconnectOnMaliciousResourcePackBehavior;
    public boolean alwaysPromptOnResourcePack;
    public boolean displayResourcePackUrls;
    public boolean logResourcePackDetails;

    public PrivacyConfig() {
        hideFromModVersions = true;
        disconnectOnTranslationExploit = true;
        logTranslationExploitDetails = false;
        disconnectOnMaliciousResourcePackBehavior = true;
        alwaysPromptOnResourcePack = true;
        displayResourcePackUrls = true;
        logResourcePackDetails = false;
    }

    @Override
    public boolean isEnabled() {
        return hideFromModVersions || disconnectOnTranslationExploit || disconnectOnMaliciousResourcePackBehavior || alwaysPromptOnResourcePack || displayResourcePackUrls;
    }
}