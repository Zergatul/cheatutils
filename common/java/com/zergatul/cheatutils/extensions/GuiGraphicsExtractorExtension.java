package com.zergatul.cheatutils.extensions;

import com.zergatul.cheatutils.font.FontRenderer;
import com.zergatul.cheatutils.font.StylizedText;

public interface GuiGraphicsExtractorExtension {
    void customText_CU(FontRenderer font, StylizedText text, int x, int y);
}