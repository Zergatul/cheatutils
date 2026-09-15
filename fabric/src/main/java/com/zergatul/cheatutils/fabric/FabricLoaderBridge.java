package com.zergatul.cheatutils.fabric;

import com.zergatul.cheatutils.common.LoaderBridge;
import com.zergatul.cheatutils.common.LoaderEnvironment;
import com.zergatul.cheatutils.common.LoaderRenderingWorkarounds;

public class FabricLoaderBridge implements LoaderBridge {

    public static final LoaderBridge INSTANCE = new FabricLoaderBridge();

    private FabricLoaderBridge() {}

    @Override
    public LoaderEnvironment getEnvironment() {
        return FabricLoaderEnvironment.INSTANCE;
    }

    @Override
    public LoaderRenderingWorkarounds getRenderingWorkarounds() {
        return FabricLoaderRenderingWorkarounds.INSTANCE;
    }
}