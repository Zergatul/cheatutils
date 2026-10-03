package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Debugging implements Module {

    public static final Debugging INSTANCE = new Debugging();

    private final LinkedList<Entry> entries = new LinkedList<>();
    private int id;

    private Debugging() {
        WebApiRegistry.INSTANCE.register(new WebApi());
    }

    public synchronized void addMessage(String message) {
        id++;
        long now = System.nanoTime();
        entries.add(new Entry(id, System.nanoTime(), message));

        clearOld(now - 1000000000L);
    }

    public synchronized List<Entry> getEntries(int since) {
        List<Entry> result = new ArrayList<>();
        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry next = iterator.next();
            if (next.id > since) {
                result.add(next);
                break;
            }
        }
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    private void clearOld(long from) {
        while (entries.size() > 100 && entries.getFirst().time < from) {
            entries.removeFirst();
        }
    }

    public record Entry(int id, long time, String message) {}

    private final class WebApi extends WebApiBase {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS");

        @Override
        public String getRoute() {
            return "debugging";
        }

        @Override
        public String get(String input) {
            int id = Integer.parseInt(input);
            List<Debugging.Entry> entries = Debugging.this.getEntries(id);
            if (entries.isEmpty()) {
                return gson.toJson(new Response(id, List.of()));
            } else {
                LocalDateTime time = LocalDateTime.now();
                long nano = System.nanoTime();
                return gson.toJson(new Response(
                        entries.getLast().id(),
                        entries.stream().map(e -> new Entry(formatter.format(time.plusNanos(e.time() - nano)), e.message())).toList()));
            }
        }

        public record Response(int lastId, List<Entry> entries) {}

        public record Entry(String time, String message) {}
    }
}