package com.zergatul.cheatutils.configs;

public record SettingUpdateResult<TConfig, TResult>(TConfig config, TResult result) {}