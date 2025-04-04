package com.zergatul.cheatutils.schematics.extensions;

public class SectionCompilerExtension {
    public static ThreadLocal<SectionCompileInfo> COMPILE_INFO = ThreadLocal.withInitial(SectionCompileInfo::new);
}