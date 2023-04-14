package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

import java.util.HashMap;
import java.util.Map;	
import java.util.ArrayList;
import java.util.List;


public class VariablesApi {

    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, List<Object>> lists = new HashMap<>();

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
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void pushString(String vname, String value) {
        if (value != null && value.length() > 1000000) {
            // prevent stupid scripts that can occupy all RAM
            value = value.substring(0, 1000000);
        }
        List<Object> list = lists.get(vname);
        list.add(value);
        lists.put(vname, list);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void pushInteger(String vname, int value) {
        List<Object> list = lists.get(vname);
        list.add(value);
        lists.put(vname, list);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void pushBoolean(String vname, boolean value) {
        List<Object> list = lists.get(vname);
        list.add(value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void removeI(String vname, int i) {
        List<Object> list = lists.get(vname);
        list.remove(i);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void shift(String vname) {
        List<Object> list = lists.get(vname);
        list.remove(0);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void unshiftString(String vname, String value) {
        List<Object> list = lists.get(vname);
        list.add(0, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void unshiftInteger(String vname, int value) {
        List<Object> list = lists.get(vname);
        list.add(0, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void unshiftBoolean(String vname, boolean value) {
        List<Object> list = lists.get(vname);
        list.add(0, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public boolean popBoolean(String vname) {
        List<Object> list = lists.get(vname);
        Object value = list.remove(list.size());
        lists.put(vname, list);
        if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return false;
        }
    }
    @ApiVisibility(ApiType.UPDATE)
    public int popInteger(String vname) {
        List<Object> list = lists.get(vname);
        Object value = list.remove(list.size());
        lists.put(vname, list);
        if (value instanceof Integer) {
            return (int) value;
        } else {
            return 0;
        }
    }
    @ApiVisibility(ApiType.UPDATE)
    public String popString(String vname) {
        List<Object> list = lists.get(vname);
        Object value = list.remove(list.size());
        lists.put(vname, list);
        if (value instanceof String) {
            return (String) value;
        } else {
            return "";
        }
    }
    public boolean getBooleanI(String vname, int i) {
        List<Object> list = lists.get(vname);
        Object value = list.get(i);
        if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return false;
        }
    }
    public String getStringI(String vname, int i) {
        List<Object> list = lists.get(vname);
        Object value = list.get(i);
        if (value instanceof String) {
            return (String) value;
        } else {
            return "";
        }
    }
    public int getIntegerI(String vname, int i) {
        List<Object> list = lists.get(vname);
        Object value = list.get(i);
        if (value instanceof Integer) {
            return (int) value;
        } else {
            return 0;
        }
    }
    
}
