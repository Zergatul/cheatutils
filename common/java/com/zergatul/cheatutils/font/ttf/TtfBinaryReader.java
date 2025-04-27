package com.zergatul.cheatutils.font.ttf;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

public class TtfBinaryReader implements Closeable {

    private final RandomAccessFile file;

    public TtfBinaryReader(String path) throws IOException {
        this.file = new RandomAccessFile(path, "r");
    }

    public short readFWord() throws IOException {
        return file.readShort();
    }

    public int readUInt16() throws IOException {
        return file.readUnsignedShort();
    }

    public int readUInt32() throws IOException {
        return file.readInt();
    }

    public String readString(int length, Charset charset) throws IOException {
        byte[] data = new byte[length];
        if (file.read(data) != length) {
            throw new EOFException();
        }
        return new String(data, charset);
    }

    public TtfFixed readFixed() throws IOException {
        return new TtfFixed(file.readInt());
    }

    public void seek(int position) throws IOException {
        file.seek(position);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}