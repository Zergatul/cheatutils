package com.zergatul.cheatutils.scripting.workspace;

/**
 * The save was not applied at all: the profile changed while saving, the script target no longer exists,
 * or the script's static initializer failed.
 */
public class ScriptSaveRejectedException extends RuntimeException {
    public ScriptSaveRejectedException(String message) {
        super(message);
    }

    public ScriptSaveRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
