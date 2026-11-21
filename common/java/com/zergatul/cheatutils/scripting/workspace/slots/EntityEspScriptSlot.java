package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.modules.esp.entity.EntityEsp;
import com.zergatul.cheatutils.modules.esp.entity.EntityEspScriptRuntime;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.events.EntityEspConsumer;
import com.zergatul.cheatutils.utils.ClassUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public class EntityEspScriptSlot extends MultiScriptSlot {

    public EntityEspScriptSlot() {
        super(ScriptType.ENTITY_ESP);
    }

    @Override
    protected List<ConfigScript> readConfigScripts() {
        return EntityEsp.INSTANCE.getSettings().get().entries().stream()
                .map(e -> new ConfigScript(e.type().getName(), e.code()))
                .toList();
    }

    @Override
    protected boolean hasConfigEntry(String identifier) {
        return EntityEsp.INSTANCE.getSettings().get().entries().contains(e -> e.type().getName().equals(identifier));
    }

    @Override
    protected void updateConfigCode(String identifier, @Nullable String code) {
        EntityEsp.INSTANCE.getSettings().update(config -> config.withEntries(
                config.entries().replaceFirst(
                        e -> e.type().getName().equals(identifier),
                        e -> e.withCode(code))));
    }

    @Override
    protected <T> void applyScript(String identifier, @Nullable T program) {
        Class<?> type;
        try {
            type = ClassUtils.forName(identifier);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Invalid class name: " + identifier, e);
        }

        EntityEspScriptRuntime.INSTANCE.setScript(type, (EntityEspConsumer) program);
    }

    @Override
    protected void clearRuntime() {
        EntityEspScriptRuntime.INSTANCE.clearScripts();
    }
}
