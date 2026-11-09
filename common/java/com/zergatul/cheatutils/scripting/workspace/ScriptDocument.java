package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

@NullMarked
public class ScriptDocument {

    public final ScriptRef ref;
    public @Nullable String code;
    public @Nullable String lastAttemptCode;
    public @Nullable List<DiagnosticMessage> lastAttemptDiagnostics;
    public @Nullable Instant lastAttemptAt;

    public ScriptDocument(ScriptRef ref) {
        this.ref = ref;
    }

    public ScriptDocument withRef(ScriptRef ref) {
        ScriptDocument newDocument = new ScriptDocument(ref);
        newDocument.code = code;
        newDocument.lastAttemptCode = lastAttemptCode;
        newDocument.lastAttemptDiagnostics = lastAttemptDiagnostics;
        newDocument.lastAttemptAt = lastAttemptAt;
        return newDocument;
    }
}