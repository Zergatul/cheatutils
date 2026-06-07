package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.concurrent.TickEndExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class OsApiCurseForgeExcluded {

    public static CompletableFuture<Integer> execute(String path) {
        Process process;
        try {
            process = new ProcessBuilder(path).start();
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }

        return process.onExit().thenApplyAsync(Process::exitValue, TickEndExecutor.instance);
    }

    public static CompletableFuture<Integer> execute(String path, String[] arguments) {
        List<String> list = new ArrayList<>();
        list.add(path);
        list.addAll(Arrays.asList(arguments));

        Process process;
        try {
            process = new ProcessBuilder(list).start();
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }

        return process.onExit().thenApplyAsync(Process::exitValue, TickEndExecutor.instance);
    }
}