package com.zergatul.cheatutils.font;

public class CannotLoadFontException extends RuntimeException {

    public CannotLoadFontException(String message) {
        super(message);
    }

    public CannotLoadFontException(Throwable cause) {
        super(cause);
    }
}