package com.zergatul.cheatutils.webui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.MethodNotSupportedException;

public abstract class ApiBase {

    protected Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public abstract String getRoute();

    public String get() throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Method not supported");
    }

    public String get(String id) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Method not supported");
    }

    public String put(String id, String body) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Method not supported");
    }

    public String post(String body) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Method not supported");
    }

    public String delete(String id) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Method not supported");
    }
}
