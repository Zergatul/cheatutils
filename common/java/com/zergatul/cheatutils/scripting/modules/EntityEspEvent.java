package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class EntityEspEvent {

    public static final EntityEspEvent instance = new EntityEspEvent();

    private EntityEspEvent() {}

    @MethodDescription("""
            Disables tracer for current entity
            """)
    public void disableTracer() {
        EntityEsp.EntityScriptResult.current.tracerDisabled = true;
    }

    @MethodDescription("""
            Disables outline for current entity
            """)
    public void disableOutline() {
        EntityEsp.EntityScriptResult.current.outlineDisabled = true;
    }

    @MethodDescription("""
            Disables overlay for current entity
            """)
    public void disableOverlay() {
        EntityEsp.EntityScriptResult.current.overlayDisabled = true;
    }

    @MethodDescription("""
            Disables collision box for current entity
            """)
    public void disableCollisionBox() {
        EntityEsp.EntityScriptResult.current.collisionBoxDisabled = true;
    }

    @MethodDescription("""
            Overrides title displayed above the entity. Works only with cheatutils title system
            """)
    public void setTitle(String title) {
        EntityEsp.EntityScriptResult.current.title = title;
    }
}