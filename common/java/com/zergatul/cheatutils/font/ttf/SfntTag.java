package com.zergatul.cheatutils.font.ttf;

public class SfntTag {

    public static int toInt32(String tag) {
        if (tag == null || tag.length() != 4) {
            throw new IllegalArgumentException();
        }
        return (tag.charAt(0) << 24) | (tag.charAt(1) << 16) | (tag.charAt(2) << 8) | tag.charAt(3);
    }
}