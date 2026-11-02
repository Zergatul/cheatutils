package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.modules.EntityEspEntry;
import com.zergatul.cheatutils.modules.esp.entity.EntityEsp;
import com.zergatul.cheatutils.modules.esp.entity.EntityEspScriptRuntime;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.events.EntityEspConsumer;
import com.zergatul.cheatutils.utils.ClassUtils;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class EntityEspScriptSlot extends MultiScriptSlot {

    public EntityEspScriptSlot() {
        super(ScriptType.ENTITY_ESP);
    }

    @Override
    public void clear() {
        EntityEspScriptRuntime.INSTANCE.clearScripts();
        clearDocuments();
    }

    @Override
    protected void updateConfigCode(String identifier, @Nullable String code) {
        EntityEsp.INSTANCE.getSettings().update(config -> {
            Class<?> type;
            try {
                type = ClassUtils.forName(identifier);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Invalid class name: " + identifier, e);
            }

            ImmutableList<EntityEspEntry> entries = config.entries();
            entries = entries.replaceFirst(
                    e -> e.type() == type,
                    e -> e.withCode(code));
            return config.withEntries(entries);
        });
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptCompilerRegistry.INSTANCE.compile(ScriptType.ENTITY_ESP, code);
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
}