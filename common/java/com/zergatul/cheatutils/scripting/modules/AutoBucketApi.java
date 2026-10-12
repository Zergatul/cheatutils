package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoBucketConfig;
import com.zergatul.cheatutils.modules.automation.AutoBucket;

public class AutoBucketApi extends SettingHandleApi<AutoBucketConfig> {

    public AutoBucketApi() {
        super(AutoBucket.instance.getSettings());
    }
}