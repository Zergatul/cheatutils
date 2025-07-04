package com.zergatul.cheatutils.utils;

public class EntityInteractionResult {

    private final String message;

    private EntityInteractionResult(String message) {
        this.message = message;
    }

    public boolean isFailed() {
        return message != null;
    }

    public boolean isSuccess() {
        return message == null;
    }

    public String getMessage() {
        return message;
    }

    public static EntityInteractionResult success() {
        return new EntityInteractionResult(null);
    }

    public static EntityInteractionResult failed(String message) {
        return new EntityInteractionResult(message);
    }
}