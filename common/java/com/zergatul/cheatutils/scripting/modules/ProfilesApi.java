package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.scripting.HelpText;
import com.zergatul.cheatutils.scripting.MethodDescription;

import java.util.List;

public class ProfilesApi {

    @MethodDescription("""
            Returns name of currently selected profile
            """)
    public String getCurrent() {
        return Profiles.instance.getCurrent();
    }

    @MethodDescription("""
            Returns true if change was successful
            """)
    public boolean change(String name) {
        if (!Profiles.instance.isValidProfileName(name)) {
            return false;
        }

        boolean exists = name.isEmpty();
        if (!exists) {
            List<String> profiles = Profiles.instance.list();
            exists = profiles.contains(name);
        }

        if (!exists) {
            return false;
        }

        Profiles.instance.change(name);
        return true;
    }
}