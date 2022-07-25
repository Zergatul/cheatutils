package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.api.Root;
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
    public String get() throws HttpException {
        List<String> refs = new ArrayList<>();
        Field[] fields = Root.class.getDeclaredFields();
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
                refs.add(f.getName() + ".<span class=\"method\">" + m.getName() + "</span>(" + paramsStr + ")" + returnStr);
            });
        });

        return gson.toJson(refs);
    }

    private String formatClass(Class clazz) {
        String name = clazz == String.class ? "String" : clazz.getName();
        return "<span class=\"class\">" + name + "</span>";
    }
}