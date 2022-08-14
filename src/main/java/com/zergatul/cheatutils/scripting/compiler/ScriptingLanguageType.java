package com.zergatul.cheatutils.scripting.compiler;

public enum ScriptingLanguageType {
    VOID(null),
    NULL(Object.class),
    INT(int.class),
    DOUBLE(double.class),
    BOOLEAN(boolean.class),
    STRING(String.class);

    private final Class type;

    ScriptingLanguageType(Class type) {
        this.type = type;
    }

    public Class getJavaClass() {
        return type;
    }

    public static ScriptingLanguageType fromJavaClass(Class type) throws ScriptCompileException {
        if (type == void.class) {
            return VOID;
        }
        if (type == int.class) {
            return INT;
        }
        if (type == double.class) {
            return DOUBLE;
        }
        if (type == boolean.class) {
            return BOOLEAN;
        }
        if (type == String.class) {
            return STRING;
        }
        throw new ScriptCompileException();
    }
}