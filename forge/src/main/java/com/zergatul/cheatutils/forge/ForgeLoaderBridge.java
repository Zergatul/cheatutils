package com.zergatul.cheatutils.forge;

import com.zergatul.cheatutils.common.LoaderBridge;
import com.zergatul.cheatutils.common.LoaderEnvironment;
import com.zergatul.cheatutils.common.LoaderInputsWorkarounds;
import com.zergatul.cheatutils.common.LoaderRenderingWorkarounds;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ForgeLoaderBridge implements LoaderBridge {

    public static final LoaderBridge INSTANCE = new ForgeLoaderBridge();

    private ForgeLoaderBridge() {}

    @Override
    public LoaderEnvironment getEnvironment() {
        return ForgeLoaderEnvironment.INSTANCE;
    }

    @Override
    public LoaderRenderingWorkarounds getRenderingWorkarounds() {
        return ForgeRenderingWorkarounds.INSTANCE;
    }

    @Override
    public LoaderInputsWorkarounds getInputsWorkarounds() {
        return ForgeInputsWorkarounds.INSTANCE;
    }
}