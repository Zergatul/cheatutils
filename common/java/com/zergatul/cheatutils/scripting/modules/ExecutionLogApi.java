package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.HiddenMethod;

import java.util.ArrayList;
import java.util.List;

public class ExecutionLogApi {

    private final List<String> records = new ArrayList<>();

    @HiddenMethod
    public void clear() {
        records.clear();
    }

    @HiddenMethod
    public List<String> getRecords() {
        List<String> result = List.copyOf(records);
        records.clear();
        return result;
    }

    @ApiVisibility(ApiType.EXEC_LOGGING)
    public void write(String message) {
        records.add(message);
    }
}