package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.HelpText;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeApi {

    private final long start = System.nanoTime();

    @HelpText("Returns seconds elapsed from some static point in the past.")
    public double getSeconds() {
        return (System.nanoTime() - start) / 1e9;
    }


    @HelpText("Example: HH:mm:ss. Full documentation about format string: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html")
    public String getSystemTimeFormatted(String format) {
        try {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
    }
}