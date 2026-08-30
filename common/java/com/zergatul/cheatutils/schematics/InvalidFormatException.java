package com.zergatul.cheatutils.schematics;

public class InvalidFormatException extends Exception {

    public InvalidFormatException(String message) {
        super(message);
    }

    public InvalidFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}