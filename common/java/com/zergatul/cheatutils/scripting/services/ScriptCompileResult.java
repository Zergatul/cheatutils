package com.zergatul.cheatutils.scripting.services;

import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ScriptCompileResult {

    private final @Nullable List<DiagnosticMessage> diagnostics;

    public ScriptCompileResult(@Nullable List<DiagnosticMessage> diagnostics) {
        this.diagnostics = diagnostics;
    }
}