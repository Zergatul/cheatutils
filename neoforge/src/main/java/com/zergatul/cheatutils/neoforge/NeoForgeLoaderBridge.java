package com.zergatul.cheatutils.neoforge;

import com.zergatul.cheatutils.common.LoaderBridge;
import com.zergatul.cheatutils.common.LoaderEnvironment;
import com.zergatul.cheatutils.common.LoaderInputsWorkarounds;
import com.zergatul.cheatutils.common.LoaderRenderingWorkarounds;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class NeoForgeLoaderBridge implements LoaderBridge {

    public static final LoaderBridge INSTANCE = new NeoForgeLoaderBridge();

    private NeoForgeLoaderBridge() {}

    @Override
    public LoaderEnvironment getEnvironment() {
        return NeoForgeLoaderEnvironment.INSTANCE;
    }

    @Override
    public LoaderRenderingWorkarounds getRenderingWorkarounds() {
        return NeoForgeRenderingWorkarounds.INSTANCE;
    }

    @Override
    public @Nullable LoaderInputsWorkarounds getInputsWorkarounds() {
        return null;
    }
}