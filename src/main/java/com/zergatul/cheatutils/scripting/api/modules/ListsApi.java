package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ListsApi {
    private final Map<String, List<Object>> lists = new HashMap<>();
    private final Map<String, Object> maxSizes = new HashMap<>();
    private boolean checkUseable(String vname) {
        List<Object> list = lists.get(vname);
        return list.size() != (int) maxSizes.get(vname);
    }
    private boolean compareIndex(List<Object> list, int i) {
        return i > -1 && (list.size()-1) > i;
    }

    public boolean exists(String vname) {
        return lists.containsKey(vname);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void newList(String vname, int length) {
        List<Object> list = new ArrayList<Object>(length);
        maxSizes.put(vname, length);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void pushString(String vname, String value) {
        if (!exists(vname)) return;
        if (value != null && value.length() > 1000000) {
            // prevent stupid scripts that can occupy all RAM
            value = value.substring(0, 1000000);
        }
        List<Object> list = lists.get(vname);
        if (!checkUseable(vname)) return;
        list.add(value);
        lists.put(vname, list);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void pushInteger(String vname, int value) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        if (!checkUseable(vname)) return;
        list.add(value);
        lists.put(vname, list);
    }

    @ApiVisibility(ApiType.UPDATE)
    public void pushBoolean(String vname, boolean value) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        if (!checkUseable(vname)) return;
        list.add(value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void removeI(String vname, int i) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        if (!compareIndex(list, i)) return;
        list.remove(i);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void shift(String vname) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        list.remove(0);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void unshiftString(String vname, String value) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        list.add(0, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void unshiftInteger(String vname, int value) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        list.add(0, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void unshiftBoolean(String vname, boolean value) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        list.add(0, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public boolean popBoolean(String vname) {
        if (!exists(vname)) return false;
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
        if (!exists(vname)) return 0;
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
        if (!exists(vname)) return "";
        List<Object> list = lists.get(vname);
        Object value = list.remove(list.size());
        lists.put(vname, list);
        if (value instanceof String) {
            return (String) value;
        } else {
            return "";
        }
    }
    @ApiVisibility(ApiType.UPDATE)
    public void setBoolean(String vname, int i, boolean value) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        if (!compareIndex(list, i)) return;
        list.set(i, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void setInteger(String vname, int i, int value) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        if (!compareIndex(list, i)) return;
        list.set(i, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void setString(String vname, int i, String value) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        if (!compareIndex(list, i)) return;
        list.set(i, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void insertBoolean(String vname, int i, boolean value) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        if (!compareIndex(list, i) || !checkUseable(vname)) return;
        list.add(i, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void insertInteger(String vname, int i, int value) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        if (!compareIndex(list, i) || !checkUseable(vname)) return;
        list.add(i, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public void insertString(String vname, int i, String value) {
        if (!exists(vname)) return;
        List<Object> list = lists.get(vname);
        if (!compareIndex(list, i) || !checkUseable(vname)) return;
        list.add(i, value);
        lists.put(vname, list);
    }
    @ApiVisibility(ApiType.UPDATE)
    public boolean removeBoolean(String vname, int i) {
        if (!exists(vname)) return false;
        List<Object> list = lists.get(vname);
        Object value = list.remove(i);
        lists.put(vname, list);
        if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return false;
        }
    }
    @ApiVisibility(ApiType.UPDATE)
    public int removeInteger(String vname, int i) {
        if (!exists(vname)) return 0;
        List<Object> list = lists.get(vname);
        Object value = list.remove(i);
        lists.put(vname, list);
        if (value instanceof Integer) {
            return (int) value;
        } else {
            return 0;
        }
    }
    @ApiVisibility(ApiType.UPDATE)
    public String removeString(String vname, int i) {
        if (!exists(vname)) return "";
        List<Object> list = lists.get(vname);
        Object value = list.remove(i);
        lists.put(vname, list);
        if (value instanceof String) {
            return (String) value;
        } else {
            return "";
        }
    }
    public boolean getBoolean(String vname, int i) {
        if (!exists(vname)) return false;
        List<Object> list = lists.get(vname);
        if (!compareIndex(list, i)) return false;
        Object value = list.get(i);
        if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return false;
        }
    }
    public String getString(String vname, int i) {
        if (!exists(vname)) return "";
        List<Object> list = lists.get(vname);
        if (!compareIndex(list, i)) return "";
        Object value = list.get(i);
        if (value instanceof String) {
            return (String) value;
        } else {
            return "";
        }
    }
    public int getInteger(String vname, int i) {
        if (!exists(vname)) return 0;
        List<Object> list = lists.get(vname);
        if (!compareIndex(list, i)) return 0;
        Object value = list.get(i);
        if (value instanceof Integer) {
            return (int) value;
        } else {
            return 0;
        }
    }
    public int length(String vname) {
        if (!exists(vname)) return 0;
        List<Object> list = lists.get(vname);
        return list.size();
    }
    public int maxCapacity(String vname) {
        if (!exists(vname)) return 0;
        List<Object> list = lists.get(vname);
        return (int) maxSizes.get(vname);
    }
    public int lastIndex(String vname) {
        if (!exists(vname)) return 0;
        List<Object> list = lists.get(vname);
        return list.size()-1;
    }
    public String toString(Object item) {
        if (item instanceof Float || item instanceof Integer) {
            return item.toString();
        } else {
            return "";
        }
    }
}
