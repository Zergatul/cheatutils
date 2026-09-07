package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.utilities.Profiles;

public class ProfilesApi extends ApiBase {
    @Override
    public String getRoute() {
        return "profiles";
    }

    @Override
    public String get(String command) throws ApiException {
        switch (command) {
            case "current": return gson.toJson(Profiles.instance.getCurrent());
            case "list": return gson.toJson(Profiles.instance.list());
            default: throw new ApiException("Unsupported command.", 400);
        }
    }

    @Override
    public String post(String body) throws ApiException {
        Request request = gson.fromJson(body, Request.class);
        if (request == null || request.command == null) {
            throw new ApiException("Command is required.", 400);
        }
        switch (request.command) {
            case "change": Profiles.instance.change(request.name); break;
            case "copy": Profiles.instance.createCopy(request.name); break;
            case "new": Profiles.instance.createNew(request.name); break;
            default: throw new ApiException("Unsupported command.", 400);
        }
        return "{}";
    }

    @Override
    public String delete(String name) {
        Profiles.instance.delete(name);
        return "{}";
    }

    public static class Request {
        public String command;
        public String name;
    }
}
