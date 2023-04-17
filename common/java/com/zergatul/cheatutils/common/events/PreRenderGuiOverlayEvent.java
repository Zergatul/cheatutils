package com.zergatul.cheatutils.common.events;

public class PreRenderGuiOverlayEvent implements CancelableEvent {

    private GuiOverlayType type;
    private boolean canceled;

    public PreRenderGuiOverlayEvent(GuiOverlayType type) {
        this.type = type;
    }

    public GuiOverlayType getGuiOverlayType() {
        return type;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void cancel() {
        canceled = true;
    }

    public enum GuiOverlayType {
        PLAYER_LIST
    }
}