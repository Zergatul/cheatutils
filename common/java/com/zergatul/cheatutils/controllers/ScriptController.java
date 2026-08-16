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
    private ScriptController() {

    }

    public Runnable compileKeys(String code) throws ParseException, ScriptCompileException {
        return handleKeybindingsCompiler.compile(code);
    }

}