package com.zergatul.cheatutils.font.ttf;

import java.io.IOException;

public class TtcHeader {

    public final int tag;
    public final int majorVersion;
    public final int minorVersion;
    public final int numFonts;
    public final int[] tableDirectoryOffsets;

    private TtcHeader(int tag, int majorVersion, int minorVersion, int numFonts, int[] tableDirectoryOffsets) {
        this.tag = tag;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.numFonts = numFonts;
        this.tableDirectoryOffsets = tableDirectoryOffsets;
    }

    public static TtcHeader read(TtfBinaryReader reader) throws IOException {
        int tag = reader.readUInt32();
        if (tag != SfntTag.toInt32("ttcf")) {
            throw new IOException("Invalid header.");
        }

        int majorVersion = reader.readUInt16();
        int minorVersion = reader.readUInt16();
        int numFonts = reader.readUInt32();
        int[] offsets = new int[numFonts];
        for (int i = 0; i < offsets.length; i++) {
            offsets[i] = reader.readUInt32();
        }
        return new TtcHeader(tag, majorVersion, minorVersion, numFonts, offsets);
    }
}