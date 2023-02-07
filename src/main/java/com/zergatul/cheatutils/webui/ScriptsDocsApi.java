package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.api.HelpText;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

public class ScriptsDocsApi extends ApiBase {

    @Override
    public String getRoute() {
        return "scripts-doc";
    }

    @Override
    public String get(String id) throws HttpException {
        Class<?> clazz;
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
        return gson.toJson(generateRootRefs(clazz));
    }

    private List<String> generateRootRefs(Class<?> clazz) {
        List<String> refs = new ArrayList<>();

        Field[] fields = clazz.getDeclaredFields();
        Arrays.stream(fields).sorted(Comparator.comparing(Field::getName)).forEach(field -> {
            getMethods(field.getType()).forEach(method -> {
                refs.add(generateHtml(field.getName(), method));
            });
            Arrays.stream(field.getType().getFields()).forEach(f -> {
                String prefix = field.getName() + "." + f.getName();
                refs.addAll(generateChildRefs(prefix, f.getType()));
            });
        });

        return refs;
    }

    private List<String> generateChildRefs(String prefix, Class<?> clazz) {
        List<String> refs = new ArrayList<>();

        getMethods(clazz).forEach(method -> {
            refs.add(generateHtml(prefix, method));
        });

        Field[] fields = clazz.getDeclaredFields();
        Arrays.stream(fields).filter(f -> Modifier.isPublic(f.getModifiers())).sorted(Comparator.comparing(Field::getName)).forEach(field -> {
            Arrays.stream(field.getType().getFields()).forEach(f -> {
                String prefixInner = prefix + "." + field.getName() + "." + f.getName();
                refs.addAll(generateChildRefs(prefixInner, f.getType()));
            });
        });

        return refs;
    }

    private Stream<Method> getMethods(Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        return Arrays.stream(methods).filter(m -> {
            if (m.getDeclaringClass() == Object.class) {
                return false; // skip Object methods
            }
            return Modifier.isPublic(m.getModifiers());
        }).sorted(Comparator.comparing(Method::getName));
    }

    private String generateHtml(String prefix, Method method) {
        final String space = "&nbsp;";
        Parameter[] parameters = method.getParameters();
        String paramsStr = Arrays.stream(parameters).map(p -> formatClass(p.getType()) + space + p.getName()).reduce((s1, s2) -> s1 + "," + space + s2).orElse("");
        String returnStr = "";
        if (method.getReturnType() != void.class) {
            returnStr = space + "→" + space + formatClass(method.getReturnType());
        }

        String comment = "";
        if (method.isAnnotationPresent(HelpText.class)) {
            HelpText helpText = method.getAnnotation(HelpText.class);
            if (helpText.value() != null && helpText.value().length() > 0) {
                comment = formatComment("/* " + helpText.value() + " */");
            }
        }

        return prefix + ".<span class=\"method\">" + method.getName() + "</span>(" + paramsStr + ")" + returnStr + comment;
    }

    private String formatClass(Class<?> clazz) {
        String name = clazz == String.class ? "String" : (clazz == double.class ? "float" : clazz.getName());
        return "<span class=\"class\">" + name + "</span>";
    }

    private String formatComment(String text) {
        return "<span class=\"comment\">&nbsp;" + StringEscapeUtils.escapeHtml4(text) + "</span>";
    }
}