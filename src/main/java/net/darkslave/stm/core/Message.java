package net.darkslave.stm.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ByteChannel;
import net.darkslave.io.Channels;
import net.darkslave.io.Streams;





public class Message {
    private final byte[] data;
    private final int size;


    public Message(byte[] data, int size) {
        this.data = data;
        this.size = size;
    }


    public byte[] getData() {
        return data;
    }


    public int getSize() {
        return size;
    }


    private static short MARKER = 0x7733;


    public static Message readFrom(InputStream stream) throws IOException {
        byte[] temp = new byte[6];
        int read;

        read = Streams.read(stream, temp);
        if (read != 6) {
            return null;
        }

        int mark = 0;
        mark|= (temp[0] & 255) << 8;
        mark|= (temp[1] & 255);

        if (mark != MARKER)
            throw new IOException("Invalid Message packet");

        int size = 0;
        size|= (temp[2] & 255) << 24;
        size|= (temp[3] & 255) << 16;
        size|= (temp[4] & 255) <<  8;
        size|= (temp[5] & 255);

        byte[] data = new byte[size];

        read = Streams.read(stream, data);
        if (read != size) {
            return null;
        }

        return new Message(data, size);
    }


    public static void writeTo(Message source, OutputStream stream) throws IOException {
        byte[] temp = new byte[6];
        temp[0] = (byte) (MARKER >>> 8);
        temp[1] = (byte) (MARKER);

        temp[2] = (byte) (source.size >>> 24);
        temp[3] = (byte) (source.size >>> 16);
        temp[4] = (byte) (source.size >>>  8);
        temp[5] = (byte) (source.size);

        stream.write(temp, 0, 6);
        stream.write(source.data, 0, source.size);
    }



    public static Message read(byte[] source, int offset, int length) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(source, offset, length);
        return readFrom(stream);
    }



    public static byte[] write(Message source) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        writeTo(source, stream);
        return stream.toByteArray();
    }


    public static Message readFrom(ByteChannel stream) throws IOException {
        byte[] temp = new byte[6];
        int read;

        read = Channels.read(stream, temp);
        if (read != 6) {
            return null;
        }

        int mark = 0;
        mark|= (temp[0] & 255) << 8;
        mark|= (temp[1] & 255);

        if (mark != MARKER)
            throw new IOException("Invalid Message packet");

        int size = 0;
        size|= (temp[2] & 255) << 24;
        size|= (temp[3] & 255) << 16;
        size|= (temp[4] & 255) <<  8;
        size|= (temp[5] & 255);

        byte[] data = new byte[size];

        read = Channels.read(stream, data);
        if (read != size) {
            return null;
        }

        return new Message(data, size);
    }


}
