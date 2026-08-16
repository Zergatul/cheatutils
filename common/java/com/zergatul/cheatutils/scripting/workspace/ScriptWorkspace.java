package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.slots.BlockAutomationScriptSlot;
import com.zergatul.cheatutils.scripting.workspace.slots.EventsScriptingScriptSlot;
import com.zergatul.cheatutils.scripting.workspace.slots.KeyBindingScriptSlot;
import com.zergatul.cheatutils.scripting.workspace.slots.StatusOverlayScriptSlot;
import com.zergatul.cheatutils.scripting.workspace.slots.VillagerRollerScriptSlot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScriptWorkspace {

    public static final ScriptWorkspace INSTANCE = new ScriptWorkspace();

    private final Map<ScriptType, ScriptSlot> slots = new EnumMap<>(ScriptType.class);

    private ScriptWorkspace() {
        slots.put(ScriptType.KEYBINDING, new KeyBindingScriptSlot());
        slots.put(ScriptType.OVERLAY, new StatusOverlayScriptSlot());
        slots.put(ScriptType.BLOCK_AUTOMATION, new BlockAutomationScriptSlot());
        slots.put(ScriptType.VILLAGER_ROLLER, new VillagerRollerScriptSlot());
        slots.put(ScriptType.EVENTS, new EventsScriptingScriptSlot());
    }

    public ScriptSlot get(ScriptType type) {
        return Objects.requireNonNull(slots.get(type));
    }

    public List<ScriptDocument> getAllInstances() {
        List<ScriptDocument> documents = new ArrayList<>();
        for (ScriptSlot slot : slots.values()) {
            documents.addAll(slot.getInstances());
        }
        return List.copyOf(documents);
    }

    public List<ScriptType> getSupportedTypes() {
        return List.copyOf(slots.keySet());
    }
}