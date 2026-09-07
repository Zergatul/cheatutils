package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.analysis.hover.HoverInfo;
import com.zergatul.scripting.analysis.hover.HoverMapper;
import com.zergatul.scripting.utility.Lists;

import java.util.List;

public class MonacoHoverMapper implements HoverMapper<List<String>> {

    @Override
    public List<String> map(HoverInfo hover) {
        String signature = "```\n" + hover.signature() + "\n```";
        if (hover.documentation() == null) {
            return Lists.of(signature);
        }
        return Lists.of(signature, hover.documentation().replace("\n", "<br>"));
    }
}