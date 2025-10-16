package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.ClassUtils;
import com.zergatul.cheatutils.wrappers.ClassRemapper;
import net.minecraft.client.Minecraft;
import org.apache.http.HttpException;

public class ClassNameApi extends ApiBase {

    @Override
    public String getRoute() {
        return "class-name";
    }

    @Override
    public String get(String className) throws HttpException {
        className = ClassRemapper.toObf(className);
        if (className == null) {
            throw new NotFoundHttpException("Class not found");
        }

        try {
            ClassUtils.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new NotFoundHttpException("Class not found");
        }

        return "{ \"ok\": true }";
    }
}