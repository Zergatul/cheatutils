package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationResult;

import java.util.ArrayList;
import java.util.List;

/** Backend entry points until the script editor and saved keybindings arrive. */
public class ScriptApi extends ApiBase {
    private final boolean execute;

    public ScriptApi(boolean execute) {
        this.execute = execute;
    }

    @Override
    public String getRoute() {
        return execute ? "script-exec" : "script-compile";
    }

    @Override
    public String post(String body) throws ApiException {
        Request request = gson.fromJson(body, Request.class);
        if (request == null || request.code == null) {
            throw new ApiException("Code is required.", 400);
        }
        CompilationResult compiled = ScriptCompilerRegistry.INSTANCE.compile(ScriptType.EXEC_CODE, request.code);
        Response response = new Response();
        if (compiled.getDiagnostics() != null) {
            for (DiagnosticMessage message : compiled.getDiagnostics()) {
                response.diagnostics.add(new Diagnostic(message));
            }
        }
        Runnable program = compiled.getProgram();
        response.success = program != null;
        if (program != null && execute) {
            Throwable failure = ScriptExecutionManager.instance.execute(ScriptType.EXEC_CODE, program);
            response.success = failure == null;
            response.executed = true;
            if (failure != null) {
                response.error = failure.getClass().getName() + ": " + failure.getMessage();
            }
        }
        return gson.toJson(response);
    }

    private static class Request {
        String code;
    }

    private static class Response {
        boolean success;
        boolean executed;
        List<Diagnostic> diagnostics = new ArrayList<>();
        String error;
    }

    private static class Diagnostic {
        final String code, message;
        final int line, column, endLine, endColumn;

        Diagnostic(DiagnosticMessage diagnostic) {
            code = diagnostic.code;
            message = diagnostic.message;
            line = diagnostic.range.getLine1();
            column = diagnostic.range.getColumn1();
            endLine = diagnostic.range.getLine2();
            endColumn = diagnostic.range.getColumn2();
        }
    }
}
