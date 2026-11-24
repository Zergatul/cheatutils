package com.zergatul.cheatutils.scripting;

/** A script's static initializer threw while the program was being instantiated. The cause is the script's failure. */
public class ScriptInitializationException extends RuntimeException {
    public ScriptInitializationException(Throwable cause) {
        super(cause.toString(), cause);
    }
}