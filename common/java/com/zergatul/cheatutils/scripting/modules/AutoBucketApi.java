package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoBucketConfig;
import com.zergatul.cheatutils.configs.ConfigStore;

public class AutoBucketApi extends ModuleApi<AutoBucketConfig> {

    @Override
    protected AutoBucketConfig getConfig() {
        return ConfigStore.instance.getConfig().autoBucketConfig;
    }
}