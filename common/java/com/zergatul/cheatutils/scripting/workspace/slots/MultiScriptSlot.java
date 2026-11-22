package com.zergatul.cheatutils.scripting.workspace.slots;

import com.zergatul.cheatutils.concurrent.ClientThread;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.*;
import com.zergatul.scripting.compiler.ClassCompilationResult;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@NullMarked
public abstract class MultiScriptSlot extends ScriptSlot {

    // copy-on-write snapshot: replaced on the client thread, read from any thread
    private volatile Map<String, ScriptDocument> documents = Map.of();

    protected MultiScriptSlot(ScriptType scriptType) {
        super(scriptType);
    }

    @Override
    public List<ScriptDocument> getInstances() {
        return List.copyOf(documents.values());
    }

    @Override
    public ScriptDocument getInstance(@Nullable String identifier) {
        if (identifier == null) {
            throw new IllegalStateException();
        }

        ScriptDocument document = documents.get(identifier);
        if (document == null) {
            throw new IllegalStateException();
        }

        return document;
    }

    @Override
    public void reload() {
        ClientThread.assertCurrent();

        clearRuntime();
        documents = Map.of();

        for (ConfigScript script : readConfigScripts()) {
            if (script.code() != null && !script.code().isEmpty()) {
                initAndReport(script.identifier(), script.code());
            }
        }
    }

    /** Client thread. Removes the script of an entry that is being deleted. */
    public void remove(String identifier) {
        ClientThread.assertCurrent();

        Map<String, ScriptDocument> copy = new LinkedHashMap<>(documents);
        copy.remove(identifier);
        documents = Collections.unmodifiableMap(copy);

        ScriptExecutionManager.INSTANCE.cancel(new ScriptRef(scriptType, identifier));
        applyScript(identifier, null);
    }

    /** Client thread. Keeps the script, document and running execution of an entry that is being renamed. */
    public void move(String oldIdentifier, String newIdentifier) {
        ClientThread.assertCurrent();

        ScriptDocument oldDocument = documents.get(oldIdentifier);
        if (oldDocument != null) {
            ScriptDocument newDocument = oldDocument.withRef(new ScriptRef(scriptType, newIdentifier));
            Map<String, ScriptDocument> copy = new LinkedHashMap<>(documents);
            copy.remove(oldIdentifier);
            copy.put(newIdentifier, newDocument);
            documents = Collections.unmodifiableMap(copy);
            ScriptExecutionManager.INSTANCE.move(oldDocument.ref(), newDocument.ref());
        }

        moveRuntime(oldIdentifier, newIdentifier);
    }

    @Override
    public ScriptSaveResult init(@Nullable String identifier, @Nullable String code) {
        if (identifier == null) {
            throw new IllegalStateException();
        }

        ClientThread.assertCurrent();

        ScriptRef ref = new ScriptRef(scriptType, identifier);
        ScriptExecutionManager.INSTANCE.cancel(ref);
        applyScript(identifier, null);

        if (code == null || code.isEmpty()) {
            return ScriptSaveResult.success();
        }

        // code loaded from settings may contain errors, the document keeps it anyway
        putDocument(new ScriptDocument(ref).withCode(code));

        ClassCompilationResult compilationResult = compileScript(code);
        if (compilationResult.isSuccess()) {
            applyScript(identifier, ScriptCompilerRegistry.instantiate(compilationResult));
            return ScriptSaveResult.success();
        } else {
            return ScriptSaveResult.fail(compilationResult.getDiagnostics());
        }
    }

    @Override
    public ScriptSaveResult commit(@Nullable String identifier, @Nullable String code, @Nullable ClassCompilationResult compilation) {
        if (identifier == null) {
            throw new IllegalStateException();
        }

        ClientThread.assertCurrent();

        if (!hasConfigEntry(identifier)) {
            throw new ScriptSaveRejectedException(
                    "Cannot find " + scriptType.getModuleName() + " entry '" + identifier + "'. The script was not saved.");
        }

        ScriptRef ref = new ScriptRef(scriptType, identifier);
        ScriptDocument document = documents.get(identifier);
        if (document == null) {
            document = new ScriptDocument(ref);
        }

        if (code == null || code.isEmpty()) {
            putDocument(document.withCode(null));
            ScriptExecutionManager.INSTANCE.cancel(ref);
            updateConfigCode(identifier, null);
            applyScript(identifier, null);
            return ScriptSaveResult.success();
        }

        Objects.requireNonNull(compilation);
        if (!compilation.isSuccess()) {
            putDocument(document.withAttempt(code, compilation.getDiagnostics()));
            return ScriptSaveResult.fail(compilation.getDiagnostics());
        }

        Object program = instantiateForSave(compilation);
        putDocument(document.withAttempt(code, List.of()).withCode(code));
        ScriptExecutionManager.INSTANCE.cancel(ref);
        updateConfigCode(identifier, code);
        applyScript(identifier, program);
        return ScriptSaveResult.success();
    }

    private void putDocument(ScriptDocument document) {
        Map<String, ScriptDocument> copy = new LinkedHashMap<>(documents);
        copy.put(Objects.requireNonNull(document.ref().identifier()), document);
        documents = Collections.unmodifiableMap(copy);
    }

    protected abstract List<ConfigScript> readConfigScripts();
    protected abstract boolean hasConfigEntry(String identifier);
    protected abstract void updateConfigCode(String identifier, @Nullable String code);
    protected abstract <T> void applyScript(String identifier, @Nullable T program);
    protected abstract void clearRuntime();

    protected void moveRuntime(String oldIdentifier, String newIdentifier) {
        throw new UnsupportedOperationException(scriptType.getModuleName() + " scripts cannot be renamed.");
    }

    public record ConfigScript(String identifier, @Nullable String code) {}
}