package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.utilities.Profiles;
import org.apache.http.HttpException;

public class ProfilesApi extends ApiBase {

    @Override
    public String getRoute() {
        return "profiles";
    }

    @Override
    public boolean requiresJsonContentType() {
        return true;
    }

    @Override
    public String get(String command) throws ApiException {
        return switch (command) {
            case "current" -> gson.toJson(Profiles.instance.getCurrent());
            case "list" -> gson.toJson(Profiles.instance.list());
            default -> throw new ApiException("Unsupported command: " + command, HttpResponseCodes.BAD_REQUEST);
        };
    }

    @Override
    public String post(String body) throws HttpException {
        Request request = WebHelper.parseJson(gson, body, Request.class);
        String command = WebHelper.requireNonBlankField(request.command, "command");
        String name = WebHelper.requireField(request.name, "name");

        try {
            switch (command) {
                case "change" -> ClientThreadDispatcher.run(() -> Profiles.instance.change(name));
                case "copy" -> ClientThreadDispatcher.run(() -> Profiles.instance.createCopy(name));
                case "new" -> ClientThreadDispatcher.run(() -> Profiles.instance.createNew(name));
                default -> throw new ApiException("Unsupported command: " + command, HttpResponseCodes.BAD_REQUEST);
            }
        } catch (IllegalStateException e) {
            throw new ApiException(e.getMessage(), HttpResponseCodes.BAD_REQUEST, e);
        }

        return "{}";
    }

    @Override
    public String delete(String name) throws HttpException {
        try {
            ClientThreadDispatcher.run(() -> Profiles.instance.delete(name));
        } catch (IllegalStateException e) {
            throw new ApiException(e.getMessage(), HttpResponseCodes.BAD_REQUEST, e);
        }
        return "{}";
    }

    private record Request(String command, String name) {}
}