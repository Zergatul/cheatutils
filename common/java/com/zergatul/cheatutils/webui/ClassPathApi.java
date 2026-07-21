package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.ClassPathExplorer;

public class ClassPathApi extends ApiBase {

    @Override
    public String getRoute() {
        return "class-path";
    }

    @Override
    public String get() throws Throwable {
        return String.join("\n", ClassPathExplorer.INSTANCE.getClasses());
    }
}
