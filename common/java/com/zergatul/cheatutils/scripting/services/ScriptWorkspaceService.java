package com.zergatul.cheatutils.scripting.services;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.ScriptsController;
import com.zergatul.cheatutils.modules.scripting.StatusOverlay;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@NullMarked
public class ScriptWorkspaceService {

    public static final ScriptWorkspaceService INSTANCE = new ScriptWorkspaceService();

    private final Map<ScriptType, ScriptStorageDescriptor> descriptors = new Object2ObjectArrayMap<>();

    private ScriptWorkspaceService() {
        descriptors.put(ScriptType.OVERLAY, new StatusOverlayDescriptor());
    }

    public ScriptStorageDescriptor get(ScriptType type) {
        return Objects.requireNonNull(descriptors.get(type));
    }

    public List<ScriptStorageDescriptor> getAll() {
        return new ArrayList<>(descriptors.values());
    }

    @NullMarked
    private static class StatusOverlayDescriptor extends ScriptStorageDescriptor {

        private final ScriptInstance instance = new ScriptInstance();

        public StatusOverlayDescriptor() {
            super(new ScriptLocator(ScriptType.BLOCK_AUTOMATION));
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
        public ScriptCompileResult compile(String code) {
            LexerInput lexerInput = new LexerInput(code);
            Lexer lexer = new Lexer(lexerInput);
            LexerOutput lexerOutput = lexer.lex();

            Parser parser = new Parser(lexerOutput);
            ParserOutput parserOutput = parser.parse();

            // TODO: optimize to not create parameters every time?
            Binder binder = new Binder(parserOutput, ScriptType.OVERLAY.createParameters());
            BinderOutput binderOutput = binder.bind();

            return new ScriptCompileResult(binderOutput.diagnostics());
        }

        @Override
        public ScriptSaveResult save(@Nullable String identifier, @Nullable String code) {
            if (identifier != null) {
                throw new IllegalStateException();
            }

            if (code == null || code.isEmpty()) {
                ConfigStore.instance.getConfig().statusOverlayConfig.code = null;
                ConfigStore.instance.requestWrite();

                StatusOverlay.instance.setScript(null);

                return ScriptSaveResult.success();
            }

            CompilationResult compilationResult = ScriptsController.instance.compileOverlay(code);

            instance.lastAttemptAt = Instant.now();
            instance.lastAttemptCode = code;
            instance.lastAttemptDiagnostics = compilationResult.getDiagnostics();

            if (compilationResult.getProgram() != null) {
                ConfigStore.instance.getConfig().statusOverlayConfig.code = code;
                ConfigStore.instance.requestWrite();

                StatusOverlay.instance.setScript(compilationResult.getProgram());

                return ScriptSaveResult.success();
            } else {
                ConfigStore.instance.getConfig().statusOverlayConfig.code = null;
                ConfigStore.instance.requestWrite();

                StatusOverlay.instance.setScript(null);

                return ScriptSaveResult.fail(compilationResult.getDiagnostics());
            }
        }
    }
}