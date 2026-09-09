package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.cheatutils.scripting.ScriptSlot;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.utility.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScriptWorkspace {

    public static final ScriptWorkspace INSTANCE = new ScriptWorkspace();

    private final Map<ScriptType, ScriptSlot> slots = new Object2ObjectArrayMap<>();

    private ScriptWorkspace() {
        slots.put(ScriptType.KEYBINDING, new KeyBindingScriptSlot());
    }

    public ScriptSlot get(ScriptType type) {
        return Objects.requireNonNull(slots.get(type));
    }

    public List<ScriptDocument> getAllInstances() {
        return Lists.from(slots.values().stream().flatMap(descriptor -> descriptor.getInstances().stream()));
    }

    public List<ScriptType> getSupportedTypes() {
        return Lists.from(slots.keySet().stream());
    }
}