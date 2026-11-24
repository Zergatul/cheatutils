package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

/** Immutable, so slots can publish documents to readers on any thread. */
@NullMarked
public record ScriptDocument(
        ScriptRef ref,
        @Nullable String code,
        @Nullable String lastAttemptCode,
        @Nullable List<DiagnosticMessage> lastAttemptDiagnostics,
        @Nullable Instant lastAttemptAt
) {
    public ScriptDocument(ScriptRef ref) {
        this(ref, null, null, null, null);
    }

    public ScriptDocument withRef(ScriptRef ref) {
        return new ScriptDocument(ref, code, lastAttemptCode, lastAttemptDiagnostics, lastAttemptAt);
    }

    public ScriptDocument withCode(@Nullable String code) {
        return new ScriptDocument(ref, code, lastAttemptCode, lastAttemptDiagnostics, lastAttemptAt);
    }

    public ScriptDocument withAttempt(String code, @Nullable List<DiagnosticMessage> diagnostics) {
        return new ScriptDocument(ref, this.code, code, diagnostics, Instant.now());
    }
}
