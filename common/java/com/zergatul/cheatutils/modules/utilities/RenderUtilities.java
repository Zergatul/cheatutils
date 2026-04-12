package com.zergatul.cheatutils.modules.utilities;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.render.*;
import com.zergatul.cheatutils.render.Texture2dRenderer;

public class RenderUtilities {

    public static final RenderUtilities instance = new RenderUtilities();

    private final Texture2dRenderer texture2dRenderer = new Texture2dRenderer();
    private final Color3dRenderer color3dRenderer = new Color3dRenderer();

    private RenderUtilities() {
        Events.WindowResize.add(this::onWindowResize);
    }

    public Texture2dRenderer getTexture2dRenderer() { return texture2dRenderer; }

    public Color3dRenderer getColor3dRenderer() { return color3dRenderer; }

    private void onWindowResize() {
        FrameBuffers.onResize();
    }
}