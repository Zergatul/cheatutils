package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.configs.Config;
import com.zergatul.cheatutils.configs.ConfigMigrationSmokeTest;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.KeyBindingsConfig;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;
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
        verifyAdvancedScriptingToggle();
        verifyRuntimeLineNumbers();
        verifyExecutionLifecycle();
        verifyWorkspace();
        verifyEventsModule();
        ConfigMigrationSmokeTest.verifyKeyBindingScripts();
        ConfigMigrationSmokeTest.verifyStatusOverlay();
        ConfigMigrationSmokeTest.verifyBlockAutomation();
        ConfigMigrationSmokeTest.verifyVillagerRoller();
        ConfigMigrationSmokeTest.verifyEventsScripting();
        ConfigMigrationSmokeTest.verifyAutoDisconnectRemoved();
        ConfigMigrationSmokeTest.verifyCoreConfig();
        ConfigMigrationSmokeTest.verifyAutoAttack();
        ConfigMigrationSmokeTest.verifyAutoBucket();
        ConfigMigrationSmokeTest.verifyBlockEspGroups();

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
                main.addText("TPS: " + tps.get().toStandardString(1));
                float tpsVal = tps.get();
                main.addText("#CDC1FF", "TPS:", "#00FF21", tpsVal.toStandardString(2));
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
        requireCompilationSuccess(ScriptType.EVENTS, """
                events.onHandleKeys(() => {
                    if (input.isKeyDown("C")) {
                        zoom.start(10, 0.2);
                    } else {
                        zoom.stop();
                    }
                });
                events.onTickEnd(() => {
                    if (blink.isEnabled() && blink.getDistance() > 10) {
                        blink.disable();
                    }
                });
                events.onMenuTickEnd(() => {});
                """);
        requireCompilationSuccess(ScriptType.EVENTS, """
                events.onTickEnd(() => {
                    if (player.getHealth() < 10) {
                        player.disconnect("", "Low HP");
                    }
                });
                """);
        requireCompilationSuccess(ScriptType.KEYBINDING, "player.disconnect(\"self-attack\");");

        requireCompilationFailure(ScriptType.OVERLAY, "main.chat(\"not-visible\");");
        requireCompilationFailure(ScriptType.BLOCK_AUTOMATION, "main.chat(\"not-visible\");");
        requireCompilationFailure(ScriptType.VILLAGER_ROLLER, "main.chat(\"not-visible\");");
        requireCompilationFailure(ScriptType.OVERLAY, "events.onTickEnd(() => {});");
        requireCompilationFailure(ScriptType.OVERLAY, "player.disconnect(\"\");");
        requireCompilationFailure(ScriptType.EVENTS, "autoDisconnect.toggle();");
    }

    private static void verifyAdvancedScriptingToggle() {
        boolean previous = ConfigStore.instance.getConfig().coreConfig.advancedScripting;
        String javaInterop = "typealias JString = Java<java.lang.String>;";
        try {
            ConfigStore.instance.getConfig().coreConfig.advancedScripting = false;
            requireCompilationFailure(ScriptType.KEYBINDING, javaInterop);

            ConfigStore.instance.getConfig().coreConfig.advancedScripting = true;
            requireCompilationSuccess(ScriptType.KEYBINDING, javaInterop);
        } finally {
            ConfigStore.instance.getConfig().coreConfig.advancedScripting = previous;
        }
    }

    private static void verifyRuntimeLineNumbers() {
        AsyncRunnable keyBindingScript = getProgram(ScriptCompilerRegistry.INSTANCE.compile(ScriptType.KEYBINDING, """
                int zero = 0;
                int value = 1 / zero;
                """));

        Throwable failure;
        try {
            keyBindingScript.run().join();
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

        Runnable overlayScript = getProgram(ScriptCompilerRegistry.INSTANCE.compile(ScriptType.OVERLAY, """
                int zero = 0;
                int value = 1 / zero;
                """));
        try {
            overlayScript.run();
            throw new IllegalStateException("Expected Status Overlay runtime failure.");
        } catch (ArithmeticException e) {
            found = false;
            for (StackTraceElement element : e.getStackTrace()) {
                if ("<StatusOverlayScript>".equals(element.getFileName()) && element.getLineNumber() == 2) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalStateException("Status Overlay runtime failure did not retain source line 2.", e);
            }
        }

        Runnable blockAutomationScript = getProgram(ScriptCompilerRegistry.INSTANCE.compile(ScriptType.BLOCK_AUTOMATION, """
                int zero = 0;
                int value = 1 / zero;
                """));
        try {
            blockAutomationScript.run();
            throw new IllegalStateException("Expected Block Automation runtime failure.");
        } catch (ArithmeticException e) {
            found = false;
            for (StackTraceElement element : e.getStackTrace()) {
                if ("<BlockAutomationScript>".equals(element.getFileName()) && element.getLineNumber() == 2) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalStateException("Block Automation runtime failure did not retain source line 2.", e);
            }
        }

        Runnable villagerRollerScript = getProgram(ScriptCompilerRegistry.INSTANCE.compile(ScriptType.VILLAGER_ROLLER, """
                int zero = 0;
                int value = 1 / zero;
                """));
        try {
            villagerRollerScript.run();
            throw new IllegalStateException("Expected Villager Roller runtime failure.");
        } catch (ArithmeticException e) {
            found = false;
            for (StackTraceElement element : e.getStackTrace()) {
                if ("<VillagerRollerScript>".equals(element.getFileName()) && element.getLineNumber() == 2) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalStateException("Villager Roller runtime failure did not retain source line 2.", e);
            }
        }

        Runnable eventsScript = getProgram(ScriptCompilerRegistry.INSTANCE.compile(ScriptType.EVENTS, """
                int zero = 0;
                int value = 1 / zero;
                """));
        try {
            eventsScript.run();
            throw new IllegalStateException("Expected Events Scripting runtime failure.");
        } catch (ArithmeticException e) {
            found = false;
            for (StackTraceElement element : e.getStackTrace()) {
                if ("<EventsScripting>".equals(element.getFileName()) && element.getLineNumber() == 2) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalStateException("Events Scripting runtime failure did not retain source line 2.", e);
            }
        }
    }

    private static void verifyWorkspace() {
        ScriptWorkspace workspace = ScriptWorkspace.INSTANCE;
        if (!workspace.getSupportedTypes().equals(List.of(
                ScriptType.KEYBINDING,
                ScriptType.OVERLAY,
                ScriptType.BLOCK_AUTOMATION,
                ScriptType.VILLAGER_ROLLER,
                ScriptType.EVENTS))) {
            throw new IllegalStateException("Unexpected initial scripting workspace types.");
        }

        String validCode = "int value = 1;";
        String invalidCode = "int value = ;";

        ScriptSlot overlay = workspace.get(ScriptType.OVERLAY);
        requireSuccess(overlay.save(validCode));
        ScriptDocument overlayDocument = overlay.getInstance(null);
        requireCode(overlayDocument, validCode);
        if (!validCode.equals(ConfigStore.instance.getConfig().statusOverlayConfig.code)) {
            throw new IllegalStateException("Valid Status Overlay source was not stored in config.");
        }

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
        if (ConfigStore.instance.getConfig().statusOverlayConfig.code != null || overlayDocument.code != null) {
            throw new IllegalStateException("Cleared Status Overlay source remained in config or workspace.");
        }

        ConfigStore.instance.getConfig().statusOverlayConfig.code = invalidCode;
        requireFailure(overlay.init(invalidCode));
        requireCode(overlayDocument, invalidCode);
        if (!invalidCode.equals(ConfigStore.instance.getConfig().statusOverlayConfig.code)) {
            throw new IllegalStateException("Invalid Status Overlay source was not preserved during reload.");
        }
        if (overlayDocument.lastAttemptCode != null || overlayDocument.lastAttemptDiagnostics != null) {
            throw new IllegalStateException("Config reload was recorded as an interactive save attempt.");
        }
        ConfigStore.instance.getConfig().statusOverlayConfig.code = validCode;
        requireSuccess(overlay.init(validCode));
        requireCode(overlayDocument, validCode);

        ScriptSlot blockAutomation = workspace.get(ScriptType.BLOCK_AUTOMATION);
        requireSuccess(blockAutomation.save(validCode));
        ScriptDocument blockDocument = blockAutomation.getInstance(null);
        requireCode(blockDocument, validCode);
        if (!validCode.equals(ConfigStore.instance.getConfig().blockAutomationConfig.code)) {
            throw new IllegalStateException("Valid Block Automation source was not stored in config.");
        }

        ScriptSaveResult invalidBlock = blockAutomation.save(invalidCode);
        requireFailure(invalidBlock);
        requireCode(blockDocument, validCode);
        if (!invalidCode.equals(blockDocument.lastAttemptCode) || blockDocument.lastAttemptDiagnostics == null) {
            throw new IllegalStateException("Failed Block Automation save did not preserve its diagnostics.");
        }

        requireSuccess(blockAutomation.save((String) null));
        if (ConfigStore.instance.getConfig().blockAutomationConfig.code != null || blockDocument.code != null) {
            throw new IllegalStateException("Cleared Block Automation source remained in config or workspace.");
        }

        ConfigStore.instance.getConfig().blockAutomationConfig.code = invalidCode;
        requireFailure(blockAutomation.init(invalidCode));
        requireCode(blockDocument, invalidCode);
        if (!invalidCode.equals(ConfigStore.instance.getConfig().blockAutomationConfig.code)) {
            throw new IllegalStateException("Invalid Block Automation source was not preserved during reload.");
        }
        ConfigStore.instance.getConfig().blockAutomationConfig.code = validCode;
        requireSuccess(blockAutomation.init(validCode));
        requireCode(blockDocument, validCode);

        ScriptSlot villagerRoller = workspace.get(ScriptType.VILLAGER_ROLLER);
        requireSuccess(villagerRoller.save(validCode));
        ScriptDocument villagerDocument = villagerRoller.getInstance(null);
        requireCode(villagerDocument, validCode);
        if (!validCode.equals(ConfigStore.instance.getConfig().villagerRollerConfig.code)) {
            throw new IllegalStateException("Valid Villager Roller source was not stored in config.");
        }

        ScriptSaveResult invalidVillager = villagerRoller.save(invalidCode);
        requireFailure(invalidVillager);
        requireCode(villagerDocument, validCode);
        if (!invalidCode.equals(villagerDocument.lastAttemptCode) || villagerDocument.lastAttemptDiagnostics == null) {
            throw new IllegalStateException("Failed Villager Roller save did not preserve its diagnostics.");
        }

        requireSuccess(villagerRoller.save((String) null));
        if (ConfigStore.instance.getConfig().villagerRollerConfig.code != null || villagerDocument.code != null) {
            throw new IllegalStateException("Cleared Villager Roller source remained in config or workspace.");
        }

        ConfigStore.instance.getConfig().villagerRollerConfig.code = invalidCode;
        requireFailure(villagerRoller.init(invalidCode));
        requireCode(villagerDocument, invalidCode);
        if (!invalidCode.equals(ConfigStore.instance.getConfig().villagerRollerConfig.code)) {
            throw new IllegalStateException("Invalid Villager Roller source was not preserved during reload.");
        }
        ConfigStore.instance.getConfig().villagerRollerConfig.code = validCode;
        requireSuccess(villagerRoller.init(validCode));
        requireCode(villagerDocument, validCode);

        ScriptSlot events = workspace.get(ScriptType.EVENTS);
        String validEventsCode = "int eventsValue = 1;";
        requireSuccess(events.save(validEventsCode));
        ScriptDocument eventsDocument = events.getInstance(null);
        requireCode(eventsDocument, validEventsCode);
        if (!validEventsCode.equals(ConfigStore.instance.getConfig().eventsScriptingConfig.code)) {
            throw new IllegalStateException("Valid Events Scripting source was not stored in config.");
        }

        ScriptSaveResult invalidEvents = events.save(invalidCode);
        requireFailure(invalidEvents);
        requireCode(eventsDocument, validEventsCode);
        if (!invalidCode.equals(eventsDocument.lastAttemptCode) || eventsDocument.lastAttemptDiagnostics == null) {
            throw new IllegalStateException("Failed Events Scripting save did not preserve its diagnostics.");
        }

        requireSuccess(events.save((String) null));
        if (ConfigStore.instance.getConfig().eventsScriptingConfig.code != null || eventsDocument.code != null) {
            throw new IllegalStateException("Cleared Events Scripting source remained in config or workspace.");
        }

        ConfigStore.instance.getConfig().eventsScriptingConfig.code = invalidCode;
        requireFailure(events.init(invalidCode));
        requireCode(eventsDocument, invalidCode);
        if (!invalidCode.equals(ConfigStore.instance.getConfig().eventsScriptingConfig.code)) {
            throw new IllegalStateException("Invalid Events Scripting source was not preserved during reload.");
        }
        ConfigStore.instance.getConfig().eventsScriptingConfig.code = validEventsCode;
        requireSuccess(events.init(validEventsCode));
        requireCode(eventsDocument, validEventsCode);

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

    private static void verifyEventsModule() {
        EventsScripting module = EventsScripting.instance;
        module.clear();
        Events.ClientTickEnd.trigger();

        ConfigStore.instance.getConfig().eventsScriptingConfig.enabled = true;
        int[] counter = new int[1];
        Runnable registration = () -> module.addOnMenuTickEnd(() -> counter[0]++);

        module.setScript(registration);
        Events.ClientTickEnd.trigger();
        requireEventsCounter(counter[0], 1);

        module.setScript(registration);
        Events.ClientTickEnd.trigger();
        requireEventsCounter(counter[0], 2);
        Events.ClientTickEnd.trigger();
        requireEventsCounter(counter[0], 3);

        module.clear();
        Events.ClientTickEnd.trigger();
        requireEventsCounter(counter[0], 3);

        Runnable failingCallback = getProgram(ScriptCompilerRegistry.INSTANCE.compile(ScriptType.EVENTS, """
                int zero = 0;
                int value = 1 / zero;
                """));
        module.setScript(() -> module.addOnMenuTickEnd(failingCallback));
        try {
            Events.ClientTickEnd.trigger();
            throw new IllegalStateException("Expected Events Scripting callback failure.");
        } catch (ArithmeticException e) {
            boolean found = false;
            for (StackTraceElement element : e.getStackTrace()) {
                if ("<EventsScripting>".equals(element.getFileName()) && element.getLineNumber() == 2) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalStateException("Events Scripting callback failure did not retain source line 2.", e);
            }
        }

        module.clear();
        Events.ClientTickEnd.trigger();
        ConfigStore.instance.getConfig().eventsScriptingConfig.enabled = false;
    }

    private static void requireEventsCounter(int actual, int expected) {
        if (actual != expected) {
            throw new IllegalStateException(
                    "Events Scripting callback was registered an unexpected number of times. Expected " +
                            expected + ", actual " + actual + ".");
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
