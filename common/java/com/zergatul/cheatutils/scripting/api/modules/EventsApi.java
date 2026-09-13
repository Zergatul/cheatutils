package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.modules.hacks.AimAssist;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class EventsApi {

    public final ModuleEventsApi modules = new ModuleEventsApi();

    @MethodDescription("""
            Triggers every tick, unless you interact with some UI, like chat or crafting table.
            Best place to work with keys, for example to bind Zoom key.
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onHandleKeys(Runnable action) {
        EventsScripting.instance.addOnHandleKeys(action);
    }

    @MethodDescription("""
            Runs at the end of each client tick while the player is in a world.
            Not called in the main menu.
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onTickEnd(Runnable action) {
        EventsScripting.instance.addOnTickEnd(action);
    }

    @MethodDescription("""
            Runs at the end of each client tick while no world is loaded
            (main menu, disconnect screen, server list).
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onMenuTickEnd(Runnable action) {
        EventsScripting.instance.addOnMenuTickEnd(action);
    }

    public static class ModuleEventsApi {
        public final AimAssistEventsApi aimAssist = new AimAssistEventsApi();
    }

    public static class AimAssistEventsApi {

        @MethodDescription("""
                Triggers when Aim Assist module searches for the target.
                You can filter for specific entity type, class or other attributes.
                Use game.entities API to work with supplied entity id.
                Return true to allow the entity as a target candidate.
                Return false to reject it. Multiple filters are combined with AND.
                Example:
                events.modules.aimAssist.addTargetFilter(entityId => game.entities.getType(entityId) == "minecraft:cow");
                """)
        @ApiVisibility(ApiType.EVENTS)
        public void addTargetFilter(AimAssist.TargetPredicate predicate) {
            EventsScripting.instance.addAimAssistTargetPredicate(predicate);
        }
    }
}