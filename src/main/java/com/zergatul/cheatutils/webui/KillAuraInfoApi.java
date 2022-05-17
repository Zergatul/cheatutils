package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.KillAuraConfig;
import org.apache.http.HttpException;

public class KillAuraInfoApi extends ApiBase {

    @Override
    public String getRoute() {
        return "kill-aura-info";
    }

    @Override
    public String get() throws HttpException {
        return gson.toJson(KillAuraConfig.PriorityEntry.entries.values().stream().map(Entry::new).toArray());
    }

    public static class Entry {
        public String name;
        public String description;
        public Entry(KillAuraConfig.PriorityEntry entry) {
            name = entry.name;
            description = entry.description;
        }
    }
}
