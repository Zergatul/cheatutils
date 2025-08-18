package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.hover.HoverProvider;
import com.zergatul.scripting.hover.Theme;
import com.zergatul.scripting.type.SClassType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class CustomHoverProvider extends HoverProvider {

    private final List<String> packages = List.of(
            "com.zergatul.cheatutils.scripting.modules",
            "com.zergatul.cheatutils.scripting.events");

    public CustomHoverProvider(Theme theme) {
        super(theme);
    }

    @Override
    protected String type(SType type) {
        if (type instanceof SClassType) {
            Class<?> clazz = type.getJavaClass();
            if (packages.stream().anyMatch(pkg -> clazz.getName().startsWith(pkg))) {
                return span(theme.getTypeColor(), clazz.getSimpleName());
            } else {
                return span(theme.getTypeColor(), clazz.getName());
            }
        }
        return super.type(type);
    }
}
