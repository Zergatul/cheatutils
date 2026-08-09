package com.zergatul.cheatutils.webui;

public class InternalServerErrorException extends ApiException {
    public InternalServerErrorException(final String message) {
        super(message, HttpResponseCodes.INTERNAL_SERVER_ERROR);
    }
}