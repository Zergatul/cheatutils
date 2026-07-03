package com.zergatul.cheatutils.scripting.services;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.services.descriptors.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@NullMarked
public class ScriptWorkspaceService {

    public static final ScriptWorkspaceService INSTANCE = new ScriptWorkspaceService();

    private final Map<ScriptType, ScriptStorageDescriptor> descriptors = new Object2ObjectArrayMap<>();

    private ScriptWorkspaceService() {
        descriptors.put(ScriptType.OVERLAY, new StatusOverlayDescriptor());
        descriptors.put(ScriptType.BLOCK_AUTOMATION, new BlockAutomationDescriptor());
        descriptors.put(ScriptType.VILLAGER_ROLLER, new VillagerRollerDescriptor());
        descriptors.put(ScriptType.EVENTS, new EventsScriptingDescriptor());
        descriptors.put(ScriptType.KILL_AURA, new KillAuraDescriptor());
        descriptors.put(ScriptType.HITBOX_SIZE, new HitboxSizeDescriptor());
    }

    public ScriptStorageDescriptor get(ScriptType type) {
        return Objects.requireNonNull(descriptors.get(type));
    }

    public List<ScriptInstance> getAllInstances() {
        return descriptors.values().stream().flatMap(descriptor -> descriptor.getInstances().stream()).toList();
    }

    public List<ScriptType> getSupportedTypes() {
        return List.copyOf(descriptors.keySet());
    }
}
