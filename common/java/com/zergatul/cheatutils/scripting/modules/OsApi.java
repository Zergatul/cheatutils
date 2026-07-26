package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.AdvancedApi;
import com.zergatul.cheatutils.scripting.CurseForgeRestricted;
import com.zergatul.scripting.MethodDescription;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@AdvancedApi
public class OsApi {

    public final FilesApi files = new FilesApi();

    @CurseForgeRestricted
    @MethodDescription("""
            Starts external program and returns exit code.
            Continuation will run from tick end event, and may run after world unload.
            """)
    public CompletableFuture<Integer> execute(String path) {
        return OsApiCurseForgeExcluded.execute(path);
    }

    @CurseForgeRestricted
    @MethodDescription("""
            Starts external program and returns exit code
            Continuation will run from tick end event, and may run after world unload.
            """)
    public CompletableFuture<Integer> execute(String path, String[] arguments) {
        return OsApiCurseForgeExcluded.execute(path, arguments);
    }

    @SuppressWarnings("unused")
    @AdvancedApi
    public static class FilesApi {

        @MethodDescription("Returns an empty array on I/O failure.")
        public String[] readAllLines(String path) {
            try {
                return Files.readAllLines(Path.of(path)).toArray(String[]::new);
            } catch (IOException e) {
                return new String[0];
            }
        }

        @MethodDescription("Returns an empty string on I/O failure.")
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
}