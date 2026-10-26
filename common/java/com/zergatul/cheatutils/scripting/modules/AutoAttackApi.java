package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.modules.AutoAttackConfig;
import com.zergatul.cheatutils.modules.automation.AutoAttack;

public class AutoAttackApi extends SettingHandle2Api<AutoAttackConfig> {
    public AutoAttackApi() {
        super(AutoAttack.INSTANCE.getSettings());
    }
}