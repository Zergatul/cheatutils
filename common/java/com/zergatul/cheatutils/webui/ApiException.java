package com.zergatul.cheatutils.webui;

import org.apache.http.HttpException;

public class ApiException extends HttpException {

    private final int code;

    public ApiException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ApiException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}