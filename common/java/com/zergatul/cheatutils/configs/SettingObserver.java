package com.zergatul.cheatutils.configs;

@FunctionalInterface
public interface SettingObserver<T extends ImmutableConfig> {
    void notify(SettingNotificationReason reason, T previous, T current);
}