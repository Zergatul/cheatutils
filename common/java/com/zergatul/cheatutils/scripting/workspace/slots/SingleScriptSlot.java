package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
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
import java.util.List;
import java.util.Objects;

public abstract class SingleScriptSlot extends ScriptSlot {

    private final ScriptType scriptType;
    private final ScriptDocument instance;

    protected SingleScriptSlot(ScriptType scriptType) {
        this.scriptType = scriptType;
        this.instance = new ScriptDocument(new ScriptRef(scriptType));
    }

    @Override
    public List<ScriptDocument> getInstances() {
        return List.of(instance);
    }

    @Override
    public ScriptDocument getInstance(@Nullable String identifier) {
        if (identifier != null) {
            throw new IllegalArgumentException("Single-script slots do not accept identifiers.");
        }
        return instance;
    }

    @Override
    public ScriptSaveResult init(@Nullable String identifier, @Nullable String code) {
        requireNoIdentifier(identifier);
        if (code == null || code.isEmpty()) {
            return ScriptSaveResult.success();
        }

        instance.code = code;
        CompilationResult result = compileScript(code);
        if (result.getProgram() != null) {
            ScriptExecutionManager.instance.cancel(instance.ref);
            onProgramChanged(result.getProgram());
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
        requireNoIdentifier(identifier);
        if (code == null || code.isEmpty()) {
            instance.code = null;
            ScriptExecutionManager.instance.cancel(instance.ref);
            onCodeChanged(null);
            onProgramChanged(null);
            return ScriptSaveResult.success();
        }

        CompilationResult result = compileScript(code);
        instance.lastAttemptAt = Instant.now();
        instance.lastAttemptCode = code;
        instance.lastAttemptDiagnostics = result.getDiagnostics();

        if (result.getProgram() != null) {
            instance.code = code;
            ScriptExecutionManager.instance.cancel(instance.ref);
            onCodeChanged(code);
            onProgramChanged(result.getProgram());
            return ScriptSaveResult.success();
        }
        return ScriptSaveResult.fail(Objects.requireNonNull(result.getDiagnostics()));
    }

    protected CompilationResult compileScript(String code) {
        return ScriptCompilerRegistry.INSTANCE.compile(scriptType, code);
    }

    protected void onCodeChanged(@Nullable String code) {}

    protected void onProgramChanged(@Nullable Object program) {}

    private static void requireNoIdentifier(@Nullable String identifier) {
        if (identifier != null) {
            throw new IllegalArgumentException("Single-script slots do not accept identifiers.");
        }
    }
}