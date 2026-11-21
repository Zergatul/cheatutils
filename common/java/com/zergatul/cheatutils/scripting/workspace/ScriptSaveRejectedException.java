package com.zergatul.cheatutils.scripting.workspace;

/** The save was not applied at all: the profile changed while saving, or the script target no longer exists. */
public class ScriptSaveRejectedException extends RuntimeException {
    public ScriptSaveRejectedException(String message) {
        super(message);
    }
}
