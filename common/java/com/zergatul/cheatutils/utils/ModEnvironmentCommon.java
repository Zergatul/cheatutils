package com.zergatul.cheatutils.utils;

import java.io.IOException;
import java.io.InputStream;

public class ModEnvironmentCommon {

    public static final boolean IS_CURSEFORGE_RESTRICTED = isCurseForgeRestricted();

    private static boolean isCurseForgeRestricted() {
        InputStream stream = ResourceHelper.get("curse-forge-build");
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException _) {}
            return true;
        } else {
            return false;
        }
    }
}