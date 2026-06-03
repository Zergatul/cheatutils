package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.scripting.AdvancedApi;
import com.zergatul.cheatutils.scripting.CurseForgeRestricted;
import com.zergatul.scripting.MethodDescription;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@AdvancedApi
public class OsApi {

    public final FilesApi files = new FilesApi();

    @CurseForgeRestricted
    @MethodDescription("""
            Starts external program and returns exit code
            """)
    public CompletableFuture<Integer> execute(String path) {
        return CurseForgeExcluded.execute(path);
    }

    @CurseForgeRestricted
    @MethodDescription("""
            Starts external program and returns exit code
            """)
    public CompletableFuture<Integer> execute(String path, String[] arguments) {
        return CurseForgeExcluded.execute(path, arguments);
    }

    @SuppressWarnings("unused")
    @AdvancedApi
    public static class FilesApi {

        public String[] readAllLines(String path) {
            try {
                return Files.readAllLines(Path.of(path)).toArray(String[]::new);
            } catch (IOException e) {
                return new String[0];
            }
        }

        public String readAllText(String path) {
            try {
                return Files.readString(Path.of(path));
            } catch (IOException e) {
                return "";
            }
        }

        public boolean writeAllLines(String path, String[] lines) {
            try {
                Files.write(Path.of(path), Arrays.stream(lines).toList());
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        public boolean writeAllText(String path, String text) {
            try {
                Files.writeString(Path.of(path), text);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    private static class CurseForgeExcluded {

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
}