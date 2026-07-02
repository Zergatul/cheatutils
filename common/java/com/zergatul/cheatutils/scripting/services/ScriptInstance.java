package com.zergatul.cheatutils.scripting.services;

import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

public class ScriptInstance {
    public @Nullable String code;
    public @Nullable String lastAttemptCode;
    public @Nullable List<DiagnosticMessage> lastAttemptDiagnostics;
    public @Nullable Instant lastAttemptAt;
}