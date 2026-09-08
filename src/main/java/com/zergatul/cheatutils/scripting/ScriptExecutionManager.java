package com.zergatul.cheatutils.scripting;

import org.apache.logging.log4j.LogManager;

/** Synchronous one-shot execution; callers execute on the Minecraft client thread. */
public class ScriptExecutionManager {
    public static final ScriptExecutionManager instance = new ScriptExecutionManager();

    /** Returns the failure, or null on success. A failure does not affect later executions. */
    public Throwable execute(ScriptType type, Runnable program) {
        try {
            program.run();
            return null;
        } catch (VirtualMachineError | ThreadDeath fatal) {
            throw fatal;
        } catch (Throwable failure) {
            LogManager.getLogger(ScriptExecutionManager.class).error("{} script failed", type, failure);
            return failure;
        }
    }
}
