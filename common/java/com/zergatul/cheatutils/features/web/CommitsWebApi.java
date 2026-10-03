package com.zergatul.cheatutils.features.web;

import com.zergatul.cheatutils.utils.ModMetadata;
import com.zergatul.cheatutils.web.ApiException;
import com.zergatul.cheatutils.web.HttpResponseCodes;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;

public class CommitsWebApi extends WebApiBase {

    public static final CommitsWebApi INSTANCE = new CommitsWebApi();

    private CommitsWebApi() {
        WebApiRegistry.INSTANCE.register(this);
    }

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