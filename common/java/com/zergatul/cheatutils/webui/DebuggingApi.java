package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.modules.scripting.Debugging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DebuggingApi extends ApiBase {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS");

    @Override
    public String getRoute() {
        return "debugging";
    }

    @Override
    public String get(String input) {
        int id = Integer.parseInt(input);
        List<Debugging.Entry> entries = Debugging.instance.getEntries(id);
        if (entries.isEmpty()) {
            return gson.toJson(new Response(id, List.of()));
        }

        LocalDateTime time = LocalDateTime.now();
        long nano = System.nanoTime();
        return gson.toJson(new Response(
                entries.get(entries.size() - 1).id(),
                entries.stream()
                        .map(entry -> new Entry(formatter.format(time.plusNanos(entry.time() - nano)), entry.message()))
                        .toList()));
    }

    public record Response(int lastId, List<Entry> entries) {}

    public record Entry(String time, String message) {}
}