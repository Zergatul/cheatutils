package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.utils.ClientTicks;

public abstract class FontBackend {

    protected long lastUsed;

    protected FontBackend() {
        markUse();
    }

    public abstract FontRenderer createFontRenderer(FontRenderDetails details);

    protected boolean isStale() {
        return (ClientTicks.get() - lastUsed) > 10 * 60 * 20; // 10 minutes
    }

    protected void markUse() {
        lastUsed = ClientTicks.get();
    }
}