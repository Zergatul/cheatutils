package com.zergatul.cheatutils.extensions;

import com.zergatul.cheatutils.render.CustomTextRenderState;
import com.zergatul.cheatutils.render.GuiCustomTextRenderState;

import java.util.function.Consumer;

public interface GuiRenderStateExtension {
    void addCustomText_CU(GuiCustomTextRenderState state);
    void addCustomTextToCurrentLayer_CU(CustomTextRenderState state);
    void forEachCustomText_CU(Consumer<GuiCustomTextRenderState> consumer);
}