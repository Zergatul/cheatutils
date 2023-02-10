package com.zergatul.cheatutils.scripting.api;

import java.util.HashMap;
import java.util.Map;

public class Variables {

    public static final Variables instance = new Variables();

    private final Map<String, Object> variables = new HashMap<>();

    private Variables() {

    }

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
        if (value instanceof Boolean) {
            return (int) value;
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

    public void setString(String name, String value) {
        variables.put(name, value);
    }
}