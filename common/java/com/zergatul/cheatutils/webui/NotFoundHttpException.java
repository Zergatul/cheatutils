package com.zergatul.cheatutils.webui;

import org.apache.http.HttpException;

public class NotFoundHttpException extends HttpException {

    public NotFoundHttpException() {
        super();
    }

    public NotFoundHttpException(String message) {
        super(message);
    }
}