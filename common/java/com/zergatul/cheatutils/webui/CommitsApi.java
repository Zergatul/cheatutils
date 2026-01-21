package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.ModMetadata;

public class CommitsApi extends ApiBase {

    @Override
    public String getRoute() {
        return "commits";
    }

    @Override
    public String get() throws Throwable {
        try {
            return gson.toJson(ModMetadata.getCommits());
        } catch (Exception ex) {
            throw new ApiException("Cannot load commits.json", HttpResponseCodes.INTERNAL_SERVER_ERROR);
        }
    }
}