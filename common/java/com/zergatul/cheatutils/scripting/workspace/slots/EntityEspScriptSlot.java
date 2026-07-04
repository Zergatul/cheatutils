package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.modules.esp.EntityEsp;
import com.zergatul.cheatutils.scripting.ScriptType;
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
        EntityEsp.instance.clearScripts();
        clearDocuments();
    }

    @Override
    protected void updateConfigCode(String identifier, @Nullable String code) {
        EntityEspConfig config = findConfig(identifier);
        config.code = code;
    }

    @Override
    protected CompilationResult compileScript(String code) {
        return ScriptsController.instance.compileEntityEsp(code);
    }

    @Override
    protected <T> void applyScript(String identifier, @Nullable T program) {
        EntityEspConfig config = findConfig(identifier);
        EntityEsp.instance.setScript(config, (EntityEspConsumer) program);
    }

    private EntityEspConfig findConfig(String className) {
        Class<?> clazz;
        try {
            clazz = ClassUtils.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Invalid class name: " + className, e);
        }

        EntitiesConfig config = ConfigStore.instance.getConfig().entities;
        return config.configs.stream()
                .filter(c -> c.clazz == clazz)
                .findFirst()
                .orElseThrow();
    }
}