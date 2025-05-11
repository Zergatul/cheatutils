package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.scripting.types.PlayerInfoWrapper;

@FunctionalInterface
public interface PlayerInfoUpdateConsumer {
    void accept(PlayerInfoWrapper info, String type);
}