package com.zergatul.cheatutils.modules.esp.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.SettingNotificationReason;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.EntityEspConfig;
import com.zergatul.cheatutils.configs.modules.EntityEspEntry;
import com.zergatul.cheatutils.font.StylizedText;
import com.zergatul.cheatutils.scripting.ScriptActivation;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.events.EntityEspConsumer;
import com.zergatul.cheatutils.scripting.modules.EntityEspEvent;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.EntityEspScriptSlot;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Predicate;

public class EntityEspScriptRuntime {

    public static final EntityEspScriptRuntime INSTANCE = new EntityEspScriptRuntime();

    private final EntityEspScriptSlot scriptSlot = (EntityEspScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.ENTITY_ESP);
    private final Map<Class<?>, ScriptActivation<EntityEspConsumer>> scripts = new IdentityHashMap<>();
    private final Map<EntityScriptResultKey, EntityScriptResult> scriptResults = new HashMap<>();

    private EntityEspScriptRuntime() {
        Settings.get(EntityEspConfig.class).observe(this::onConfigChanged);
    }

    public void clearScripts() {
        synchronized (this) {
            scripts.values().forEach(ScriptActivation::deactivate);
            scripts.clear();
            scriptResults.clear();
        }
    }

    public void setScript(Class<?> type, @Nullable EntityEspConsumer script) {
        synchronized (this) {
            ScriptActivation<EntityEspConsumer> previous = scripts.remove(type);
            if (previous != null) {
                previous.deactivate();
            }
            if (script != null) {
                scripts.put(type, new ScriptActivation<>(new ScriptRef(ScriptType.ENTITY_ESP, type.getName()), script));
            }
            scriptResults.keySet().removeIf(key -> key.type() == type);
        }
    }

    public void clearScriptResults() {
        synchronized (this) {
            scriptResults.clear();
        }
    }

    /** Caller should synchronize */
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

    /** Caller should synchronize */
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

    /** Caller should synchronize */
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

    private void onConfigChanged(SettingNotificationReason reason, EntityEspConfig prev, EntityEspConfig curr) {
        if (reason == SettingNotificationReason.INITIAL || reason == SettingNotificationReason.PROFILE_LOAD) {
            synchronized (this) {
                scriptSlot.clear();

                for (EntityEspEntry entry : curr.entries()) {
                    if (entry.code() != null) {
                        scriptSlot.initAndReport(entry.type().getName(), entry.code());
                    }
                }
            }
        }
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