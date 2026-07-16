package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.type.CustomType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@CustomType(name = "Regex")
public class Regex {

    private static final Regex INVALID = new Regex(Pattern.compile(""), false);

    private final Pattern pattern;
    private final boolean valid;

    private Regex(Pattern pattern) {
        this(pattern, true);
    }

    private Regex(Pattern pattern, boolean valid) {
        this.pattern = pattern;
        this.valid = valid;
    }

    @MethodDescription("Returns an invalid Regex when the pattern is invalid; its match methods return no matches.")
    public static Regex compile(String regex) {
        try {
            return new Regex(Pattern.compile(regex));
        } catch (PatternSyntaxException e) {
            return INVALID;
        }
    }

    @MethodDescription("Flags are a bit mask of java.util.regex.Pattern constants. Invalid patterns return an invalid Regex.")
    public static Regex compile(String regex, int flags) {
        try {
            return new Regex(Pattern.compile(regex, flags));
        } catch (PatternSyntaxException e) {
            return INVALID;
        }
    }

    @MethodDescription("Splits around matches; trailing empty strings are omitted.")
    public String[] split(String input) {
        if (!this.valid) {
            return new String[0];
        }
        return pattern.split(input);
    }

    @MethodDescription("Checks whether the entire input matches the pattern.")
    public boolean isMatch(String input) {
        if (!this.valid) {
            return false;
        }
        return pattern.matcher(input).matches();
    }

    @MethodDescription("Returns the first match, or an unsuccessful Match when none is found.")
    public Match match(String input) {
        if (!this.valid) {
            return Match.EMPTY;
        }

        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return new Match(matcher.toMatchResult());
        } else {
            return Match.EMPTY;
        }
    }

    @MethodDescription("Returns all non-overlapping matches.")
    public Match[] matches(String input) {
        if (!this.valid) {
            return new Match[0];
        }

        Matcher matcher = pattern.matcher(input);
        return matcher.results().map(Match::new).toArray(Match[]::new);
    }
}