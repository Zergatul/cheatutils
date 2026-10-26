package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AutoBucketConfig;
import com.zergatul.cheatutils.modules.automation.AutoBucket;

public class AutoBucketApi extends SettingHandle2Api<AutoBucketConfig> {
    public AutoBucketApi() {
        super(AutoBucket.INSTANCE.getSettings());
    }
}