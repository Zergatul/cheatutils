package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.scripting.ApiDocsGenerator;

import java.io.IOException;

public class ApiGenHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                throw new ApiException("Method not allowed", HttpResponseCodes.METHOD_NOT_ALLOWED);
            }

            WebHelper.sendText(exchange, HttpResponseCodes.OK, String.join("\n", ApiDocsGenerator.generateLines()) + "\n");
        } catch (ApiException e) {
            WebHelper.sendException(exchange, e.getCode(), e);
        } catch (Throwable e) {
            WebHelper.sendException(exchange, HttpResponseCodes.INTERNAL_SERVER_ERROR, e);
        }
    }
}