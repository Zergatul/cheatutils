package com.zergatul.cheatutils.font.ttf;

import java.io.IOException;

public class TableDirectory {

    public int tag;
    public int checksum;
    public int offset;
    public int length;

    public static TableDirectory read(TtfBinaryReader reader) throws IOException {
        var table = new TableDirectory();
        table.tag = reader.readUInt32();
        table.checksum = reader.readUInt32();
        table.offset = reader.readUInt32();
        table.length = reader.readUInt32();
        return table;
    }

    public String getTagString() {
        return new StringBuilder(4)
                .append((char) ((tag >> 0x18) & 0xFF))
                .append((char) ((tag >> 0x10) & 0xFF))
                .append((char) ((tag >> 0x08) & 0xFF))
                .append((char) ((tag >> 0x00) & 0xFF))
                .toString();
    }
}

