package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public class ScriptDocument {

    public final ScriptRef ref;
    public @Nullable String code;
    public @Nullable String lastAttemptCode;
    public @Nullable List<DiagnosticMessage> lastAttemptDiagnostics;
    public @Nullable Instant lastAttemptAt;

    public ScriptDocument(ScriptRef ref) {
        this.ref = ref;
    }
}