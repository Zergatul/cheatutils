package com.zergatul.cheatutils.configs.modules;

import com.zergatul.cheatutils.configs.ImmutableConfig;
import com.zergatul.cheatutils.configs.ModuleStateProvider;

public record PrivacyConfig(
        boolean hideFromModVersions,
        boolean disconnectOnTranslationExploit,
        boolean logTranslationExploitDetails,
        boolean disconnectOnMaliciousResourcePackBehavior,
        boolean alwaysPromptOnResourcePack,
        boolean displayResourcePackUrls,
        boolean logResourcePackDetails
) implements ImmutableConfig, ModuleStateProvider {

    public static PrivacyConfig defaults() {
        return new Builder()
                .hideFromModVersions(true)
                .disconnectOnTranslationExploit(true)
                .logTranslationExploitDetails(false)
                .disconnectOnMaliciousResourcePackBehavior(true)
                .alwaysPromptOnResourcePack(true)
                .displayResourcePackUrls(true)
                .logResourcePackDetails(false)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return hideFromModVersions || disconnectOnTranslationExploit || disconnectOnMaliciousResourcePackBehavior || alwaysPromptOnResourcePack || displayResourcePackUrls;
    }

    // <generated-code>

    public PrivacyConfig withHideFromModVersions(boolean hideFromModVersions) {
        return Builder.from(this).hideFromModVersions(hideFromModVersions).build();
    }

    public PrivacyConfig withDisconnectOnTranslationExploit(boolean disconnectOnTranslationExploit) {
        return Builder.from(this).disconnectOnTranslationExploit(disconnectOnTranslationExploit).build();
    }

    public PrivacyConfig withLogTranslationExploitDetails(boolean logTranslationExploitDetails) {
        return Builder.from(this).logTranslationExploitDetails(logTranslationExploitDetails).build();
    }

    public PrivacyConfig withDisconnectOnMaliciousResourcePackBehavior(boolean disconnectOnMaliciousResourcePackBehavior) {
        return Builder.from(this).disconnectOnMaliciousResourcePackBehavior(disconnectOnMaliciousResourcePackBehavior).build();
    }

    public PrivacyConfig withAlwaysPromptOnResourcePack(boolean alwaysPromptOnResourcePack) {
        return Builder.from(this).alwaysPromptOnResourcePack(alwaysPromptOnResourcePack).build();
    }

    public PrivacyConfig withDisplayResourcePackUrls(boolean displayResourcePackUrls) {
        return Builder.from(this).displayResourcePackUrls(displayResourcePackUrls).build();
    }

    public PrivacyConfig withLogResourcePackDetails(boolean logResourcePackDetails) {
        return Builder.from(this).logResourcePackDetails(logResourcePackDetails).build();
    }

    public static class Builder {

        private boolean hideFromModVersions;
        private boolean disconnectOnTranslationExploit;
        private boolean logTranslationExploitDetails;
        private boolean disconnectOnMaliciousResourcePackBehavior;
        private boolean alwaysPromptOnResourcePack;
        private boolean displayResourcePackUrls;
        private boolean logResourcePackDetails;

        private Builder() {}

        public static Builder from(PrivacyConfig config) {
            return new Builder()
                    .hideFromModVersions(config.hideFromModVersions)
                    .disconnectOnTranslationExploit(config.disconnectOnTranslationExploit)
                    .logTranslationExploitDetails(config.logTranslationExploitDetails)
                    .disconnectOnMaliciousResourcePackBehavior(config.disconnectOnMaliciousResourcePackBehavior)
                    .alwaysPromptOnResourcePack(config.alwaysPromptOnResourcePack)
                    .displayResourcePackUrls(config.displayResourcePackUrls)
                    .logResourcePackDetails(config.logResourcePackDetails);
        }

        public Builder hideFromModVersions(boolean hideFromModVersions) {
            this.hideFromModVersions = hideFromModVersions;
            return this;
        }

        public Builder disconnectOnTranslationExploit(boolean disconnectOnTranslationExploit) {
            this.disconnectOnTranslationExploit = disconnectOnTranslationExploit;
            return this;
        }

        public Builder logTranslationExploitDetails(boolean logTranslationExploitDetails) {
            this.logTranslationExploitDetails = logTranslationExploitDetails;
            return this;
        }

        public Builder disconnectOnMaliciousResourcePackBehavior(boolean disconnectOnMaliciousResourcePackBehavior) {
            this.disconnectOnMaliciousResourcePackBehavior = disconnectOnMaliciousResourcePackBehavior;
            return this;
        }

        public Builder alwaysPromptOnResourcePack(boolean alwaysPromptOnResourcePack) {
            this.alwaysPromptOnResourcePack = alwaysPromptOnResourcePack;
            return this;
        }

        public Builder displayResourcePackUrls(boolean displayResourcePackUrls) {
            this.displayResourcePackUrls = displayResourcePackUrls;
            return this;
        }

        public Builder logResourcePackDetails(boolean logResourcePackDetails) {
            this.logResourcePackDetails = logResourcePackDetails;
            return this;
        }

        public PrivacyConfig build() {
            return new PrivacyConfig(hideFromModVersions, disconnectOnTranslationExploit, logTranslationExploitDetails, disconnectOnMaliciousResourcePackBehavior, alwaysPromptOnResourcePack, displayResourcePackUrls, logResourcePackDetails);
        }
    }

    // </generated-code>
}