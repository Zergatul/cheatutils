package com.zergatul.cheatutils.scripting.api.modules;

import java.util.HashMap;
import java.util.Map;

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
        if (value != null && value.length() > 1000000) {
            // prevent stupid scripts that can occupy all RAM
            value = value.substring(0, 1000000);
        }
        variables.put(name, value);
    }
}