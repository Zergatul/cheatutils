package com.zergatul.cheatutils.scripting.structure;

import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.compiler.CompilationResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ScriptRegistry {

    private final Map<ScriptType, ScriptStorageDescriptor> descriptors = new Object2ObjectArrayMap<>();

    private ScriptRegistry() {
        descriptors.put(ScriptType.OVERLAY, new StatusOverlayDescriptor());
    }

    public ScriptStorageDescriptor get(ScriptType type) {
        return descriptors.get(type);
    }

    private static class StatusOverlayDescriptor extends ScriptStorageDescriptor {

        private final ScriptInstance instance = new ScriptInstance();

        @Override
        public List<ScriptInstance> getInstances() {
            return List.of(instance);
        }

        @Override
        public String getCode(@Nullable String identifier) {
            if (identifier != null) {
                throw new IllegalStateException();
            }

            return instance.code;
        }

        @Override
        public String getLastAttemptedCode(@Nullable String identifier) {
            if (identifier != null) {
                throw new IllegalStateException();
            }

            return instance.lastAttemptCode;
        }

        @Override
        public CompilationResult compile(String code) {
            return ScriptsController.instance.compileOverlay(code);
        }

        @Override
        public Object save() {
            return null;
        }
    }
}