package com.zergatul.cheatutils.webui;

import com.google.gson.Gson;
import com.zergatul.cheatutils.configs.ConfigStore;
import org.apache.http.HttpException;

public abstract class ApiBase {

    protected Gson gson = ConfigStore.instance.gson;

    public abstract String getRoute();

    public boolean requiresJsonContentType() {
        return false;
    }

    public String get() throws HttpException {
        throw methodNotAllowed();
    }

    public String get(String id) throws HttpException {
        throw methodNotAllowed();
    }

    public String put(String id, String body) throws HttpException {
        throw methodNotAllowed();
    }

    public String post(String body) throws HttpException {
        throw methodNotAllowed();
    }

    public String delete(String id) throws HttpException {
        throw methodNotAllowed();
    }

    private ApiException methodNotAllowed() {
        return new ApiException("Method not allowed", HttpResponseCodes.METHOD_NOT_ALLOWED);
    }
}