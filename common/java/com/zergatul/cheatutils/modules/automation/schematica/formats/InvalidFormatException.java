package com.zergatul.cheatutils.modules.automation.schematica.formats;

public class InvalidFormatException extends Exception {

    public InvalidFormatException(String message) {
        super(message);
    }

    public InvalidFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}