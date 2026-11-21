package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.analysis.AnalysisResult;
import com.zergatul.scripting.analysis.Analyzer;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.InstanceCompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Binds one script type to its module: reads and writes code in settings, keeps documents,
 * installs compiled programs into the module runtime.
 * <p>
 * Documents and module runtimes are owned by the client thread. Methods that change them must be called
 * on the client thread; other threads go through {@link ScriptWorkspace#save}. Compilation and the
 * document getters are safe on any thread.
 */
@NullMarked
public abstract class ScriptSlot {

    protected final ScriptType scriptType;

    protected ScriptSlot(ScriptType scriptType) {
        this.scriptType = scriptType;
    }

    public ScriptType getType() {
        return scriptType;
    }

    public abstract List<ScriptDocument> getInstances();
    public abstract ScriptDocument getInstance(@Nullable String identifier);

    /** Analysis only, any thread. */
    public ScriptCompileResult compile(String code) {
        CompilationParameters compilationParameters = ScriptCompilerRegistry.INSTANCE.getParameters(scriptType);
        AnalysisResult result = new Analyzer().analyze(code, compilationParameters);
        return new ScriptCompileResult(result.binderOutput().diagnostics());
    }

    /** Full compilation without side effects, any thread. */
    public InstanceCompilationResult compileScript(String code) {
        return ScriptCompilerRegistry.INSTANCE.compile(scriptType, code);
    }

    /**
     * Client thread. Installs code loaded from settings. Always retires the previous program,
     * even when the code is empty or doesn't compile.
     */
    public abstract ScriptSaveResult init(@Nullable String identifier, @Nullable String code);

    /**
     * Client thread. Applies a save compiled earlier: document, settings and runtime change together.
     * {@code compilation} is null for empty code. An invalid save keeps the last working program.
     */
    public abstract ScriptSaveResult commit(@Nullable String identifier, @Nullable String code, @Nullable InstanceCompilationResult compilation);

    /** Client thread. Replaces documents and runtime with scripts from the current settings. Reports failures, never throws. */
    public abstract void reload();

    protected void initAndReport(@Nullable String identifier, @Nullable String code) {
        ScriptRef ref = new ScriptRef(scriptType, identifier);
        try {
            ScriptSaveResult result = init(identifier, code);
            if (!result.isSuccess()) {
                ScriptRuntimeFailureHandler.INSTANCE.reportCompilationFailure(ref, result.getDiagnostics());
            }
        } catch (Throwable e) {
            ScriptRuntimeFailureHandler.INSTANCE.reportInitializationFailure(ref, e);
        }
    }
}
