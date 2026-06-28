package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ScriptSaveResult {

    private final @Nullable List<DiagnosticMessage> diagnostics;

    private ScriptSaveResult(@Nullable List<DiagnosticMessage> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public static ScriptSaveResult success() {
        return new ScriptSaveResult(null);
    }

    public static ScriptSaveResult fail(List<DiagnosticMessage> diagnostics) {
        return new ScriptSaveResult(diagnostics);
    }

    public boolean isSuccess() {
        return diagnostics == null;
    }

    public List<DiagnosticMessage> getDiagnostics() {
        return Objects.requireNonNull(diagnostics);
    }
}