package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.ModMetadata;
import org.apache.http.HttpException;

public class CommitsApi extends ApiBase {

    @Override
    public String getRoute() {
        return "commits";
    }

    @Override
    public String get() throws HttpException {
        try {
            return gson.toJson(ModMetadata.getCommits());
        } catch (Exception e) {
            throw new ApiException("Cannot load commits.json", HttpResponseCodes.INTERNAL_SERVER_ERROR, e);
        }
    }
}