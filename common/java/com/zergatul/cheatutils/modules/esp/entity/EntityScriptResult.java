package com.zergatul.cheatutils.modules.esp.entity;

import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.font.StylizedText;

public class EntityScriptResult {

    public final int id;
    public final EntityEspConfig config;
    public boolean tracerDisabled;
    public boolean outlineDisabled;
    public boolean overlayDisabled;
    public boolean collisionBoxDisabled;
    public StylizedText title;
    public Integer tracerColorOverride;

    public EntityScriptResult(int id, EntityEspConfig config) {
        this.id = id;
        this.config = config;
    }
}