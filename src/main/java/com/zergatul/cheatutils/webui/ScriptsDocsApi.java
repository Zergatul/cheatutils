package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.api.HelpText;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ScriptsDocsApi extends ApiBase {

    @Override
    public String getRoute() {
        return "scripts-doc";
    }

    @Override
    public String get(String id) throws HttpException {
        List<String> refs = new ArrayList<>();
        Class<?> clazz = null;
        switch (id) {
            case "keys":
                clazz = com.zergatul.cheatutils.scripting.api.keys.Root.class;
                break;
            case "overlay":
                clazz = com.zergatul.cheatutils.scripting.api.overlay.Root.class;
                break;
            default:
                return null;
        }
        Field[] fields = clazz.getDeclaredFields();
        String space = "&nbsp;";
        Arrays.stream(fields).sorted(Comparator.comparing(Field::getName)).forEach(f -> {
            Method[] methods = f.getType().getDeclaredMethods();
            Arrays.stream(methods).filter(m -> Modifier.isPublic(m.getModifiers())).sorted(Comparator.comparing(Method::getName)).forEach(m -> {
                Parameter[] parameters = m.getParameters();
                String paramsStr = Arrays.stream(parameters).map(p -> formatClass(p.getType()) + space + p.getName()).reduce((s1, s2) -> s1 + "," + space + s2).orElse("");
                String returnStr = "";
                if (m.getReturnType() != void.class) {
                    returnStr = space + "→" + space + formatClass(m.getReturnType());
                }
                String comment = "";
                if (m.isAnnotationPresent(HelpText.class)) {
                    HelpText helpText = m.getAnnotation(HelpText.class);
                    if (helpText.value() != null && helpText.value().length() > 0) {
                        comment = formatComment("/* " + helpText.value() + " */");
                    }
                }
                refs.add(f.getName() + ".<span class=\"method\">" + m.getName() + "</span>(" + paramsStr + ")" + returnStr + comment);
            });
        });

        return gson.toJson(refs);
    }

    private String formatClass(Class clazz) {
        String name = clazz == String.class ? "String" : (clazz == double.class ? "float" : clazz.getName());
        return "<span class=\"class\">" + name + "</span>";
    }

    private String formatComment(String text) {
        return "<span class=\"comment\">&nbsp;" + StringEscapeUtils.escapeHtml4(text) + "</span>";
    }
}