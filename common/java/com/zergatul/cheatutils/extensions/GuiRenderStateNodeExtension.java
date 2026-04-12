package com.zergatul.cheatutils.extensions;

import com.zergatul.cheatutils.render.CustomTextRenderState;
import com.zergatul.cheatutils.render.GuiCustomTextRenderState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface GuiRenderStateNodeExtension {
    void addCustomText_CU(GuiCustomTextRenderState state);
    void addCustomText_CU(CustomTextRenderState state);
    @Nullable List<GuiCustomTextRenderState> getCustomTextStates_CU();
}