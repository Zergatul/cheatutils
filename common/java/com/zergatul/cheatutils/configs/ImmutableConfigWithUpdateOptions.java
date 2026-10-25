package com.zergatul.cheatutils.configs;

public interface ImmutableConfigWithUpdateOptions<O> extends ImmutableConfig {
    ImmutableConfigWithUpdateOptions<O> withOptions(O options);
}