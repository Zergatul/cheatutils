package com.zergatul.cheatutils.scripting.services;

import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class ScriptCompileResult {

    private final List<DiagnosticMessage> diagnostics;

    public ScriptCompileResult(List<DiagnosticMessage> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public boolean isSuccess() {
        return diagnostics.isEmpty();
    }

    public List<DiagnosticMessage> getDiagnostics() {
        return diagnostics;
    }
}