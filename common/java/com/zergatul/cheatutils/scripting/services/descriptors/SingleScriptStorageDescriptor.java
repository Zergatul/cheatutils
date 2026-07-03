package com.zergatul.cheatutils.scripting.services.descriptors;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.services.*;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;

@NullMarked
public abstract class SingleScriptStorageDescriptor extends ScriptStorageDescriptor {

    private final ScriptType scriptType;
    private final ScriptInstance instance;

    protected SingleScriptStorageDescriptor(ScriptType scriptType) {
        this.scriptType = scriptType;
        this.instance = new ScriptInstance(new ScriptLocator(scriptType));
    }

    @Override
    public List<ScriptInstance> getInstances() {
        return List.of(instance);
    }

    @Override
    public @Nullable String getCode(@Nullable String identifier) {
        if (identifier != null) {
            throw new IllegalStateException();
        }

        return instance.code;
    }

    @Override
    public @Nullable String getLastAttemptedCode(@Nullable String identifier) {
        if (identifier != null) {
            throw new IllegalStateException();
        }

        return instance.lastAttemptCode;
    }

    @Override
    public ScriptSaveResult init(@Nullable String identifier, @Nullable String code) {
        if (identifier != null) {
            throw new IllegalStateException();
        }

        if (code == null || code.isEmpty()) {
            return ScriptSaveResult.success();
        }

        // initial code loaded from config may contain errors
        instance.code = code;

        CompilationResult compilationResult = compileScript(code);
        if (compilationResult.getProgram() != null) {
            applyScript(compilationResult.getProgram());
            return ScriptSaveResult.success();
        } else {
            return ScriptSaveResult.fail(compilationResult.getDiagnostics());
        }
    }

    @Override
    public ScriptCompileResult compile(String code) {
        LexerInput lexerInput = new LexerInput(code);
        Lexer lexer = new Lexer(lexerInput);
        LexerOutput lexerOutput = lexer.lex();

        Parser parser = new Parser(lexerOutput);
        ParserOutput parserOutput = parser.parse();

        // TODO: optimize to not create parameters every time?
        Binder binder = new Binder(parserOutput, scriptType.createParameters());
        BinderOutput binderOutput = binder.bind();

        return new ScriptCompileResult(binderOutput.diagnostics());
    }

    @Override
    public ScriptSaveResult save(@Nullable String identifier, @Nullable String code) {
        if (identifier != null) {
            throw new IllegalStateException();
        }

        if (code == null || code.isEmpty()) {
            instance.code = null;

            updateConfigCode(null);
            ConfigStore.instance.requestWrite();

            applyScript(null);

            return ScriptSaveResult.success();
        }

        CompilationResult compilationResult = compileScript(code);

        instance.lastAttemptAt = Instant.now();
        instance.lastAttemptCode = code;
        instance.lastAttemptDiagnostics = compilationResult.getDiagnostics();

        if (compilationResult.getProgram() != null) {
            instance.code = code;
            updateConfigCode(code);
            ConfigStore.instance.requestWrite();

            applyScript(compilationResult.getProgram());

            return ScriptSaveResult.success();
        } else {
            return ScriptSaveResult.fail(compilationResult.getDiagnostics());
        }
    }

    protected abstract void updateConfigCode(@Nullable String code);
    protected abstract CompilationResult compileScript(String code);
    protected abstract <T> void applyScript(@Nullable T program);
}