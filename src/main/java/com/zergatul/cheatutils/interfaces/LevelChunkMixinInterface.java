package com.zergatul.cheatutils.interfaces;

import com.zergatul.cheatutils.utils.Dimension;

public interface LevelChunkMixinInterface {
    long getLoadTime();
    Dimension getDimension();
    boolean isUnloaded();
    void onLoad();
    void onUnload();
}