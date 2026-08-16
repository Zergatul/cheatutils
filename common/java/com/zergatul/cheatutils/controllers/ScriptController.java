package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.scripting.api.Root;
import com.zergatul.cheatutils.scripting.api.VisibilityCheck;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import com.zergatul.cheatutils.scripting.compiler.ScriptingLanguageCompiler;
import com.zergatul.cheatutils.scripting.generated.ParseException;

public class ScriptController {

    public static final ScriptController instance = new ScriptController();

    private final ScriptingLanguageCompiler handleKeybindingsCompiler = new ScriptingLanguageCompiler(
            Root.class,
            VisibilityCheck.getTypes("handle-keybindings"));
    private final ScriptingLanguageCompiler autoDisconnectCompiler = new ScriptingLanguageCompiler(
            Root.class,
            VisibilityCheck.getTypes("auto-disconnect"));
    private final ScriptingLanguageCompiler villagerRollerCompiler = new ScriptingLanguageCompiler(
            Root.class,
            VisibilityCheck.getTypes("villager-roller"));
    private ScriptController() {

    }

    public Runnable compileKeys(String code) throws ParseException, ScriptCompileException {
        return handleKeybindingsCompiler.compile(code);
    }

    public Runnable compileAutoDisconnect(String code) throws ParseException, ScriptCompileException {
        return autoDisconnectCompiler.compile(code);
    }

    public Runnable compileVillagerRoller(String code) throws ParseException, ScriptCompileException {
        return villagerRollerCompiler.compile(code);
    }
}