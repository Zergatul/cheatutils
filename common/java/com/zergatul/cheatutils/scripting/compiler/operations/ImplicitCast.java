package com.zergatul.cheatutils.scripting.compiler.operations;

import com.zergatul.cheatutils.scripting.compiler.types.SFloatType;
import com.zergatul.cheatutils.scripting.compiler.types.SIntType;
import com.zergatul.cheatutils.scripting.compiler.types.SStringType;
import com.zergatul.cheatutils.scripting.compiler.types.SType;

public class ImplicitCast {
    public static UnaryOperation get(SType source, SType destination) {
        if (source == SIntType.instance && destination == SFloatType.instance) {
            return UnaryOperation.INT_TO_FLOAT;
        }
        if (source == SIntType.instance && destination == SStringType.instance) {
            return UnaryOperation.INT_TO_STRING;
        }
        return null;
    }
}