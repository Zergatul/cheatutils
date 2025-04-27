package com.zergatul.cheatutils.font.ttf;

import java.io.IOException;

public class NameTable {

    public final int format;
    public final int count;
    public final int stringOffset;
    public final NameRecord[] records;

    private NameTable(int format, int count, int stringOffset, NameRecord[] records) {
        this.format = format;
        this.count = count;
        this.stringOffset = stringOffset;
        this.records = records;
    }

    public static NameTable read(TtfBinaryReader reader) throws IOException {
        int format = reader.readUInt16();
        int count = reader.readUInt16();
        int stringOffset = reader.readUInt16();
        NameRecord[] records = new NameRecord[count];
        for (int i = 0; i < records.length; i++) {
            records[i] = NameRecord.read(reader);
        }
        return new NameTable(format, count, stringOffset, records);
    }
}