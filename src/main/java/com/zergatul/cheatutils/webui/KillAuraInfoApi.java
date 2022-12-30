package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KillAuraConfig;
import org.apache.http.HttpException;

import java.util.ArrayList;
import java.util.List;

public class KillAuraInfoApi extends ApiBase {

    @Override
    public String getRoute() {
        return "kill-aura-info";
    }

    @Override
    public String get() throws HttpException {
        List<KillAuraConfig.PriorityEntry> entries = new ArrayList<>(KillAuraConfig.PredefinedPriorityEntry.entries.values());
        for (KillAuraConfig.PriorityEntry entry: ConfigStore.instance.getConfig().killAuraConfig.customEntries) {
            entries.add(entry);
        }
        return gson.toJson(entries);
    }
}
