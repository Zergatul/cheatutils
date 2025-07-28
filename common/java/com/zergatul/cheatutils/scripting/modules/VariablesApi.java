package com.zergatul.cheatutils.scripting.modules;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
public class VariablesApi {

    private final Map<String, Object> variables = new HashMap<>();

    public boolean getBoolean(String name) {
        Object value = variables.get(name);
        if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return false;
        }
    }

    public int getInteger(String name) {
        Object value = variables.get(name);
        if (value instanceof Integer) {
            return (int) value;
        } else {
            return 0;
        }
    }

    public double getFloat(String name) {
        Object value = variables.get(name);
        if (value instanceof Double) {
            return (double) value;
        } else {
            return 0;
        }
    }

    public String getString(String name) {
        Object value = variables.get(name);
        if (value instanceof String) {
            return (String) value;
        } else {
            return "";
        }
    }

    public Object getObject(String name) {
        Object value = variables.get(name);
        return Objects.requireNonNullElseGet(value, Object::new);
    }

    public void setBoolean(String name, boolean value) {
        variables.put(name, value);
    }

    public void setInteger(String name, int value) {
        variables.put(name, value);
    }

    public void setFloat(String name, double value) {
        variables.put(name, value);
    }

    public void setString(String name, String value) {
        variables.put(name, value);
    }

    public void setObject(String name, Object value) {
        variables.put(name, value);
    }
}