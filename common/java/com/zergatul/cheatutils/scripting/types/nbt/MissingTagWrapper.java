package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.scripting.type.CustomType;

@CustomType(name = "MissingTag")
public class MissingTagWrapper extends TagWrapper {

    public static final TagWrapper INSTANCE = new MissingTagWrapper();

    private MissingTagWrapper() {}
}