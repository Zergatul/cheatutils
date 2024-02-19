package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.wrappers.CurrentEntity;

public class CurrentEntityEspApi {

    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public int getId() {
        return CurrentEntity.id;
    }

    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableTracer() {
        CurrentEntity.entityEspResult.tracerDisabled = true;
    }

    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableOutline() {
        CurrentEntity.entityEspResult.outlineDisabled = true;
    }

    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableOverlay() {
        CurrentEntity.entityEspResult.overlayDisabled = true;
    }

    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableCollisionBox() {
        CurrentEntity.entityEspResult.collisionBoxDisabled = true;
    }

    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void setTitle(String title) {
        CurrentEntity.entityEspResult.title = title;
    }
}