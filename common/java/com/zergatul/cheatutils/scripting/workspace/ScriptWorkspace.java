package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.slots.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@NullMarked
public class ScriptWorkspace {

    public static final ScriptWorkspace INSTANCE = new ScriptWorkspace();

    private final Map<ScriptType, ScriptSlot> slots = new Object2ObjectArrayMap<>();

    private ScriptWorkspace() {
        slots.put(ScriptType.KEYBINDING, new KeyBindingScriptSlot());
        slots.put(ScriptType.OVERLAY, new StatusOverlayScriptSlot());
        slots.put(ScriptType.BLOCK_AUTOMATION, new BlockAutomationScriptSlot());
        slots.put(ScriptType.VILLAGER_ROLLER, new VillagerRollerScriptSlot());
        slots.put(ScriptType.EVENTS, new EventsScriptingScriptSlot());
        slots.put(ScriptType.BLOCK_ESP, new BlockEspScriptSlot());
        slots.put(ScriptType.KILL_AURA, new KillAuraScriptSlot());
        slots.put(ScriptType.HITBOX_SIZE, new HitboxSizeScriptSlot());
    }

    public ScriptSlot get(ScriptType type) {
        return Objects.requireNonNull(slots.get(type));
    }

    public List<ScriptDocument> getAllInstances() {
        return slots.values().stream().flatMap(descriptor -> descriptor.getInstances().stream()).toList();
    }

    public List<ScriptType> getSupportedTypes() {
        return List.copyOf(slots.keySet());
    }
}