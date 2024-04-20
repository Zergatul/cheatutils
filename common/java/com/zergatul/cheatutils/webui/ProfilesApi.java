package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.utilities.Profiles;
import org.apache.http.HttpException;
import org.apache.http.MethodNotSupportedException;

public class ProfilesApi extends ApiBase {

    @Override
    public String getRoute() {
        return "profiles";
    }

    @Override
    public String get(String command) throws HttpException {
        return switch (command) {
            case "current" -> gson.toJson(Profiles.instance.getCurrent());
            case "list" -> gson.toJson(Profiles.instance.list());
            default -> throw new MethodNotSupportedException("Unsupported command.");
        };
    }

    @Override
    public String post(String body) throws HttpException {
        Request request = gson.fromJson(body, Request.class);
        switch (request.command) {
            case "change": Profiles.instance.change(request.name); break;
            case "copy": Profiles.instance.createCopy(request.name); break;
            case "new": Profiles.instance.createNew(request.name); break;
            default: throw new MethodNotSupportedException("Unsupported command.");
        }

        return "{}";
    }

    @Override
    public String delete(String name) throws HttpException {
        Profiles.instance.delete(name);
        return "{}";
    }

    public record Request(String command, String name) {}
}
