package com.zergatul.cheatutils.modules.esp.entity;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.modules.EntityEspEntry;
import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.scripting.ScriptActivation;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.events.EntityEspConsumer;
import com.zergatul.cheatutils.scripting.modules.EntityEspEvent;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class EntityEspScriptRuntime {

    public static final EntityEspScriptRuntime INSTANCE = new EntityEspScriptRuntime();

    private final Map<Class<?>, ScriptActivation<EntityEspConsumer>> scripts = new IdentityHashMap<>();
    private final Map<EntityScriptResultKey, EntityScriptResult> scriptResults = new HashMap<>();

    private EntityEspScriptRuntime() {}

    public void clearScripts() {
        ClientTickEndExecutor.instance.execute(() -> {
            scripts.values().forEach(ScriptActivation::deactivate);
            scripts.clear();
            scriptResults.clear();
        });
    }

    public void setScript(Class<?> type, @Nullable EntityEspConsumer script) {
        // Have to run from the main thread, since EntityEsp module doesn't snapshot scripts
        // and if update happens mid-frame it can cause NullReference exception.
        ClientTickEndExecutor.instance.execute(() -> {
            ScriptActivation<EntityEspConsumer> previous = scripts.remove(type);
            if (previous != null) {
                previous.deactivate();
            }
            if (script != null) {
                scripts.put(type, new ScriptActivation<>(new ScriptRef(ScriptType.ENTITY_ESP, type.getName()), script));
            }
            scriptResults.keySet().removeIf(key -> key.type() == type);
        });
    }

    public void clearScriptResults() {
        scriptResults.clear();
    }

    public boolean getBooleanFromScript(EntityEspEntry entry, Entity entity, Predicate<EntityScriptResult> predicate) {
        ScriptActivation<EntityEspConsumer> script = scripts.get(entry.type());
        if (!entry.scriptEnabled() || script == null || !script.isActive()) {
            return false;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), entry.type()));
        if (result != null) {
            return predicate.test(result);
        }

        return predicate.test(executeScript(entry, entity, script));
    }

    public Integer getTracerColorOverride(EntityEspEntry entry, Entity entity) {
        ScriptActivation<EntityEspConsumer> script = scripts.get(entry.type());
        if (!entry.scriptEnabled() || script == null || !script.isActive()) {
            return null;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), entry.type()));
        if (result != null) {
            return result.tracerColorOverride;
        } else {
            return executeScript(entry, entity, script).tracerColorOverride;
        }
    }

    public StylizedText getTitleOverride(EntityEspEntry entry, Entity entity) {
        ScriptActivation<EntityEspConsumer> script = scripts.get(entry.type());
        if (!entry.scriptEnabled() || script == null || !script.isActive()) {
            return null;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), entry.type()));
        if (result != null) {
            return result.title;
        }

        return executeScript(entry, entity, script).title;
    }

    private EntityScriptResult executeScript(EntityEspEntry entry, Entity entity, ScriptActivation<EntityEspConsumer> script) {
        EntityScriptResult result = new EntityScriptResult(entity.getId());
        scriptResults.put(new EntityScriptResultKey(entity.getId(), entry.type()), result);
        if (!script.run("entity rendering", () -> script.program.accept(entity.getId(), new EntityEspEvent(result)))) {
            scriptResults.keySet().removeIf(key -> key.type() == entry.type());
            return new EntityScriptResult(entity.getId());
        }
        return result;
    }
}