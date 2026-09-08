package com.zergatul.cheatutils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.webui.ScriptApi;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;

import java.io.DataInputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Runs on Java 8 with the release jar and Forge's older ASM on the classpath. */
public class ScriptingChecks {
    public static void main(String[] args) throws Exception {
        verifyJar(args[0]);
        require(Compiler.class.getProtectionDomain().getCodeSource().getLocation().toString().endsWith(".jar"),
                "Checks must load scripting from the packaged jar.");
        Class<?> originalAsm = Class.forName("org.objectweb.asm.Opcodes");
        try {
            originalAsm.getField("ASM9");
            throw new AssertionError("Forge ASM was upgraded.");
        } catch (NoSuchFieldException expected) {
            // Scripting uses Forge's ASM directly.
        }

        Compiler compiler = new Compiler(new CompilationParametersBuilder().setRoot(TestRoot.class).build());
        CompilationResult result = compiler.compile("for (int i = 0; i < 4; i++) { output.add(i); }");
        require(result.getProgram() != null, "Loop script must compile.");
        ((Runnable) result.getProgram()).run();
        require(TestRoot.output.value == 6, "Generated Java 8 bytecode must execute correctly.");

        ScriptCompilerRegistry registry = ScriptCompilerRegistry.INSTANCE;
        CompilationResult api = registry.compile(ScriptType.EXEC_CODE,
                "freeCam.toggle(); freeCam.enable(); freeCam.disable(); ui.systemMessage(\"hello\"); boolean active = freeCam.isEnabled();");
        require(api.getProgram() != null, "Minimal API names must compile: " + api.getDiagnostics());
        CompilationResult invalid = registry.compile(ScriptType.EXEC_CODE, "freeCam.unknown();");
        require(invalid.getProgram() == null && !invalid.getDiagnostics().isEmpty(), "Invalid methods must produce diagnostics.");
        require(invalid.getDiagnostics().get(0).range.getLine1() == 1, "Diagnostics must include source locations.");
        require(registry.compile(ScriptType.EXEC_CODE, "Java<java.lang.String> value;").getProgram() == null,
                "Java interop must remain unavailable in this batch.");

        Runnable fails = registry.compile(ScriptType.EXEC_CODE, "int zero = 0; int value = 1 / zero;").getProgram();
        require(fails != null, "Runtime-failure sample must compile.");
        require(ScriptExecutionManager.instance.execute(ScriptType.EXEC_CODE, fails) instanceof ArithmeticException,
                "Runtime exception must be contained.");
        Runnable succeeds = registry.compile(ScriptType.EXEC_CODE, "int value = 42;").getProgram();
        require(ScriptExecutionManager.instance.execute(ScriptType.EXEC_CODE, succeeds) == null,
                "A failure must not disable subsequent scripts.");

        JsonObject compiled = json(new ScriptApi(false).post("{\"code\":\"int value = 42;\"}"));
        require(compiled.get("success").getAsBoolean() && !compiled.get("executed").getAsBoolean(), "Compile-only must not execute.");
        JsonObject diagnostics = json(new ScriptApi(true).post("{\"code\":\"freeCam.unknown();\"}"));
        require(!diagnostics.get("success").getAsBoolean() && diagnostics.getAsJsonArray("diagnostics").size() > 0,
                "API must return compile diagnostics.");
        JsonObject executed = json(new ScriptApi(true).post("{\"code\":\"int value = 42;\"}"));
        require(executed.get("success").getAsBoolean() && executed.get("executed").getAsBoolean(), "API must execute valid code.");
        verifySavedScripts();
        System.out.println("Packaged scripting checks passed: Java 8 execution, Forge ASM, diagnostics, API and failure recovery.");
    }

    private static JsonObject json(String text) {
        return new JsonParser().parse(text).getAsJsonObject();
    }

    private static void verifySavedScripts() throws Exception {
        com.zergatul.cheatutils.modules.scripting.KeyBindings keys = com.zergatul.cheatutils.modules.scripting.KeyBindings.instance;
        com.zergatul.cheatutils.configs.ConfigStore store = com.zergatul.cheatutils.configs.ConfigStore.instance;
        java.nio.file.Path directory = java.nio.file.Files.createTempDirectory("cheatutils-scripts-");
        java.io.File first = directory.resolve("first.json").toFile();
        java.io.File second = directory.resolve("second.json").toFile();
        store.read(first);
        require(keys.list().size() == 2 && keys.get("Toggle ESP") != null, "Defaults must be installed for existing configs without scripting fields.");
        keys.execute("Toggle ESP");
        require(keys.getError("Toggle ESP") == null, "ESP placeholder must execute without effects or failure.");
        com.zergatul.cheatutils.webui.KeyBindingScriptsApi api = new com.zergatul.cheatutils.webui.KeyBindingScriptsApi();
        require(json(api.post("{\"name\":\"test\",\"code\":\"esp.toggle();\"}")).get("ok").getAsBoolean(), "Create API must save compiled code.");
        keys.assign(4, "test");
        require(!json(api.put("test", "{\"name\":\"renamed\",\"code\":\"esp.missing();\"}")).get("ok").getAsBoolean(), "Invalid save must return diagnostics.");
        require(keys.get("test") != null && "test".equals(store.getConfig().keyBindingsConfig.bindings[4]), "Invalid rename must leave script and binding intact.");
        require(json(api.put("test", "{\"name\":\"renamed\",\"code\":\"esp.toggle();\"}")).get("ok").getAsBoolean(), "Rename must succeed.");
        require("renamed".equals(store.getConfig().keyBindingsConfig.bindings[4]), "Rename must preserve binding.");
        keys.assign(4, "Toggle ESP");
        require(json(api.get("renamed")).get("key").getAsInt() == -1, "Assignment must replace slot occupant.");
        try {
            keys.assign(30, "renamed");
            throw new AssertionError("Out-of-range assignment accepted.");
        } catch (IllegalArgumentException expected) { }
        keys.assign(5, "renamed");
        api.delete("renamed");
        require(store.getConfig().keyBindingsConfig.bindings[5] == null, "Delete must clear assignment.");
        keys.add("fails", "int zero = 0; int value = 1 / zero;");
        keys.execute("fails");
        require(keys.getError("fails") != null, "Runtime failure must disable the script.");
        keys.update("fails", "fails", "esp.toggle();");
        require(keys.getError("fails") == null, "Successful save must re-enable failed script.");
        store.getWriteToFileTask().run();
        store.createNew(second);
        require(keys.get("fails") == null, "New profile must not retain old scripts.");
        store.read(first);
        require(keys.get("fails") != null && "Toggle ESP".equals(store.getConfig().keyBindingsConfig.bindings[4]), "Profile reload must restore saved scripts and assignments.");
        keys.remove("Toggle ESP");
        keys.remove("Toggle FreeCam");
        keys.remove("fails");
        store.getWriteToFileTask().run();
        store.read(first);
        require(keys.list().isEmpty(), "Explicitly empty script list must remain empty after reload.");
        com.zergatul.cheatutils.configs.ConfigWriterQueue.instance.clear();
        java.nio.file.Files.deleteIfExists(first.toPath());
        java.nio.file.Files.deleteIfExists(second.toPath());
        java.nio.file.Files.delete(directory);
        System.out.println("Saved script checks passed: defaults, CRUD, assignments, recovery and profile reload.");
    }

    private static void verifyJar(String filename) throws Exception {
        try (ZipFile jar = new ZipFile(filename)) {
            Enumeration<? extends ZipEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                require(!entry.getName().startsWith("org/objectweb/asm/"), "ASM must be supplied by Forge.");
                require(!entry.getName().startsWith("com/zergatul/cheatutils/scripting/asm/"), "Relocated ASM must not be packaged.");
                require(!entry.getName().startsWith("org/jspecify/"), "Annotation stubs must not be packaged.");
                if (entry.getName().endsWith(".class")) {
                    try (DataInputStream stream = new DataInputStream(jar.getInputStream(entry))) {
                        require(stream.readInt() == 0xCAFEBABE, "Invalid class file.");
                        stream.readUnsignedShort();
                        require(stream.readUnsignedShort() <= 52, "Class is newer than Java 8: " + entry.getName());
                    }
                }
            }
            require(jar.getEntry("META-INF/licenses/java-scripting-language.txt") != null, "Scripting license must be packaged.");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static class TestRoot {
        public static final Output output = new Output();
    }

    public static class Output {
        int value;
        public void add(int value) { this.value += value; }
    }
}
