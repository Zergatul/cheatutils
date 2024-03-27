package com.zergatul.cheatutils.scripting.modules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class StringsApi {

    public boolean contains(String source, String search) {
        return source.contains(search);
    }

    public int indexOf(String source, String search) {
        return source.indexOf(search);
    }

    public String substring(String input, int beginIndex) {
        return input.substring(beginIndex);
    }

    public String substring(String input, int beginIndex, int endIndex) {
        return input.substring(beginIndex, endIndex);
    }

    public boolean isMatch(String input, String regex) {
        try {
            return Pattern.matches(regex, input);
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    public String[] getMatches(String input, String regex) {
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            return new String[0];
        }

        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String[] matches = new String[matcher.groupCount() + 1];
            for (int i = 0; i < matches.length; i++) {
                matches[i] = matcher.group(i);
            }
            return matches;
        } else {
            return new String[0];
        }
    }
}