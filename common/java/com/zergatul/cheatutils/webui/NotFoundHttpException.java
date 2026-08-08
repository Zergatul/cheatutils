package com.zergatul.cheatutils.webui;

public class NotFoundHttpException extends ApiException {

    public NotFoundHttpException() {
        this("Not found");
    }

    public NotFoundHttpException(String message) {
        super(message, HttpResponseCodes.NOT_FOUND);
    }
}