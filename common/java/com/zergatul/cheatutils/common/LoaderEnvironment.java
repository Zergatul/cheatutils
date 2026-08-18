package com.zergatul.cheatutils.common;

import java.util.List;

public interface LoaderEnvironment {
    boolean isProduction();
    String getLoaderName();
    String getLoaderVersion();
    String getModVersion();
    int getModCount();
    boolean hasMod(String id);
    List<String> getModsJars();
}