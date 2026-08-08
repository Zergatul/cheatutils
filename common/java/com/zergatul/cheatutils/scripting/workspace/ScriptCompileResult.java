package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.scripting.DiagnosticMessage;

import java.util.List;

public class ScriptCompileResult {

    private final List<DiagnosticMessage> diagnostics;

    public ScriptCompileResult(List<DiagnosticMessage> diagnostics) {
        this.diagnostics = List.copyOf(diagnostics);
    }

    public boolean isSuccess() {
        return diagnostics.isEmpty();
    }

    public List<DiagnosticMessage> getDiagnostics() {
        return diagnostics;
    }
}