package com.zergatul.cheatutils.features;

import com.zergatul.cheatutils.features.web.*;

public class Features {

    public static void register() {
        register(CommitsWebApi.INSTANCE);
        register(GeneralInformationWebApi.INSTANCE);
        register(FontsWebApi.INSTANCE);
        register(ItemInfoWebApi.INSTANCE);
        register(ModulesStatusWebApi.INSTANCE);
        register(NotificationsWebApi.INSTANCE);
        register(ResetConfigWebApi.INSTANCE);
        register(ScriptsDocsWebApi.INSTANCE);
        register(UserWebApi.INSTANCE);
    }

    private static void register(Object instance) {}
}