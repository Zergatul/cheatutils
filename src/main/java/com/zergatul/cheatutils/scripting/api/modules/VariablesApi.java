package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

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

    public String getString(String name) {
        Object value = variables.get(name);
        if (value instanceof String) {
            return (String) value;
        } else {
            return "";
        }
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setBoolean(String name, boolean value) {
        variables.put(name, value);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setInteger(String name, int value) {
        variables.put(name, value);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void setString(String name, String value) {
        if (value != null && value.length() > 1000000) {
            // prevent stupid scripts that can occupy all RAM
            value = value.substring(0, 1000000);
        }
        variables.put(name, value);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void newList(String vname, int length) {
        List<Object> list = new ArrayList<Object>(length);
        variables.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void addString(String vname, String value) {
        if (value != null && value.length() > 1000000) {
            // prevent stupid scripts that can occupy all RAM
            value = value.substring(0, 1000000);
        }
        List<Object> list = variables.get(vname);
        list.add(value);
        variables.put(vname, list);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void addInteger(String vname, int value) {
        List<Object> list = variables.get(vname);
        list.add(value);
        variables.put(vname, list);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void addBoolean(String vname, boolean value) {
        List<Object> list = variables.get(vname);
        list.add(value);
        variables.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void getBooleanI(String vname, int i) {
        List<Object> list = variables.get(vname);
        boolean value = list.indexOf(i);
        if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return false;
        }
    }
    @ApiVisibility(ApiType.UPDATE)
    public void getStringI(String vname, int i) {
        List<Object> list = variables.get(vname);
        String value = list.indexOf(i);
        if (value instanceof String) {
            return (String) value;
        } else {
            return "";
        }
    }
    @ApiVisibility(ApiType.UPDATE)
    public void getIntegerI(String vname, int i) {
        List<Object> list = variables.get(vname);
        int value = list.indexOf(i);
        if (value instanceof Integer) {
            return (int) value;
        } else {
            return 0;
        }
    }
    
}
