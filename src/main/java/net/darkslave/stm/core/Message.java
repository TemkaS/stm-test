package net.darkslave.stm.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;





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
        try {
            DataInput dis = new DataInputStream(stream);

            short marker = dis.readShort();
            if (marker != MARKER)
                throw new IOException("Invalid Message packet");

            int size = dis.readInt();

            byte[] data = new byte[size];
            dis.readFully(data);

            return new Message(data, size);

        } catch (EOFException e) {
            return null;
        }
    }


    public static void writeTo(Message source, OutputStream stream) throws IOException {
        DataOutput dos = new DataOutputStream(stream);

        dos.writeShort(MARKER);

        dos.writeInt(source.data.length);
        dos.write(source.data);
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


}
