package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.concurrent.ClientThread;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.*;
import com.zergatul.scripting.compiler.ClassCompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@NullMarked
public abstract class SingleScriptSlot extends ScriptSlot {

    // written on the client thread, read from any thread
    private volatile ScriptDocument document;

    protected SingleScriptSlot(ScriptType scriptType) {
        super(scriptType);
        this.document = new ScriptDocument(new ScriptRef(scriptType));
    }

    @Override
    public List<ScriptDocument> getInstances() {
        return List.of(document);
    }

    @Override
    public ScriptDocument getInstance(@Nullable String identifier) {
        return document;
    }

    @Override
    public void reload() {
        ClientThread.assertCurrent();
        initAndReport(null, readConfigCode());
    }

    public ScriptSaveResult init(@Nullable String code) {
        return init(null, code);
    }

    @Override
    public ScriptSaveResult init(@Nullable String identifier, @Nullable String code) {
        if (identifier != null) {
            throw new IllegalStateException();
        }

        ClientThread.assertCurrent();

        // code loaded from settings may contain errors, the document keeps it anyway
        document = document.withCode(code);
        ScriptExecutionManager.INSTANCE.cancel(document.ref());
        applyScript(null);

        if (code == null || code.isEmpty()) {
            return ScriptSaveResult.success();
        }

        ClassCompilationResult compilationResult = compileScript(code);
        if (compilationResult.isSuccess()) {
            applyScript(ScriptCompilerRegistry.instantiate(compilationResult));
            return ScriptSaveResult.success();
        } else {
            return ScriptSaveResult.fail(compilationResult.getDiagnostics());
        }
    }

    @Override
    public ScriptSaveResult commit(@Nullable String identifier, @Nullable String code, @Nullable ClassCompilationResult compilation) {
        if (identifier != null) {
            throw new IllegalStateException();
        }

        ClientThread.assertCurrent();

        if (code == null || code.isEmpty()) {
            document = document.withCode(null);
            ScriptExecutionManager.INSTANCE.cancel(document.ref());
            updateConfigCode(null);
            applyScript(null);
            return ScriptSaveResult.success();
        }

        Objects.requireNonNull(compilation);
        if (!compilation.isSuccess()) {
            document = document.withAttempt(code, compilation.getDiagnostics());
            return ScriptSaveResult.fail(compilation.getDiagnostics());
        }

        Object program = instantiateForSave(compilation);
        document = document.withAttempt(code, List.of()).withCode(code);
        ScriptExecutionManager.INSTANCE.cancel(document.ref());
        updateConfigCode(code);
        applyScript(program);
        return ScriptSaveResult.success();
    }

    protected abstract @Nullable String readConfigCode();
    protected abstract void updateConfigCode(@Nullable String code);
    protected abstract <T> void applyScript(@Nullable T program);
}