package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.*;
import com.zergatul.scripting.analysis.AnalysisResult;
import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

@NullMarked
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
        return instance;
    }

    @Override
    public ScriptSaveResult init(@Nullable String identifier, @Nullable String code) {
        if (identifier != null) {
            throw new IllegalStateException();
        }

        if (code == null || code.isEmpty()) {
            return ScriptSaveResult.success();
        }

        // initial code loaded from config may contain errors
        instance.code = code;

        CompilationResult compilationResult = compileScript(code);
        if (compilationResult.getProgram() != null) {
            ScriptExecutionManager.instance.cancel(instance.ref);
            applyScript(compilationResult.getProgram());
            return ScriptSaveResult.success();
        } else {
            return ScriptSaveResult.fail(compilationResult.getDiagnostics());
        }
    }

    @Override
    public ScriptCompileResult compile(String code) {
        CompilationParameters compilationParameters = ScriptCompilerRegistry.INSTANCE.getParameters(scriptType);
        AnalysisResult result = new Analyzer().analyze(code, compilationParameters);
        return new ScriptCompileResult(result.binderOutput().diagnostics());
    }

    @Override
    public ScriptSaveResult save(@Nullable String identifier, @Nullable String code) {
        if (identifier != null) {
            throw new IllegalStateException();
        }

        if (code == null || code.isEmpty()) {
            instance.code = null;
            ScriptExecutionManager.instance.cancel(instance.ref);

            updateConfigCode(null);
            ConfigStore.instance.requestWrite();

            applyScript(null);

            return ScriptSaveResult.success();
        }

        CompilationResult compilationResult = compileScript(code);

        instance.lastAttemptAt = Instant.now();
        instance.lastAttemptCode = code;
        instance.lastAttemptDiagnostics = compilationResult.getDiagnostics();

        if (compilationResult.getProgram() != null) {
            instance.code = code;
            ScriptExecutionManager.instance.cancel(instance.ref);
            updateConfigCode(code);
            ConfigStore.instance.requestWrite();

            applyScript(compilationResult.getProgram());

            return ScriptSaveResult.success();
        } else {
            return ScriptSaveResult.fail(compilationResult.getDiagnostics());
        }
    }

    protected abstract void updateConfigCode(@Nullable String code);
    protected abstract CompilationResult compileScript(String code);
    protected abstract <T> void applyScript(@Nullable T program);
}