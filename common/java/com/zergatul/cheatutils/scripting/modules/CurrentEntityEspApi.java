package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

public class CurrentEntityEspApi {

    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableTracer() {
        EntityEsp.EntityScriptResult.current.tracerDisabled = true;
    }

    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableOutline() {
        EntityEsp.EntityScriptResult.current.outlineDisabled = true;
    }

    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableOverlay() {
        EntityEsp.EntityScriptResult.current.overlayDisabled = true;
    }

    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableCollisionBox() {
        EntityEsp.EntityScriptResult.current.collisionBoxDisabled = true;
    }

    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void setTitle(String title) {
        EntityEsp.EntityScriptResult.current.title = title;
    }
}