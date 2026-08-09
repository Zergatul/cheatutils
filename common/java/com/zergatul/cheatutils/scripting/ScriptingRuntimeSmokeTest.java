package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.scripting.workspace.ScriptDocument;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptSlot;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.MultiScriptSlot;
import com.zergatul.scripting.compiler.CompilationResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ScriptingRuntimeSmokeTest {

    private static final Logger LOGGER = LogManager.getLogger(ScriptingRuntimeSmokeTest.class);

    private ScriptingRuntimeSmokeTest() {}

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        ScriptCompilerRegistry registry = ScriptCompilerRegistry.INSTANCE;

        Runnable synchronous = getProgram(registry.compile(ScriptType.OVERLAY, ""));
        verifyClassLoader(synchronous);
        synchronous.run();

        AsyncRunnable asynchronous = getProgram(registry.compile(ScriptType.KEYBINDING, ""));
        verifyClassLoader(asynchronous);
        CompletableFuture<?> future = Objects.requireNonNull(asynchronous.run());
        future.join();

        verifyWorkspace();

        LOGGER.info("Modern scripting runtime smoke test passed for synchronous and asynchronous scripts.");
    }

    private static void verifyWorkspace() {
        ScriptWorkspace workspace = ScriptWorkspace.INSTANCE;
        if (!workspace.getSupportedTypes().equals(List.of(
                ScriptType.KEYBINDING,
                ScriptType.OVERLAY,
                ScriptType.BLOCK_AUTOMATION,
                ScriptType.VILLAGER_ROLLER))) {
            throw new IllegalStateException("Unexpected initial scripting workspace types.");
        }

        String validCode = "int value = 1;";
        String invalidCode = "int value = ;";

        ScriptSlot overlay = workspace.get(ScriptType.OVERLAY);
        requireSuccess(overlay.save(validCode));
        ScriptDocument overlayDocument = overlay.getInstance(null);
        requireCode(overlayDocument, validCode);

        ScriptSaveResult invalidOverlay = overlay.save(invalidCode);
        requireFailure(invalidOverlay);
        requireCode(overlayDocument, validCode);
        if (!invalidCode.equals(overlayDocument.lastAttemptCode) || overlayDocument.lastAttemptDiagnostics == null) {
            throw new IllegalStateException("Failed workspace save did not preserve its diagnostics.");
        }
        requireSuccess(overlay.save((String) null));

        MultiScriptSlot keyBindings = (MultiScriptSlot) workspace.get(ScriptType.KEYBINDING);
        requireSuccess(keyBindings.save("smoke", validCode));
        ScriptDocument keyBindingDocument = keyBindings.getInstance("smoke");
        requireCode(keyBindingDocument, validCode);
        requireFailure(keyBindings.save("smoke", invalidCode));
        requireCode(keyBindingDocument, validCode);
        keyBindings.remove("smoke");
    }

    private static void requireSuccess(ScriptSaveResult result) {
        if (!result.isSuccess()) {
            throw new IllegalStateException("Expected workspace save to succeed: " + result.getDiagnostics());
        }
    }

    private static void requireFailure(ScriptSaveResult result) {
        if (result.isSuccess() || result.getDiagnostics().isEmpty()) {
            throw new IllegalStateException("Expected workspace save to fail with diagnostics.");
        }
    }

    private static void requireCode(ScriptDocument document, String code) {
        if (!code.equals(document.code)) {
            throw new IllegalStateException("Workspace replaced the last valid source unexpectedly.");
        }
    }

    private static <T> T getProgram(CompilationResult result) {
        if (result.getDiagnostics() != null) {
            throw new IllegalStateException("Scripting runtime smoke compilation failed: " + result.getDiagnostics());
        }
        return Objects.requireNonNull(result.getProgram());
    }

    private static void verifyClassLoader(Object program) {
        ClassLoader expected = ScriptCompilerRegistry.class.getClassLoader();
        ClassLoader actual = program.getClass().getClassLoader().getParent();
        if (actual != expected) {
            throw new IllegalStateException("Generated script uses an unexpected parent classloader.");
        }
    }
}