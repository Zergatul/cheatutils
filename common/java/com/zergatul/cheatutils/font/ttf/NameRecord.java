package com.zergatul.cheatutils.font.ttf;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NameRecord {

    public final int platformId;
    public final int platformSpecificId;
    public final int languageId;
    public final int nameId;
    public final int length;
    public final int offset;

    private NameRecord(int platformId, int platformSpecificId, int languageId, int nameId, int length, int offset) {
        this.platformId = platformId;
        this.platformSpecificId = platformSpecificId;
        this.languageId = languageId;
        this.nameId = nameId;
        this.length = length;
        this.offset = offset;
    }

    public static NameRecord read(TtfBinaryReader reader) throws IOException {
        int platformId = reader.readUInt16();
        int platformSpecificId = reader.readUInt16();
        int languageId = reader.readUInt16();
        int nameId = reader.readUInt16();
        int length = reader.readUInt16();
        int offset = reader.readUInt16();
        return new NameRecord(platformId, platformSpecificId, languageId, nameId, length, offset);
    }

    public String getValue(TtfBinaryReader reader, int baseOffset) throws IOException {
        reader.seek(baseOffset + offset);

        Charset charset;
        if (platformId == PlatformIdentifiers.UNICODE) {
            charset = StandardCharsets.UTF_16BE;
        } else if (platformId == PlatformIdentifiers.MICROSOFT && (platformSpecificId == 0 || platformSpecificId == 1)) {
            charset = StandardCharsets.UTF_16BE;
        } else if (platformId == PlatformIdentifiers.MACINTOSH && platformSpecificId == 0) {
            charset = StandardCharsets.US_ASCII; //MacRomanCharset;
        } else {
            charset = StandardCharsets.US_ASCII; // fallback
        }

        return reader.readString(length, charset);
    }
}