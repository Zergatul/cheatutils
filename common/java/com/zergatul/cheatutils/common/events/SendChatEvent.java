package com.zergatul.cheatutils.common.events;

public class SendChatEvent implements CancelableEvent {

    private final String message;
    private boolean canceled;

    public SendChatEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }
}