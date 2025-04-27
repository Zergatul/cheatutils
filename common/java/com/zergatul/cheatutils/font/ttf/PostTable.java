package com.zergatul.cheatutils.font.ttf;

import java.io.IOException;

public class PostTable {

    public final TtfFixed format;
    public final TtfFixed italicAngle;
    public final short underlinePosition;
    public final short underlineThickness;
    public final int isFixedPitch;

    private PostTable(TtfFixed format, TtfFixed italicAngle, short underlinePosition, short underlineThickness, int isFixedPitch) {
        this.format = format;
        this.italicAngle = italicAngle;
        this.underlinePosition = underlinePosition;
        this.underlineThickness = underlineThickness;
        this.isFixedPitch = isFixedPitch;
    }

    public static PostTable read(TtfBinaryReader reader) throws IOException {
        TtfFixed format = reader.readFixed();
        TtfFixed italicAngle = reader.readFixed();
        short underlinePosition = reader.readFWord();
        short underlineThickness = reader.readFWord();
        int isFixedPitch = reader.readUInt32();
        return new PostTable(format, italicAngle, underlinePosition, underlineThickness, isFixedPitch);
    }

    public boolean isMonospace() {
        return isFixedPitch != 0;
    }
}