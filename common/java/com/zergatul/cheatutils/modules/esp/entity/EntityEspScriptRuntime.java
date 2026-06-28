package com.zergatul.cheatutils.modules.esp.entity;

import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.EntityEspConfig;
import com.zergatul.cheatutils.font.StylizedText;
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

    private final Map<EntityEspConfig, EntityEspConsumer> scripts = new IdentityHashMap<>();
    private final Map<EntityScriptResultKey, EntityScriptResult> scriptResults = new HashMap<>();

    private EntityEspScriptRuntime() {}

    public void clearScripts() {
        TickEndExecutor.instance.execute(() -> {
            scripts.clear();
            scriptResults.clear();
        });
    }

    public void setScript(EntityEspConfig config, @Nullable EntityEspConsumer script) {
        // Have to run from the main thread, since EntityEsp module doesn't snapshot scripts
        // and if update happens mid-frame it can cause NullReference exception.
        TickEndExecutor.instance.execute(() -> {
            if (script == null) {
                scripts.remove(config);
            } else {
                scripts.put(config, script);
            }
            scriptResults.keySet().removeIf(key -> key.config == config);
        });
    }

    public void clearScriptResults() {
        scriptResults.clear();
    }

    public boolean getBooleanFromScript(EntityEspConfig config, Entity entity, Predicate<EntityScriptResult> predicate) {
        EntityEspConsumer script = scripts.get(config);
        if (!config.scriptEnabled || script == null) {
            return false;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), config));
        if (result != null) {
            return predicate.test(result);
        }

        return predicate.test(executeScript(config, entity, script));
    }

    public Integer getTracerColorOverride(EntityEspConfig config, Entity entity) {
        EntityEspConsumer script = scripts.get(config);
        if (!config.scriptEnabled || script == null) {
            return null;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), config));
        if (result != null) {
            return result.tracerColorOverride;
        } else {
            return executeScript(config, entity, script).tracerColorOverride;
        }
    }

    public StylizedText getTitleOverride(EntityEspConfig config, Entity entity) {
        EntityEspConsumer script = scripts.get(config);
        if (!config.scriptEnabled || script == null) {
            return null;
        }

        EntityScriptResult result = scriptResults.get(new EntityScriptResultKey(entity.getId(), config));
        if (result != null) {
            return result.title;
        }

        return executeScript(config, entity, script).title;
    }

    private EntityScriptResult executeScript(EntityEspConfig config, Entity entity, EntityEspConsumer script) {
        EntityScriptResult result = new EntityScriptResult(entity.getId(), config);
        scriptResults.put(new EntityScriptResultKey(entity.getId(), config), result);
        script.accept(entity.getId(), new EntityEspEvent(result));
        return result;
    }
}