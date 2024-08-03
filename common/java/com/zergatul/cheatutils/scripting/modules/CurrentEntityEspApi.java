package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.MethodDescription;

@SuppressWarnings("unused")
public class CurrentEntityEspApi {

    @MethodDescription("""
            Disables tracer for current entity
            """)
    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableTracer() {
        EntityEsp.EntityScriptResult.current.tracerDisabled = true;
    }

    @MethodDescription("""
            Disables outline for current entity
            """)
    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableOutline() {
        EntityEsp.EntityScriptResult.current.outlineDisabled = true;
    }

    @MethodDescription("""
            Disables overlay for current entity
            """)
    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableOverlay() {
        EntityEsp.EntityScriptResult.current.overlayDisabled = true;
    }

    @MethodDescription("""
            Disables collision box for current entity
            """)
    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void disableCollisionBox() {
        EntityEsp.EntityScriptResult.current.collisionBoxDisabled = true;
    }

    @MethodDescription("""
            Overrides title displayed above the entity. Works only with cheatutils title system
            """)
    @ApiVisibility(ApiType.CURRENT_ENTITY_ESP)
    public void setTitle(String title) {
        EntityEsp.EntityScriptResult.current.title = title;
    }
}