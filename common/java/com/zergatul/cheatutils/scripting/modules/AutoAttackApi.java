package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.configs.AutoAttackConfig;
import com.zergatul.cheatutils.modules.automation.AutoAttack;

public class AutoAttackApi extends SettingHandleApi<AutoAttackConfig> {

    public AutoAttackApi() {
        super(AutoAttack.instance.getSettings());
    }
}
