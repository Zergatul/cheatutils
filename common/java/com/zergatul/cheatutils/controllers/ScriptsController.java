package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;

public class ScriptsController {

    public static final ScriptsController instance = new ScriptsController();

    private final Compiler handleKeybindingsCompiler = new Compiler(ScriptType.KEYBINDING.createParameters());
    private final Compiler overlayCompiler = new Compiler(ScriptType.OVERLAY.createParameters());
    private final Compiler blockAutomationCompiler = new Compiler(ScriptType.BLOCK_AUTOMATION.createParameters());
    private final Compiler villagerRollerCompiler = new Compiler(ScriptType.VILLAGER_ROLLER.createParameters());
    private final Compiler eventsCompiler = new Compiler(ScriptType.EVENTS.createParameters());
    private final Compiler blockEspCompiler = new Compiler(ScriptType.BLOCK_ESP.createParameters());
    private final Compiler entityEspCompiler = new Compiler(ScriptType.ENTITY_ESP.createParameters());
    private final Compiler killAuraCompiler = new Compiler(ScriptType.KILL_AURA.createParameters());
    private final Compiler hitboxSizeCompiler = new Compiler(ScriptType.HITBOX_SIZE.createParameters());

    private ScriptsController() {

    }

    public CompilationResult compileOverlay(String code) {
        return overlayCompiler.compile(code);
    }

    public CompilationResult compileKeys(String code) {
        return handleKeybindingsCompiler.compile(code);
    }

    public CompilationResult compileBlockAutomation(String code) {
        return blockAutomationCompiler.compile(code);
    }

    public CompilationResult compileVillagerRoller(String code) {
        return villagerRollerCompiler.compile(code);
    }

    public CompilationResult compileEvents(String code) {
        return eventsCompiler.compile(code);
    }

    public CompilationResult compileBlockEsp(String code) {
        return blockEspCompiler.compile(code);
    }

    public CompilationResult compileEntityEsp(String code) {
        return entityEspCompiler.compile(code);
    }

    public CompilationResult compileKillAura(String code) {
        return killAuraCompiler.compile(code);
    }

    public CompilationResult compileHitboxSize(String code) {
        return hitboxSizeCompiler.compile(code);
    }
}