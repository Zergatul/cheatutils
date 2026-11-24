package com.zergatul.cheatutils.fabric;

import com.zergatul.cheatutils.common.LoaderBridge;
import com.zergatul.cheatutils.common.LoaderEnvironment;
import com.zergatul.cheatutils.common.LoaderInputsWorkarounds;
import com.zergatul.cheatutils.common.LoaderRenderingWorkarounds;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FabricLoaderBridge implements LoaderBridge {

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

    @Override
    public @Nullable LoaderInputsWorkarounds getInputsWorkarounds() {
        return null;
    }
}