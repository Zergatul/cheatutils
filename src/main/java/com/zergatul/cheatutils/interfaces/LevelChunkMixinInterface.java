package com.zergatul.cheatutils.interfaces;

import com.zergatul.cheatutils.utils.Dimension;

public interface LevelChunkMixinInterface {
    Dimension getDimension();
    void onLoad();
}