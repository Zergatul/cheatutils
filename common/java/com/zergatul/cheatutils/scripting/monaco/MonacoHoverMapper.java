package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.analysis.hover.HoverInfo;
import com.zergatul.scripting.analysis.hover.HoverMapper;

import java.util.List;

public class MonacoHoverMapper implements HoverMapper<List<String>> {

    @Override
    public List<String> map(HoverInfo hover) {
        String signature = "```\n" + hover.signature() + "\n```";
        if (hover.documentation() == null) {
            return List.of(signature);
        }
        return List.of(signature, hover.documentation().replace("\n", "<br>"));
    }
}