package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.ClassUtils;

public class ClassNameApi extends ApiBase {

    @Override
    public String getRoute() {
        return "class-name";
    }

    @Override
    public String get(String className) throws ApiException {
        if (className == null) {
            throw new ApiException("Class not found", HttpResponseCodes.NOT_FOUND);
        }

        try {
            ClassUtils.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ApiException("Class not found", HttpResponseCodes.NOT_FOUND);
        }

        return "{ \"ok\": true }";
    }
}