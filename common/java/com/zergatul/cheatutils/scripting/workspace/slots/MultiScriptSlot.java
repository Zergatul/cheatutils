package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.*;
import com.zergatul.scripting.analysis.AnalysisResult;
import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public abstract class MultiScriptSlot extends ScriptSlot {

    private final ScriptType scriptType;
    private final Map<String, ScriptDocument> instances;

    protected MultiScriptSlot(ScriptType scriptType) {
        this.scriptType = scriptType;
        this.instances = new LinkedHashMap<>();
    }

    @Override
    public List<ScriptDocument> getInstances() {
        return instances.values().stream().toList();
    }

    @Override
    public ScriptDocument getInstance(@Nullable String identifier) {
        if (identifier == null) {
            throw new IllegalStateException();
        }

        ScriptDocument instance = instances.get(identifier);
        if (instance == null) {
            throw new IllegalStateException();
        }

        return instance;
    }

    public void clear() {
        for (String identifier : new ArrayList<>(instances.keySet())) {
            remove(identifier);
        }
    }

    public void clearDocuments() {
        instances.clear();
    }

    public void remove(String identifier) {
        if (instances.remove(identifier) != null) {
            applyScript(identifier, null);
        }
    }

    @Override
    public ScriptSaveResult init(@Nullable String identifier, @Nullable String code) {
        if (identifier == null) {
            throw new IllegalStateException();
        }

        if (code == null || code.isEmpty()) {
            return ScriptSaveResult.success();
        }

        ScriptDocument instance = new ScriptDocument(new ScriptRef(scriptType, identifier));
        instances.put(identifier, instance);

        // initial code loaded from config may contain errors
        instance.code = code;

        CompilationResult compilationResult = compileScript(code);
        if (compilationResult.getProgram() != null) {
            applyScript(identifier, compilationResult.getProgram());
            return ScriptSaveResult.success();
        } else {
            return ScriptSaveResult.fail(compilationResult.getDiagnostics());
        }
    }

    @Override
    public ScriptCompileResult compile(String code) {
        // TODO: optimize to not create parameters every time?
        AnalysisResult result = new Analyzer().analyze(code, scriptType.createParameters());
        return new ScriptCompileResult(result.binderOutput().diagnostics());
    }

    @Override
    public ScriptSaveResult save(@Nullable String identifier, @Nullable String code) {
        if (identifier == null) {
            throw new IllegalStateException();
        }

        ScriptDocument instance = instances.get(identifier);

        if (code == null || code.isEmpty()) {
            if (instance != null) {
                instance.code = null;
            }

            updateConfigCode(identifier, null);
            ConfigStore.instance.requestWrite();

            applyScript(identifier, null);

            return ScriptSaveResult.success();
        }

        if (instance == null) {
            instance = new ScriptDocument(new ScriptRef(scriptType, identifier));
            instances.put(identifier, instance);
        }

        CompilationResult compilationResult = compileScript(code);

        instance.lastAttemptAt = Instant.now();
        instance.lastAttemptCode = code;
        instance.lastAttemptDiagnostics = compilationResult.getDiagnostics();

        if (compilationResult.getProgram() != null) {
            instance.code = code;
            updateConfigCode(identifier, code);
            ConfigStore.instance.requestWrite();

            applyScript(identifier, compilationResult.getProgram());

            return ScriptSaveResult.success();
        } else {
            return ScriptSaveResult.fail(compilationResult.getDiagnostics());
        }
    }

    protected abstract void updateConfigCode(String identifier, @Nullable String code);
    protected abstract CompilationResult compileScript(String code);
    protected abstract <T> void applyScript(String identifier, @Nullable T program);
}