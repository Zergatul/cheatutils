package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;
import com.zergatul.cheatutils.scripting.ScriptType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public abstract class ScriptSlot {

    public ScriptSaveResult init(@Nullable String code) {
        return init(null, code);
    }

    public ScriptSaveResult save(String code) {
        return save(null, code);
    }

    public abstract List<ScriptDocument> getInstances();
    public abstract ScriptDocument getInstance(@Nullable String identifier);
    public abstract ScriptSaveResult init(@Nullable String identifier, @Nullable String code);
    public abstract ScriptCompileResult compile(String code);
    public abstract ScriptSaveResult save(@Nullable String identifier, @Nullable String code);

    protected void initAndReport(ScriptType scriptType, @Nullable String identifier, @Nullable String code) {
        ScriptRef ref = new ScriptRef(scriptType, identifier);
        try {
            ScriptSaveResult result = this.init(identifier, code);
            if (!result.isSuccess()) {
                ScriptRuntimeFailureHandler.INSTANCE.reportCompilationFailure(ref, result.getDiagnostics());
            }
        } catch (Throwable e) {
            ScriptRuntimeFailureHandler.INSTANCE.reportInitializationFailure(ref, e);
        }
    }
}