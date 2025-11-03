package com.zergatul.cheatutils.webui;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CommitsApi extends ApiBase {

    @Override
    public String getRoute() {
        return "commits";
    }

    @Override
    public String get() throws Throwable {
        InputStream stream = CommitsApi.class.getClassLoader().getResourceAsStream("commits.json");
        try (stream) {
            if (stream == null) {
                return "{}";
            }

            byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(stream);
            return new String(bytes, StandardCharsets.US_ASCII);
        } catch (Exception ex) {
            throw new ApiException("Cannot load commits.json", HttpResponseCodes.INTERNAL_SERVER_ERROR);
        }
    }
}