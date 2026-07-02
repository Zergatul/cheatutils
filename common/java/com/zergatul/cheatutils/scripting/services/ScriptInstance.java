package com.zergatul.cheatutils.scripting.services;

import com.zergatul.scripting.DiagnosticMessage;

import java.time.Instant;
import java.util.List;

public class ScriptInstance {
    public String code;
    public String lastAttemptCode;
    public List<DiagnosticMessage> lastAttemptDiagnostics;
    public Instant lastAttemptAt;
}