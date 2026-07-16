package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.analysis.hover.HoverProvider;
import com.zergatul.scripting.type.SClassType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class CustomHoverProvider extends HoverProvider {

    private final List<String> packages = List.of(
            "com.zergatul.cheatutils.scripting.modules",
            "com.zergatul.cheatutils.scripting.events");

    @Override
    protected String formatType(SType type) {
        if (type instanceof SClassType) {
            Class<?> clazz = type.getJavaClass();
            if (packages.stream().anyMatch(pkg -> clazz.getName().startsWith(pkg))) {
                return formatType(clazz.getSimpleName());
            } else {
                return formatType(clazz.getName());
            }
        }
        return super.formatType(type);
    }

    @Override
    protected String formatPredefinedType(String text) {
        return bold(text);
    }

    @Override
    protected String formatType(String text) {
        return bold(text);
    }

    @Override
    protected String formatDescription(String text) {
        return italic(text);
    }

    private String bold(String text) {
        return text.isBlank() ? text : "**" + text + "**";
    }

    private String italic(String text) {
        return text.isBlank() ? text : "*" + text + "*";
    }
}