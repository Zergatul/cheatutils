package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.scripting.MethodDescription;

import java.util.List;

public class ProfilesApi {

    @MethodDescription("""
            Returns name of currently selected profile
            """)
    public String getCurrent() {
        return Profiles.INSTANCE.getCurrent();
    }

    @MethodDescription("""
            Returns true if change was successful
            """)
    public boolean change(String name) {
        if (!Profiles.INSTANCE.isValidProfileName(name)) {
            return false;
        }

        boolean exists = name.isEmpty();
        if (!exists) {
            List<String> profiles = Profiles.INSTANCE.list();
            exists = profiles.contains(name);
        }

        if (!exists) {
            return false;
        }

        Profiles.INSTANCE.change(name);
        return true;
    }
}