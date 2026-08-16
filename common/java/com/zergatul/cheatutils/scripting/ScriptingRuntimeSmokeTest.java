package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.configs.Config;
import com.zergatul.cheatutils.configs.ConfigMigrationSmokeTest;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.modules.scripting.KeyBindings;
import com.zergatul.cheatutils.scripting.workspace.ScriptDocument;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
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
import java.util.concurrent.CompletionException;

public class ScriptingRuntimeSmokeTest {

    private static final Logger LOGGER = LogManager.getLogger(ScriptingRuntimeSmokeTest.class);

    private ScriptingRuntimeSmokeTest() {}

    public static void main(String[] args) {
        run();
        verifyKeyBindingModule();
        LOGGER.info("Key Binding Scripts mutation smoke test passed.");
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

        verifyInitialApiCompatibility();
        verifyRuntimeLineNumbers();
        verifyExecutionLifecycle();
        verifyWorkspace();
        ConfigMigrationSmokeTest.verifyKeyBindingScripts();

        LOGGER.info("Modern scripting runtime smoke test passed for synchronous and asynchronous scripts.");
    }

    private static void verifyExecutionLifecycle() {
        ScriptExecutionManager manager = ScriptExecutionManager.instance;
        ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, "execution-smoke");
        int[] starts = new int[1];
        AsyncRunnable script = () -> {
            starts[0]++;
            return new CompletableFuture<>();
        };

        CompletableFuture<?> first = manager.execute(ref, script);
        CompletableFuture<?> second = manager.execute(ref, script);
        if (first != second || starts[0] != 1 || !manager.isRunning(ref)) {
            throw new IllegalStateException("Concurrent execution of one async script was not suppressed.");
        }

        manager.cancel(ref);
        if (!first.isCancelled() || manager.isRunning(ref)) {
            throw new IllegalStateException("Tracked async script was not cancelled.");
        }

        manager.execute(ref, () -> CompletableFuture.completedFuture(null));
        if (manager.getActiveCount() != 0) {
            throw new IllegalStateException("Completed async script remained tracked.");
        }

        CompletableFuture<?> pending1 = new CompletableFuture<>();
        CompletableFuture<?> pending2 = new CompletableFuture<>();
        manager.track(ref, pending1);
        manager.track(new ScriptRef(ScriptType.KEYBINDING, "execution-smoke-2"), pending2);
        manager.cancelAll();
        if (!pending1.isCancelled() || !pending2.isCancelled() || manager.getActiveCount() != 0) {
            throw new IllegalStateException("Global async script cancellation failed.");
        }
    }

    private static void verifyInitialApiCompatibility() {
        requireCompilationSuccess(ScriptType.KEYBINDING, "esp.toggle();");
        requireCompilationFailure(ScriptType.KEYBINDING, "main.toggleEsp();");

        requireCompilationSuccess(ScriptType.KEYBINDING, """
                float speedFactor = movement.getSpeedMultiplierFactor();
                float jumpFactor = movement.getJumpFactor();
                boolean jumpDown = keys.jump.isDown();
                variables.setFloat("movement-factor-sum", speedFactor + jumpFactor);
                """);
        requireCompilationSuccess(ScriptType.OVERLAY, """
                main.setOverlayHorizontalPosition("left");
                main.addText(convert.toString(tps.get(), 1));
                """);
        requireCompilationSuccess(ScriptType.BLOCK_AUTOMATION, """
                if (currentBlock.getY() >= 0) {
                    blockPlacer.setBlockId(currentBlock.getId());
                }
                """);
        requireCompilationSuccess(ScriptType.VILLAGER_ROLLER, """
                if (villagerRoller.isBestPrice()) {
                    main.systemMessage(villagerRoller.getEnchantmentName());
                }
                """);

        requireCompilationFailure(ScriptType.OVERLAY, "main.chat(\"not-visible\");");
        requireCompilationFailure(ScriptType.BLOCK_AUTOMATION, "main.chat(\"not-visible\");");
        requireCompilationFailure(ScriptType.VILLAGER_ROLLER, "main.chat(\"not-visible\");");
    }

    private static void verifyRuntimeLineNumbers() {
        AsyncRunnable script = getProgram(ScriptCompilerRegistry.INSTANCE.compile(ScriptType.KEYBINDING, """
                int zero = 0;
                int value = 1 / zero;
                """));

        Throwable failure;
        try {
            script.run().join();
            throw new IllegalStateException("Expected key-binding runtime failure.");
        } catch (CompletionException e) {
            failure = e.getCause();
        }

        boolean found = false;
        for (StackTraceElement element : failure.getStackTrace()) {
            if ("<KeyBindingScript>".equals(element.getFileName()) && element.getLineNumber() == 2) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalStateException("Key-binding runtime failure did not retain source line 2.", failure);
        }
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

        CompletableFuture<?> overlayExecution = new CompletableFuture<>();
        ScriptExecutionManager.instance.track(overlayDocument.ref, overlayExecution);
        ScriptSaveResult invalidOverlay = overlay.save(invalidCode);
        requireFailure(invalidOverlay);
        requireCode(overlayDocument, validCode);
        if (overlayExecution.isCancelled()) {
            throw new IllegalStateException("Failed save cancelled the last valid script execution.");
        }
        if (!invalidCode.equals(overlayDocument.lastAttemptCode) || overlayDocument.lastAttemptDiagnostics == null) {
            throw new IllegalStateException("Failed workspace save did not preserve its diagnostics.");
        }
        requireSuccess(overlay.save(validCode + "\n"));
        if (!overlayExecution.isCancelled()) {
            throw new IllegalStateException("Valid script replacement did not cancel its active execution.");
        }
        requireSuccess(overlay.save((String) null));

        MultiScriptSlot keyBindings = (MultiScriptSlot) workspace.get(ScriptType.KEYBINDING);
        requireSuccess(keyBindings.save("smoke", validCode));
        ScriptDocument keyBindingDocument = keyBindings.getInstance("smoke");
        requireCode(keyBindingDocument, validCode);
        requireFailure(keyBindings.save("smoke", invalidCode));
        requireCode(keyBindingDocument, validCode);
        CompletableFuture<?> keyBindingExecution = new CompletableFuture<>();
        ScriptExecutionManager.instance.track(keyBindingDocument.ref, keyBindingExecution);
        keyBindings.remove("smoke");
        if (!keyBindingExecution.isCancelled()) {
            throw new IllegalStateException("Script removal did not cancel its active execution.");
        }
    }

    private static void verifyKeyBindingModule() {
        KeyBindings module = KeyBindings.instance;
        Config config = ConfigStore.instance.getConfig();
        config.keyBindingScriptsConfig.scripts.clear();
        config.keyBindingsConfig.bindings = new String[KeyBindingsConfig.KeysCount];
        module.clear();

        String originalCode = "esp.toggle();";
        if (!module.add("smoke", originalCode, false).isEmpty()) {
            throw new IllegalStateException("Valid key-binding script could not be added.");
        }
        module.assign(3, "smoke");
        if (!"smoke".equals(config.keyBindingsConfig.bindings[3])) {
            throw new IllegalStateException("Key-binding script assignment was not stored.");
        }

        String invalidCode = "int value = ;";
        if (module.add("rejected", invalidCode, false).isEmpty() || module.exists("rejected")) {
            throw new IllegalStateException("Invalid new key-binding script was stored.");
        }
        if (module.add("preserved-invalid", invalidCode, true).isEmpty() || !module.exists("preserved-invalid")) {
            throw new IllegalStateException("Invalid migrated key-binding script was not preserved.");
        }
        module.assign(4, "preserved-invalid");
        if (!"preserved-invalid".equals(config.keyBindingsConfig.bindings[4])) {
            throw new IllegalStateException("Invalid migrated key-binding script lost its assignment.");
        }

        String updatedCode = "freeCam.toggle();";
        if (!module.update("smoke", "renamed", updatedCode).isEmpty()) {
            throw new IllegalStateException("Valid key-binding script update failed.");
        }
        if (!"renamed".equals(config.keyBindingsConfig.bindings[3]) || module.exists("smoke") || !module.exists("renamed")) {
            throw new IllegalStateException("Key-binding script rename did not preserve its assignment.");
        }

        if (module.update("renamed", "renamed", invalidCode).isEmpty()) {
            throw new IllegalStateException("Invalid key-binding script update unexpectedly succeeded.");
        }
        KeyBindings.Script renamed = Objects.requireNonNull(module.get("renamed"));
        ScriptDocument document = ScriptWorkspace.INSTANCE.get(ScriptType.KEYBINDING).getInstance("renamed");
        if (!updatedCode.equals(renamed.code) || !invalidCode.equals(document.lastAttemptCode)) {
            throw new IllegalStateException("Failed key-binding save replaced the last valid source or lost diagnostics.");
        }

        module.remove("renamed");
        if (config.keyBindingsConfig.bindings[3] != null || module.exists("renamed")) {
            throw new IllegalStateException("Removed key-binding script remained assigned.");
        }
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

    private static void requireCompilationSuccess(ScriptType type, String code) {
        CompilationResult result = ScriptCompilerRegistry.INSTANCE.compile(type, code);
        if (result.getProgram() == null) {
            throw new IllegalStateException(type + " API compatibility compilation failed: " + result.getDiagnostics());
        }
    }

    private static void requireCompilationFailure(ScriptType type, String code) {
        CompilationResult result = ScriptCompilerRegistry.INSTANCE.compile(type, code);
        if (result.getProgram() != null || result.getDiagnostics() == null || result.getDiagnostics().isEmpty()) {
            throw new IllegalStateException(type + " API visibility check unexpectedly compiled.");
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
