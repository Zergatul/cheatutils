package com.zergatul.cheatutils.configs;

@FunctionalInterface
public interface SettingObserver<T extends ImmutableConfig> {
    void notify(boolean isInitial, T previous, T current);
}