package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptCompileResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptDocument;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptSlot;
import com.zergatul.scripting.analysis.AnalysisResult;
import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class MultiScriptSlot extends ScriptSlot {

    private final ScriptType scriptType;
    private final Map<String, ScriptDocument> instances = new LinkedHashMap<>();

    protected MultiScriptSlot(ScriptType scriptType) {
        this.scriptType = scriptType;
    }

    @Override
    public List<ScriptDocument> getInstances() {
        return List.copyOf(instances.values());
    }

    @Override
    public ScriptDocument getInstance(@Nullable String identifier) {
        return Objects.requireNonNull(instances.get(requireIdentifier(identifier)), "Unknown script identifier.");
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
            onCodeChanged(identifier, null);
            onProgramChanged(identifier, null);
        }
    }

    @Override
    public ScriptSaveResult init(@Nullable String identifier, @Nullable String code) {
        String id = requireIdentifier(identifier);
        if (code == null || code.isEmpty()) {
            return ScriptSaveResult.success();
        }

        ScriptDocument instance = new ScriptDocument(new ScriptRef(scriptType, id));
        instances.put(id, instance);
        instance.code = code;

        CompilationResult result = compileScript(code);
        if (result.getProgram() != null) {
            onProgramChanged(id, result.getProgram());
            return ScriptSaveResult.success();
        }
        return ScriptSaveResult.fail(Objects.requireNonNull(result.getDiagnostics()));
    }

    @Override
    public ScriptCompileResult compile(String code) {
        CompilationParameters parameters = ScriptCompilerRegistry.INSTANCE.getParameters(scriptType);
        AnalysisResult result = new Analyzer().analyze(code, parameters);
        return new ScriptCompileResult(result.binderOutput().diagnostics());
    }

    @Override
    public ScriptSaveResult save(@Nullable String identifier, @Nullable String code) {
        String id = requireIdentifier(identifier);
        ScriptDocument instance = instances.get(id);

        if (code == null || code.isEmpty()) {
            if (instance != null) {
                instance.code = null;
            }
            onCodeChanged(id, null);
            onProgramChanged(id, null);
            return ScriptSaveResult.success();
        }

        if (instance == null) {
            instance = new ScriptDocument(new ScriptRef(scriptType, id));
            instances.put(id, instance);
        }

        CompilationResult result = compileScript(code);
        instance.lastAttemptAt = Instant.now();
        instance.lastAttemptCode = code;
        instance.lastAttemptDiagnostics = result.getDiagnostics();

        if (result.getProgram() != null) {
            instance.code = code;
            onCodeChanged(id, code);
            onProgramChanged(id, result.getProgram());
            return ScriptSaveResult.success();
        }
        return ScriptSaveResult.fail(Objects.requireNonNull(result.getDiagnostics()));
    }

    protected CompilationResult compileScript(String code) {
        return ScriptCompilerRegistry.INSTANCE.compile(scriptType, code);
    }

    protected void onCodeChanged(String identifier, @Nullable String code) {}

    protected void onProgramChanged(String identifier, @Nullable Object program) {}

    private static String requireIdentifier(@Nullable String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Multi-script slots require a non-empty identifier.");
        }
        return identifier;
    }
}