package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.scripting.api.HelpText;

public class TimeApi {

    private final long start = System.nanoTime();

    @HelpText("Returns seconds elapsed from some static point in the past.")
    public double getSeconds() {
        return (System.nanoTime() - start) / 1e9;
    }
}