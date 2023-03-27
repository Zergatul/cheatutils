package com.zergatul.cheatutils.scripting.compiler;

import com.zergatul.cheatutils.ModMain;

public class ScriptingClassLoader extends ClassLoader {

    public ScriptingClassLoader() {
        super(ModMain.class.getClassLoader());
    }

    public Class defineClass(String name, byte[] code) {
        return defineClass(name, code, 0, code.length);
    }
}