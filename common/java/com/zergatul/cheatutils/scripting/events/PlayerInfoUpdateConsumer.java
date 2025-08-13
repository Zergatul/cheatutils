package com.zergatul.cheatutils.scripting.events;

import com.zergatul.cheatutils.scripting.types.PlayerInfoWrapper;

@FunctionalInterface
public interface PlayerInfoUpdateConsumer {
    void accept(PlayerInfoWrapper info, String type);
}